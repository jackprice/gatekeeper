package io.gatekeeper.configuration.data;

import io.gatekeeper.configuration.ConfigurationInterface;
import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.annotation.Discriminator;
import io.gatekeeper.configuration.annotation.DiscriminatorMapping;
import io.gatekeeper.configuration.data.output.DirectoryOutput;
import io.gatekeeper.node.service.output.AbstractOutputService;

@Discriminator(
    field = "type",
    map = {
        @DiscriminatorMapping(name = "directory", mappedTo = DirectoryOutput.class)
    }
)
public abstract class OutputConfiguration<T extends ConfigurationInterface, U extends AbstractOutputService> implements
    ConfigurationInterface<T> {

    @Config(name = "after_update", type = String.class)
    public String afterUpdate;

    @Config(name = "tags", type = String.class)
    public String tags;

    @Config(name = "domains", type = String.class)
    public String domains;

    /**
     * @return The service class that implements this output
     */
    public abstract Class<U> getServiceClass();
}
