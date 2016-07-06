package io.gatekeeper.node.service.replication.common;

import org.json.JSONObject;

public class ReplicationInformation extends JSONObject {

    public ReplicationInformation(String type, Integer nodes) {
        put("type", type);
        put("nodes", nodes);
    }
}
