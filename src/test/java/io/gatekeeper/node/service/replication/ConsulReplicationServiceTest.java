package io.gatekeeper.node.service.replication;

import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.replication.ConsulReplicationConfiguration;
import io.gatekeeper.node.service.replication.common.Node;
import io.gatekeeper.node.service.replication.consul.Client;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ConsulReplicationServiceTest {

    @Test
    public void testStart() throws ExecutionException, InterruptedException, IOException {
        Configuration configuration = new Configuration();

        configuration.replication = new ConsulReplicationConfiguration();

        ConsulReplicationService service = new ConsulReplicationService(configuration);

        service.start().get();

        service.close();
    }

    @Test
    public void testCountNodes() throws ExecutionException, InterruptedException, IOException {
        Configuration configuration = new Configuration();

        configuration.replication = new ConsulReplicationConfiguration();

        configuration.replication.server = true;

        ConsulReplicationService service = createMockedService(configuration);

        service.start().get();

        Integer nodeCount = service.countNodes().get();

        assertEquals((long) 1, (long) nodeCount);

        service.close();
    }

    @Test
    public void testGetNodes() throws ExecutionException, InterruptedException, IOException {
        Configuration configuration = new Configuration();

        configuration.replication = new ConsulReplicationConfiguration();

        configuration.replication.server = true;

        ConsulReplicationService service = createMockedService(configuration);

        service.start().get();

        List<Node> nodes = service.fetchNodes().get();

        assertEquals((long) 1, (long) nodes.size());

        assertEquals(configuration.api.address, nodes.get(0).host);
        assertEquals(configuration.api.port, nodes.get(0).port);

        service.close();
    }

    private ConsulReplicationService createMockedService(Configuration configuration) {

        return new ConsulReplicationService(configuration, new Client(
            ((ConsulReplicationConfiguration) configuration.replication).host,
            ((ConsulReplicationConfiguration) configuration.replication).port,
            ((ConsulReplicationConfiguration) configuration.replication).service,
            configuration.api.address,
            configuration.api.port,
            ((ConsulReplicationConfiguration) configuration.replication).token
        ) {

            private List<JSONObject> serviceNodes = new ArrayList<>();

            @Override
            @SuppressWarnings("unchecked")
            protected <T> HttpResponse<T> doConsulRequest(
                HttpMethod method, String path, JSONObject postData, Class<T> responseClass
            ) throws UnirestException {

                URI uri;

                try {
                    uri = new URI(path);
                } catch (URISyntaxException e) {
                    throw new UnirestException("Mocked consul client received invalid request");
                }

                if (uri.getPath().equals("v1/catalog/service/" + serviceName)) {
                    JSONArray data = new JSONArray();

                    serviceNodes.forEach(data::put);

                    return createResponseFromJson(data, responseClass);
                }

                if (uri.getPath().equals("v1/agent/check/register")) {
                    return createResponseFromJson(new JSONObject(), responseClass);
                }

                if (uri.getPath().equals("v1/agent/service/register")) {
                    if (!postData.has("ID")) {
                        throw new UnirestException("Mocked consul client received invalid request");
                    }

                    if (!postData.getString("ID").equals(serviceName)) {
                        return createResponseFromJson(new JSONObject(), responseClass);
                    }

                    JSONObject node = new JSONObject();

                    node.put("Node", "localhost");
                    node.put("Address", postData.getString("Address"));
                    node.put("ServiceID", postData.getString("ID"));
                    node.put("ServiceName", postData.getString("Name"));
                    node.put("ServiceAddress", postData.getString("Address"));
                    node.put("ServicePort", postData.getInt("Port"));
                    node.put("ServiceTags", JSONObject.NULL);

                    serviceNodes.add(node);

                    return createResponseFromJson(new JSONObject(), responseClass);
                }

                throw new UnirestException("Mocked consul client received invalid request");
            }

            private <T> HttpResponse<T> createResponseFromJson(Object object, Class<T> responseClass) {
                BasicHttpResponse response = new BasicHttpResponse(
                    new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK")
                );

                HttpEntity entity;

                try {
                    entity = new StringEntity(object.toString(), ContentType.APPLICATION_JSON.toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return new HttpResponse<>(new BasicHttpResponse(
                        new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 500, "Internal Server Error")
                    ), responseClass);
                }

                response.setEntity(entity);

                response.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());

                return new HttpResponse<>(response, responseClass);
            }
        });
    }
}
