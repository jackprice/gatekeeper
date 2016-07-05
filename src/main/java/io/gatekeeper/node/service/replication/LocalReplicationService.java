package io.gatekeeper.node.service.replication;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.replication.common.Node;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LocalReplicationService extends ReplicationService<LocalReplicationConfiguration> {

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
    public void close() throws IOException {
        super.close();
    }
}
