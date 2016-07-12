package io.gatekeeper.node.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.gatekeeper.GatekeeperException;
import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.HttpResponseException;
import io.gatekeeper.api.NotFoundException;
import io.gatekeeper.api.controller.*;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.logging.Loggers;
import io.gatekeeper.model.*;
import io.gatekeeper.node.ServiceContainer;
import io.swagger.client.ApiClient;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("HardcodedFileSeparator")
public class ApiService implements Service {

    private final Logger logger;

    private final Configuration configuration;

    private final ThreadPoolExecutor executor;

    private final Vertx vertx;

    private final HttpServer server;

    private final Router router;

    private final ServiceContainer container;

    public ApiService(Configuration configuration, ServiceContainer container) {
        this.container = container;
        this.logger = Loggers.getApiLogger();
        this.configuration = configuration;
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(
            (new ThreadFactoryBuilder())
                .setNameFormat("API Service %d")
                .build()
        );

        this.executor.prestartCoreThread();
        this.executor.prestartAllCoreThreads();

        this.vertx = Vertx.vertx();
        this.server = this.vertx.createHttpServer();
        this.router = Router.router(this.vertx);
    }

    @Override
    public CompletableFuture start() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        executor.execute(() -> {
            configureRouter();

            server.requestHandler(router::accept);

            server.listen(configuration.api.port, configuration.api.address, (result) -> {
                if (result.succeeded()) {
                    logger.info(String.format(
                        "Listening on http://%s:%d",
                        configuration.api.address,
                        configuration.api.port
                    ));

                    future.complete(null);
                } else {
                    logger.warning("Failed to start API server");

                    future.completeExceptionally(result.cause());
                }
            });
        });

        return future;
    }

    /**
     * Configure the vertx router to dispatch to the appropriate controllers.
     *
     * {@link #getRoutes}
     */
    private void configureRouter() {
        Map<PathDefinition, Class> routes = getRoutes();

        router.route().handler(BodyHandler.create().setBodyLimit(10240));

        for (Map.Entry<PathDefinition, Class> entry : routes.entrySet()) {
            PathDefinition path = entry.getKey();
            Class controller = entry.getValue();

            router
                .route(path.method, path.path)
                .handler((context) -> spawnHandler(context, controller))
            ;
        }
    }

    /**
     * Called to build a map of route definitions to controller implementations.
     *
     * TODO: Allow this to be built dynamically or from plugins
     *
     * @return A mapping of PathDefinition => ControllerClass
     */
    private Map<PathDefinition, Class> getRoutes() {
        Map<PathDefinition, Class> routes = new HashMap<>();

        routes.put(new PathDefinition("/api/version", HttpMethod.GET), GetVersion.class);
        routes.put(new PathDefinition("/api/replication/info", HttpMethod.GET), GetReplicationInfo.class);
        routes.put(new PathDefinition("/api/endpoint/:id/certificate", HttpMethod.GET), GetCertificate.class);
        routes.put(new PathDefinition("/api/endpoint/:id/certificate", HttpMethod.POST), ReissueCertificate.class);
        routes.put(new PathDefinition("/api/endpoint/:id", HttpMethod.PATCH), PatchEndpoint.class);
        routes.put(new PathDefinition("/api/endpoint/:id", HttpMethod.GET), GetEndpoint.class);
        routes.put(new PathDefinition("/api/endpoint", HttpMethod.GET), GetEndpoints.class);
        routes.put(new PathDefinition("/api/endpoint", HttpMethod.POST), CreateEndpoint.class);
        routes.put(new PathDefinition("/api/provider/:id", HttpMethod.GET), GetProvider.class);
        routes.put(new PathDefinition("/api/provider", HttpMethod.GET), GetProviders.class);
        routes.put(new PathDefinition("/api/provider", HttpMethod.POST), CreateProvider.class);

        return routes;
    }

    /**
     * This function simply adds a runnable to the executor pool to dispatch the routed context.
     *
     * {@link #handle(RoutingContext, Class)}
     */
    private <Controller extends AbstractController, ControllerClass extends Class<Controller>> void spawnHandler(
        RoutingContext context,
        ControllerClass clazz
    ) {
        executor.execute(() -> handle(context, clazz));
    }

    /**
     * Handle the given routed context and dispatch it into the given controller.
     */
    private <Controller extends AbstractController, ControllerClass extends Class<Controller>> void handle(
        RoutingContext context,
        ControllerClass clazz
    ) {
        Controller controller;

        // Configure CORS properly
        // TODO: Allow this to come from a configuration value?
        context.response().putHeader("Access-Control-Allow-Origin", "*");

        try {
            controller = instantiateController(clazz, context);

            Object result = dispatchToController(context, controller);

            sendResponseFromObject(context, result);
        } catch (Exception exception) {
            sendResponseFromException(context, exception);
        }
    }

    /**
     * Parse the request in the context given and dispatch it into the controller.
     *
     * @param context    The routed context
     * @param controller The controller to dispatch to
     */
    private <Controller extends AbstractController> Object dispatchToController(
        RoutingContext context,
        Controller controller
    ) throws Exception {
        return controller.invoke();
    }

    /**
     * Parse the given object into a response suitable for sending, and send it to the client.
     *
     * @param context The routed context
     * @param object  A response from the mapped controller
     */
    @SuppressWarnings("unchecked") // It's fine - I promise!
    private void sendResponseFromObject(RoutingContext context, Object object) throws HttpResponseException {
        if (object == null) {
            throw new NotFoundException();
        }

        // Convert lists of objects if possible
        if (List.class.isAssignableFrom(object.getClass())) {
            object = ((List) object).stream()
                .map(this::convertObjectToApiObject)
                .collect(Collectors.toList());
        }

        object = convertObjectToApiObject(object);

        OutputStream output = new ByteArrayOutputStream();
        ApiClient client = new ApiClient();
        JsonGenerator generator;

        try {
            generator = client
                .getObjectMapper()
                .getFactory()
                .createGenerator(output);

            generator.useDefaultPrettyPrinter();
            generator.writeObject(object);
            generator.close();
        } catch (IOException exception) {
            sendResponseFromException(context, exception);

            return;
        }

        context.response().putHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        context.response().end(output.toString());
    }

    /**
     * Convert the given object into an API object, if a *direct* conversion exists.
     *
     * @param object The object to convert
     *
     * @return The converted object, or the original object if no conversion could be made.
     */
    private Object convertObjectToApiObject(Object object) {
        assert null != object;

        if (EndpointModel.class.isAssignableFrom(object.getClass())) {
            return new EndpointModelBuilder().toApiModel((EndpointModel) object);
        }
        if (ProviderModel.class.isAssignableFrom(object.getClass())) {
            return new ProviderModelBuilder().toApiModel((ProviderModel) object);
        }
        if (CertificateModel.class.isAssignableFrom(object.getClass())) {
            return new CertificateModelBuilder().toApiModel((CertificateModel) object);
        }

        return object;
    }

    /**
     * Create a new controller of the given class and for the given routing context.
     *
     * @param clazz   The controller class
     * @param context The current routing context
     *
     * @return An instantiated controller, ready to handle requests
     */
    private <Controller, ControllerClass extends Class<Controller>> Controller instantiateController(
        ControllerClass clazz,
        RoutingContext context
    ) {
        try {
            Constructor<Controller> constructor = clazz.getConstructor(ServiceContainer.class, RoutingContext.class);

            return constructor.newInstance(container, context);
        } catch (Exception exception) {
            throw new GatekeeperException(
                String.format("Could not create controller %s", clazz.getCanonicalName())
            );
        }
    }

    /**
     * Convert the given exception into a sensible error message.
     *
     * @param context   The routing context
     * @param exception The exception to convert into a response
     */
    private void sendResponseFromException(RoutingContext context, Exception exception) {
        Integer code = 500;
        String message = "Internal server error";
        String detail = null;

        if (exception instanceof TimeoutException) {
            code = 504;
            message = "Gateway timeout";
        } else if (HttpResponseException.class.isAssignableFrom(exception.getClass())) {
            code = ((HttpResponseException) exception).getCode();
            message = ((HttpResponseException) exception).getMessage();
            detail = ((HttpResponseException) exception).getDetail();
        }

        context.response().putHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        context.response().setStatusCode(code);
        context.response().setStatusMessage(message);

        JSONObject response = new JSONObject();

        response.put("code", code);
        response.put("message", message);
        response.put("_exception_class", exception.getClass().getCanonicalName());
        response.put("_exception_message", exception.getMessage());
        response.put("_exception_trace", exception.getStackTrace());

        if (detail != null) {
            response.put("detail", detail);
        }

        context.response().end(response.toString(4));
    }

    private <Controller, ControllerClass extends Class<Controller>> Controller instantiateController(
        ControllerClass
            clazz
    )
        throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        assert null != clazz;

        Constructor<Controller> constructor = clazz.getConstructor(ServiceContainer.class);

        return constructor.newInstance(container);
    }

    @Override
    public void close() throws IOException {
        logger.info("Stopping API service");

        server.close();
        vertx.close();
    }

    /**
     * A path definition collects together a URI and an HTTP method into one class.
     */
    private class PathDefinition {

        String path;

        HttpMethod method;

        PathDefinition(String path, HttpMethod method) {
            this.path = path;
            this.method = method;
        }
    }
}
