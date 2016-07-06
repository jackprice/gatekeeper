package io.gatekeeper.node;

import io.gatekeeper.node.service.Service;

import java.util.HashMap;

final public class ServiceContainer extends HashMap<String, Service> {

    /**
     * Define a service in the service container.
     */
    public <T extends Service, U extends Class<T>> void service(U clazz, T implementation) {
        assert null != clazz;
        assert Service.class.isAssignableFrom(clazz);
        assert null != implementation;
        assert clazz.isAssignableFrom(implementation.getClass());

        put(clazz.getCanonicalName(), implementation);
    }

    /**
     * Get a service from the service container by its abstract class.
     *
     * @param clazz The abstract class of the service to retrieve
     */
    @SuppressWarnings("unchecked")
    public <T extends Service, U extends Class<T>> T service(U clazz) {
        assert null != clazz;
        assert Service.class.isAssignableFrom(clazz);

        return (T) get(clazz.getCanonicalName());
    }
}
