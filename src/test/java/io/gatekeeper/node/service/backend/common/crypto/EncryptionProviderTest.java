package io.gatekeeper.node.service.backend.common.crypto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class EncryptionProviderTest {

    @Test
    public void testEncryptingWithNoKey() throws Exception {
        EncryptionProvider provider = new EncryptionProvider(null);

        String plainText = "Hello, World!";
        String cipherText = provider.encrypt(plainText);

        assertEquals(plainText, provider.decrypt(cipherText));
        assertNotEquals(plainText, cipherText);
    }

    @Test
    public void testEncryptingWithKey() throws Exception {
        EncryptionProvider provider = new EncryptionProvider("Here's a secret key");

        String plainText = "Hello, World!";
        String cipherText = provider.encrypt(plainText);

        assertEquals(plainText, provider.decrypt(cipherText));
        assertNotEquals(plainText, cipherText);
    }

    @Test(expected = InvalidCipherTextException.class)
    public void testDecryptingWithInvalidData() throws Exception {
        EncryptionProvider provider = new EncryptionProvider("Here's a secret key");

        provider.decrypt("Invalid data");
    }

    @Test(expected = InvalidCipherTextException.class)
    public void testDecryptingWithInvalidData2() throws Exception {
        EncryptionProvider provider = new EncryptionProvider("Here's a secret key");

        String plainText = "Hello, World!";
        String cipherText = provider.encrypt(plainText);

        cipherText = cipherText.concat("a");

        provider.decrypt(cipherText);
    }

}
