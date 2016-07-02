package io.gatekeeper.node.service.replication;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.model.Endpoint;
import io.gatekeeper.node.service.ReplicationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LocalReplicationService extends ReplicationService {

    public LocalReplicationService(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    protected CompletableFuture startServer() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected CompletableFuture startClient() {
        return CompletableFuture.completedFuture(null);
    }
}
