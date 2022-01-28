package me.cqp.JRbot.Utils;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 几组测试数据：
 * sha512(12345)=3627909a29c31381a071ec27f7c9ca97726182aed29a7ddd2e54353322cfb30abb9e3a6df2ac2c20fe23436311d678564d0c8d305930575f60e2d3d048184d79
 * sha256(12345)=5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5
 * sha1(12345)=8cb2237d0679ca88db6464eac60da96345513964
 * md5(12345)=827ccb0eea8a706c4c34a16891f84e7b
 */

public class Message_Digest {
    public static String MD5(String command, String src) {
        if (command.equals("decode") || command.equals("解密"))
            return "";
        else {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] encodebytes = md.digest(src.getBytes());
                return Hex.encodeHexString(encodebytes);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static String sha(String command, String src,String algorithm_name) {
        if (command.equals("decode") || command.equals("解密"))
            return "";
        else {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithm_name);
                md.update(src.getBytes());
                return Hex.encodeHexString(md.digest());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}


