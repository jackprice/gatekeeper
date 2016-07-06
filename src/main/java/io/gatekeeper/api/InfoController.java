package io.gatekeeper.api;

import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.replication.common.ReplicationInformation;
import io.vertx.core.http.HttpServerRequest;
import org.json.JSONObject;

public class InfoController extends AbstractController {

    public InfoController(ServiceContainer container) {
        super(container);
    }

    @Override
    protected JSONObject handle(HttpServerRequest request) throws Exception {
        return (ReplicationInformation) container
            .service(ReplicationService.class)
            .getInformation()
            .get(timeout, timeoutUnit);
    }
}
