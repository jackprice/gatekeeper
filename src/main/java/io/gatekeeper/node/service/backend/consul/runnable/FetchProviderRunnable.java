package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.model.ProviderModelBuilder;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FetchProviderRunnable implements Runnable {

    private ReplicationService replication;

    private UUID uuid;

    private CompletableFuture<ProviderModel> future;

    private Client client;

    public FetchProviderRunnable(
        ReplicationService replication,
        UUID uuid,
        CompletableFuture<ProviderModel> future,
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

    private ProviderModel runInternal() throws Exception {
        String key = String.format("provider/%s", uuid.toString());

        String data = client.get(key);

        if (data == null) {
            return null;
        }

        return new ProviderModelBuilder().unserialise(data);
    }
}
