package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.BackendService;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class GetEndpoints extends AbstractController {

    /**
     * {@inheritDoc}
     */
    public GetEndpoints(ServiceContainer container, RoutingContext context) {
        super(container, context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke() throws Exception {
        List<EndpointModel> endpoints;
        String query = query("domain");

        if (query != null) {
            endpoints = (List<EndpointModel>) get(BackendService.class)
                .fetchEndpoints(query)
                .get(timeout, timeoutUnit);
        } else {
            endpoints = (List<EndpointModel>) get(BackendService.class)
                .fetchEndpoints()
                .get(timeout, timeoutUnit);
        }

        return endpoints;
    }
}
