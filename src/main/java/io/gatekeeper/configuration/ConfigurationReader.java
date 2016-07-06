package io.gatekeeper.configuration;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.annotation.Discriminator;
import io.gatekeeper.configuration.annotation.DiscriminatorMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ConfigurationReader {

    public <T extends ConfigurationInterface> T createConfigurationObjectFromData(
        Class<T> clazz,
        Map<String, Object> data
    ) throws InstantiationException, IllegalAccessException {
        assert null != clazz;
        assert ConfigurationInterface.class.isAssignableFrom(clazz);
        assert data != null;

        return createConfigurationObjectFromData(clazz, data, "$");
    }

    public <T extends ConfigurationInterface> T createConfigurationObjectFromData(
        Class<T> clazz,
        Map<String, Object> data,
        String trace
    ) throws InstantiationException, IllegalAccessException {
        assert null != clazz;
        assert ConfigurationInterface.class.isAssignableFrom(clazz);
        assert data != null;
        assert trace != null;

        T configuration;

        if (this.isAbstractClass(clazz)) {
            configuration = this.createAbstractInstance(clazz, data, trace);
        } else {
            configuration = this.createInstance(clazz);
        }

        this.addDataToConfigurationObject(configuration, data, trace);

        return configuration;
    }

    /**
     * @param clazz The configuration class to check
     *
     * @return True if the given class is abstract and configured properly
     */
    private <T extends ConfigurationInterface> boolean isAbstractClass(Class<T> clazz)
        throws InvalidConfigurationException {
        assert clazz != null;
        assert ConfigurationInterface.class.isAssignableFrom(clazz);

        if (!Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }

        if (clazz.getAnnotation(Discriminator.class) == null) {
            throw new InvalidConfigurationException(
                String.format("Abstract class %s has no discriminator annotation", clazz.getCanonicalName())
            );
        }

        return true;
    }

    /**
     * Add data to the given configuration object.
     *
     * The trace parameter is used to keep track of nested configuration parameters, so we can provide a detailed
     * overview of any configuration errors.
     */
    private void addDataToConfigurationObject(
        ConfigurationInterface object,
        Map<String, Object> data,
        String trace
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

                if (config.name().equals(key)) {
                    Object fieldValue = this.instantiateProperty(field, config, value, trace + "." + config.name());

                    assert null != fieldValue;

                    field.set(object, fieldValue);
                }
            }

        }
    }

    @SuppressWarnings("unchecked")
    private <U> U instantiateObjectFromData(Class<U> clazz, Object data, String trace) throws
        IllegalAccessException,
        InstantiationException {
        assert null != clazz;
        assert null != data;
        assert null != trace;

        if (clazz.equals(Boolean.class) || clazz.equals(Integer.class) || clazz.equals(String.class)) {
            if (!clazz.isAssignableFrom(data.getClass())) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Invalid type %s at %s (expected %s)",
                        data.getClass().getCanonicalName(),
                        trace,
                        clazz.getCanonicalName()
                    )
                );
            }

            return clazz.cast(data);
        }

        if (ConfigurationInterface.class.isAssignableFrom(clazz)) {
            if (!Map.class.isAssignableFrom(data.getClass())) {
                throw new InvalidConfigurationException(
                    String.format("Invalid configuration value %s - (expected a map)", data.getClass())
                );
            }

            U object = (U) this.createConfigurationObjectFromData((Class<ConfigurationInterface>) clazz, (Map) data, trace);

            assert null != object;

            return object;
        }

        throw new InvalidConfigurationException(
            String.format("Can't instantiate object of type %s", clazz.getCanonicalName())
        );
    }

    @SuppressWarnings("unchecked")
    private Object instantiateProperty(Field field, Config config, Object data, String trace)
        throws InstantiationException, IllegalAccessException {
        assert null != field;
        assert null != config;
        assert null != data;
        assert null != trace;

        if (config.collection()) {
            if (!List.class.isAssignableFrom(data.getClass())) {
                throw new InvalidConfigurationException(
                    String.format("Invalid value at %s (expected a list)", trace)
                );
            }

            List list = new ArrayList();

            for (Object item : (List<Object>) data) {
                list.add(this.instantiateObjectFromData(config.type(), item, trace));
            }

            return list;
        } else {
            return this.instantiateObjectFromData(config.type(), data, trace);
        }
    }

    private Map<Config, Field> getFieldsForClass(Class clazz) {
        assert null != clazz;

        Map<Config, Field> properties = new HashMap<>();
        List<Field> fields = new ArrayList<>();
        Collections.addAll(fields, clazz.getFields());

        fields.stream().filter((field) -> field.getAnnotation(Config.class) != null).forEach((field) -> {
            Config annotation = field.getAnnotation(Config.class);

            properties.put(annotation, field);
        });

        return properties;
    }

    /**
     * Instantiates an instance of the given concrete class.
     *
     * @param clazz The class to instantiate
     *
     * @return The instantiated class
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private <T extends ConfigurationInterface> T createInstance(Class<T> clazz)
        throws IllegalAccessException, InstantiationException {
        assert null != clazz;
        assert ConfigurationInterface.class.isAssignableFrom(clazz);

        // TODO: Allow custom factories

        return clazz.newInstance();
    }

    /**
     * Instantiates an instance of the given abstract class.
     *
     * @param clazz The class to instantiate
     *
     * @return The instantiated class
     */
    @SuppressWarnings("unchecked")
    private <T extends ConfigurationInterface> T createAbstractInstance(
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

        if (!data.containsKey(annotation.field())) {
            throw new InvalidConfigurationException(String.format("Required field %s not found", annotation.field()));
        }

        String key = data.get(annotation.field()).toString();
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
