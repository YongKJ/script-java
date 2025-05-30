package com.yongkj.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AesUtil {

    private AesUtil() {

    }

    private static final String ALGORITHM = "AES";

    private static final String ENCRYPT = "AES";
    private static final String ENCRYPT_TYPE = "AES/CBC/PKCS5Padding";

    /**
     * 生成随机向量
     *
     * @return
     */
    public static String generateIV() {
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

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
            return "";
        }
    }

    /**
     * AES 加密
     *
     * @param key
     * @param content
     * @return
     */
    public static String aesEncrypt(String key, String iv, String content) {
        try {
            byte[] raw = Base64.getDecoder().decode(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, ENCRYPT);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
            byte[] encrypt = cipher.doFinal(content.getBytes());
            return Base64.getEncoder().encodeToString(encrypt);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
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
            return "";
        }
    }

    /**
     * AES 解密
     *
     * @param key
     * @param content
     * @return
     */
    public static String aesDecrypt(String key, String iv, String content) {
        try {
            byte[] raw = Base64.getDecoder().decode(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, ENCRYPT);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
            byte[] encrypt = Base64.getDecoder().decode(content);
            byte[] decrypt = cipher.doFinal(encrypt);
            return new String(decrypt);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
