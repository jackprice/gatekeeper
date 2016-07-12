package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.model.*;
import io.gatekeeper.node.service.ProviderService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.concurrent.CompletableFuture;

public class UpdateCertificateRunnable implements Runnable {

    private final ReplicationService replication;

    private final EndpointModel endpoint;

    private final CertificateModel certificate;

    private final CompletableFuture<Void> future;

    private final Client client;

    public UpdateCertificateRunnable(
        ReplicationService replication,
        EndpointModel endpoint,
        CertificateModel certificate,
        CompletableFuture<Void> future,
        Client client
    ) {
        this.replication = replication;
        this.endpoint = endpoint;
        this.certificate = certificate;
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

    private void runInternal() throws Exception {
        String certificateData = new CertificateModelBuilder().serialise(certificate);
        String certificateKey = String.format("certificate/%s", certificate.getUuid().toString());

        client.put(certificateKey, certificateData);

        endpoint.setCertificate(certificate.getUuid());

        String endpointData = new EndpointModelBuilder().serialise(endpoint);
        String endpointKey = String.format("endpoint/%s", endpoint.getUuid().toString());

        client.put(endpointKey, endpointData);
    }
}
