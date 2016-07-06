package io.gatekeeper.configuration.data;

import io.gatekeeper.configuration.ConfigurationInterface;
import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.annotation.Discriminator;
import io.gatekeeper.configuration.annotation.DiscriminatorMapping;
import io.gatekeeper.configuration.data.backend.ConsulBackendConfiguration;
import io.gatekeeper.configuration.data.backend.LocalBackendConfiguration;
import io.gatekeeper.configuration.data.replication.ConsulReplicationConfiguration;
import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;
import io.gatekeeper.node.service.BackendService;

@Discriminator(field = "type", map = {
    @DiscriminatorMapping(name = "local", mappedTo = LocalBackendConfiguration.class),
    @DiscriminatorMapping(name = "consul", mappedTo = ConsulBackendConfiguration.class)
})
public abstract class BackendConfiguration<T extends ConfigurationInterface, U extends BackendService>
    implements ConfigurationInterface<T> {

    @Config(name = "key", type = String.class)
    public String key;

    public abstract Class<U> serviceClass();
}
