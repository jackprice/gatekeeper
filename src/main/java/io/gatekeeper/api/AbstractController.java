package io.gatekeeper.api;

import com.fasterxml.jackson.core.JsonGenerator;
import io.gatekeeper.node.Node;
import io.gatekeeper.node.ServiceContainer;
import io.swagger.client.ApiClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        Object data;
        JSONObject jsonData;

        try {
            data = handle(context.request());

            context.response().putHeader("content-type", ContentType.APPLICATION_JSON.toString());

            context.response().end(modelToJSON(data));

            return;
        } catch (TimeoutException exception) {
            context.response().setStatusCode(502);
            context.response().putHeader("content-type", ContentType.APPLICATION_JSON.toString());

            jsonData = new JSONObject();

            jsonData.put("message", "Timed out waiting for response");

            context.response().end(jsonData.toString(4));

            return;
        } catch (Exception exception) {
            context.response().setStatusCode(500);
            context.response().putHeader("content-type", ContentType.APPLICATION_JSON.toString());

            jsonData = new JSONObject();

            jsonData.put("exception", exception.getClass().getCanonicalName());
            jsonData.put("message", exception.getMessage());

            context.response().end(jsonData.toString(4));

            return;
        }
    }

    protected abstract Object handle(HttpServerRequest request) throws Exception;

    protected String modelToJSON(Object model) throws IOException {
        ApiClient client = new ApiClient();

        OutputStream output = new ByteArrayOutputStream();
        JsonGenerator generator = client
            .getObjectMapper()
            .getFactory()
            .createGenerator(output);

        generator.useDefaultPrettyPrinter();

        generator.writeObject(model);

        generator.close();

        return output.toString();
    }
}
