package io.gatekeeper.configuration;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.annotation.Discriminator;
import io.gatekeeper.configuration.annotation.DiscriminatorMapping;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class EnvironmentConfigurationReader extends ConfigurationReader {

    public <T extends ConfigurationInterface> T createConfigurationObjectFromEnvironment(
        Class<T> clazz
    ) throws InstantiationException, IllegalAccessException {
        assert null != clazz;

        return createConfigurationObjectFromEnvironment(clazz, System.getenv());
    }

    public <T extends ConfigurationInterface> T createConfigurationObjectFromEnvironment(
        Class<T> clazz,
        Map<String, String> data
    ) throws IllegalAccessException, InstantiationException {
        assert null != clazz;
        assert null != data;

        return createConfigurationObjectFromData(clazz, createDataMapFromEnvironment(data), "");
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> createDataMapFromEnvironment(Map<String, String> data) {
        Map<String, Object> map = new HashMap<>();

        data.forEach((key, value) -> {
            Map<String, Object> subMap = map;
            String[] parts = key.split("_");

            for (int i = 0; i < parts.length - 1; i++) {
                if (!subMap.containsKey(parts[i])) {
                    subMap.put(parts[i], new HashMap<String, Object>());
                }

                Object object = subMap.get(parts[i]);

                if (!Map.class.isAssignableFrom(object.getClass())) {
                    subMap.put(parts[i], new HashMap<String, Object>());
                }

                subMap = (Map<String, Object>) subMap.get(parts[i]);
            }

            subMap.put(parts[parts.length - 1], value);
        });

        return map;
    }

    @Override
    protected void addDataToConfigurationObject(
        ConfigurationInterface object, Map<String, Object> data, String trace
    ) throws InstantiationException, IllegalAccessException {

        assert null != object;
        assert null != data;
        assert null != trace;

        Map<Config, Field> fields = this.getFieldsForClass(object.getClass());

        assert null != fields;

        for (Map.Entry<String, Object> dataEntry : data.entrySet()) {
            String key = dataEntry.getKey();
            Object value = dataEntry.getValue();

            for (Map.Entry<Config, Field> entry : fields.entrySet()) {
                Config config = entry.getKey();
                Field field = entry.getValue();

                if (config.name().toUpperCase().equals(key)) {
                    Object fieldValue = this.instantiateProperty(field, config, value, trace + "." + config.name());

                    assert null != fieldValue;

                    field.set(object, fieldValue);
                }
            }

        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <U> U instantiateObjectFromData(Class<U> clazz, Object data, String trace) throws
        IllegalAccessException,
        InstantiationException {
        assert null != clazz;
        assert null != data;
        assert null != trace;

        if (clazz.equals(Boolean.class)) {
            if (data.toString().toUpperCase().equals("TRUE")) {
                return (U) Boolean.TRUE;
            } else if (data.toString().toUpperCase().equals("FALSE")) {
                return (U) Boolean.FALSE;
            } else {
                throw new InvalidConfigurationException(
                    String.format(
                        "Invalid value for %s: %s",
                        trace,
                        data.toString()
                    )
                );
            }
        }

        return super.instantiateObjectFromData(clazz, data, trace);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected  <T extends ConfigurationInterface> T createAbstractInstance(
        Class<T> clazz,
        Map<String, Object> data,
        String trace
    ) throws InstantiationException, IllegalAccessException {
        assert null != clazz;
        assert ConfigurationInterface.class.isAssignableFrom(clazz);
        assert null != data;
        assert null != trace;

        Discriminator annotation = clazz.getAnnotation(Discriminator.class);
        DiscriminatorMapping[] mappings = annotation.map();

        if (!data.containsKey(annotation.field().toUpperCase())) {
            throw new InvalidConfigurationException(String.format("Required field %s not found", annotation.field()));
        }

        String key = data.get(annotation.field().toUpperCase()).toString();
        Class<T> subclass = null;

        for (DiscriminatorMapping mapping : mappings) {
            if (mapping.name().equals(key)) {
                if (!clazz.isAssignableFrom(mapping.mappedTo())) {
                    throw new InvalidConfigurationException(
                        String.format(
                            "%s is not assignable from %s",
                            clazz.getCanonicalName(),
                            mapping.mappedTo().getCanonicalName()
                        )
                    );
                }

                subclass = mapping.mappedTo();

                break;
            }
        }

        if (subclass == null) {
            throw new InvalidConfigurationException(
                String.format("%s is not a valid value for %s at %s", key, annotation.field(), trace)
            );
        }

        return this.createInstance(subclass);
    }
}
