package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.model.ProviderModelBuilder;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FetchProviderByIdUnsafeRunnable implements Runnable {

    private String id;

    private CompletableFuture<ProviderModel> future;

    private Client client;

    public FetchProviderByIdUnsafeRunnable(
        String id,
        CompletableFuture<ProviderModel> future,
        Client client
    ) {
        this.id = id;
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

    private ProviderModel runInternal() throws Exception {
        List<String> keys = client.list("provider/");

        if (keys == null) {
            return null;
        }

        for (String key : keys) {
            String data = client.get(key);
            ProviderModel model = new ProviderModelBuilder().unserialise(data);

            if (model.getId().equals(id)) {
                return model;
            }
        }

        return null;
    }
}
