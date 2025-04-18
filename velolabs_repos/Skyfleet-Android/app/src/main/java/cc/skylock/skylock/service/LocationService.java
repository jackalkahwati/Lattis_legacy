package cc.skylock.skylock.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Velo Labs Android on 27-01-2016.
 */
public class LocationService implements LocationListener {

//    //The minimum distance to change updates in meters
//    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 10 meters
//
//    //The minimum time beetwen updates in milliseconds
//    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 3;//1000 * 60 * 1; // 1 minute

    private final static boolean forceNetwork = false;

    private static LocationService instance = null;

    private LocationManager locationManager;
    public static Location mLocationChanged;
    public double longitude;
    public double latitude;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean locationServiceAvailable = false;
    public static int MY_PERMISSION_ACCESS_COURSE_LOCATION = 124;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 5;

    private static final long MIN_TIME_BW_UPDATE = 1000*60*1;

    /**
     * Singleton implementation
     *
     * @return
     */
    public static LocationService getLocationManager(Context context) {
        if (instance == null) {
            instance = new LocationService(context);
        }
        return instance;
    }

    /**
     * Local constructor
     */
    public LocationService(Context context) {

        initLocationService(context);
        //     isLocationServiceEnabled(context);
    }

    /**
     * Sets up location service after permissions is granted
     */
    @TargetApi(23)
    private void initLocationService(Context context) {


        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


            try{
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
                isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

                if(!isGPSEnabled && !isNetworkEnabled){
                    //no network or gps
                }else{
//                    setCanGetLocation(true);

                    if(isNetworkEnabled){
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATE,
                                MIN_DISTANCE_CHANGE_FOR_UPDATE,
                                this
                        );
                        if(locationManager!=null){
                            mLocationChanged = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        }
                    }
                    if (isGPSEnabled) {
                        if (mLocationChanged == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATE,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
                            if (locationManager != null) {
                                mLocationChanged = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            }
                        }
                    }
                }
//            }catch (Exception e){
//
//            }
            updateCoordinates();
        } catch (Exception ex) {

        }
    }

    public static LatLng updateCoordinates() {

        if (mLocationChanged != null) {
            LatLng latLng = new LatLng(mLocationChanged.getLatitude(),
                    mLocationChanged.getLongitude());
        //    System.out.println("latLng :" + latLng.toString());

            return latLng;
        }
        return null;
    }


    @Override
    public void onLocationChanged(Location location) {
        // do stuff here with location object
        mLocationChanged = location;
      //  System.out.println("location latLng :" + location.getLatitude() + " :" + location.getLongitude());
        updateCoordinates();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public static boolean isLocationServiceEnabled(Context context) {
        LocationManager locationManager = null;
        boolean gps_enabled = false, network_enabled = false;

        if (locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }

        return gps_enabled || network_enabled;

    }

}