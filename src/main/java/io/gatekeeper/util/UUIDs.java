package io.gatekeeper.util;

import com.google.common.base.Charsets;

import java.util.UUID;

/**
 * A class for manipulating UUIDs.
 */
public class UUIDs {

    public static UUID mutatePredictably(UUID uuid, String extra) {
        long bitsA = uuid.getLeastSignificantBits();
        long bitsB = uuid.getMostSignificantBits();
        byte[] bytes = extra.getBytes(Charsets.UTF_8);

        for (int i = 0; i < bytes.length; i ++) {
            bitsB = bitsB | bytes[i];
        }

        return new UUID(bitsA, bitsB);
    }
}
