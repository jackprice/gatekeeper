package io.gatekeeper.model;

import com.google.common.base.Charsets;
import com.migcomponents.migbase64.Base64;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractModelBuilder<ApiModel, Model extends AbstractModel> {

    @SuppressWarnings("unchecked")
    public Model unserialise(String data) throws Exception {
        byte[] bytes = Base64.decode(data);

        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        ObjectInputStream unserializer = new ObjectInputStream(stream);

        return (Model) unserializer.readObject();
    }

    public String serialise(Model model) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        ObjectOutputStream serializer = new ObjectOutputStream(stream);

        serializer.writeObject(model);

        stream.close();

        return Base64.encodeToString(stream.toByteArray(), false);
    }

    public abstract ApiModel toApiModel(Model model);

    public abstract Model fromApiModel(ApiModel model);

    static <T extends Enum, U extends Enum> U castEnum(T from, Class<U> toClass) throws Exception {
        return enumFromString(toClass, from.toString());
    }

    static <U extends Enum> U enumFromString(Class<U> clazz, String string) throws Exception {
        U constants[] = clazz.getEnumConstants();

        for (U constant : constants) {
            if (constant.toString().equals(string)) {
                return constant;
            }
        }

        throw new Exception(
            String.format("Invalid enum constant %s for %s", string, clazz.getCanonicalName())
        );
    }

    static Map<String, Object> jsonObjectToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<>();

        json.keys().forEachRemaining((key) -> map.put(key, json.get(key)));

        return map;
    }
}
