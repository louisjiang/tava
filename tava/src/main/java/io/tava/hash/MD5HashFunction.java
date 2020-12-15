package io.tava.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-12-15 15:58
 */
public class MD5HashFunction implements HashFunction {

    private MessageDigest instance;

    public MD5HashFunction() {
        try {
            instance = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
        }
    }

    @Override
    public long hash(String key) {
        instance.reset();
        instance.update(key.getBytes());
        byte[] digest = instance.digest();

        long h = 0;
        for (int i = 0; i < 4; i++) {
            h <<= 8;
            h |= ((int) digest[i]) & 0xFF;
        }
        return h;
    }
}