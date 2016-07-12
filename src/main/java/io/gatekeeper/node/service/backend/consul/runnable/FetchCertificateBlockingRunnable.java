package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.node.service.ProviderService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;
import io.gatekeeper.node.service.provider.AbstractProvider;

import java.sql.Date;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class FetchCertificateBlockingRunnable extends FetchCertificateRunnable {

    public FetchCertificateBlockingRunnable(
        ReplicationService replication,
        ProviderService providers,
        EndpointModel endpoint,
        CompletableFuture<CertificateModel> future,
        Client client
    ) {
        super(replication, providers, endpoint, future, client);
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
        CertificateModel current = fetchExisting();

        if (current != null) {

            // TODO: Start renewing certificates before they expire!
            if (current.getExpires().after(Date.from(Instant.now()))) {
                return current;
            }

        }

        if (providers.canRenew(endpoint)) {
            return providers.renewEndpoint(endpoint).get();
        }

        throw new HttpResponseException(405, "This provider does not support renewing");
    }
}
