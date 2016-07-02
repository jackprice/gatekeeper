package io.gatekeeper.node.service.backend;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.node.service.BackendService;

public class LocalBackendService extends BackendService {

    public LocalBackendService(Configuration configuration) {
        super(configuration);
    }
}
