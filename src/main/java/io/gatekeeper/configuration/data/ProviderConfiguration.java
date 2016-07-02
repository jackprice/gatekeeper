package io.gatekeeper.configuration.data;

import io.gatekeeper.configuration.ConfigurationInterface;
import io.gatekeeper.configuration.annotation.Discriminator;

@Discriminator(field = "type", map = {
    
})
public abstract class ProviderConfiguration<T extends ConfigurationInterface> implements ConfigurationInterface<T> {

}
