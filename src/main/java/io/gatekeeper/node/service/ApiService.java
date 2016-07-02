package io.gatekeeper.node.service;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.setup.Environment;
import io.gatekeeper.api.v1.EndpointResource;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.logging.Loggers;
import org.eclipse.jetty.server.Server;

import javax.validation.Validation;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ApiService implements Service {

    private final Logger logger;

    private final Configuration configuration;

    private final ReplicationService replication;

    private final Environment environment;

    private Server server;

    public ApiService(Configuration configuration, ReplicationService replication) {
        this.configuration = configuration;
        this.replication = replication;
        this.logger = Loggers.getApiLogger();
        this.environment = new Environment(
            "Gatekeeper API",
            null,
            Validation.buildDefaultValidatorFactory().getValidator(),
            new MetricRegistry(),
            null
        );

        this.environment.jersey().register(new EndpointResource(this.replication));
    }

    public CompletableFuture start() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        DefaultServerFactory factory = new io.dropwizard.server.DefaultServerFactory();

        HttpConnectorFactory connector = new HttpConnectorFactory();

//        connector.setPort(this.configuration.api.bindPort);
//        connector.setBindHost(this.configuration.api.bindAddress);

        factory.setApplicationConnectors(Collections.singletonList(connector));

        this.server = factory.build(this.environment);

        try {
            this.server.start();

            future.complete(null);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public void close() throws IOException {
        this.logger.info("Shutting down Jetty server");

        try {
            this.server.stop();
            this.server.join();

        } catch (Exception e) {
            this.logger.warning("Jetty server shutdown did not shut down cleanly");
        }
    }
}
