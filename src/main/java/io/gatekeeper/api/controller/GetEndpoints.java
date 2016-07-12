package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.BackendService;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
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
        String tag = query("tag");

        if (tag == null && query == null) {
            return get(BackendService.class)
                .fetchEndpoints()
                .get(timeout, timeoutUnit);
        }

        if (tag == null) {
            return get(BackendService.class)
                .fetchEndpoints(query)
                .get(timeout, timeoutUnit);
        }

        if (query == null) {
            return get(BackendService.class)
                .fetchEndpointsByTag(tag)
                .get(timeout, timeoutUnit);
        }

        // Combined filters!
        endpoints = new ArrayList<>();
        List<EndpointModel> queried = (List<EndpointModel>) get(BackendService.class)
            .fetchEndpoints(query)
            .get(timeout, timeoutUnit);
        List<EndpointModel> tagged = (List<EndpointModel>) get(BackendService.class)
            .fetchEndpointsByTag(tag)
            .get(timeout, timeoutUnit);

        for (EndpointModel queryModel : queried) {
            for (EndpointModel tagModel : tagged) {
                if (queryModel.getUuid().equals(tagModel.getUuid())) {
                    endpoints.add(queryModel);

                    break;
                }
            }
        }

        return endpoints;
    }
}
