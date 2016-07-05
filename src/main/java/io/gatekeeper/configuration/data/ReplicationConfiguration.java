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

    @Config(name = "rpc_address", type = String.class)
    public String rpcAddress = "127.0.0.1";

    @Config(name = "rpc_port", type = Integer.class)
    public Integer rpcPort = 8123;

    public void merge(ReplicationConfiguration configuration) {
        this.server = configuration.server;
        this.rpcAddress = configuration.rpcAddress;
        this.rpcPort = configuration.rpcPort;
    }

    public abstract Class<U> serviceClass();
}
