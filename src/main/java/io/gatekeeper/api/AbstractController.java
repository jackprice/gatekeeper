package io.gatekeeper.api;

import io.gatekeeper.node.Node;
import io.gatekeeper.node.ServiceContainer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

public abstract class AbstractController {

    protected final ServiceContainer container;

    public AbstractController(ServiceContainer container) {
        assert null != container;

        this.container = container;
    }

    public void handle(RoutingContext context) {
        JSONObject data = handle(context.request());

        context.response().putHeader("content-type", ContentType.APPLICATION_JSON.toString());

        context.response().end(data.toString(4));
    }

    protected abstract JSONObject handle(HttpServerRequest request);
}
