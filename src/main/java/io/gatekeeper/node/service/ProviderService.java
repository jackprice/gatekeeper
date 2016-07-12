package io.gatekeeper.node.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.api.model.Provider;
import io.gatekeeper.logging.Loggers;
import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.ServiceContainerAware;
import io.gatekeeper.node.service.provider.AbstractProvider;
import io.gatekeeper.node.service.provider.SelfSignedProvider;

import java.lang.reflect.Constructor;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

public class ProviderService extends ServiceContainerAware implements Service {

    protected final Logger logger;

    protected final ThreadPoolExecutor executor;

    protected final Map<String, AbstractProvider> providers = new HashMap<>();

    /**
     * Default public constructor.
     */
    public ProviderService() {
        logger = Loggers.getNodeLogger();
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(
            (new ThreadFactoryBuilder())
                .setNameFormat("Provider Service %d")
                .build()
        );

        this.executor.prestartCoreThread();
        this.executor.prestartAllCoreThreads();
    }

    /**
     * Builder helper function for the default constructor.
     *
     * @param services The service container
     *
     * @return A properly-configured provider service (unstarted)
     */
    public static ProviderService build(ServiceContainer services) {
        ProviderService service = new ProviderService();

        service.setContainer(services);

        return service;
    }

    /**
     * Signal a provider service to stop, reload its configuration and restart.
     *
     * @param model The model representation of the provider to reload
     */
    public void reloadProvider(ProviderModel model) {
        assert null != model;

        synchronized (providers) {
            stopAndRemoveProvider(model);
            loadProvider(model);
            startProvider(model);
        }
    }

    /**
     * Trigger the given endpoint to renew.
     *
     * @param endpoint The endpoint to renew
     */
    public CompletableFuture<CertificateModel> renewEndpoint(EndpointModel endpoint) {
        CompletableFuture<CertificateModel> future = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                lockEndpoint(endpoint.getUuid());

                CertificateModel certificate = ((AbstractProvider.Renewable) getProvider(endpoint.getProvider()))
                    .renew(endpoint)
                    .get();

                updateCertificateSync(endpoint, certificate);

                future.complete(certificate);
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            } finally {
                try {
                    unlockEndpoint(endpoint.getUuid());
                } catch (Exception exception) {
                    // TODO: Log me!
                }
            }
        });

        return future;
    }

    private void updateCertificateSync(EndpointModel endpoint, CertificateModel certificate) throws Exception {
        service(BackendService.class)
            .updateCertificate(endpoint, certificate)
            .get();
    }

    /**
     * Check if the given endpoint is renewable.
     *
     * @param endpoint The endpoint to check
     *
     * @return True if the endpoint can be renewed
     */
    public Boolean canRenew(EndpointModel endpoint) throws Exception {
        return getProvider(endpoint.getProvider()) instanceof AbstractProvider.Renewable;
    }

    /**
     * Acquire an exclusive lock on the given endpoint.
     *
     * @param uuid The UUID of the endpoint to lock
     *
     * @throws InterruptedException
     */
    private void lockEndpoint(UUID uuid) throws InterruptedException {
        service(ReplicationService.class).lock(
            String.format("endpoint-%s", uuid.toString())
        );
    }

    /**
     * Release an exclusive lock on the given endpoint.
     *
     * @param uuid The UUID of the endpoint to unlock
     *
     * @throws InterruptedException
     */
    private void unlockEndpoint(UUID uuid) throws InterruptedException {
        service(ReplicationService.class).unlock(
            String.format("endpoint-%s", uuid.toString())
        );
    }

    /**
     * Get a started and configured provider service.
     *
     * @param id The ID of the provider
     */
    public AbstractProvider getProvider(String id) throws Exception {
        synchronized (providers) {
            if (providers.containsKey(id)) {
                return providers.get(id);
            } else {
                ProviderModel provider = (ProviderModel) service(BackendService.class)
                    .fetchProviderUnsafe(id)
                    .get();

                loadProvider(provider);
                startProvider(provider);

                return providers.get(id);
            }
        }
    }

    /**
     * Get a started and configured provider service.
     *
     * @param model The model representation of the provider
     */
    public AbstractProvider getProvider(ProviderModel model) {
        synchronized (providers) {
            if (providers.containsKey(model.getId())) {
                return providers.get(model.getId());
            } else {
                loadProvider(model);
                startProvider(model);

                return providers.get(model.getId());
            }
        }
    }

    /**
     * Start the given provider.
     *
     * Note that the provider specified must have already been loaded ({@link #loadProvider(ProviderModel)})
     *
     * @param model
     */
    private void startProvider(ProviderModel model) {
        assert null != model;

        providers.get(model.getId()).start().join();
    }

    /**
     * Load the provider specified in the model passed into memory (unstarted).
     *
     * @param model The model holding provider data
     */
    private void loadProvider(ProviderModel model) {
        assert null != model;

        providers.put(model.getId(), createProviderFromModel(model));
    }

    /**
     * Stop the provider given by the ID passed.
     * Note that this will SILENTLY fail for providers that don't exist, as we're presuming they just haven't been
     * loaded yet.
     *
     * This will also remove the provider from memory.
     *
     * @param model The model representation of the provider to stop
     */
    private void stopAndRemoveProvider(ProviderModel model) {
        if (!providers.containsKey(model.getId())) {
            return;
        }

        try {
            providers.get(model.getId()).close();
        } catch (Exception exception) {
            logger.warning(
                String.format(
                    "Could not cleanly stop provider %s (%s: %s)",
                    model.getId(),
                    exception.getClass().getCanonicalName(),
                    exception.getMessage()
                )
            );
        }

        providers.remove(model.getId());
    }

    @Override
    public CompletableFuture start() {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Validate that the given provider model will create a properly configured service.
     *
     * @param model The model holding the configuration
     *
     * @throws InvalidConfigurationException
     */
    public void validateProviderModel(ProviderModel model) throws InvalidConfigurationException {
        // TODO: Is creating a provider too heavy here?
        createProviderFromModel(model);
    }

    /**
     * Convert a provider model enum into its corresponding service class.
     *
     * @param type The provider type enum
     *
     * @return The corresponding service class
     *
     * @throws Exception
     */
    private Class<? extends AbstractProvider> getClassFromType(Provider.TypeEnum type) throws Exception {
        switch (type) {
            case SIGNED:
                return SelfSignedProvider.class;

            default:
                throw new Exception("Invalid provider type");
        }
    }

    /**
     * Create a provider service from the given model.
     *
     * @param model The model holding the configuration for this provider
     *
     * @return A configured (but unstarted) service
     *
     * @throws InvalidConfigurationException
     */
    AbstractProvider createProviderFromModel(ProviderModel model) throws InvalidConfigurationException {
        try {
            Class<? extends AbstractProvider> clazz = getClassFromType(model.getType());

            Constructor<? extends AbstractProvider> constructor = clazz.getConstructor(Executor.class);

            try {
                AbstractProvider provider = constructor.newInstance(executor);

                provider.configure(model);

                return provider;
            } catch (Exception exception) {
                throw new InvalidConfigurationException("Could not create provider", exception);
            }
        } catch (Exception exception) {
            throw new InvalidConfigurationException("Could not create provider", exception);
        }
    }

    @Override
    public void close() throws Exception {
        // NOP
    }
}
