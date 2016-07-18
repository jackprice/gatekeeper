package io.gatekeeper.model;

import io.gatekeeper.InvalidConfigurationException;

import java.io.Serializable;
import java.util.UUID;

abstract public class AbstractModel implements Serializable {

    /**
     * The unique UUID of this model.
     */
    protected UUID uuid = UUID.randomUUID();

    /**
     * @return The unique UUID of this model
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Set the UUID of this model
     *
     * @param uuid
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Validate the data in this model
     *
     * @throws InvalidConfigurationException If the configuration is invalid
     */
    public void validate() throws InvalidConfigurationException {

    }
}
