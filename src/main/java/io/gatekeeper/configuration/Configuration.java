package io.gatekeeper.configuration;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.data.BackendConfiguration;
import io.gatekeeper.configuration.data.ProviderConfiguration;
import io.gatekeeper.configuration.data.ReplicationConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Configuration implements ConfigurationInterface<Configuration> {

    @Config(name = "replication", type = ReplicationConfiguration.class)
    public ReplicationConfiguration replication;

    @Config(name = "backend", type = BackendConfiguration.class)
    public BackendConfiguration backend;

    @Config(name = "providers", type = ProviderConfiguration.class, collection = true)
    public List<ProviderConfiguration> providers = new ArrayList<>();

    @Override
    public void validate() throws InvalidConfigurationException {
        if (this.replication == null) {
            throw new InvalidConfigurationException("No replication configured");
        }
        if (this.backend == null) {
            throw new InvalidConfigurationException("No backend configured");
        }
        if (this.providers == null || this.providers.size() == 0) {
            throw new InvalidConfigurationException("No providers configured");
        }

        this.replication.validate();
        this.backend.validate();
        this.providers.forEach(ConfigurationInterface::validate);
    }

    @Override
    public void merge(Configuration configuration) {
        if (this.replication == null) {
            this.replication = configuration.replication;
        }
        if (configuration.replication != null) {
            if (!this.replication.getClass().equals(configuration.replication.getClass())) {
                throw new InvalidConfigurationException(
                    "Attempted to merge two different replication providers - only one can be configured"
                );
            }

            this.replication.merge(configuration.replication);
        }

        if (this.backend == null) {
            this.backend = configuration.backend;
        }
        if (configuration.backend != null) {
            if (!this.backend.getClass().equals(configuration.backend.getClass())) {
                throw new InvalidConfigurationException(
                    "Attempted to merge two different backend providers - only one can be configured"
                );
            }

            this.backend.merge(configuration.backend);
        }


        this.providers.addAll(configuration.providers);
    }
}
