package com.xyq.tweb.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 * 加盐密码工具类
 * 生成长度47个字符
 * </p>
 *
 * @author xuyiqing
 * @since 2022/7/4
 */
public class SaltPasswordUtils {

    private static final char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    public static boolean auth(String inputPassword, String cipherPassword) {
        String[] plains = cipherPassword.split("\\$");
        int iterations = Integer.parseInt(plains[1]);
        String encrypt = encryptPBKDF2(inputPassword, plains[0], iterations);
        return encrypt.equals(cipherPassword);
    }

    public static String generate(String plain) {
        return encryptPBKDF2(plain, salt(), iterations());
    }

    private static String salt() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            char ch = chars[random.nextInt(chars.length)];
            stringBuilder.append(ch);
        }
        return stringBuilder.toString();
    }

    private static Integer iterations() {
        return random.nextInt(900, 1000);
    }

    private static String encryptMD5(String plain, String salt, int iterations) {
        String sourcePlain = plain;
        String sourceSlat = salt;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            for (int j = 0; j < iterations; j++) {
                plain = sourcePlain + salt;  // 拼接 原生密码和加盐字符串
                messageDigest.update(plain.getBytes());
                salt = new BigInteger(1, messageDigest.digest()).toString(16);  // 生成新的加盐字符串, 并作为最后的密文
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sourceSlat + "$" + iterations + "$" + salt;
    }

    private static String encryptPBKDF2(String password, String salt, int iterations) {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), iterations, 128);
        byte[] encoded = new byte[0];
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            encoded = factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        String pwd = new BigInteger(1, encoded).toString(16);
        return salt + "$" + iterations + "$" + pwd;
    }

}
