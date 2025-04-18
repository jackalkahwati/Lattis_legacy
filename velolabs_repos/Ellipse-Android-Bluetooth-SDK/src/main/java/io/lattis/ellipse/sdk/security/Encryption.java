package io.lattis.ellipse.sdk.security;

import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    private static final String TAG = Encryption.class.getName();

    public static String getResult(String userId, byte[] challengeData) {
        return getAuthSignature256(getChallengeKey(userId), getChallengeResult(challengeData));
    }

    public static String getChallengeKey(String userId) {
        final StringBuilder sb = new StringBuilder(getMD5Hash(userId));
        for (int i = sb.length(); i < 64; i++) {
            sb.append("f");
        }
        return sb.toString();
    }

    private static String getMD5Hash(String userId) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(userId.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    private static String getChallengeResult(byte[] temp) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[temp.length * 2];
        for (int j = 0; j < temp.length; j++) {
            int v = temp[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static String getAuthSignature256(String challengeData, String challengeKey) {
        String authSig;
        MessageDigest digest;
        String value = challengeData + challengeKey;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            int len = value.length();
            byte[] bytes = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                bytes[i / 2] = (byte) ((Character.digit(value.charAt(i), 16) << 4)
                        + Character.digit(value.charAt(i + 1), 16));
            }
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }
            authSig = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return authSig.toLowerCase();
    }

    public static byte[] encodeMessage(String message){
        int len = message.length();
        byte[] byteMessage = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            byteMessage[i / 2] = (byte) ((Character.digit(message.charAt(i), 16) << 4)
                    + Character.digit(message.charAt(i + 1), 16));
        }
        return byteMessage;
    }

    public @Nullable static String encrypt(String key, String value){
        try {
            Cipher cipher = getCipher(key,true);
            return new String(Base64.encode(cipher.doFinal(value.getBytes("UTF-8")), Base64.DEFAULT));
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String key, String value) {
        try {
            Cipher cipher = getCipher(key,false);
            return new String(cipher.doFinal(Base64.decode(value.getBytes("UTF-8"), Base64.DEFAULT)));
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Cipher getCipher(String key, boolean encrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init( encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes("UTF-8"), "AES"));
        return cipher;
    }
}
