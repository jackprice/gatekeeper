package io.gatekeeper.model;

import io.gatekeeper.api.model.Provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
}
