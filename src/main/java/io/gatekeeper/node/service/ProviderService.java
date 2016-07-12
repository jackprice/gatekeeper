package io.gatekeeper.node.service;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.api.model.Provider;
import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.provider.AbstractProvider;
import io.gatekeeper.provider.SelfSignedProvider;

import java.lang.reflect.Constructor;
import java.util.concurrent.CompletableFuture;

public class ProviderService implements Service {

    @Override
    public CompletableFuture start() {
        return CompletableFuture.completedFuture(null);
    }

    public void validateProviderModel(ProviderModel model) throws InvalidConfigurationException {
        createProviderFromModel(model);
    }

    private Class<? extends AbstractProvider> getClassFromType(Provider.TypeEnum type) throws Exception {
        switch (type) {
            case SIGNED:
                return SelfSignedProvider.class;

            default:
                throw new Exception("Invalid provider type");
        }
    }

    public AbstractProvider createProviderFromModel(ProviderModel model) throws InvalidConfigurationException {
        try {
            Class<? extends AbstractProvider> clazz = getClassFromType(model.getType());

            Constructor<? extends AbstractProvider> constructor = clazz.getConstructor();

            try {
                AbstractProvider provider = constructor.newInstance();

                provider.configure(model);

                return provider;
            } catch (Exception exception) {
                throw new InvalidConfigurationException("Could not create provider", exception);
            }
        } catch (Exception exception) {
            throw new InvalidConfigurationException("Could not create provider", exception);
        }
    }

    @Override
    public void close() throws Exception {
        // NOP
    }
}
