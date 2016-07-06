package io.gatekeeper.api;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

public abstract class AbstractController {

    public void handle(RoutingContext context) {
        JSONObject data = handle(context.request());

        context.response().putHeader("content-type", ContentType.APPLICATION_JSON.toString());

        context.response().end(data.toString(4));
    }

    protected abstract JSONObject handle(HttpServerRequest request);
}
