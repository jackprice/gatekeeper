package io.gatekeeper.configuration.data.output;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.data.OutputConfiguration;
import io.gatekeeper.node.service.output.DirectoryOutputService;

public class DirectoryOutput extends OutputConfiguration<DirectoryOutput, DirectoryOutputService> {

    @Config(name = "path", type = String.class)
    public String path;

    @Config(name = "concatenate", type = Boolean.class)
    public Boolean concatenate = false;

    @Override
    public Class<DirectoryOutputService> getServiceClass() {
        return DirectoryOutputService.class;
    }

    @Override
    public void validate() throws InvalidConfigurationException {
        // TODO
    }

    @Override
    public void merge(DirectoryOutput configuration) {
        // TODO
    }
}
