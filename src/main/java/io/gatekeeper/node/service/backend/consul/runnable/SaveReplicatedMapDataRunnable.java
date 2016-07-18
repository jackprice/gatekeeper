package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.node.service.backend.consul.Client;
import io.gatekeeper.util.Serialiser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SaveReplicatedMapDataRunnable<V extends Serializable> implements Runnable {

    private final UUID uuid;

    private final HashMap<String, V> data;

    private final CompletableFuture<Void> future;

    private final Client client;

    public SaveReplicatedMapDataRunnable(
        UUID uuid,
        HashMap<String, V> data,
        CompletableFuture<Void> future,
        Client client
    ) {
        this.uuid = uuid;
        this.data = data;
        this.future = future;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            runInternal();

            future.complete(null);
        } catch (Throwable exception) {
            future.completeExceptionally(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private void runInternal() throws Exception {
        String key = String.format("map/%s", uuid.toString());

        String data = Serialiser.build(HashMap.class).serialise(this.data);

        client.put(key, data);
    }
}
