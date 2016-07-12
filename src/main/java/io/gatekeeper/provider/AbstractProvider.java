package io.gatekeeper.provider;

import io.gatekeeper.model.ProviderModel;

public abstract class AbstractProvider {

    public abstract void validate(ProviderModel model);

    public abstract void configure(ProviderModel model);
}
