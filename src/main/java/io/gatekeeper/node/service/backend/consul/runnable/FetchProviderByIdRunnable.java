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

public class FetchProviderByIdRunnable implements Runnable {

    private ReplicationService replication;

    private String id;

    private CompletableFuture<ProviderModel> future;

    private Client client;

    public FetchProviderByIdRunnable(
        ReplicationService replication,
        String id,
        CompletableFuture<ProviderModel> future,
        Client client
    ) {
        this.replication = replication;
        this.id = id;
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
