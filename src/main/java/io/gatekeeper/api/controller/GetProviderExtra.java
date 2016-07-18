package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.NotFoundException;
import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.BackendService;
import io.gatekeeper.node.service.ProviderService;
import io.gatekeeper.node.service.provider.AbstractProvider;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class GetProviderExtra extends AbstractController {

    /**
     * {@inheritDoc}
     */
    public GetProviderExtra(ServiceContainer container, RoutingContext context) {
        super(container, context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke() throws Exception {
        UUID id;
        ProviderModel provider = null;

        try {
            id = UUID.fromString(query("id"));

            provider = (ProviderModel) get(BackendService.class)
                .fetchProvider(id)
                .get(timeout, timeoutUnit);
        } catch (IllegalArgumentException exception) {
            provider = (ProviderModel) get(BackendService.class)
                .fetchProvider((String) query("id"))
                .get(timeout, timeoutUnit);
        }

        if (provider == null) {
            throw new NotFoundException();
        }

        AbstractProvider service = get(ProviderService.class)
            .getProvider(provider);

        if (service instanceof AbstractProvider.SubRoutable) {
            ((AbstractProvider.SubRoutable) service).handle(context);
        }

        throw new NotFoundException();
    }
}
