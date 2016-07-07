package io.gatekeeper.model;

import java.util.UUID;

abstract class AbstractModel {

    private UUID id = UUID.randomUUID();

    public UUID id() {
        return id;
    }
}
