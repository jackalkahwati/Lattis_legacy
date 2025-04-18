package com.lattis.ellipse.Utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.lattis.ellipse.R;

/**
 * Created by raverat on 4/19/17.
 */

public class UtilHelper {

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
    public static String getDateCurrentTimeZone(Context context,long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getTimeZone("UTC");
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            Date currenTimeZone = (Date) calendar.getTime();


            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

            return  dateFormat.format(currenTimeZone) +" " + context.getString(R.string.at_label) +" "+timeFormat.format(currenTimeZone) ;
        }catch (Exception e) {
        }
        return "";
    }

    public static String getTimeFromDuration(String duration) {
        try {
            if (duration != null && !duration.isEmpty()) {
               return convertSecondsToHMmSs(Long.parseLong(duration));
            }
        }catch(Exception e){
            Log.e("getTimeFromDuration", e.getLocalizedMessage());
        }
        return "";
    }




    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60));
        if(h ==0 ){
            return String.format("%02d:%02d", m,s);
        }else{
            return String.format("%d:%02d:%02d", h,m,s);
        }

    }






    public static String getDurationBreakdown(Context context,long millis) {

        millis = millis * 1000;

        if (millis < 0) {
            //throw new IllegalArgumentException("Duration must be greater than zero!");
            return "";
        }

//        long days = TimeUnit.MILLISECONDS.toDays(millis);
//        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
//        if (days != 0) {
//            sb.append(days);
//            sb.append(" " + context.getString(R.string.days) + " ");
//        }

        if (hours != 0) {
            if (hours < 10) {
                sb.append("0" + hours);
            } else {
                sb.append(hours);
            }

            sb.append(":");
        }

        if (minutes < 10) {
            sb.append("0" + minutes);
        } else {
            sb.append(minutes);
        }
        sb.append(":");

        if (seconds < 10) {
            sb.append("0" + seconds);
        } else {
            sb.append(seconds);
        }
        return sb.toString();
    }

    public static long getTime() {
        Date date = new Date();
        return date.getTime() / 1000;
    }


    public static String getDotAfterNumber(String number){
        if(number!=null){
            if(number.indexOf(".")!=-1){
                return  number;
            }else{
                return number+".00";
            }
        }else{
            return "";
        }

    }


}
