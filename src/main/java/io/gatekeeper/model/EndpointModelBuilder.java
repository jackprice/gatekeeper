package io.gatekeeper.model;

import io.gatekeeper.api.model.Endpoint;
import io.gatekeeper.api.model.NewEndpoint;
import io.gatekeeper.api.model.UpdateEndpoint;
import org.json.JSONObject;

import java.util.UUID;

public class EndpointModelBuilder extends AbstractModelBuilder<Endpoint, EndpointModel> {

    @Override
    public EndpointModel unserialise(String data) {
        JSONObject object = new JSONObject(data);
        EndpointModel model = new EndpointModel();

        if (object.has("uuid")) {
            model.setUuid(UUID.fromString(object.getString("uuid")));
        }
        if (object.has("certificate")) {
            model.setCertificate(UUID.fromString(object.getString("certificate")));
        }
        if (object.has("domains")) {
            object.getJSONArray("domains")
                .forEach((domain) -> {
                    model.addDomain((String) domain);
                });
        }
        if (object.has("tags")) {
            object.getJSONArray("tags")
                .forEach((tag) -> {
                    model.addTag((String) tag);
                });
        }
        if (object.has("provider")) {
            model.setProvider(object.getString("provider"));
        }
        if (object.has("dn")) {
            model.setDn(object.getString("dn"));
        }

        return model;
    }

    @Override
    public String serialise(EndpointModel model) {
        JSONObject object = new JSONObject();

        object.put("uuid", model.getUuid().toString());

        if (model.getCertificate() != null) {
            object.put("certificate", model.getCertificate().toString());
        }

        object.put("domains", model.getDomains());
        object.put("tags", model.getTags());
        object.put("provider", model.getProvider());
        object.put("dn", model.getDn());

        return object.toString();
    }

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
