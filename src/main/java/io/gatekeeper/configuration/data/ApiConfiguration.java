package io.gatekeeper.configuration.data;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.ConfigurationInterface;
import io.gatekeeper.configuration.annotation.Config;

public class ApiConfiguration implements ConfigurationInterface<ApiConfiguration> {

    @Config(name = "address", type = String.class)
    public String address = "127.0.0.1";

    @Config(name = "port", type = Integer.class)
    public Integer port = 8123;

    @Override
    public void validate() throws InvalidConfigurationException {

    }

    @Override
    public void merge(ApiConfiguration configuration) {
        this.address = configuration.address;
        this.port = configuration.port;
    }
}
