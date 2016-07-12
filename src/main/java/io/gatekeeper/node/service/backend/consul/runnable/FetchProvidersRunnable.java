package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.EndpointModelBuilder;
import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.model.ProviderModelBuilder;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FetchProvidersRunnable implements Runnable {

    private ReplicationService replication;

    private CompletableFuture<List<ProviderModel>> future;

    private Client client;

    public FetchProvidersRunnable(
        ReplicationService replication,
        CompletableFuture<List<ProviderModel>> future,
        Client client
    ) {
        this.replication = replication;
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

    private List<ProviderModel> runInternal() throws Exception {
        List<String> keys = client.list("provider/");

        if (keys == null) {
            return Collections.emptyList();
        }

        List<ProviderModel> providers = new ArrayList<>(keys.size());

        for (String key : keys) {
            String data = client.get(key);
            ProviderModel model = new ProviderModelBuilder().unserialise(data);

            providers.add(model);
        }

        return providers;
    }
}
