package io.gatekeeper.node.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.ReplicationConfiguration;
import io.gatekeeper.logging.Loggers;
import io.gatekeeper.node.service.replication.common.Node;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

public abstract class ReplicationService<ReplicationConfigurationType extends ReplicationConfiguration> implements Service {

    protected final ReplicationConfigurationType replicationConfiguration;

    protected final Configuration configuration;

    protected final Logger logger;

    protected final ThreadPoolExecutor executor;

    public ReplicationService(Configuration configuration) {
        assert null != configuration;

        this.configuration = configuration;
        this.replicationConfiguration = (ReplicationConfigurationType) configuration.replication;
        this.logger = Loggers.getReplicationLogger();
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(
            (new ThreadFactoryBuilder())
                .setNameFormat("Replication Service %d")
                .build()
        );

        this.executor.prestartCoreThread();
        this.executor.prestartAllCoreThreads();
    }

    public abstract CompletableFuture<Void> start();

    public abstract CompletableFuture<Integer> countNodes();

    public abstract CompletableFuture<List<Node>> fetchNodes();

    public abstract void lock(String name) throws InterruptedException;

    public abstract void unlock(String name) throws InterruptedException;

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
}
