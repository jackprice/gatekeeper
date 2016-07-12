package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.api.model.NewEndpoint;
import io.gatekeeper.api.model.UpdateEndpoint;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.EndpointModelBuilder;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.BackendService;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class PatchEndpoint extends AbstractController {

    /**
     * {@inheritDoc}
     */
    public PatchEndpoint(ServiceContainer container, RoutingContext context) {
        super(container, context);
    }

    @Override
    public Object invoke() throws Exception {
        UpdateEndpoint data = readBodyAs(UpdateEndpoint.class);
        UUID id;

        try {
            id = UUID.fromString(query("id"));
        } catch (IllegalArgumentException exception) {
            throw new HttpResponseException(422, "Invalid ID");
        }

        EndpointModel endpoint = (EndpointModel) get(BackendService.class)
            .fetchEndpoint(id)
            .get(timeout, timeoutUnit);

        EndpointModel updatedEndpoint = new EndpointModelBuilder().fromApiModel(data);

        updatedEndpoint.setUuid(id);

        get(BackendService.class)
            .updateEndpoint(updatedEndpoint)
            .get(timeout, timeoutUnit);

        return updatedEndpoint;
    }
}
