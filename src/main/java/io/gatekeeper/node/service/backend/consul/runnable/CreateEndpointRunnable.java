package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.EndpointModelBuilder;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CreateEndpointRunnable implements Runnable {

    private ReplicationService replication;

    private EndpointModel endpoint;

    private CompletableFuture<EndpointModel> future;

    private Client client;

    public CreateEndpointRunnable(
        ReplicationService replication,
        EndpointModel endpoint,
        CompletableFuture<EndpointModel> future,
        Client client
    ) {
        this.replication = replication;
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

    private EndpointModel runInternal() throws Exception {
        String data = new EndpointModelBuilder().serialise(endpoint);
        String key = String.format("endpoint/%s", endpoint.getUuid().toString());

        client.put(key, data);

        return endpoint;
    }
}
