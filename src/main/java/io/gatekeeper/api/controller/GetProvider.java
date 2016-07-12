package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.BackendService;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class GetProvider extends AbstractController {

    /**
     * {@inheritDoc}
     */
    public GetProvider(ServiceContainer container, RoutingContext context) {
        super(container, context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke() throws Exception {
        UUID id;

        try {
            id = UUID.fromString(query("id"));
        } catch (IllegalArgumentException exception) {
            return get(BackendService.class)
                .fetchProvider((String) query("id"))
                .get(timeout, timeoutUnit);
        }

        return get(BackendService.class)
            .fetchProvider(id)
            .get(timeout, timeoutUnit);
    }
}
