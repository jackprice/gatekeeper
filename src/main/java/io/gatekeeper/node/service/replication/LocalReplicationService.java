package io.gatekeeper.node.service.replication;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.replication.common.Node;
import io.gatekeeper.node.service.replication.common.ReplicationInformation;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

public class LocalReplicationService extends ReplicationService<LocalReplicationConfiguration> {

    private final Map<String, Semaphore> semaphores = new HashMap<>();

    public LocalReplicationService(Configuration configuration) {
        super(configuration);
    }

    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Integer> countNodes() {
        return CompletableFuture.completedFuture(1);
    }

    @Override
    public CompletableFuture<List<Node>> fetchNodes() {
        return CompletableFuture.completedFuture(Collections.singletonList(new Node(
            "local",
            configuration.api.address,
            configuration.api.port
        )));
    }

    @Override
    public void lock(String name) throws InterruptedException {
        assert null != name;
        assert name.length() > 0;

        synchronized (semaphores) {
            if (!semaphores.containsKey(name)) {
                semaphores.put(name, new Semaphore(1));
            }

            semaphores.get(name).acquire();
        }
    }

    @Override
    public void unlock(String name) throws InterruptedException {
        assert null != name;
        assert name.length() > 0;

        synchronized (semaphores) {
            if (semaphores.containsKey(name)) {
                semaphores.get(name).release();
            }
        }
    }

    @Override
    public CompletableFuture<ReplicationInformation> getInformation() {
        return CompletableFuture.completedFuture(new ReplicationInformation(
            "local",
            1,
            new HashMap<>()
        ));
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
