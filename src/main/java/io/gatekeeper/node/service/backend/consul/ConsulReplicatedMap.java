package io.gatekeeper.node.service.backend.consul;

import io.gatekeeper.node.service.backend.common.ReplicatedMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A map replicated to Consul.
 */
public class ConsulReplicatedMap<V extends Serializable> extends ReplicatedMap<V> {

    private final Function<HashMap<String, V>, CompletableFuture<Void>> setDataCallable;

    private final Callable<CompletableFuture<HashMap<String, V>>> getDataCallable;

    /**
     * Public constructor.
     *
     * @param getDataCallable A callable provided by the replication service that, when invoked, will return up-to-date
     *                        data for this map.
     */
    public ConsulReplicatedMap(
        Callable<CompletableFuture<HashMap<String, V>>> getDataCallable,
        Function<HashMap<String, V>, CompletableFuture<Void>> setDataCallable
    ) {
        assert null != getDataCallable;
        assert null != setDataCallable;

        this.getDataCallable = getDataCallable;
        this.setDataCallable = setDataCallable;
    }

    private HashMap<String, V> getData() throws Exception {
        return this.getDataCallable.call().get();
    }

    @Override
    public int size() throws Exception {
        return getData().size();
    }

    @Override
    public boolean containsKey(String key) throws Exception {
        return getData().containsKey(key);
    }

    @Override
    public V get(String key) throws Exception {
        return getData().get(key);
    }

    @Override
    public V put(String key, V value) throws Exception {
        HashMap<String, V> data = getData();

        data.put(key, value);

        setDataCallable.apply(data).get();

        return value;
    }
}
