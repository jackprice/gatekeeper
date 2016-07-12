package io.gatekeeper.node.service.backend.consul.runnable;

import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.EndpointModelBuilder;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.backend.consul.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FetchEndpointsRunnable implements Runnable {

    private ReplicationService replication;

    private CompletableFuture<List<EndpointModel>> future;

    private Client client;

    public FetchEndpointsRunnable(
        ReplicationService replication,
        CompletableFuture<List<EndpointModel>> future,
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

    private List<EndpointModel> runInternal() throws Exception {
        List<String> keys = client.list("endpoint/");

        if (keys == null) {
            return Collections.emptyList();
        }

        List<EndpointModel> endpoints = new ArrayList<>(keys.size());

        for (String key : keys) {
            String data = client.get(key);
            EndpointModel model = new EndpointModelBuilder().unserialise(data);

            endpoints.add(model);
        }

        return endpoints;
    }
}
