package io.gatekeeper.node.service.backend.consul;

import io.gatekeeper.GatekeeperException;

/**
 * A generic exception for consul-related errors.
 */
public class ConsulException extends GatekeeperException {

    public ConsulException(String message) {
        super(message);
    }

    public ConsulException(String message, Throwable cause) {
        super(message, cause);
    }
}
