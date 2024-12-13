package cn.navibeidou.beidou.Util;

/**
 * author : Stanley
 * e-mail : bryant_liu24@126.com
 * date   : 2020/4/28 16:00
 * desc   : ZouZou project
 * version: 1.0
 */

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

    public class AESUtil {
        private final String CIPHERMODEPADDING = "AES/CBC/PKCS5Padding";// AES/CBC/PKCS7Padding

        private SecretKeySpec skforAES = null;
        private static String ivParameter = "1234huangxiaoguo";// 密钥默认偏移，可更改

        private byte[] iv = ivParameter.getBytes();
        private IvParameterSpec IV;
        String sKey = "huangxiaoguo1234";// key必须为16位，可更改为自己的key

        /*private static AES instance = null;

        public static AES getInstance() {
            if (instance == null) {
                synchronized (AES.class) {
                    if (instance == null) {
                        instance = new AES();
                    }
                }
            }
            return instance;
        }

        public AES() {
            byte[] skAsByteArray;
            try {
                skAsByteArray = sKey.getBytes("ASCII");
                skforAES = new SecretKeySpec(skAsByteArray, "AES");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            IV = new IvParameterSpec(iv);
        }

        public String encrypt(byte[] plaintext) {
            byte[] ciphertext = encrypt(CIPHERMODEPADDING, skforAES, IV, plaintext);
            String base64_ciphertext = Base64Encoder.encode(ciphertext);
            return base64_ciphertext;
        }

        public String decrypt(String ciphertext_base64) {
            byte[] s = Base64Decoder.decodeToBytes(ciphertext_base64);
            String decrypted = new String(decrypt(CIPHERMODEPADDING, skforAES, IV,
                    s));
            return decrypted;
        }

        private byte[] encrypt(String cmp, SecretKey sk, IvParameterSpec IV,
                               byte[] msg) {
            try {
                Cipher c = Cipher.getInstance(cmp);
                c.init(Cipher.ENCRYPT_MODE, sk, IV);
                return c.doFinal(msg);
            } catch (Exception nsae) {
            }
            return null;
        }

        private byte[] decrypt(String cmp, SecretKey sk, IvParameterSpec IV,
                               byte[] ciphertext) {
            try {
                Cipher c = Cipher.getInstance(cmp);
                c.init(Cipher.DECRYPT_MODE, sk, IV);
                return c.doFinal(ciphertext);
            } catch (Exception nsae) {
            }
            return null;
        }*/

}
