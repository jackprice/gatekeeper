package io.gatekeeper.node.service.provider.acme;

import java.net.URL;

/**
 * This object is a wrapper around all the configuration values an ACME provider can have.
 */
public class Configuration {

    /**
     * The absolute URL to the ACME server directory path.
     */
    final public URL directory;

    public Configuration(URL directory) {
        this.directory = directory;
    }
}
