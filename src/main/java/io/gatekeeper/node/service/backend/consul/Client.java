package io.gatekeeper.node.service.backend.consul;

import com.mashape.unirest.http.HttpClientHelper;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import io.gatekeeper.configuration.data.backend.ConsulBackendConfiguration;
import io.gatekeeper.node.service.backend.common.crypto.EncryptionProvider;

import java.util.ArrayList;
import java.util.List;

public class Client {

    private String host;

    private Integer port;

    private String prefix;

    private String token;

    private EncryptionProvider encryption;

    public static Client build(ConsulBackendConfiguration configuration) throws Exception {
        try {
            return build(
                configuration.host,
                configuration.port,
                configuration.prefix,
                configuration.token,
                new EncryptionProvider(configuration.key)
            );
        } catch (Exception exception) {
            throw new Exception("Could not create encryption provider");
        }
    }

    public static Client build(String host, Integer port, String prefix, String token, EncryptionProvider encryption) {
        return new Client(host, port, prefix, token, encryption);
    }

    Client(String host, Integer port, String prefix, String token, EncryptionProvider encryption) {
        assert null != host;
        assert null != port;
        assert null != prefix;
        assert null != encryption;

        this.host = host;
        this.port = port;
        this.prefix = prefix;
        this.token = token;
        this.encryption = encryption;
    }

    public void put(String key, String value) throws Exception {
        String encrypted;

        try {
            encrypted = encryption.encrypt(value);
        } catch (Exception e) {
            throw new Exception("Could not encrypt value");
        }

        HttpRequest request = new HttpRequestWithBody(
            HttpMethod.PUT,
            makeConsulUrl(key)
        ).body(encrypted).getHttpRequest();

        authorizeHttpRequest(request);

        HttpResponse<String> response;

        try {
            response = HttpClientHelper.request(request, String.class);
        } catch (Exception exception) {
            throw new ConsulException("Consul request failed", exception);
        }

        if (!response.getBody().equals("true")) {
            throw new ConsulException(
                String.format("Consul PUT %s failed", key)
            );
        }
    }

    public String get(String key) throws Exception {
        HttpRequest request = new HttpRequestWithBody(
            HttpMethod.GET,
            makeConsulUrl(key) + "?raw"
        ).getHttpRequest();

        authorizeHttpRequest(request);

        HttpResponse<String> response;

        try {
            response = HttpClientHelper.request(request, String.class);
        } catch (Exception exception) {
            throw new ConsulException("Consul request failed", exception);
        }

        if (response.getStatus() == 404) {
            return null;
        }

        String encrypted = response.getBody();

        return encryption.decrypt(encrypted);
    }

    public List<String> list(String key) throws Exception {
        HttpRequest request = new HttpRequestWithBody(
            HttpMethod.GET,
            makeConsulUrl(key) + "?keys&separator=/"
        ).getHttpRequest();

        authorizeHttpRequest(request);

        HttpResponse<JsonNode> response;

        try {
            response = HttpClientHelper.request(request, JsonNode.class);
        } catch (Exception exception) {
            throw new ConsulException("Consul request failed", exception);
        }

        if (response.getStatus() == 404) {
            return null;
        }

        JsonNode data = response.getBody();

        if (!data.isArray()) {
            throw new ConsulException("Malformed response - expected an array");
        }

        List<String> keys = new ArrayList<>(data.getArray().length());

        data.getArray().forEach((object) -> {
            keys.add(object.toString());
        });

        return keys;
    }

    /**
     * Adds the appropriate authorization data to the given pre-built request.
     *
     * @param request The request to modify
     */
    private void authorizeHttpRequest(HttpRequest request) {
        if (token != null) {
            request.header("X-Consul-Token", token);
        }
    }

    private String makeConsulUrl(String path) {
        assert null != path;

        if (prefix != null) {
            return String.format("http://%s:%s/v1/kv/%s/%s", host, port, prefix, path);
        } else {
            return String.format("http://%s:%s/v1/kv/%s", host, port, path);
        }
    }
}
