package io.gatekeeper.node;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.gatekeeper.GatekeeperException;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.logging.Loggers;
import io.gatekeeper.node.service.BackendService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Node implements Closeable {

    private final UUID name;

    private final Configuration configuration;

    private final Logger logger;

    private final List<Service> services;

    private final ThreadPoolExecutor executor;

    public Node(Configuration configuration) {
        this.name = UUID.randomUUID();
        this.configuration = configuration;
        this.logger = Loggers.getNodeLogger();
        this.services = this.createServices();
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(
            (new ThreadFactoryBuilder())
                .setNameFormat("Node %d")
                .build()
        );

        this.executor.prestartCoreThread();
        this.executor.prestartAllCoreThreads();
    }

    public CompletableFuture<Void> start() {
        this.logger.info(String.format("Starting node %s", this.name.toString()));

        CompletableFuture<Void> future = new CompletableFuture<>();

        this.executor.execute(() -> {
            this.startServices().join();

            future.complete(null);
        });

        return future;
    }

    private CompletableFuture<Void> startServices() {
        this.logger.info("Starting services");

        List<CompletableFuture> futures = services
            .stream()
            .map(Service::start)
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public void close() throws IOException {
        this.logger.info("Stopping services");

        for (Service service : this.services) {
            service.close();
        }

        this.logger.info(String.format("Shutting down %d node threads", this.executor.getActiveCount()));

        this.executor.shutdown();

        try {
            this.executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            this.logger.warning(String.format(
                "Killing %d node threads that did not shut down in time",
                this.executor.getActiveCount()
            ));
        }

        this.logger.info("Node shutdown complete");
    }

    private List<Service> createServices() {
        ReplicationService replication = this.createReplicationService();
        BackendService backend = this.createBackendService();

        return Arrays.asList(replication, backend);
    }

    @SuppressWarnings("unchecked")
    private ReplicationService createReplicationService() {
        Class<ReplicationService> clazz = (Class<ReplicationService>) this.configuration.replication.serviceClass();

        try {
            return clazz.getConstructor(Configuration.class).newInstance(this.configuration);
        } catch (Exception exception) {
            throw new GatekeeperException(
                String.format("Failed to create replication service %s", clazz.getCanonicalName()),
                exception
            );
        }
    }

    @SuppressWarnings("unchecked")
    private BackendService createBackendService() {
        Class<BackendService> clazz = (Class<BackendService>) this.configuration.backend.serviceClass();

        try {
            return clazz.getConstructor(Configuration.class).newInstance(this.configuration);
        } catch (Exception exception) {
            throw new GatekeeperException(
                String.format("Failed to create backend service %s", clazz.getCanonicalName()),
                exception
            );
        }
    }

}
