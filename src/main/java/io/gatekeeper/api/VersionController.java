package io.gatekeeper.api;

import io.gatekeeper.Version;
import io.vertx.core.http.HttpServerRequest;
import org.json.JSONObject;

public class VersionController extends AbstractController {

    @Override
    protected JSONObject handle(HttpServerRequest request) {
        JSONObject data = new JSONObject();

        data.put("version", Version.CURRENT.toString());

        return data;
    }
}
