package io.gatekeeper;

/**
 * A base class for all gatekeeper exceptions.
 */
public class GatekeeperException extends RuntimeException {

    public GatekeeperException(String message) {
        super(message);
    }

    public GatekeeperException(String message, Throwable cause) {
        super(message, cause);
    }
}
