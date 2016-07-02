package io.gatekeeper.configuration.data;

import io.gatekeeper.configuration.ConfigurationInterface;
import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.annotation.Discriminator;
import io.gatekeeper.configuration.annotation.DiscriminatorMapping;
import io.gatekeeper.configuration.data.replication.ConsulReplicationConfiguration;
import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;

@Discriminator(field = "type", map = {

})
public abstract class BackendConfiguration<T extends ConfigurationInterface> implements ConfigurationInterface<T> {

}
