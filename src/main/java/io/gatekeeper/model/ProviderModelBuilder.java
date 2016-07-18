package io.gatekeeper.model;

import io.gatekeeper.api.model.*;
import org.json.JSONObject;

import java.util.UUID;

public class ProviderModelBuilder extends AbstractModelBuilder<Provider, ProviderModel> {

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
