package io.gatekeeper.configuration;

import io.gatekeeper.InvalidConfigurationException;

public interface ConfigurationInterface<T extends ConfigurationInterface> {

    public void validate() throws InvalidConfigurationException;

    public void merge(T configuration);
}
