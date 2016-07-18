package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.node.service.backend.consul.Client;
import io.gatekeeper.util.Serialiser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GetReplicatedMapDataRunnable<V extends Serializable> implements Runnable {

    private final UUID uuid;

    private final CompletableFuture<HashMap<String, V>> future;

    private final Client client;

    public GetReplicatedMapDataRunnable(
        UUID uuid,
        CompletableFuture<HashMap<String, V>> future,
        Client client
    ) {

        this.uuid = uuid;
        this.future = future;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            future.complete(runInternal());
        } catch (Throwable exception) {
            future.completeExceptionally(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, V> runInternal() throws Exception {
        String key = String.format("map/%s", uuid.toString());

        String data = client.get(key);

        if (data == null) {
            return new HashMap<>();
        }

        Object map = Serialiser.build(HashMap.class).unserialise(data);

        if (HashMap.class.isAssignableFrom(map.getClass())) {
            return (HashMap<String, V>) map;
        }

        return new HashMap<>();
    }
}
