package io.gatekeeper.model;

import io.gatekeeper.api.model.Endpoint;
import io.gatekeeper.api.model.NewEndpoint;
import io.gatekeeper.api.model.UpdateEndpoint;
import org.json.JSONObject;

import java.util.UUID;

public class EndpointModelBuilder extends AbstractModelBuilder<Endpoint, EndpointModel> {

    @Override
    public Endpoint toApiModel(EndpointModel model) {
        Endpoint endpoint = new Endpoint();

        endpoint.setUuid(model.getUuid().toString());
        endpoint.setDomains(model.getDomains());
        endpoint.setTags(model.getTags());
        endpoint.setProvider(model.getProvider());
        endpoint.setDn(model.getDn());

        return endpoint;
    }

    @Override
    public EndpointModel fromApiModel(Endpoint endpoint) {
        EndpointModel model = new EndpointModel();

        model.setUuid(UUID.fromString(endpoint.getUuid()));
        model.setDomains(endpoint.getDomains());
        model.setTags(endpoint.getTags());
        model.setProvider(endpoint.getProvider());
        model.setDn(endpoint.getDn());

        return model;
    }

    public EndpointModel fromApiModel(NewEndpoint endpoint) {
        EndpointModel model = new EndpointModel();

        model.setDomains(endpoint.getDomains());
        model.setTags(endpoint.getTags());
        model.setProvider(endpoint.getProvider());
        model.setDn(endpoint.getDn());

        return model;
    }

    public EndpointModel fromApiModel(UpdateEndpoint endpoint) {
        EndpointModel model = new EndpointModel();

        model.setDomains(endpoint.getDomains());
        model.setTags(endpoint.getTags());
        model.setProvider(endpoint.getProvider());
        model.setDn(endpoint.getDn());

        return model;
    }
}
