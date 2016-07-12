package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.BackendService;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.UUID;

public class GetEndpoint extends AbstractController {

    /**
     * {@inheritDoc}
     */
    public GetEndpoint(ServiceContainer container, RoutingContext context) {
        super(container, context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke() throws Exception {
        UUID id;

        try {
            id = UUID.fromString(query("id"));
        } catch (IllegalArgumentException exception) {
            throw new HttpResponseException(422, "Invalid ID");
        }

        return get(BackendService.class)
            .fetchEndpoint(id)
            .get(timeout, timeoutUnit);
    }
}
