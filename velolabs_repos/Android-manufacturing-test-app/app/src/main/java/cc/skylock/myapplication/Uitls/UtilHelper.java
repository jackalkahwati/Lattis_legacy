package cc.skylock.myapplication.Uitls;

import android.util.Log;

public class UtilHelper {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        Log.i("bytesToHex", "" + new String(hexChars));
        return new String(hexChars);
    }
    public static String macAddColon(String macId) {
        String x = macId;
        String finals = "";
        for (int i = 0; i < x.length(); i = i + 2) {
            if ((i + 2) < x.length())
                finals += x.substring(i, i + 2) + ":";
            if ((i + 2) == x.length()) {
                finals += x.substring(i, i + 2);

            }

        }
        return finals;
    }


    public static String hexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        char[] hexData = hex.toCharArray();
        for (int count = 0; count < hexData.length - 1; count += 2) {
            int firstDigit = Character.digit(hexData[count], 16);
            int lastDigit = Character.digit(hexData[count + 1], 16);
            int decimal = firstDigit * 16 + lastDigit;
            sb.append((char) decimal);
        }
        return sb.toString();
    }


}
