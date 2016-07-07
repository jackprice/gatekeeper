package io.gatekeeper.node.service.backend;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.backend.ConsulBackendConfiguration;
import io.gatekeeper.configuration.data.backend.LocalBackendConfiguration;
import io.gatekeeper.model.DomainModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.node.service.BackendService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ConsulBackendService extends BackendService<LocalBackendConfiguration> {

    public ConsulBackendService(Configuration configuration, ReplicationService replication) throws Exception {
        this(configuration, replication, Client.build((ConsulBackendConfiguration) configuration.backend));
    }

    @Override
    public CompletableFuture<List<EndpointModel>> fetchEndpoints() {
        return null;
    }

    @Override
    public CompletableFuture<EndpointModel> fetchEndpoint(UUID id) {
        return null;
    }

    @Override
    public CompletableFuture<EndpointModel> fetchEndpoint(DomainModel domain) {
        return null;
    }

    @Override
    public CompletableFuture<List<EndpointModel>> fetchEndpoints(String pattern) {
        return null;
    }

    @Override
    public CompletableFuture<EndpointModel> createEndpoint(EndpointModel endpoint) {
        return null;
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
