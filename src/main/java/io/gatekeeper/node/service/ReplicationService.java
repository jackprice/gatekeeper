package io.gatekeeper.node.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.logging.Loggers;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.Logger;

public abstract class ReplicationService implements Service {

    private final Configuration configuration;

    private final Logger logger;

    protected final ThreadPoolExecutor executor;

    public ReplicationService(Configuration configuration) {
        this.configuration = configuration;
        this.logger = Loggers.getReplicationLogger();
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(
            (new ThreadFactoryBuilder())
                .setNameFormat("Replication Service %d")
                .build()
        );

        this.executor.prestartCoreThread();
        this.executor.prestartAllCoreThreads();
    }

    public CompletableFuture start() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        this.executor.execute(() -> {
            if (this.configuration.replication.server) {
                this.startServer().join();
            }

            this.startClient().join();

            future.complete(null);
        });

        return future;
    }

    @Override
    public void close() throws IOException {
        this.logger.info(String.format("Shutting down %d replication threads", this.executor.getActiveCount()));

        this.executor.shutdown();

        try {
            this.executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            this.logger.warning(String.format(
                "Killing %d replication threads that did not shut down in time",
                this.executor.getActiveCount()
            ));
        }

        this.logger.info("Replication threads halted");
    }

    abstract protected CompletableFuture startServer();

    abstract protected CompletableFuture startClient();
}
