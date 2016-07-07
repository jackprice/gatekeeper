package io.gatekeeper.api.controller;

import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.model.ReplicationStatus;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.replication.common.ReplicationInformation;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class GetReplicationInfo extends AbstractController {

    /**
     * {@inheritDoc}
     */
    public GetReplicationInfo(ServiceContainer container, RoutingContext context) {
        super(container, context);
    }

    @Override
    public Object invoke() throws Exception {
        ReplicationStatus replicationStatus = new ReplicationStatus();

        ReplicationInformation information = (ReplicationInformation) get(ReplicationService.class)
            .getInformation()
            .get(timeout, timeoutUnit);

        replicationStatus
            .type(information.type)
            .nodes(information.nodes)
            .extra(information.extra);

        return replicationStatus;
    }
}
