package io.gatekeeper.api;

import com.fasterxml.jackson.core.JsonParser;
import io.gatekeeper.node.ServiceContainer;
import io.gatekeeper.node.service.Service;
import io.swagger.client.ApiClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.TimeUnit;

/**
 * A controller takes a request, handles it and returns a response.
 *
 * All controllers are thread safe, and new ones are instantiated for each request, so any side effects from a request
 * will be discarded.
 */
public abstract class AbstractController {

    /**
     * A default timeout value for controllers making requests to services.
     */
    protected final static long timeout = 5000;

    /**
     * The unit for default {@link #timeout timeout values}
     */
    protected final static TimeUnit timeoutUnit = TimeUnit.MILLISECONDS;

    /**
     * The service container.
     *
     * Can be used to retrieve any configured services.
     */
    private final ServiceContainer container;

    /**
     * The currently-being handled routing context.
     */
    private RoutingContext context;

    /**
     * The request that is currently being handled.
     */
    protected HttpServerRequest request;

    /**
     * Internal constructor.
     *
     * @param container The current service container
     * @param context   The routing context that invoked this controller
     */
    public AbstractController(ServiceContainer container, RoutingContext context) {
        assert null != container;
        assert null != context;

        this.container = container;
        this.context = context;
        this.request = context.request();
    }

    /**
     * Retrieve a service from the service container.
     *
     * @param clazz The service interface to retrieve
     *
     * @return The concrete service from the container
     */
    protected <T extends Service, U extends Class<T>> T get(U clazz) {
        T service = this.container.service(clazz);

        assert null != service;

        return service;
    }

    /**
     * Retrieve the HTTP body content as a JSON object.
     *
     * @return The content of the HTTP body
     */
    protected JsonObject body() {
        return context.getBodyAsJson();
    }

    /**
     * Read the HTTP body content as a JSON object ande decode it to the given API model.
     *
     * @param clazz The class of the API model
     */
    protected <T> T readBodyAs(Class<T> clazz) throws HttpResponseException {
        try {
            String json = context.getBodyAsString();
            ApiClient client = new ApiClient();
            JsonParser parser = client.getObjectMapper()
                .getFactory()
                .createParser(json);

            return parser.readValueAs(clazz);
        } catch (Exception exception) {
            throw new HttpResponseException(422, "Malformed entity");
        }
    }

    /**
     * Retrieve a parameter from the query string.
     *
     * @param name The name of the querystring
     *
     * @return The value of the query, or null if it is not defined
     */
    protected String query(String name) {
        return context.request().getParam(name);
    }

    /**
     * Set the status code of the response.
     *
     * @param status The status code to set (defaults to 200 if not specified)
     */
    protected void status(Integer status) {
        context.response().setStatusCode(status);
    }

    /**
     * Called to invoke this controller and handle the configured request.
     *
     * @return A response object, list or null
     */
    public abstract Object invoke() throws Exception;
}
