package io.gatekeeper.node.service.replication.common;


import java.util.Map;

public class ReplicationInformation {

    public final String type;

    public final Integer nodes;

    public final Map<String, Object> extra;

    public ReplicationInformation(String type, Integer nodes, Map<String, Object> extra) {
        this.type = type;
        this.nodes = nodes;
        this.extra = extra;
    }
}
