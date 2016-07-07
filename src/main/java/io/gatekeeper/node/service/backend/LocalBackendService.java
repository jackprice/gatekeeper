package io.gatekeeper.node.service.backend;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.backend.LocalBackendConfiguration;
import io.gatekeeper.model.DomainModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.node.service.BackendService;
import io.gatekeeper.node.service.ReplicationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LocalBackendService extends BackendService<LocalBackendConfiguration> {

    private List<EndpointModel> endpoints = new ArrayList<>();

    public LocalBackendService(Configuration configuration, ReplicationService replication) throws Exception {
        super(configuration, replication);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<List<EndpointModel>> fetchEndpoints() {
        List<EndpointModel> endpoints = (List<EndpointModel>) ((ArrayList) this.endpoints).clone();

        return CompletableFuture.completedFuture(endpoints);
    }

    @Override
    public CompletableFuture<EndpointModel> fetchEndpoint(UUID id) {
        List<EndpointModel> matches = this.endpoints
            .stream()
            .filter((endpoint -> endpoint.id().equals(id)))
            .collect(Collectors.toList());

        return CompletableFuture.completedFuture(matches.size() == 1 ? matches.get(0) : null);
    }

    @Override
    public CompletableFuture<EndpointModel> fetchEndpoint(DomainModel domain) {
        List<EndpointModel> matches = this.endpoints
            .stream()
            .filter((endpoint -> endpoint.contains(domain)))
            .collect(Collectors.toList());

        return CompletableFuture.completedFuture(matches.size() == 1 ? matches.get(0) : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<List<EndpointModel>> fetchEndpoints(String pattern) {
        List<EndpointModel> matches = this.endpoints
            .stream()
            .filter((endpoint -> endpoint.matches(pattern)))
            .collect(Collectors.toList());

        List<EndpointModel> endpoints = (List<EndpointModel>) ((ArrayList) matches).clone();

        return CompletableFuture.completedFuture(endpoints);
    }

    @Override
    public CompletableFuture<EndpointModel> createEndpoint(EndpointModel endpoint) {
        this.endpoints.add(endpoint);

        return CompletableFuture.completedFuture(endpoint);
    }

    @Override
    public CompletableFuture start() {
        logger.info("Starting local backend service");

        return CompletableFuture.completedFuture(null);
    }
}
