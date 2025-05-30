package com.yongkj.util;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 非对称加解密工具类
 */
public class RsaUtil {

    private RsaUtil() {

    }

    private static final String ALGORITHM = "RSA";
    public static final String KEY_TYPE_PUBLIC_KEY = "publicKey";
    public static final String KEY_TYPE_PRIVATE_KEY = "privateKey";

    /**
     * 生成公钥和私钥
     *
     * @return
     */
    public static Map<String, String> generateKey() {
        Map<String, String> resultMap = new HashMap<>();
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            Base64.Encoder encoder = Base64.getEncoder();
            resultMap.put(KEY_TYPE_PRIVATE_KEY, encoder.encodeToString(keyPair.getPrivate().getEncoded()));
            resultMap.put(KEY_TYPE_PUBLIC_KEY, encoder.encodeToString(keyPair.getPublic().getEncoded()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * RSA加密
     *
     * @param key
     * @param content
     * @param keyType
     * @return
     * @throws Exception
     */
    public static String rsaEncrypt(String key, String content, String keyType) {
        return rsa(key, content.getBytes(), keyType, Cipher.ENCRYPT_MODE);
    }

    /**
     * RSA解密
     *
     * @param key
     * @param content
     * @param keyType
     * @return
     * @throws Exception
     */
    public static String rsaDecrypt(String key, String content, String keyType) {
        return rsa(key, Base64.getDecoder().decode(content), keyType, Cipher.DECRYPT_MODE);
    }

    private static String rsa(String key, byte[] content, String keyType, int mode) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            if (KEY_TYPE_PRIVATE_KEY.equals(keyType)) {
                cipher.init(mode, keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key))));
            } else {
                cipher.init(mode, keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key))));
            }
            byte[] bytes = cipher.doFinal(content);
            return mode == Cipher.DECRYPT_MODE ? new String(bytes) : Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
