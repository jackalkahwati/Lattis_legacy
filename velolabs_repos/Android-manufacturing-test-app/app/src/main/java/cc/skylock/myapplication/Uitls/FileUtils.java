package cc.skylock.myapplication.Uitls;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by Velo Labs Android on 20-02-2016.
 */
public class FileUtils {

    public static boolean writeFile(String filename, String message) {
        boolean success = false;
        try {
            // this will create a new name everytime and unique
            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Skylock");
            // if external memory exists and folder with name Notes
            System.out.println("File root : " + root);
            if (!root.exists()) {
                root.mkdirs(); // this will create folder.
                Log.i("root.mkdirs() :", "" + root.mkdirs());
            } else {
                Log.i("root.exists() :", "" + root.exists());
            }
            File filepath = new File(root, filename + ".bat");  // file path to save
            FileWriter writer = new FileWriter(filepath);
            writer.append(message);
            writer.flush();
            writer.close();
            success = true;
            Log.i("Success ", "" + success);

        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
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


    public static byte[] readFirmwareFile(Context context) {
        String[] fileArray = null;
        File dir = new File("/storage/emulated/0/Skylock/");
        File[] files = dir.listFiles();
        String fileName = null;
        fileArray = new String[files.length];
        for (int i = 0; i < files.length; ++i) {
            if (files[i].getName().contains(".BIN") || files[i].getName().contains(".bin")) {
                fileName = files[i].getName();
                break;
            }
        }

        if (fileName != null) {
            File file = new File("/storage/emulated/0/Skylock/"+fileName);

            byte[] b = new byte[(int) file.length()];
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(b);

                return b;

            } catch (FileNotFoundException e) {
                System.out.println("File Not Found.");
                e.printStackTrace();
            }
            catch (IOException e1) {
                System.out.println("Error Reading The File.");
                e1.printStackTrace();
            }
//            try {
//                File myFile = new File("/storage/emulated/0/Skylock/" + fileName);
//
//                FileInputStream fIn = new FileInputStream(myFile);
//                BufferedReader myReader = new BufferedReader(
//                        new InputStreamReader(fIn));
//                String aDataRow = "";
//                String aBuffer = "";
//                while ((aDataRow = myReader.readLine()) != null) {
//                    aBuffer += aDataRow + "\n";
//                }
//
//                myReader.close();
//                return aBuffer;
//            } catch (Exception e) {
//
//            }

        }

        return null;
    }


    public static String readFromFile(Context context, String filename) {
        String ret = "";
        try {
            File myFile = new File("/storage/emulated/0/Skylock/"+filename+".bat");
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(fIn));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            ret = aBuffer;
            myReader.close();

        } catch (Exception e) {

        }

        return ret;
    }

    public static String  getVersionName(Context context)
    {
        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            return versionName;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "";
    }


}
