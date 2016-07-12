package io.gatekeeper.node.service;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.OutputConfiguration;
import io.gatekeeper.logging.Loggers;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.output.AbstractOutputService;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class OutputService implements Service {

    protected Configuration configuration;

    protected ReplicationService replication;

    protected Logger logger;

    protected List<AbstractOutputService> outputs = new ArrayList<>();

    /**
     * Internal constructor
     *
     * @param configuration The global configuration
     * @param replication   The global replication service
     */
    OutputService(Configuration configuration, ReplicationService replication) {
        this.configuration = configuration;
        this.replication = replication;
        this.logger = Loggers.getOutputLogger();
    }

    /**
     * Public factory method for building the service from configuration.
     *
     * @param configuration The global configuration
     * @param services      The global service container
     *
     * @return An initialised (unstarted) service
     */
    public static OutputService build(Configuration configuration, ServiceContainer services) {
        return new OutputService(configuration, services.service(ReplicationService.class));
    }

    @Override
    public CompletableFuture start() {
        try {
            createServices();
        } catch (Exception exception) {
            CompletableFuture<Void> future = new CompletableFuture<>();

            future.completeExceptionally(exception);

            return future;
        }

        CompletableFuture futures[] = new CompletableFuture[outputs.size()];

        for (int i = 0; i < outputs.size(); i++) {
            futures[i] = outputs.get(i).start();
        }

        return CompletableFuture.allOf(futures);
    }

    @Override
    public void close() throws Exception {
        this.logger.info(String.format("Shutting down %d output services", this.outputs.size()));

        Exception thrown = null;

        for (AbstractOutputService output : outputs) {
            try {
                output.close();
            } catch (Exception exception) {
                this.logger.info(String.format("Output service %s failed to shut down cleanly", output.getClass()));

                thrown = exception;
            }
        }

        if (thrown != null) {
            throw thrown;
        }
    }

    /**
     * Create the services specified by the configuration.
     */
    @SuppressWarnings("unchecked")
    private void createServices() throws Exception {
        for (OutputConfiguration configuration : this.configuration.outputs) {
            outputs.add(createService(configuration));
        }
    }

    /**
     * Create the service specified by the given configuration.
     *
     * @param configuration The configuration for the service
     *
     * @return The configured (unstarted) service
     */
    private <U extends AbstractOutputService> U createService(OutputConfiguration<?, U> configuration) throws
        Exception {
        Class<U> clazz = configuration.getServiceClass();

        Constructor<U> constructor = clazz.getConstructor(
            Configuration.class,
            configuration.getClass(),
            ReplicationService.class
        );

        return constructor.newInstance(this.configuration, configuration, replication);
    }
}
