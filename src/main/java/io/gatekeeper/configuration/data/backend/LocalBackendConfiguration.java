package io.gatekeeper.configuration.data.backend;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.data.BackendConfiguration;
import io.gatekeeper.node.service.backend.LocalBackendService;

public class LocalBackendConfiguration extends BackendConfiguration<LocalBackendConfiguration, LocalBackendService> {

    @Override
    public Class<LocalBackendService> serviceClass() {
        return LocalBackendService.class;
    }

    @Override
    public void validate() throws InvalidConfigurationException {
        // TODO
    }

    @Override
    public void merge(LocalBackendConfiguration configuration) {
        // TODO
    }
}
