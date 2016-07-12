package io.gatekeeper.node.service.replication.consul;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import io.gatekeeper.Version;
import io.gatekeeper.logging.Loggers;
import io.gatekeeper.node.service.replication.common.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@SuppressWarnings("HardcodedFileSeparator")
public class Client implements Closeable {

    protected final Logger logger;

    protected final String consulHost;

    protected final Integer consulPort;

    protected final String serviceName;

    protected final String serviceHost;

    protected final Integer servicePort;

    protected final String token;

    protected final Executor executor;

    public Client(
        String consulHost,
        Integer consulPort,
        String serviceName,
        String serviceHost,
        Integer servicePort,
        String token
    ) {
        assert null != consulHost;
        assert null != consulPort;
        assert null != serviceName;
        assert null != serviceHost;
        assert null != servicePort;

        this.logger = Loggers.getReplicationLogger();
        this.consulHost = consulHost;
        this.consulPort = consulPort;
        this.serviceName = serviceName;
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;
        this.token = token;

        this.executor = Executors.newSingleThreadExecutor(
            (new ThreadFactoryBuilder())
                .setNameFormat("Consul Interface")
                .build()
        );
    }

    public CompletableFuture<Lock> lock(String name) {
        assert null != name;
        assert name.length() > 0;

        CompletableFuture<Lock> future = new CompletableFuture<>();

        this.executor.execute(() -> {
            try {
                future.complete(doLock(name));
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    public CompletableFuture<Void> registerService() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        this.executor.execute(() -> {
            while (true) {
                try {
                    doRegisterService();
                    doRegisterCheck();

                    future.complete(null);

                    break;
                } catch (Exception exception) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interrupted) {
                        future.completeExceptionally(interrupted);
                    }
                }
            }
        });

        return future;
    }

    public CompletableFuture<List<Node>> getNodes() {
        CompletableFuture<List<Node>> future = new CompletableFuture<>();

        this.executor.execute(() -> {
            try {
                List<Node> nodes = doGetNodes();

                if (nodes.size() == 0) {
                    registerService().thenRunAsync(() -> {
                        try {
                            List<Node> nodes0 = doGetNodes();

                            if (nodes0.size() == 0) {
                                throw new Exception("Could not find nodes");
                            }

                            future.complete(doGetNodes());

                        } catch (Exception exception) {
                            future.completeExceptionally(exception);
                        }
                    }, executor);

                    return;
                }

                future.complete(doGetNodes());

            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    private List<Node> doGetNodes() throws Exception {
        HttpResponse<JsonNode> response = doConsulRequest(
            HttpMethod.GET,
            String.format("v1/catalog/service/%s?tag=%s", serviceName, Version.CURRENT.minimumCompatibibleVersion().toString()),
            null,
            JsonNode.class
        );

        if (response.getStatus() != 200) {
            throw new Exception();
        }

        JSONArray data = response.getBody().getArray();
        List<Node> nodes = new ArrayList<>(data.length());

        data.forEach((object) -> {
            JSONObject node = (JSONObject) object;

            nodes.add(new Node(
                node.getString("Node"),
                node.getString("ServiceAddress"),
                node.getInt("ServicePort")
            ));
        });

        return nodes;
    }

    private void doRegisterService() throws Exception {
        logger.info(String.format("Registering consul service %s on %s:%d", serviceName, serviceHost, servicePort));

        JSONObject data = new JSONObject(getServiceDefinition());

        HttpResponse<JsonNode> response = doConsulRequest(
            HttpMethod.PUT,
            "v1/agent/service/register",
            data,
            JsonNode.class
        );

        if (response.getStatus() != 200) {
            throw new Exception(); // TODO;
        }
    }

    private void doRegisterCheck() throws Exception {
        logger.info(String.format("Registering consul check %s:rpc on %s:%d", serviceName, serviceHost, servicePort));

        JSONObject data = new JSONObject();

        data.put("ID", serviceName + ":api");
        data.put("Name", "Gatekeeper API port check");
        data.put("ServiceID", serviceName);
        data.put("HTTP", String.format("http://%s:%d/api/version", serviceHost, servicePort));
        data.put("Interval", "10s");
        data.put("TTL", "15s");

        HttpResponse<JsonNode> response = doConsulRequest(
            HttpMethod.PUT,
            "v1/agent/check/register",
            data,
            JsonNode.class
        );

        if (response.getStatus() != 200) {
            throw new Exception(); // TODO;
        }
    }

    private Map<String, Object> getServiceDefinition() {
        Map<String, Object> service = new HashMap<>();
        Map<String, Object> check = new HashMap<>();
        List<String> tags = new ArrayList<>();

        check.put("HTTP", String.format("http://%s:%s/api/version", serviceHost, servicePort));

        tags.add(Version.CURRENT.minimumCompatibibleVersion().toString());

        service.put("ID", serviceName);
        service.put("Name", serviceName);
        service.put("Address", serviceHost);
        service.put("Port", servicePort);
        service.put("Check", check);
        service.put("Tags", tags);

        return service;
    }

    private Lock doLock(String name) throws Exception {
        assert null != name;
        assert name.length() > 0;

        String session = createSession();

        JSONObject data = new JSONObject();

        HttpResponse<String> response = doConsulRequest(
            HttpMethod.PUT,
            String.format("v1/kv/%s/%s.local?acquire=%s", serviceName, name, session),
            data,
            String.class
        );

        if (!response.getBody().equals("true")) {
            throw new Exception(); // TODO
        }

        return new Lock(name, this, session);
    }

    private String createSession() throws Exception {
        Map<String, Object> map = new HashMap<>();

        map.put("TTL", "30s");
        map.put("LockDelay", "0s");

        JSONObject data = new JSONObject(map);

        HttpResponse<JsonNode> response = doConsulRequest(HttpMethod.PUT, "v1/session/create", data, JsonNode.class);

        if (response.getStatus() != 200) {
            throw new Exception(); // TODO
        }

        return response.getBody().getObject().getString("ID");
    }

    private void releaseSession(String session) throws Exception {
        assert null != session;
        assert session.length() > 0;

        JSONObject data = new JSONObject();

        HttpResponse<JsonNode> response = doConsulRequest(
            HttpMethod.PUT,
            "v1/session/destroy/" + session,
            data,
            JsonNode.class
        );

        if (response.getStatus() != 200) {
            throw new Exception(); // TODO
        }
    }

    void unlock(Lock lock) throws Exception {
        assert null != lock;

        releaseSession(lock.session);
    }

    protected <T> HttpResponse<T> doConsulRequest(
        HttpMethod method,
        String path,
        JSONObject postData,
        Class<T> responseClass
    ) throws UnirestException {
        assert null != method;
        assert null != path;
        assert null != responseClass;

        HttpRequest request;

        if (postData == null) {
            request = new HttpRequest(method, makeConsulUrl(path));
        } else {
            request = new HttpRequestWithBody(method, makeConsulUrl(path)).body(postData).getHttpRequest();
        }

        if (token != null) {
            request.header("X-Consul-Token", token);
        }

        return HttpClientHelper.request(
            request,
            responseClass
        );
    }

    private String makeConsulUrl(String path) {
        assert null != path;

        return String.format("http://%s:%s/%s", consulHost, consulPort, path);
    }


    @Override
    public void close() throws IOException {

    }
}
