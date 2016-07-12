package io.gatekeeper.model;

import io.gatekeeper.api.model.*;
import org.json.JSONObject;

import java.util.UUID;

public class ProviderModelBuilder extends AbstractModelBuilder<Provider, ProviderModel> {

    @Override
    public ProviderModel unserialise(String data) throws Exception {
        JSONObject object = new JSONObject(data);
        ProviderModel model = new ProviderModel();

        if (object.has("uuid")) {
            model.setUuid(UUID.fromString(object.getString("uuid")));
        }
        if (object.has("id")) {
            model.setId(object.getString("id"));
        }
        if (object.has("type")) {
            model.setType(enumFromString(Provider.TypeEnum.class, object.getString("type")));
        }
        if (object.has("configuration")) {
            model.setConfiguration(jsonObjectToMap(object.getJSONObject("configuration")));
        }

        return model;
    }

    @Override
    public String serialise(ProviderModel model) {
        JSONObject object = new JSONObject();

        object.put("uuid", model.getUuid().toString());
        object.put("id", model.getId());
        object.put("type", model.getType().toString());
        object.put("configuration", model.getConfiguration());

        return object.toString();
    }

    @Override
    public Provider toApiModel(ProviderModel model) {
        Provider provider = new Provider();

        provider.uuid(model.getUuid().toString());
        provider.id(model.getId());
        provider.type(model.getType());
        provider.configuration(model.getConfiguration());

        return provider;
    }

    @Override
    public ProviderModel fromApiModel(Provider provider) {
        ProviderModel model = new ProviderModel();

        model.setUuid(UUID.fromString(provider.getUuid()));
        model.setId(provider.getId());
        model.setType(provider.getType());
        model.setConfiguration(provider.getConfiguration());

        return model;
    }

    public ProviderModel fromApiModel(NewProvider provider) throws Exception {
        ProviderModel model = new ProviderModel();

        model.setId(provider.getId());
        model.setType(castEnum(provider.getType(), Provider.TypeEnum.class));
        model.setConfiguration(provider.getConfiguration());

        return model;
    }
}
