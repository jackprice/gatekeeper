package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.node.service.ProviderService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.concurrent.CompletableFuture;

public class ReissueCertificateRunnable implements Runnable {

    protected ReplicationService replication;

    protected ProviderService providers;

    protected EndpointModel endpoint;

    protected CompletableFuture<CertificateModel> future;

    protected Client client;

    public ReissueCertificateRunnable(
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

            runInternal();

            future.complete(null);
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
        if (providers.canRenew(endpoint)) {
            providers.renewEndpoint(endpoint);

            return null;
        }

        throw new HttpResponseException(405, "This provider does not support renewing");
    }
}
