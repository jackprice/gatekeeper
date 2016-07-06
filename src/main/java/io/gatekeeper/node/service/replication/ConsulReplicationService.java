package io.gatekeeper.node.service.replication;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.replication.ConsulReplicationConfiguration;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.replication.common.Node;
import io.gatekeeper.node.service.replication.consul.Client;
import io.gatekeeper.node.service.replication.consul.Lock;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ConsulReplicationService extends ReplicationService<ConsulReplicationConfiguration> {

    private final ExecutorService consulExecutor;

    private final Client client;

    private final Map<String, Lock> locks = new HashMap<>();

    public ConsulReplicationService(Configuration configuration) {
        this(configuration, new Client(
            ((ConsulReplicationConfiguration) configuration.replication).host,
            ((ConsulReplicationConfiguration) configuration.replication).port,
            ((ConsulReplicationConfiguration) configuration.replication).service,
            configuration.api.address,
            configuration.api.port,
            ((ConsulReplicationConfiguration) configuration.replication).token
        ));
    }

    public ConsulReplicationService(Configuration configuration, Client client) {
        super(configuration);

        this.client = client;

        consulExecutor = Executors.newSingleThreadExecutor(
            (new ThreadFactoryBuilder())
                .setNameFormat("Consul Replication Service")
                .build()
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
    public synchronized void lock(String name) throws InterruptedException {
        synchronized (locks) {
            Lock lock;

            try {
                lock = client.lock(name).get();
            } catch (ExecutionException exception) {
                throw new InterruptedException("Could not obtain lock");
            }

            locks.put(name, lock);
        }
    }

    @Override
    public synchronized void unlock(String name) throws InterruptedException {
        assert null != name;
        assert name.length() > 0;

        synchronized (locks) {
            if (!locks.containsKey(name)) {
                return;
            }

            try {
                locks.get(name).release();
            } catch (Exception exception) {
                throw new InterruptedException("Could not release lock");
            }
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
