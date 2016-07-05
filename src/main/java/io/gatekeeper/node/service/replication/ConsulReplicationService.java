package io.gatekeeper.node.service.replication;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.replication.ConsulReplicationConfiguration;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.replication.common.Node;
import io.gatekeeper.node.service.replication.consul.Client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class ConsulReplicationService extends ReplicationService<ConsulReplicationConfiguration> {

    private final ExecutorService consulExecutor;

    private final Client client;

    public ConsulReplicationService(Configuration configuration) {
        super(configuration);

        consulExecutor = Executors.newSingleThreadExecutor(
            (new ThreadFactoryBuilder())
                .setNameFormat("Consul Replication Service")
                .build()
        );

        this.client = new Client(
            this.replicationConfiguration.host,
            this.replicationConfiguration.port,
            this.replicationConfiguration.service,
            this.replicationConfiguration.rpcAddress,
            this.replicationConfiguration.rpcPort
        );
    }

    @Override
    public CompletableFuture<Void> start() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        consulExecutor.execute(() -> {
            if (replicationConfiguration.server) {
                client.registerService().join();
            }

            future.complete(null);
        });

        return future;
    }

    @Override
    public CompletableFuture<Integer> countNodes() {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        consulExecutor.execute(() -> {
            try {
                List<Node> nodes = client.getNodes().get();

                future.complete(nodes.size());
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<List<Node>> fetchNodes() {
        CompletableFuture<List<Node>> future = new CompletableFuture<>();

        consulExecutor.execute(() -> {
            try {
                future.complete(client.getNodes().get());
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
