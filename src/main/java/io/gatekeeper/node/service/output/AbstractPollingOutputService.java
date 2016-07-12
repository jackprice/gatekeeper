package io.gatekeeper.node.service.output;

import io.gatekeeper.api.model.Certificate;
import io.gatekeeper.api.model.DefaultApi;
import io.gatekeeper.api.model.Endpoint;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.OutputConfiguration;
import io.gatekeeper.node.service.ReplicationService;
import io.swagger.client.ApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

abstract class AbstractPollingOutputService<OutputConfigurationType extends OutputConfiguration> extends
    AbstractOutputService<OutputConfigurationType> {

    protected long pollingFrequency = 30;

    protected TimeUnit pollingFrequencyUnit = TimeUnit.SECONDS;

    AbstractPollingOutputService(
        Configuration configuration,
        OutputConfigurationType outputConfiguration,
        ReplicationService replication
    ) {
        super(configuration, outputConfiguration, replication);
    }

    @Override
    public CompletableFuture start() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                startSync();

                future.complete(null);
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    /**
     * Start this service.
     * Called internally ({@link #start()}).
     */
    void startSync() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                poll();
            } catch (Throwable exception) {
                logger.warning("Polling exited exceptionally");
            }
        }, 5, pollingFrequency, pollingFrequencyUnit);
    }

    /**
     * Look for all certificates that we have access to.
     */
    void poll() throws Exception {
        logger.info("Polling for certificates");

        DefaultApi client = getApi();
        List<Endpoint> endpoints;

        if (client == null) {
            logger.info("No client available, skipping polling");

            return;
        }

        try {
            endpoints = client.endpointGet(outputConfiguration.domains, outputConfiguration.tags);
        } catch (Exception exception) {
            logger.warning("Fetching endpoints failed");

            return;
        }

        if (endpoints == null) {
            logger.warning("Fetching endpoints failed");

            return;
        }

        logger.info(String.format("Found %d endpoints", endpoints.size()));

        Map<Endpoint, Certificate> certificates = new HashMap<>();

        for (Endpoint endpoint : endpoints) {
            Certificate certificate;

            try {
                certificate = client.endpointIdCertificateGet(endpoint.getUuid(), null);
            } catch (Exception exception) {
                logger.warning("Fetching endpoint certificate failed");

                return;
            }

            certificates.put(endpoint, certificate);
        }

        logger.info(String.format("Found %d certificates", certificates.size()));

        poll(certificates);
    }

    /**
     * Fired when certificates are updated.
     *
     * @param certificates All certificates for this output
     */
    abstract void poll(Map<Endpoint, Certificate> certificates);
}
