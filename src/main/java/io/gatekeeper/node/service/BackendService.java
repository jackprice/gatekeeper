package io.gatekeeper.node.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.BackendConfiguration;
import io.gatekeeper.logging.Loggers;
import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.node.service.backend.common.crypto.EncryptionProvider;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * The backend service is responsible for storing, retrieving and querying data in a data store, such a consul.
 *
 * @param <BackendConfigurationType> The concrete configuration class that this backend accepts
 */
public abstract class BackendService<BackendConfigurationType extends BackendConfiguration> implements Service {

    private final static String LOCK_GLOBAL = ".global";

    private final static String LOCK_WRITE = ".write";

    protected final BackendConfigurationType backendConfiguration;

    protected final Configuration configuration;

    protected final EncryptionProvider encryption;

    protected final Logger logger;

    protected final ThreadPoolExecutor executor;

    protected final ReplicationService replication;

    protected final ProviderService providers;

    public BackendService(Configuration configuration, ReplicationService replication, ProviderService providers) throws
        Exception {
        assert null != configuration;
        assert null != replication;
        assert null != providers;

        this.configuration = configuration;
        this.backendConfiguration = (BackendConfigurationType) configuration.backend;
        this.replication = replication;
        this.providers = providers;
        this.logger = Loggers.getBackendLogger();
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(
            (new ThreadFactoryBuilder())
                .setNameFormat("Backend Service %d")
                .build()
        );

        this.executor.prestartCoreThread();
        this.executor.prestartAllCoreThreads();

        try {
            this.encryption = new EncryptionProvider(configuration.backend.key);
        } catch (Exception exception) {
            throw new Exception("Could not start encryption provider");
        }
    }

    @Override
    public void close() throws IOException {
        this.logger.info(String.format("Shutting down %d backend threads", this.executor.getActiveCount()));

        this.executor.shutdown();

        try {
            this.executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            this.logger.warning(String.format(
                "Killing %d backend threads that did not shut down in time",
                this.executor.getActiveCount()
            ));
        }

        this.logger.info("backend threads halted");
    }

    public void obtainGlobalLock() throws InterruptedException {
        replication.lock(LOCK_GLOBAL);
    }

    public void releaseGlobalLock() throws InterruptedException {
        replication.unlock(LOCK_GLOBAL);
    }

    public void obtainWriteLock() throws InterruptedException {
        replication.lock(LOCK_WRITE);
    }

    public void releaseWriteLock() throws InterruptedException {
        replication.unlock(LOCK_WRITE);
    }

    /**
     * Returns all endpoints known to the system.
     *
     * @return A collection of endpoints
     */
    public abstract CompletableFuture<List<EndpointModel>> fetchEndpoints();

    /**
     * Fetch an endpoint by its ID.
     *
     * @param id The ID of the endpoint
     *
     * @return The specified endpoint
     */
    public abstract CompletableFuture<EndpointModel> fetchEndpoint(UUID id);

    /**
     * Fetch the endpoint that contains the given domain.
     *
     * @param domain The domain to search for
     *
     * @return The endpoint that contains the domain
     */
    public abstract CompletableFuture<EndpointModel> fetchEndpoint(String domain);

    /**
     * Fetch all endpoints that match the given domain pattern.
     *
     * @param pattern A pattern, such as `*.example.com`
     *
     * @return All endpoints that match the given pattern
     */
    public abstract CompletableFuture<List<EndpointModel>> fetchEndpoints(String pattern);

    /**
     * Fetch all endpoints that contain the given tag.
     *
     * @param tag An arbitrary tag
     *
     * @return All endpoints that match the given tag
     */
    public abstract CompletableFuture<List<EndpointModel>> fetchEndpointsByTag(String tag);

    /**
     * Create a new endpoint.
     *
     * @param endpoint A partially-filled endpoint
     *
     * @return The finalised endpoint
     */
    public abstract CompletableFuture<EndpointModel> createEndpoint(EndpointModel endpoint);

    /**
     * Permanently delete the endpoint.
     *
     * @param endpoint The endpoint to delete
     */
    public abstract CompletableFuture<Void> deleteEndpoint(EndpointModel endpoint);

    /**
     * Persists any changes to the given endpoint.
     *
     * @param endpoint The endpoint to update
     */
    public abstract CompletableFuture<Void> updateEndpoint(EndpointModel endpoint);

    /**
     * Fetch all configured providers.
     *
     * @return All providers known to gatekeeper
     */
    public abstract CompletableFuture<List<ProviderModel>> fetchProviders();

    /**
     * Create a new provider.
     *
     * @param provider A partially-filled provider
     *
     * @return The finalised provider
     */
    public abstract CompletableFuture<ProviderModel> createProvider(ProviderModel provider);

    /**
     * Fetch a provider by its UUID.
     *
     * @param uuid The UUID of the provider
     *
     * @return The specified provider
     */
    public abstract CompletableFuture<ProviderModel> fetchProvider(UUID uuid);

    /**
     * Fetch a provider by its ID.
     *
     * @param id The ID of the provider
     *
     * @return The specified provider
     */
    public abstract CompletableFuture<ProviderModel> fetchProvider(String id);

    /**
     * Fetch a provider by its ID without acquiring a global lock first.
     *
     * @param id The ID of the provider
     *
     * @return The specified provider
     */
    public abstract CompletableFuture<ProviderModel> fetchProviderUnsafe(String id);

    /**
     * Fetch the certificate for the given endpoint.
     *
     * @param endpoint The endpoint to query
     *
     * @return The certificate, if it exists
     */
    public abstract CompletableFuture<CertificateModel> fetchCertificate(EndpointModel endpoint);

    /**
     * Fetch the certificate for the given endpoint, blocking until the certificate is generated.
     *
     * @param endpoint The endpoint to query
     *
     * @return The certificate, if it exists
     */
    public abstract CompletableFuture<CertificateModel> fetchCertificateBlocking(EndpointModel endpoint);

    /**
     * Update the certificate on the given endpoint
     *
     * @param endpoint    The endpoint to update
     * @param certificate The new certificate
     */
    public abstract CompletableFuture<Void> updateCertificate(EndpointModel endpoint, CertificateModel certificate);
}
