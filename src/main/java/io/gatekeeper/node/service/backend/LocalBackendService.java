package io.gatekeeper.node.service.backend;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.backend.LocalBackendConfiguration;
import io.gatekeeper.node.service.BackendService;
import io.gatekeeper.node.service.ReplicationService;

import java.util.concurrent.CompletableFuture;

public class LocalBackendService extends BackendService<LocalBackendConfiguration> {

    public LocalBackendService(Configuration configuration, ReplicationService replication) throws Exception {
        super(configuration, replication);
    }

    @Override
    public CompletableFuture start() {
        logger.info("Starting local backend service");

        return CompletableFuture.completedFuture(null);
    }
}
