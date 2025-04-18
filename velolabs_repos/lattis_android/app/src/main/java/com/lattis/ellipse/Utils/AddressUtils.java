package com.lattis.ellipse.Utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.lattis.ellipse.domain.model.Location;

import java.util.List;
import java.util.Locale;

import io.lattis.ellipse.R;


public class AddressUtils {
    public static String getAddress(Context context, Location location) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }

                strAdd = strReturnedAddress.toString();

                if(strAdd.equals("")){
                    return  context.getString(R.string.current_location_label);
                }else{
                    return strAdd;
                }

            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context.getString(R.string.current_location_label);
    }
}
