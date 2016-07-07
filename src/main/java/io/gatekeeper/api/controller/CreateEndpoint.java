package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.api.model.Endpoint;
import io.gatekeeper.api.model.NewEndpoint;
import io.gatekeeper.model.DomainModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.BackendService;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class CreateEndpoint extends AbstractController {

    /**
     * {@inheritDoc}
     */
    public CreateEndpoint(ServiceContainer container, RoutingContext context) {
        super(container, context);
    }

    @Override
    public Object invoke() throws Exception {
        NewEndpoint data = readBodyAs(NewEndpoint.class);

        if (data.getDomains().size() == 0) {
            throw new HttpResponseException(422, "Malformed entity");
        }

        final EndpointModel endpoint = new EndpointModel();

        data.getDomains().forEach((domain) -> endpoint.add(new DomainModel(domain)));

        get(BackendService.class)
            .createEndpoint(endpoint)
            .get(timeout, timeoutUnit);

        return endpoint;
    }
}
