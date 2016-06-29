package io.gatekeeper.node.service;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.ConnectionStrategies;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.copycat.client.RecoveryStrategies;
import io.atomix.copycat.client.ServerSelectionStrategies;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.logging.Loggers;
import io.gatekeeper.node.service.replication.StateMachine;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ReplicationService implements Closeable {

    private final NettyTransport transport;

    private final Address address;

    private final Storage storage;

    private final Configuration configuration;

    private final Logger logger;

    private final Executor executor;

    private final List<Address> initialClusterAddresses;

    private CopycatServer server;

    private CopycatClient client;

    public ReplicationService(Configuration configuration) {
        this.configuration = configuration;
        this.logger = Loggers.getReplicationLogger();
        this.executor = Executors.newFixedThreadPool(2);
        this.address = new Address(
            this.configuration.replication.bindAddress,
            this.configuration.replication.bindPort
        );
        this.transport = new NettyTransport();
        this.storage = new Storage(this.configuration.replication.dataDirectory);

        this.initialClusterAddresses = new ArrayList<>(this.configuration.replication.nodes.size());
        this.initialClusterAddresses.addAll(this.configuration.replication.nodes
            .stream()
            .map(Address::new)
            .collect(Collectors.toList()));
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

    private CompletableFuture startServer() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        this.executor.execute(() -> {
            this.logger.info("Starting server");

            this.server = CopycatServer.builder(this.address)
                .withTransport(this.transport)
                .withStorage(this.storage)
                .withStateMachine(StateMachine::new)
                .withName(this.address.toString())
                .build();

            this.server.onStateChange(this::onServerStateChange);

            if (this.configuration.replication.bootstrap) {
                this.doServerLog("Bootstrapping server");

                this.server.bootstrap().join();
            }

            if (this.initialClusterAddresses.size() > 0) {
                this.doServerLog("Joining cluster");

                this.server.join(this.initialClusterAddresses).join();
            }

            this.doServerLog("Server startup complete");


            future.complete(null);
        });

        return future;
    }

    private CompletableFuture startClient() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        this.executor.execute(() -> {
            this.logger.info("Starting client");

            this.client = CopycatClient.builder(this.address)
                .withTransport(this.transport)
                .withConnectionStrategy(ConnectionStrategies.FIBONACCI_BACKOFF)
                .withRecoveryStrategy(RecoveryStrategies.RECOVER)
                .withServerSelectionStrategy(ServerSelectionStrategies.FOLLOWERS)
                .build();

            this.client.onStateChange(this::onClientStateChange);

            this.logger.info("Connecting to cluster");

            this.client.connect(this.initialClusterAddresses).join();

            this.logger.info("Client startup complete");

            future.complete(null);
        });

        return future;
    }

    private void onServerStateChange(CopycatServer.State state) {
        switch (state) {
            case INACTIVE:
                this.doServerLog("Server state changed to INACTIVE");
                break;
            case RESERVE:
                this.doServerLog("Server state changed to RESERVE");
                break;
            case PASSIVE:
                this.doServerLog("Server state changed to PASSIVE");
                break;
            case FOLLOWER:
                this.doServerLog("Server state changed to FOLLOWER");
                break;
            case CANDIDATE:
                this.doServerLog("Server state changed to CANDIDATE");
                break;
            case LEADER:
                this.doServerLog("Server state changed to LEADER");
                break;
        }
    }

    private void onClientStateChange(CopycatClient.State state) {
        switch (state) {
            case CONNECTED:
                this.logger.info("Client state changed to CONNECTED");
                break;
            case SUSPENDED:
                this.logger.info("Client state changed to SUSPENDED");
                break;
            case CLOSED:
                this.logger.info("Client state changed to CLOSED");
                break;
        }
    }

    @Override
    public void close() {
        if (this.server != null) {
            this.server.shutdown().join();
        }

        if (this.client != null) {
            this.client.close().join();
        }
    }

    private void doServerLog(String log) {
        this.logger.info(this.server.name() + ": " + log);
    }
}
