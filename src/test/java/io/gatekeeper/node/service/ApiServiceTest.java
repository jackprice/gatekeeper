package io.gatekeeper.node.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.gatekeeper.Version;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.node.ServiceContainer;
import org.apache.http.client.HttpClient;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ApiServiceTest {

    @Test
    public void testStart() throws IOException {
        Configuration configuration = new Configuration();

        ApiService service = new ApiService(configuration, new ServiceContainer());

        service.start().join();

        service.close();
    }

    @Test
    public void testVersion() throws IOException, UnirestException {
        Configuration configuration = new Configuration();

        ApiService service = new ApiService(configuration, new ServiceContainer());

        service.start().join();

        HttpResponse<JsonNode> node = Unirest
            .get(String.format("http://%s:%d/api/version", configuration.api.address, configuration.api.port))
            .asJson();

        assertEquals(Version.CURRENT.toString(), node.getBody().getObject().getString("version"));

        service.close();
    }
}
