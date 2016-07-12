package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.BackendService;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

public class GetProviders extends AbstractController {

    /**
     * {@inheritDoc}
     */
    public GetProviders(ServiceContainer container, RoutingContext context) {
        super(container, context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke() throws Exception {
        return get(BackendService.class)
            .fetchProviders()
            .get(timeout, timeoutUnit);
    }
}
