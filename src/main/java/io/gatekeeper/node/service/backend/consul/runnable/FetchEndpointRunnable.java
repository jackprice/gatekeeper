package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.EndpointModelBuilder;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FetchEndpointRunnable implements Runnable {

    private ReplicationService replication;

    private UUID uuid;

    private CompletableFuture<EndpointModel> future;

    private Client client;

    public FetchEndpointRunnable(
        ReplicationService replication,
        UUID uuid,
        CompletableFuture<EndpointModel> future,
        Client client
    ) {
        this.replication = replication;
        this.uuid = uuid;
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
        String key = String.format("endpoint/%s", uuid.toString());

        String data = client.get(key);

        if (data == null) {
            return null;
        }

        return new EndpointModelBuilder().unserialise(data);
    }
}
