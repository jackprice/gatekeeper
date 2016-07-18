package io.gatekeeper.node.service.backend;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.backend.ConsulBackendConfiguration;
import io.gatekeeper.configuration.data.backend.LocalBackendConfiguration;
import io.gatekeeper.model.*;
import io.gatekeeper.node.service.BackendService;
import io.gatekeeper.node.service.ProviderService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;
import io.gatekeeper.node.service.backend.consul.runnable.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ConsulBackendService extends BackendService<LocalBackendConfiguration> {

    /**
     * A Consul client implementation for making consul API requests.
     */
    private Client client;

    /**
     * Public constructor
     *
     * @param configuration Global configuration
     * @param replication   The replication service
     */
    public ConsulBackendService(
        Configuration configuration,
        ReplicationService replication,
        ProviderService providers
    ) throws Exception {
        this(configuration, replication, providers, Client.build((ConsulBackendConfiguration) configuration.backend));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<EndpointModel>> fetchEndpoints() {
        CompletableFuture<List<EndpointModel>> future = new CompletableFuture<>();

        executor.execute(new FetchEndpointsRunnable(replication, future, client));

        return future;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<EndpointModel> fetchEndpoint(UUID id) {
        CompletableFuture<EndpointModel> future = new CompletableFuture<>();

        executor.execute(new FetchEndpointRunnable(replication, id, future, client));

        return future;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<EndpointModel> fetchEndpoint(String domain) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<EndpointModel>> fetchEndpoints(String pattern) {
        CompletableFuture<List<EndpointModel>> future = new CompletableFuture<>();

        executor.execute(new FetchEndpointsByPatternRunnable(replication, pattern, future, client));

        return future;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<EndpointModel>> fetchEndpointsByTag(String tag) {
        CompletableFuture<List<EndpointModel>> future = new CompletableFuture<>();

        executor.execute(new FetchEndpointsByTagRunnable(replication, tag, future, client));

        return future;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<EndpointModel> createEndpoint(EndpointModel endpoint) {
        CompletableFuture<EndpointModel> future = new CompletableFuture<>();

        executor.execute(new CreateEndpointRunnable(replication, endpoint, future, client));

        return future;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> deleteEndpoint(EndpointModel endpoint) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> updateEndpoint(EndpointModel endpoint) {
        return null;
    }

    @Override
    public CompletableFuture<List<ProviderModel>> fetchProviders() {
        CompletableFuture<List<ProviderModel>> future = new CompletableFuture<>();

        executor.execute(new FetchProvidersRunnable(replication, future, client));

        return future;
    }

    @Override
    public CompletableFuture<ProviderModel> createProvider(ProviderModel provider) {
        CompletableFuture<ProviderModel> future = new CompletableFuture<>();

        executor.execute(new CreateProviderRunnable(replication, providers, provider, future, client));

        return future;
    }

    @Override
    public CompletableFuture<ProviderModel> fetchProvider(UUID uuid) {
        CompletableFuture<ProviderModel> future = new CompletableFuture<>();

        executor.execute(new FetchProviderRunnable(replication, uuid, future, client));

        return future;
    }

    @Override
    public CompletableFuture<ProviderModel> fetchProvider(String id) {
        CompletableFuture<ProviderModel> future = new CompletableFuture<>();

        executor.execute(new FetchProviderByIdRunnable(replication, id, future, client));

        return future;
    }

    @Override
    public CompletableFuture<ProviderModel> fetchProviderUnsafe(String id) {
        CompletableFuture<ProviderModel> future = new CompletableFuture<>();

        executor.execute(new FetchProviderByIdUnsafeRunnable(id, future, client));

        return future;
    }

    @Override
    public CompletableFuture<Void> saveProviderDataUnsafe(UUID id, Map<String, Object> data) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        executor.execute(new SaveProviderDataByIdUnsafeRunnable(id, data, future, client));

        return future;
    }

    @Override
    public CompletableFuture<CertificateModel> fetchCertificate(EndpointModel endpoint) {
        CompletableFuture<CertificateModel> future = new CompletableFuture<>();

        executor.execute(new FetchCertificateRunnable(replication, providers, endpoint, future, client));

        return future;
    }

    @Override
    public CompletableFuture<CertificateModel> fetchCertificateBlocking(EndpointModel endpoint) {
        CompletableFuture<CertificateModel> future = new CompletableFuture<>();

        executor.execute(new FetchCertificateBlockingRunnable(replication, providers, endpoint, future, client));

        return future;
    }

    @Override
    public CompletableFuture<CertificateModel> reissueCertificate(EndpointModel endpoint) {
        CompletableFuture<CertificateModel> future = new CompletableFuture<>();

        executor.execute(new ReissueCertificateRunnable(replication, providers, endpoint, future, client));

        return future;
    }

    @Override
    public CompletableFuture<CertificateModel> reissueCertificateBlocking(EndpointModel endpoint) {
        CompletableFuture<CertificateModel> future = new CompletableFuture<>();

        executor.execute(new ReissueCertificateBlockingRunnable(replication, providers, endpoint, future, client));

        return future;
    }

    @Override
    public CompletableFuture<Void> updateCertificate(
        EndpointModel endpoint, CertificateModel certificate
    ) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        executor.execute(new UpdateCertificateRunnable(replication, endpoint, certificate, future, client));

        return future;
    }

    /**
     * Private internal constructor
     *
     * @param configuration The global configuration
     * @param replication   The replication service
     * @param providers     The providers service
     * @param client        A Consul client implementation for making API requests
     */
    ConsulBackendService(
        Configuration configuration,
        ReplicationService replication,
        ProviderService providers,
        Client client
    ) throws Exception {
        super(configuration, replication, providers);

        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture start() {
        logger.info("Starting consul backend service");

        return CompletableFuture.completedFuture(null);
    }
}
