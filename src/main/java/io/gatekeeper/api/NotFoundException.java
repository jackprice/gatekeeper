package io.gatekeeper.api;

/**
 * This exception is thrown when a resource could not be located.
 */
public class NotFoundException extends HttpResponseException {

    public NotFoundException() {
        super(404, "Not found");
    }
}
