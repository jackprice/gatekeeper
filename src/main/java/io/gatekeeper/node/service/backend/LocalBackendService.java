package io.gatekeeper.node.service.backend;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.backend.LocalBackendConfiguration;
import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.node.service.BackendService;
import io.gatekeeper.node.service.ProviderService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.common.ReplicatedMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LocalBackendService extends BackendService<LocalBackendConfiguration> {

    private List<EndpointModel> endpoints = new ArrayList<>();

    private List<ProviderModel> providers = new ArrayList<>();

    private List<CertificateModel> certificates = new ArrayList<>();

    public LocalBackendService(
        Configuration configuration,
        ReplicationService replication,
        ProviderService providers
    ) throws Exception {
        super(configuration, replication, providers);
    }

    @Override
    public <V extends Serializable> ReplicatedMap<V> getReplicatedMap(UUID uuid) {
        return null;
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
            .filter((endpoint -> endpoint.getUuid().equals(id)))
            .collect(Collectors.toList());

        return CompletableFuture.completedFuture(matches.size() == 1 ? matches.get(0) : null);
    }

    @Override
    public CompletableFuture<EndpointModel> fetchEndpoint(String domain) {
        List<EndpointModel> matches = this.endpoints
            .stream()
            .filter((endpoint -> endpoint.containsDomain(domain)))
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
    @SuppressWarnings("unchecked")
    public CompletableFuture<List<EndpointModel>> fetchEndpointsByTag(String tag) {
        List<EndpointModel> matches = this.endpoints
            .stream()
            .filter((endpoint -> endpoint.containsTag(tag)))
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
    public CompletableFuture<Void> deleteEndpoint(EndpointModel endpoint) {
        this.endpoints.removeIf((match) -> endpoint.getUuid().equals(match.getUuid()));

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateEndpoint(EndpointModel endpoint) {
        this.endpoints.removeIf((match) -> endpoint.getUuid().equals(match.getUuid()));
        this.endpoints.add(endpoint);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<ProviderModel>> fetchProviders() {
        return CompletableFuture.completedFuture(this.providers);
    }

    @Override
    public CompletableFuture<ProviderModel> createProvider(ProviderModel provider) {
        this.providers.add(provider);

        return CompletableFuture.completedFuture(provider);
    }

    @Override
    public CompletableFuture<ProviderModel> fetchProvider(UUID uuid) {
        List<ProviderModel> matches = this.providers
            .stream()
            .filter((endpoint -> endpoint.getUuid().equals(uuid)))
            .collect(Collectors.toList());

        return CompletableFuture.completedFuture(matches.size() == 1 ? matches.get(0) : null);
    }

    @Override
    public CompletableFuture<ProviderModel> fetchProvider(String id) {
        List<ProviderModel> matches = this.providers
            .stream()
            .filter((endpoint -> endpoint.getId().equals(id)))
            .collect(Collectors.toList());

        return CompletableFuture.completedFuture(matches.size() == 1 ? matches.get(0) : null);
    }

    @Override
    public CompletableFuture<ProviderModel> fetchProviderUnsafe(String id) {
        return fetchProvider(id);
    }

    @Override
    public CompletableFuture<CertificateModel> fetchCertificate(EndpointModel endpoint) {
        UUID id = endpoint.getCertificate();

        List<CertificateModel> matches = this.certificates
            .stream()
            .filter((certificate -> certificate.getUuid().equals(id)))
            .collect(Collectors.toList());

        return CompletableFuture.completedFuture(matches.size() == 1 ? matches.get(0) : null);
    }

    @Override
    public CompletableFuture<CertificateModel> fetchCertificateBlocking(EndpointModel endpoint) {
        return fetchCertificate(endpoint);
    }

    @Override
    public CompletableFuture<CertificateModel> reissueCertificate(EndpointModel endpoint) {
        return null;
    }

    @Override
    public CompletableFuture<CertificateModel> reissueCertificateBlocking(EndpointModel endpoint) {
        return null;
    }

    @Override
    public CompletableFuture<Void> updateCertificate(
        EndpointModel endpoint, CertificateModel certificate
    ) {
        return null;
    }

    @Override
    public CompletableFuture start() {
        logger.info("Starting local backend service");

        return CompletableFuture.completedFuture(null);
    }
}
