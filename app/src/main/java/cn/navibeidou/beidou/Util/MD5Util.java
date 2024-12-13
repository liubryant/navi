package cn.navibeidou.beidou.Util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {


    private static final String salt = "&%9527***&&%%$$#@";

    public static String md5WithSalt(String str) {
        String base = str + "/" + salt;
        return md5(base);
    }

    public static String md5(String str) {
         byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString().toUpperCase();
    }

    public static String genToken(String str) {
        String base = str + "ep_!@#$%9636*&&^";
        return md5(base);
    }

    public static String genToken(String str, String additionContent) {
        String base = str + "ep_!@#$%9636*&&^" + additionContent;
        return md5(base);
    }
}
