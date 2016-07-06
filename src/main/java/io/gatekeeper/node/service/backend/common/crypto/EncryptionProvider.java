package io.gatekeeper.node.service.backend.common.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

/**
 * This class serves as a wrapper for encrypting and decrypting data in a safe manner.
 *
 * It takes plaintext as a freeform string, and ciphertext as base64-encoded data.
 */
public class EncryptionProvider {

    private static final String DEFAULT_KEY = "";

    private static final Integer KEY_SIZE = 128;

    private static final Integer IV_SIZE = KEY_SIZE / 8;

    private static final Integer KEY_HASHES = 128;

    private static final String CIPHER = "AES/CBC/PKCS5PADDING";

    private final SecretKeySpec key;

    /**
     * Initialise this encryption provider.
     *
     * @param key A string encryption key in any format
     */
    public EncryptionProvider(String key) throws NoSuchAlgorithmException, NoSuchPaddingException {
        if (key == null) {
            key = DEFAULT_KEY;
        }

        this.key = createKeyFromBytes(convertKeyToBytes(key));
    }

    /**
     * Convert a key string to a sequence of bytes suitible for use as an encryption key.
     *
     * @param key Any string
     *
     * @return The resulting bytes
     */
    private static byte[] convertKeyToBytes(String key) throws NoSuchAlgorithmException {
        byte[] data = key.getBytes();

        for (int i = 0; i < KEY_HASHES; i++) {
            data = computeHash(data);
        }

        return Arrays.copyOfRange(data, 0, KEY_SIZE / 8);
    }

    /**
     * Creates a secret key from the given sequence of bytes.
     *
     * @param bytes The bytes to use for generating the key
     *
     * @return The generated key
     */
    private static SecretKeySpec createKeyFromBytes(byte[] bytes) {
        return new SecretKeySpec(bytes, "AES");
    }

    /**
     * Computes the SHA-256 hash of the given data.
     *
     * @param data The data to hash
     *
     * @return The raw SHA-256 has of the data
     */
    private static byte[] computeHash(byte[] data) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(data);
    }

    /**
     * Encrypt the given plaintext.
     *
     * @param plaintext Any given plaintext
     *
     * @return The encrypted ciphertext as expected by {@link #decrypt(String)} in a safe string format
     */
    public String encrypt(String plaintext) throws
        NoSuchAlgorithmException,
        NoSuchPaddingException,
        InvalidAlgorithmParameterException,
        InvalidKeyException,
        BadPaddingException,
        IllegalBlockSizeException {
        assert null != plaintext;

        Cipher cipher = Cipher.getInstance(CIPHER);

        byte[] iv = generateIv();

        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] plaintextBytes = plaintext.getBytes();
        byte[] ciphertextBytes = cipher.doFinal(plaintextBytes);

        String ivString = Base64.getEncoder().encodeToString(iv);
        String ciphertext = Base64.getEncoder().encodeToString(ciphertextBytes);

        return ivString + ":" + ciphertext;
    }

    /**
     * Decrypt the given ciphertext.
     *
     * @param ciphertext The ciphertext as outputted by {@link #encrypt(String)}
     *
     * @return The original plaintext
     */
    public String decrypt(String ciphertext) throws
        InvalidCipherTextException,
        NoSuchPaddingException,
        NoSuchAlgorithmException,
        InvalidAlgorithmParameterException,
        InvalidKeyException,
        BadPaddingException,
        IllegalBlockSizeException {
        assert null != ciphertext;

        Integer ivLength = ((int) Math.floor(IV_SIZE / 3) + 1) * 4;

        if (ciphertext.length() < ivLength + 2) {
            throw new InvalidCipherTextException();
        }

        String ivString = ciphertext.substring(0, ivLength);
        String ciphertextString = ciphertext.substring(ivLength + 1);

        byte[] iv;

        try {
            iv = Base64.getDecoder().decode(ivString);
        } catch (Exception exception) {
            throw new InvalidCipherTextException();
        }

        if (iv == null || iv.length != IV_SIZE) {
            throw new InvalidCipherTextException();
        }

        byte[] ciphertextBytes;

        try {
            ciphertextBytes = Base64.getDecoder().decode(ciphertextString);
        } catch (Exception exception) {
            throw new InvalidCipherTextException();
        }

        if (ciphertextBytes == null) {
            throw new InvalidCipherTextException();
        }

        Cipher cipher = Cipher.getInstance(CIPHER);

        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] plaintextBytes = cipher.doFinal(ciphertextBytes);

        if (plaintextBytes == null) {
            throw new InvalidCipherTextException();
        }

        return new String(plaintextBytes);
    }

    /**
     * Securely generate an IV for initialising encryption
     *
     * @return The raw bytes of the IV
     */
    private byte[] generateIv() throws NoSuchAlgorithmException {
        byte[] bytes = new byte[IV_SIZE];

        SecureRandom random = SecureRandom.getInstanceStrong();

        random.nextBytes(bytes);

        return bytes;
    }
}
