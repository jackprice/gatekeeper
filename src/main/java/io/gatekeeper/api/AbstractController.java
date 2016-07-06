package io.gatekeeper.api;

import io.gatekeeper.node.Node;
import io.gatekeeper.node.ServiceContainer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractController {

    protected final ServiceContainer container;

    protected final long timeout = 1000;

    protected final TimeUnit timeoutUnit = TimeUnit.MILLISECONDS;

    public AbstractController(ServiceContainer container) {
        assert null != container;

        this.container = container;
    }

    public void handle(RoutingContext context) {
        JSONObject data;

        try {
            data = handle(context.request());
        } catch (TimeoutException exception) {
            context.response().setStatusCode(502);
            context.response().putHeader("content-type", ContentType.APPLICATION_JSON.toString());

            data = new JSONObject();

            data.put("message", "Timed out waiting for response");

            context.response().end(data.toString(4));

            return;
        } catch (Exception exception) {
            context.response().setStatusCode(500);
            context.response().putHeader("content-type", ContentType.APPLICATION_JSON.toString());

            data = new JSONObject();

            data.put("exception", exception.getClass().getCanonicalName());
            data.put("message", exception.getMessage());

            context.response().end(data.toString(4));

            return;
        }

        context.response().putHeader("content-type", ContentType.APPLICATION_JSON.toString());

        context.response().end(data.toString(4));
    }

    protected abstract JSONObject handle(HttpServerRequest request) throws Exception;
}
