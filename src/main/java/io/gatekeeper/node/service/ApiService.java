package io.gatekeeper.node.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.gatekeeper.api.AbstractController;
import io.gatekeeper.api.controller.GetReplicationInfo;
import io.gatekeeper.api.controller.GetVersion;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.logging.Loggers;
import io.gatekeeper.node.ServiceContainer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

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
                    logger.info(String.format("Listening on http://%s:%d", configuration.api.address, configuration.api.port));

                    future.complete(null);
                } else {
                    logger.warning("Failed to start API server");

                    future.completeExceptionally(result.cause());
                }
            });
        });

        return future;
    }

    private void configureRouter() {
        router.route(HttpMethod.GET, "/api/version").handler((context) -> handle(context, GetVersion.class));
        router.route(HttpMethod.GET, "/api/replication/info").handler((context) -> handle(context, GetReplicationInfo.class));
    }

    private <T extends AbstractController, U extends Class<T>> void handle(RoutingContext context, U clazz) {
        T controller;

        context.response().putHeader("Access-Control-Allow-Origin", "*");

        try {
            controller = instantiateController(clazz);
        } catch (Exception exception) {
            return;
        }

        controller.handle(context);
    }

    private <Controller, ControllerClass extends Class<Controller>> Controller instantiateController(ControllerClass clazz)
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
}
