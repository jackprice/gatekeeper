package io.gatekeeper.api.controller;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.api.model.NewEndpoint;
import io.gatekeeper.api.model.NewProvider;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.EndpointModelBuilder;
import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.model.ProviderModelBuilder;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.BackendService;
import io.gatekeeper.node.service.ProviderService;
import io.vertx.ext.web.RoutingContext;

public class CreateProvider extends AbstractController {

    /**
     * {@inheritDoc}
     */
    public CreateProvider(ServiceContainer container, RoutingContext context) {
        super(container, context);
    }

    @Override
    public Object invoke() throws Exception {
        NewProvider data = readBodyAs(NewProvider.class);

        if (data.getId() == null || data.getId().length() == 0) {
            throw new HttpResponseException(422, "Malformed entity", "id is required");
        }
        if (data.getConfiguration() == null) {
            throw new HttpResponseException(422, "Malformed entity", "configuration is required");
        }

        ProviderModel provider = new ProviderModelBuilder().fromApiModel(data);

        try {
            get(ProviderService.class)
                .validateProviderModel(provider);
        } catch (InvalidConfigurationException exception) {
            throw new HttpResponseException(422, "Invalid provider configuration", exception.getMessage());
        }

        get(BackendService.class)
            .createProvider(provider)
            .get(timeout, timeoutUnit);

        status(201);

        return provider;
    }
}
