package cc.skylock.skylock.utils;

import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import cc.skylock.skylock.R;

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

    public static String getCountryDialCode(Context context) {
        String countryID = null;
        String contryDialCode = null;

        TelephonyManager telephonyMngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        countryID = telephonyMngr.getSimCountryIso().toUpperCase();
        String[] arrContryCode = context.getResources().getStringArray(R.array.DialingCountryCode);
        for (int i = 0; i < arrContryCode.length; i++) {
            String[] arrDial = arrContryCode[i].split(",");
            if (arrDial[1].trim().equals(countryID.trim())) {
                contryDialCode = arrDial[0];
                break;
            }
        }
        return contryDialCode;
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

    public static int getScreenWidthResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        return width;
    }

    public static String getDeviceMode(BluetoothDevice bluetoothDevice) {

        if (bluetoothDevice != null && bluetoothDevice.getName() != null) {
            char[] test = bluetoothDevice.getName().toCharArray();
            System.out.println("device_mode" + bluetoothDevice.getName());
            if (test[7] == '-') {
                return "SHIPPING_MODE";
            } else if (test[7] == ' ') {
                return "ON_BOARDED_MODE";
            } else {
                return "BOOT_MODE";
            }
        } else
            return null;

    }


    public static Typeface getTypface(Context context) {
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SanFranciscoDisplay-Regular.otf");
        return typeface;
    }

    public static String getCurrentTimeStamp() {
        Long tsLong = System.currentTimeMillis() / 1000;
        final String ts = tsLong.toString();
        return ts;
    }

    public static String getDateCurrentTimeZone(long timestamp) {
        return getDateCurrentTimeZone(timestamp, "yyyy-MM-dd HH:mm");
    }

    public static String getDateCurrentTimeZone(long timestamp, String format) {
        try {
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setTimeInMillis(timestamp * 1000);
            String date = DateFormat.format(format, calendar).toString();
            /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date currenTimeZone = (Date) calendar.getTime();*/
            return date;
        } catch (Exception e) {
        }
        return "";
    }

    public static String littleEndianconversion(String hex) {
        char[] temp = hex.toCharArray();
        String result = String.valueOf(temp[2]) + String.valueOf(temp[3]) + String.valueOf(temp[0])
                + String.valueOf(temp[1]);
        return result;
    }

    public static String getContactName(Context context, String phoneNumber) {
        if (phoneNumber != null) {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor == null) {
                return null;
            }
            String contactName = null;
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            return contactName;
        }
        return null;
    }

    public static Bitmap openPhoto(Context context, long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {

                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeByteArray(data, 0, data.length);
                }
            }
        } finally {
            cursor.close();
        }
        return BitmapFactory.decodeResource(context.getResources(),
                R.drawable.em_contacts);
    }

    public static void analyticTrackUserAction(String action, String eventType, String screen, String errorMsg, String OSType) {
        ContentViewEvent contentViewEvent = new ContentViewEvent()
                .putContentName(action)
                .putContentType(eventType)
                .putCustomAttribute("screen", screen)
                .putCustomAttribute("OS", OSType);
        if (errorMsg != null) {
            contentViewEvent.putCustomAttribute("errorMsg", errorMsg);
        }
        Answers.getInstance().logContentView(contentViewEvent);

    }

    public static String getMD5Hash(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static boolean isLockAvailable(ArrayList<BluetoothDevice> mBluetoothDeviceList, String macId) {
        ArrayList<String> mArrayList = new ArrayList<>();

        if (mBluetoothDeviceList != null && mBluetoothDeviceList.size() > 0) {
            for (BluetoothDevice mBluetoothDevice : mBluetoothDeviceList) {
                final String scanLockMacID = mBluetoothDevice.getName().substring(8, mBluetoothDevice.getName().length());
                mArrayList.add(scanLockMacID);
            }
            if (mArrayList.contains(macId)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static String getLockMacIDFromName(String nameWithAddress) {
        return nameWithAddress.substring(8, nameWithAddress.length());
    }

}
