package io.gatekeeper.api;

/**
 * This exception is thrown when a resource could not be accessed due to permissions.
 */
public class PermissionDeniedException extends HttpResponseException {

    public PermissionDeniedException() {
        super(403, "Permission denied");
    }
}
