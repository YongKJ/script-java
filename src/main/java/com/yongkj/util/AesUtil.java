package com.yongkj.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AesUtil {

    private AesUtil() {

    }

    private static final String ALGORITHM = "AES";

    /**
     * 生成随机秘钥
     *
     * @return
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] b = secretKey.getEncoded();
            return Base64.getEncoder().encodeToString(b);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("没有此算法");
        }
    }

    /**
     * AES 加密
     *
     * @param key
     * @param content
     * @return
     */
    public static String aesEncrypt(String key, String content) {
        try {
            byte[] raw = Base64.getDecoder().decode(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypt = cipher.doFinal(content.getBytes());
            return Base64.getEncoder().encodeToString(encrypt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * AES 解密
     *
     * @param key
     * @param content
     * @return
     */
    public static String aesDecrypt(String key, String content) {
        try {
            byte[] raw = Base64.getDecoder().decode(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] encrypt = Base64.getDecoder().decode(content);
            byte[] decrypt = cipher.doFinal(encrypt);
            return new String(decrypt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
