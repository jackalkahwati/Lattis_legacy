package cc.skylock.skylock.bluetooth;

import android.util.Log;

import java.security.MessageDigest;


public class SkylockChallengeResultGenerator {


    private static String getAuthSignature256(
            String challengeData, String challengekey) {

        String authSig = null;
        MessageDigest digest = null;
        String value = challengeData + challengekey;
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

        }

        return authSig.toLowerCase();
    }

    private static String asciiToHex(String asciiValue) {
        char[] chars = asciiValue.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }

    public static String getChallengeKey(String userID) {
      //  final String result = asciiToHex(userID);
        String key = null;
        final StringBuffer sb = new StringBuffer(userID);
        for (int i = sb.length(); i < 64; i++) {
            sb.append("f");
        }
        key = sb.toString();
        return key;
    }

    private static String hexToASCII(String hexValue) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2) {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static String getChallengeResult(String userID, String challengeData) {
        String result = null;
        String challengeKey = getChallengeKey(userID);
        result = getAuthSignature256(challengeKey, challengeData);

        return result;
    }


	/*public static void main (String args[]) throws UnsupportedEncodingException
	{
		SkylockChallengeResultGenerator sam = new SkylockChallengeResultGenerator();
		String userID = "415";
		String challengeData = "343135ffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
		System.out.print( "Result : "+sam.getChallengeResult(userID, challengeData));
		sam.getChallengeResult(userID, challengeData);
		
	}*/


}
