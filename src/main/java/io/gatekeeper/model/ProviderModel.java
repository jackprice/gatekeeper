package io.gatekeeper.model;

import io.gatekeeper.api.model.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * A provider is responsible for generating and managing the certificates for endpoints.
 * Each endpoint has a single configured provider.
 */
public class ProviderModel extends AbstractModel {

    /**
     * A unique alphanumeric ID for this provider.
     */
    protected String id;

    /**
     * The type of provider this is - "self-signed" etc.
     */
    protected Provider.TypeEnum type;

    /**
     * Provider-specific configuration for this provider.
     */
    protected Object configuration;

    /**
     * Internally stored data that is persisted by this provider.
     */
    protected Map<String, Object> data = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Provider.TypeEnum getType() {
        return type;
    }

    public void setType(Provider.TypeEnum type) {
        this.type = type;
    }

    public Object getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Object configuration) {
        this.configuration = configuration;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
