package io.gatekeeper.node.service.backend;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.backend.ConsulBackendConfiguration;
import io.gatekeeper.configuration.data.backend.LocalBackendConfiguration;
import io.gatekeeper.node.service.BackendService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.concurrent.CompletableFuture;

public class ConsulBackendService extends BackendService<LocalBackendConfiguration> {

    public ConsulBackendService(Configuration configuration, ReplicationService replication) throws Exception {
        this(configuration, replication, Client.build((ConsulBackendConfiguration) configuration.backend));
    }

    ConsulBackendService(Configuration configuration, ReplicationService replication, Client client) throws Exception {
        super(configuration, replication);
    }

    @Override
    public CompletableFuture start() {
        logger.info("Starting consul backend service");

        return CompletableFuture.completedFuture(null);
    }
}
