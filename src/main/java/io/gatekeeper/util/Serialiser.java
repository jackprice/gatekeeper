package io.gatekeeper.util;

import com.migcomponents.migbase64.Base64;

import java.io.*;

/**
 * A class for serialising and unserialising data safely.
 *
 * TODO: Perform class type checks etc. for security.
 */
public class Serialiser<T extends Serializable> {

    private Class<T> clazz;

    Serialiser(Class<T> clazz) {
        this.clazz = clazz;
    }

    public static <U extends Serializable> Serialiser<U> build(Class<U> clazz) {
        return new Serialiser<U>(clazz);
    }

    @SuppressWarnings("unchecked")
    public T unserialise(String data) throws Exception {
        byte[] bytes = Base64.decode(data);

        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        ObjectInputStream unserializer = new ObjectInputStream(stream);

        return (T) unserializer.readObject();
    }

    public String serialise(T object) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        ObjectOutputStream serializer = new ObjectOutputStream(stream);

        serializer.writeObject(object);

        stream.close();

        return Base64.encodeToString(stream.toByteArray(), false);
    }

}
