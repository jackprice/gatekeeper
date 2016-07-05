package io.gatekeeper.node.service.replication.consul;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
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

@SuppressWarnings("HardcodedFileSeparator")
final public class Client implements Closeable {

    private final String consulHost;

    private final Integer consulPort;

    private final String serviceName;

    private final String serviceHost;

    private final Integer servicePort;

    private final Executor executor;

    public Client(
        String consulHost,
        Integer consulPort,
        String serviceName,
        String serviceHost,
        Integer servicePort
    ) {
        this.consulHost = consulHost;
        this.consulPort = consulPort;
        this.serviceName = serviceName;
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;

        this.executor = Executors.newSingleThreadExecutor(
            (new ThreadFactoryBuilder())
                .setNameFormat("Consul Interface")
                .build()
        );
    }

    public CompletableFuture<Lock> lock(String name) {
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
            "v1/catalog/service/" + serviceName,
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
        JSONObject data = new JSONObject();

        data.put("ID", serviceName + ":rpc");
        data.put("Name", "Gatekeeper RPC port check");
        data.put("ServiceID", serviceName);
        data.put("TCP", String.format("%s:%s", serviceHost, servicePort));
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

        check.put("TCP", String.format("%s:%s", serviceHost, servicePort));

        service.put("ID", serviceName);
        service.put("Name", serviceName);
        service.put("Address", serviceHost);
        service.put("Port", servicePort);
        service.put("Check", check);

        return service;
    }

    private Lock doLock(String name) throws Exception {
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

        JSONObject data = new JSONObject(map);

        HttpResponse<JsonNode> response = doConsulRequest(HttpMethod.PUT, "v1/session/create", data, JsonNode.class);

        if (response.getStatus() != 200) {
            throw new Exception(); // TODO
        }

        return response.getBody().getObject().getString("ID");
    }

    private void releaseSession(String session) throws Exception {
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
        releaseSession(lock.session);
    }

    private <T> HttpResponse<T> doConsulRequest(
        HttpMethod method,
        String path,
        JSONObject postData,
        Class<T> responseClass
    ) throws UnirestException {
        if (postData == null) {
            return HttpClientHelper.request(
                new HttpRequest(method, makeConsulUrl(path)),
                responseClass
            );
        } else {
            return HttpClientHelper.request(
                new HttpRequestWithBody(method, makeConsulUrl(path)).body(postData).getHttpRequest(),
                responseClass
            );
        }
    }

    private String makeConsulUrl(String path) {
        return String.format("http://%s:%s/%s", consulHost, consulPort, path);
    }


    @Override
    public void close() throws IOException {

    }
}
