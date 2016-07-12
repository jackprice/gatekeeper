package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.node.service.ProviderService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.concurrent.CompletableFuture;

public class ReissueCertificateBlockingRunnable implements Runnable {

    protected ReplicationService replication;

    protected ProviderService providers;

    protected EndpointModel endpoint;

    protected CompletableFuture<CertificateModel> future;

    protected Client client;

    public ReissueCertificateBlockingRunnable(
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
            future.complete(runInternal());
        } catch (Exception exception) {
            future.completeExceptionally(exception);
        }
    }

    protected CertificateModel runInternal() throws Exception {
        if (providers.canRenew(endpoint)) {
            return providers.renewEndpoint(endpoint).get();
        }

        throw new HttpResponseException(405, "This provider does not support renewing");
    }
}
