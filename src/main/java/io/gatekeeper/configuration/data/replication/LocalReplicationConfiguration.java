package io.gatekeeper.configuration.data.replication;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.data.ReplicationConfiguration;
import io.gatekeeper.node.service.replication.LocalReplicationService;

public class LocalReplicationConfiguration
    extends ReplicationConfiguration<LocalReplicationConfiguration, LocalReplicationService> {

    @Override
    public void validate() throws InvalidConfigurationException {
        // TODO
    }

    @Override
    public void merge(LocalReplicationConfiguration configuration) {
        super.merge((ReplicationConfiguration) configuration);
    }

    @Override
    public Class<LocalReplicationService> serviceClass() {
        return LocalReplicationService.class;
    }
}
