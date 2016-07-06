package io.gatekeeper.configuration.data.replication;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.ConfigurationInterface;
import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.data.ReplicationConfiguration;
import io.gatekeeper.node.service.replication.ConsulReplicationService;

import java.util.Collections;
import java.util.List;

public class ConsulReplicationConfiguration
    extends ReplicationConfiguration<ConsulReplicationConfiguration, ConsulReplicationService> {

    @Config(name = "service", type = String.class)
    public String service = "gatekeeper";

    @Config(name = "host", type = String.class)
    public String host = "127.0.0.1";

    @Config(name = "port", type = Integer.class)
    public Integer port = 8500;

    @Config(name = "ttl", type = Integer.class)
    public Integer ttl = 15;

    @Config(name = "token", type = String.class)
    public String token;

    @Override
    public void validate() throws InvalidConfigurationException {
        if (this.service == null || this.service.length() == 0) {
            throw new InvalidConfigurationException("Invalid consul service name");
        }
    }

    @Override
    public void merge(ConsulReplicationConfiguration configuration) {
        super.merge((ReplicationConfiguration) configuration);

        this.service = configuration.service;
        this.port = configuration.port;
        this.host = configuration.host;
        this.ttl = configuration.ttl;
        this.token = configuration.token;
    }

    @Override
    public Class<ConsulReplicationService> serviceClass() {
        return ConsulReplicationService.class;
    }
}
