package cc.skylock.skylock.utils;

/**
 * Created by Velo Labs Android on 09-11-2016.
 */


import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecrypt {

    public static String crypto(String key, String inString, boolean decrypt) {
        try {
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            byte[] inputByte = inString.getBytes("UTF-8");
            if (decrypt) {
                cipher.init(Cipher.DECRYPT_MODE, aesKey);
                return new String(cipher.doFinal(Base64.decode(inputByte, Base64.DEFAULT)));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                return new String(Base64.encode(cipher.doFinal(inputByte), Base64.DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
