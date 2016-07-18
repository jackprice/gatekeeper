package io.gatekeeper.node.service.backend.common;

import java.io.Serializable;

/**
 * A map that is managed and stored by the backend service.
 *
 * Reads and writes to this map MUST happen atomically, and data MUST NOT be stale when returned.
 */
public abstract class ReplicatedMap<V extends Serializable> {

    public abstract int size() throws Exception;

    public abstract boolean containsKey(String key) throws Exception;

    public abstract V get(String key) throws Exception;

    public abstract V put(String key, V value) throws Exception;
}
