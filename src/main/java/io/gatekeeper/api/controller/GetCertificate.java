package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.api.NotFoundException;
import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.BackendService;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GetCertificate extends AbstractController {

    /**
     * {@inheritDoc}
     */
    public GetCertificate(ServiceContainer container, RoutingContext context) {
        super(container, context);
    }

    @Override
    public Object invoke() throws Exception {
        String wait = query("wait");
        Integer waitSeconds = null;
        UUID id;
        EndpointModel endpoint;
        CertificateModel certificate;

        if (wait != null) {
            try {
                waitSeconds = Integer.parseUnsignedInt(wait);
            } catch (NumberFormatException exception) {
                throw new HttpResponseException(422, "Invalid value for wait parameter");
            }
        }

        try {
            id = UUID.fromString(query("id"));
        } catch (IllegalArgumentException exception) {
            throw new HttpResponseException(422, "Invalid ID");
        }

        endpoint = (EndpointModel) get(BackendService.class)
            .fetchEndpoint(id)
            .get(timeout, timeoutUnit);

        if (endpoint == null) {
            throw new NotFoundException();
        }

        if (waitSeconds == null) {
            certificate = (CertificateModel) get(BackendService.class)
                .fetchCertificate(endpoint)
                .get(timeout, timeoutUnit);

            return certificate;
        } else {
            try {
                certificate = (CertificateModel) get(BackendService.class)
                    .fetchCertificateBlocking(endpoint)
                    .get(waitSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                status(204);

                return new Object();
            }

            return certificate;
        }
    }
}
