package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.model.ReplicationStatus;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.replication.common.ReplicationInformation;
import io.vertx.core.http.HttpServerRequest;

public class GetReplicationInfo extends AbstractController {

    public GetReplicationInfo(ServiceContainer container) {
        super(container);
    }

    @Override
    protected Object handle(HttpServerRequest request) throws Exception {
        ReplicationStatus replicationStatus = new ReplicationStatus();

        ReplicationInformation information = (ReplicationInformation) container
            .service(ReplicationService.class)
            .getInformation()
            .get(timeout, timeoutUnit);

        replicationStatus
            .type(information.type)
            .nodes(information.nodes)
            .extra(information.extra);

        return replicationStatus;
    }
}
