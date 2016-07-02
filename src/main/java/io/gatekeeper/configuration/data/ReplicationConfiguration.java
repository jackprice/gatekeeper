package io.gatekeeper.configuration.data;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.ConfigurationInterface;
import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.annotation.Discriminator;
import io.gatekeeper.configuration.annotation.DiscriminatorMapping;
import io.gatekeeper.configuration.data.replication.ConsulReplicationConfiguration;
import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;
import io.gatekeeper.node.service.ReplicationService;

@Discriminator(field = "type", map = {
    @DiscriminatorMapping(name = "local", mappedTo = LocalReplicationConfiguration.class),
    @DiscriminatorMapping(name = "consul", mappedTo = ConsulReplicationConfiguration.class)
})
public abstract class ReplicationConfiguration<T extends ConfigurationInterface, U extends ReplicationService>
    implements ConfigurationInterface<T> {

    @Config(name = "server", type = Boolean.class)
    public Boolean server = false;

    @Config(name = "bootstrap", type = Boolean.class)
    public Boolean bootstrap = false;

    @Config(name = "address", type = String.class)
    public String address = "127.0.0.1";

    @Config(name = "port", type = Integer.class)
    public Integer port = 8123;

    public void merge(ReplicationConfiguration configuration) {
        this.server = configuration.server;
        this.bootstrap = configuration.bootstrap;
        this.address = configuration.address;
        this.port = configuration.port;
    }

    public abstract Class<U> serviceClass();
}
