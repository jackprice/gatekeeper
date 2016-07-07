package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.model.Version;
import io.gatekeeper.node.ServiceContainer;
import io.vertx.ext.web.RoutingContext;

import static io.gatekeeper.Version.CURRENT;

public class GetVersion extends AbstractController {

    /**
     * {@inheritDoc}
     */
    public GetVersion(ServiceContainer container, RoutingContext context) {
        super(container, context);
    }

    @Override
    public Object invoke() throws Exception {
        Version version = new Version();

        version.version(CURRENT.toString())
            .major((int) CURRENT.major)
            .minor((int) CURRENT.minor)
            .revision((int) CURRENT.revision);

        return version;
    }
}
