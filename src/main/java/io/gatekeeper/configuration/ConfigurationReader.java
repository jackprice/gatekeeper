package io.gatekeeper.configuration;

import com.sun.org.apache.xpath.internal.operations.Bool;
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
    ) throws
        InstantiationException,
        IllegalAccessException {

        T configuration;

        if (this.isAbstractClass(clazz)) {
            configuration = this.createAbstractInstance(clazz, data);
        } else {
            configuration = this.createInstance(clazz);
        }

        this.addDataToConfigurationObject(configuration, data);

        return configuration;
    }

    /**
     * @param clazz The configuration class to check
     *
     * @return True if the given class is abstract and configured properly
     */
    private <T extends ConfigurationInterface> boolean isAbstractClass(Class<T> clazz) throws
        InvalidConfigurationException {
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

    private void addDataToConfigurationObject(ConfigurationInterface object, Map<String, Object> data) throws
        InstantiationException,
        IllegalAccessException {
        Map<Config, Field> fields = this.getFieldsForClass(object.getClass());

        for (Map.Entry<String, Object> dataEntry : data.entrySet()) {
            String key = dataEntry.getKey();
            Object value = dataEntry.getValue();


            for (Map.Entry<Config, Field> entry : fields.entrySet()) {
                Config config = entry.getKey();
                Field field = entry.getValue();

                if (config.name().equals(key)) {
                    Object fieldValue = this.instantiateProperty(field, config, value);

                    field.set(object, fieldValue);
                }
            }

        }
    }

    @SuppressWarnings("unchecked")
    private <U> U instantiateObjectFromData(Class<U> clazz, Object data) throws
        IllegalAccessException,
        InstantiationException {

        if (clazz.equals(Boolean.class) || clazz.equals(Integer.class) || clazz.equals(String.class)) {
            if (!clazz.isAssignableFrom(data.getClass())) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Invalid type %s (expected %s)",
                        data.getClass().getCanonicalName(),
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

            return (U) this.createConfigurationObjectFromData((Class<ConfigurationInterface>) clazz, (Map) data);
        }

        throw new InvalidConfigurationException(
            String.format("Can't instantiate object of type %s", clazz.getCanonicalName())
        );
    }

    @SuppressWarnings("unchecked")
    private Object instantiateProperty(Field field, Config config, Object data) throws
        InstantiationException,
        IllegalAccessException {
        if (config.collection()) {
            if (!List.class.isAssignableFrom(data.getClass())) {
                throw new InvalidConfigurationException(
                    String.format("Invalid type %s (expected a list)", data.getClass().getCanonicalName())
                );
            }

            List list = new ArrayList();

            for (Object item : (List<Object>) data) {
                list.add(this.instantiateObjectFromData(config.type(), item));
            }

            return list;
        } else {
            return this.instantiateObjectFromData(config.type(), data);
        }
    }

    private Map<Config, Field> getFieldsForClass(Class clazz) {
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
    private <T extends ConfigurationInterface> T createInstance(Class<T> clazz) throws
        IllegalAccessException,
        InstantiationException {

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
    private <T extends ConfigurationInterface> T createAbstractInstance(Class<T> clazz, Map<String, Object> data) throws
        InstantiationException,
        IllegalAccessException {
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
                String.format("%s is not a valid value for %s", key, annotation.field())
            );
        }

        return this.createInstance(subclass);
    }
}
