package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.model.*;
import io.gatekeeper.node.service.ProviderService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;
import io.gatekeeper.node.service.provider.AbstractProvider;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FetchCertificateRunnable implements Runnable {

    protected ReplicationService replication;

    protected ProviderService providers;

    protected EndpointModel endpoint;

    protected CompletableFuture<CertificateModel> future;

    protected Client client;

    public FetchCertificateRunnable(
        ReplicationService replication,
        ProviderService providers,
        EndpointModel endpoint,
        CompletableFuture<CertificateModel> future,
        Client client
    ) {
        this.replication = replication;
        this.providers = providers;
        this.endpoint = endpoint;
        this.future = future;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            replication.lock();

            future.complete(runInternal());
        } catch (Exception exception) {
            future.completeExceptionally(exception);
        } finally {
            try {
                replication.unlock();
            } catch (InterruptedException exception) {
                // TODO: Log this
            }
        }
    }

    protected CertificateModel runInternal() throws Exception {
        CertificateModel current = fetchExisting();

        if (current != null) {

            // TODO: Start renewing certificates before they expire!
            if (current.getExpires().after(Date.from(Instant.now()))) {
                return current;
            }

        }

        if (providers.canRenew(endpoint)) {
            providers.renewEndpoint(endpoint);

            return null;
        }

        throw new HttpResponseException(405, "This provider does not support renewing");
    }

    /**
     * Attempt to fetch the existing certificate for this endpoint.
     *
     * @return The current certificate, if set
     *
     * @throws Exception
     */
    protected CertificateModel fetchExisting() throws Exception {
        if (endpoint.getCertificate() == null) {
            return null;
        }

        String key = String.format("certificate/%s", endpoint.getCertificate().toString());

        String data = client.get(key);

        if (data == null) {
            return null;
        }

        return new CertificateModelBuilder().unserialise(data);
    }

    /**
     * Retrieve the provider for this endpoint.
     *
     * @return The provider for this endpoint
     *
     * @throws Exception
     */
    protected ProviderModel getProvider() throws Exception {
        List<String> keys = client.list("provider/");

        if (keys == null) {
            return null;
        }

        for (String key : keys) {
            String data = client.get(key);
            ProviderModel model = new ProviderModelBuilder().unserialise(data);

            if (model.getId().equals(endpoint.getProvider())) {
                return model;
            }
        }

        throw new Exception("Could not find provider");
    }
}
