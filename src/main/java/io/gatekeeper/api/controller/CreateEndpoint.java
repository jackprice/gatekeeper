package io.gatekeeper.api.controller;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.api.model.NewEndpoint;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.EndpointModelBuilder;
import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.BackendService;
import io.vertx.ext.web.RoutingContext;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

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
            throw new HttpResponseException(422, "Malformed entity", "domains have at least one entry");
        }

        if (data.getProvider() == null || data.getProvider().length() == 0) {
            throw new HttpResponseException(422, "Malformed entity", "provider is required");
        }

        if (data.getDn() == null || data.getDn().length() == 0) {
            throw new HttpResponseException(422, "Malformed entity", "dn is required");
        }

        ProviderModel provider = (ProviderModel) get(BackendService.class)
            .fetchProvider(data.getProvider())
            .get(timeout, timeoutUnit);

        if (provider == null) {
            throw new HttpResponseException(422, "Malformed entity", "No such provider");
        }

        EndpointModel endpoint = new EndpointModelBuilder().fromApiModel(data);

        try {
            endpoint.validate();
        } catch (InvalidConfigurationException exception) {
            throw new HttpResponseException(422, "Malformed entity", exception.getMessage());
        }

        get(BackendService.class)
            .createEndpoint(endpoint)
            .get(timeout, timeoutUnit);

        status(201);

        return endpoint;
    }
}
