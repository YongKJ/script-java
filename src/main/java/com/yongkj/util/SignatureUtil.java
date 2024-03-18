package com.yongkj.util;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 签名验证
 */
public class SignatureUtil {

    private SignatureUtil() {

    }

    public static final String KEY_TYPE_PRIVATE_KEY = "privateKey";
    public static final String KEY_TYPE_PUBLIC_KEY = "publicKey";
    private static final String SIGN_ALGORITHM = "SHA1withRSA";
    private static final String ALGORITHM = "RSA";

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
     * 私钥签名
     *
     * @param privateKeyStr
     * @param content
     * @return
     */
    public static String generateSignature(String privateKeyStr, String content) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr)));
            signature.initSign(privateKey);
            signature.update(content.getBytes());
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 公钥验证
     *
     * @param publicKeyStr
     * @param content
     * @param sign
     * @return
     */
    public static boolean verifySignature(String publicKeyStr, String content, String sign) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyStr)));
            signature.initVerify(publicKey);
            signature.update(content.getBytes());
            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
