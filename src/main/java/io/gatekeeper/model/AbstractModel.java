package io.gatekeeper.model;

import java.util.UUID;

abstract public class AbstractModel <ApiModel> {

    private UUID id = UUID.randomUUID();

    public UUID id() {
        return id;
    }

    /**
     * Convert this model to its swagger API representation.
     *
     * @return The equivalent model
     */
    public abstract ApiModel toApiModel();

    /**
     * Pull data for this model from its swagger API representation.
     *
     * @param model The swagger model
     */
    public abstract void fromApiModel(ApiModel model);
}
