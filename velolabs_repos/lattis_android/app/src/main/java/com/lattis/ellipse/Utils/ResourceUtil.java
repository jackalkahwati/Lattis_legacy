package com.lattis.ellipse.Utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import io.lattis.ellipse.R;

public class ResourceUtil {


   public static String e_bike_selected = "e_bike_selected";
   public static String e_bike_unselected = "e_bike_unselected";

   public static String regular_selected = "regular_selected";
   public static String regular_unselected = "regular_unselected";

    public static String kick_scooter_selected = "kick_scooter_selected";
    public static String kick_scooter_unselected = "kick_scooter_unselected";


   public static String ic_pick_location = "ic_pick_location";

   public static String generic_parking = "generic_parking";
   public static String parking_racks = "parking_racks";
    public static String parking_meter = "parking_meter";
   public static String charging_spots = "charging_spots";

    public static String user_location = "user_location";


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap getBitmap(VectorDrawableCompat vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public static int getResource(String card_type) {
        switch (card_type) {
            case "VISA":
                return R.drawable.bt_ic_visa;

            case "MASTERCARD":
                return R.drawable.bt_ic_mastercard;

            case "DISCOVER":
                return R.drawable.bt_ic_discover;

            case "AMERICAN_EXPRESS":
                return R.drawable.bt_ic_amex;

            case "MAESTRO":
                return R.drawable.bt_ic_maestro;

            case "DINERS_CLUB":
                return R.drawable.bt_ic_diners_club;

            case "UNIONPAY":
                return R.drawable.bt_ic_unionpay;

            case "JCB":
                return R.drawable.bt_ic_jcb;
        }

        return R.drawable.bt_ic_unknown;
    }

    public static String getBikeResource( String bike_type, boolean selected) {
        if(bike_type != null && !bike_type.equals("")) {
            switch (bike_type) {
                case "REGULAR":
                    return selected ? regular_selected : regular_unselected;

                case "ELECTRIC":
                    return selected ? e_bike_selected : e_bike_unselected;

                case "KICK SCOOTER":
                    return selected ? kick_scooter_selected : kick_scooter_unselected;
            }
        }
        return regular_unselected;
    }

    public static String getUserLocationResource() {
        return user_location;
    }


    public static String getResourcesByParkingType(String type) {
        switch (type) {
            case "GENERIC_PARKING":
                return generic_parking;
            case "PARKING_METER":
                return parking_meter;
            case "CHARGING_SPOT":
                return charging_spots;
            case "PARKING_RACKS":
            case "BIKE_RACK":
                return parking_racks;
            case "SHEFFIELD_STAND":
            case "LOCKER":
                return generic_parking;
        }
        return generic_parking;
    }

}
