package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.model.ProviderModelBuilder;
import io.gatekeeper.node.service.ProviderService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.concurrent.CompletableFuture;

public class CreateProviderRunnable implements Runnable {

    private final ReplicationService replication;

    private final ProviderService providers;

    private final ProviderModel provider;

    private final CompletableFuture<ProviderModel> future;

    private final Client client;

    public CreateProviderRunnable(
        ReplicationService replication,
        ProviderService providers,
        ProviderModel provider,
        CompletableFuture<ProviderModel> future,
        Client client
    ) {
        this.replication = replication;
        this.providers = providers;
        this.provider = provider;
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
        providers.validateProviderModel(provider);

        String data = new ProviderModelBuilder().serialise(provider);
        String key = String.format("provider/%s", provider.getUuid().toString());

        client.put(key, data);

        return provider;
    }
}
