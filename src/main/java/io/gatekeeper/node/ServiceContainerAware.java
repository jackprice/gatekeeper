package io.gatekeeper.node;

import io.gatekeeper.node.service.Service;

/**
 * Services that extend this abstract will have the service container injected after runtime.
 */
public abstract class ServiceContainerAware {

    private ServiceContainer container;

    protected void setContainer(ServiceContainer container) {
        this.container = container;
    }

    /**
     * Retrieve a service from the container.
     *
     * @param clazz The abstract service class to retrieve
     *
     * @return The implemented service
     */
    public <T extends Service, U extends Class<T>> T service(U clazz) {
        assert null != clazz;
        assert Service.class.isAssignableFrom(clazz);

        return container.service(clazz);
    }

}
