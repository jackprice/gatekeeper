package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.model.ProviderModelBuilder;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SaveProviderDataByIdUnsafeRunnable implements Runnable {

    private UUID id;

    private Map<String, Object> data;

    private CompletableFuture<Void> future;

    private Client client;

    public SaveProviderDataByIdUnsafeRunnable(
        UUID id,
        Map<String, Object> data,
        CompletableFuture<Void> future,
        Client client
    ) {
        this.id = id;
        this.data = data;
        this.future = future;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            runInternal();

            future.complete(null);
        } catch (Exception exception) {
            future.completeExceptionally(exception);
        }
    }

    private void runInternal() throws Exception {
        String key = String.format("provider/%s", id.toString());

        String data = client.get(key);

        if (data == null) {
            throw new Exception("Provider not found");
        }

        ProviderModel model = new ProviderModelBuilder().unserialise(data);

        Map<String, Object> mergedData = model.getData();

        mergedData.putAll(this.data);

        model.setData(mergedData);

        client.put(String.format("provider/%s", id.toString()), new ProviderModelBuilder().serialise(model));
    }
}
