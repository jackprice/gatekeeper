package io.gatekeeper.model;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractModelBuilder<ApiModel, Model extends AbstractModel> {

    public abstract Model unserialise(String data) throws Exception;

    public abstract String serialise(Model model);

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

    static Map<String, ?> jsonObjectToMap(JSONObject json) {
        Map<String, ?> map = new HashMap<>();

        return map;
    }
}
