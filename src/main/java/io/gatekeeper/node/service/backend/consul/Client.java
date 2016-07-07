package io.gatekeeper.node.service.backend.consul;

import io.gatekeeper.configuration.data.backend.ConsulBackendConfiguration;

public class Client {

    public static Client build(ConsulBackendConfiguration configuration) {
        return build(configuration.host, configuration.port, configuration.prefix);
    }

    public static Client build(String host, Integer port, String prefix) {
        return new Client(host, port, prefix);
    }

    Client(String host, Integer port, String prefix) {
        assert null != host;
        assert null != port;
        assert null != prefix;
    }
}
