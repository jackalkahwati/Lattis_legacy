package cc.skylock.skylock.generator;

import android.content.Context;

import java.io.FileInputStream;
import java.security.MessageDigest;

import cc.skylock.skylock.ObjectRepo;

/**
 * Created by Velo Labs Android on 18-01-2016.
 */
public class HashGenerator {
    public static MessageDigest messageDigest;
    public static FileInputStream fileInputStream;
    Context context;
    ObjectRepo objectRepo;

    public HashGenerator(Context context, ObjectRepo objectRepo) {
        this.context = context;
        this.objectRepo = objectRepo;
    }


    public static void checkSumSHA256(String hashstring) {
        // fileInputStream = new FileInputStream("UTF-8");
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            fileInputStream = new FileInputStream("UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }
        messageDigest.update(hashstring.getBytes());

        byte byteData[] = messageDigest.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        System.out.println("Hex format : " + sb.toString());

        //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        System.out.println(" Test Hex format : " + hexString.toString());

    }

    public static void aTOb() {
        try {
            String s = "0000405c78cf9bbffac5201ae9d1351d8c5e193fed61729433ae4379e01dbf47ffffffff002c2a26ff5b9391ba398117d827bb5be77fe5be8944e583a370f56bb3dcb4c162c5cfe1afcccc8149dc9ac94389cee012ab64c75118d4f1f31d38336168e04cd8";
            //   String s =  "velolabsindia";
            System.out.println("Initial string : " + s);
            byte[] b = s.getBytes("UTF-8");
            System.out.println("Initial byte : " + b.toString());
            String s1 = new String(b, "US-ASCII");
            System.out.println("Initial output : " + s1);
            int len = s.length();
            byte[] byteTemp = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                byteTemp[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }

            //   CHAR_SIGN_MSG.setValue(byteTemp);
            //   mBluetoothGatt.writeCharacteristic(CHAR_SIGN_MSG);
            System.out.println(" byte to string : " + byteTemp.toString());
            bTOa(byteTemp);

        } catch (Exception e) {

        }
    }

    public static void bTOa(byte[] byteTemp) {
        try {
            byte[] bytes = byteTemp;
            String s = new String(bytes);
            System.out.println("Text Decryted : " + s);
        } catch (Exception e) {

        }
    }

}
