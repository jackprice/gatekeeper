package io.gatekeeper.configuration.data.backend;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.data.BackendConfiguration;
import io.gatekeeper.node.service.backend.ConsulBackendService;
import io.gatekeeper.node.service.backend.LocalBackendService;

public class ConsulBackendConfiguration extends BackendConfiguration<ConsulBackendConfiguration, ConsulBackendService> {

    @Config(name = "prefix", type = String.class)
    public String prefix = "gatekeeper";

    @Config(name = "host", type = String.class)
    public String host = "127.0.0.1";

    @Config(name = "port", type = Integer.class)
    public Integer port = 8500;

    @Config(name = "token", type = String.class)
    public String token;

    @Override
    public Class<ConsulBackendService> serviceClass() {
        return ConsulBackendService.class;
    }

    @Override
    public void validate() throws InvalidConfigurationException {
        // TODO
    }

    @Override
    public void merge(ConsulBackendConfiguration configuration) {
        prefix = configuration.prefix;
        host = configuration.host;
        port = configuration.port;
        token = configuration.token;
    }
}
