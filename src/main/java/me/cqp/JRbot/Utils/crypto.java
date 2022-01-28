package me.cqp.JRbot.Utils;

import org.apache.commons.codec.binary.Hex;
import org.meowy.cqp.jcq.util.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class crypto {

    public static String rsa(String command, String key, String src) {
        //MD5withRSA
        return "Not implemented";
    }

    public static String base64(String command, String src) {
        switch (command) {
            case "encode":
            case "加密": {
                byte[] encodeBytes = Base64.getEncoder().encode(src.getBytes());
                return new String(encodeBytes);
            }
            case "decode":
            case "解密": {
                byte[] decodeBytes = Base64.getDecoder().decode(src.getBytes());
                return new String(decodeBytes);
            }
            default:
                return "<Error>Invalid mode";
        }
    }

    public static String encryptDES(String key, String data) {
        try{
            SecretKeySpec key2 = new SecretKeySpec(key.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key2);
            byte[] encryptedData = cipher.doFinal(data.getBytes());

            return Hex.encodeHexString(encryptedData);
        }catch (Exception e){
            e.printStackTrace();
            return "Error-Not encrypted";
        }

    }

    public static String decryptDES(String password, String data) {
        try {
            byte[] byteMi = Hex.decodeHex(data);
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedData = cipher.doFinal(byteMi);
            return new String(decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error-Not decrypted";
        }
    }

    public static byte[] hex(String key) {
        String f = DigestUtils.md5Hex(key.getBytes());
        byte[] bkeys = f.getBytes();
        byte[] enk = new byte[24];
        System.arraycopy(bkeys, 0, enk, 0, 24);
        return enk;
    }

    public static String encode3Des(String key, String srcStr) {
        byte[] keybyte = hex(key);
        byte[] src = srcStr.getBytes();
        try {
            //生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, "DESede");
            //加密
            Cipher c1 = Cipher.getInstance("DESede");
            c1.init(Cipher.ENCRYPT_MODE, deskey);

            return Hex.encodeHexString(c1.doFinal(src));
        } catch (Exception e1) {
            e1.printStackTrace();
            return "<Error>";
        }

    }

    public static String decode3Des(String key, String desStr) {
        byte[] keybyte = hex(key);
        try {
            byte[] src = Hex.decodeHex(desStr);
            //byte[] src=desStr.getBytes();
            //生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, "DESede");
            //解密
            Cipher c1 = Cipher.getInstance("DESede");
            c1.init(Cipher.DECRYPT_MODE, deskey);
            return new String(c1.doFinal(src));
        } catch (Exception e1) {
            e1.printStackTrace();
            return "<Error>";
        }
    }

}

