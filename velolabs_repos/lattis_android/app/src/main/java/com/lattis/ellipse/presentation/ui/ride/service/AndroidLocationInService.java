package com.lattis.ellipse.presentation.ui.ride.service;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.lattis.ellipse.data.platform.locationfilter.KalmanLatLong;
import com.lattis.ellipse.data.platform.locationfilter.KalmanLocationManager;
import com.lattis.ellipse.domain.model.Location;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by ssd3 on 7/11/17.
 */

public class AndroidLocationInService implements  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final static String TAG = AndroidLocationInService.class.getSimpleName();

    private PublishSubject<Location> subject = PublishSubject.create();
    private int subscriberCount = 0;

    private GoogleApiClient googleApiClient = null;
    private LocationRequest locationRequest = null;

    private android.location.Location currentLocation;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private KalmanLocationManager mKalmanLocationManager;

    /**
     * Request location updates with the highest possible frequency on gps.
     * Typically, this means one update per second for gps.
     */
    private static final long GPS_TIME = 1000;

    /**
     * For the network provider, which gives locations with less accuracy (less reliable),
     * request updates every 5 seconds.
     */
    private static final long NET_TIME = 5000;

    /**
     * For the filter-time argument we use a "real" value: the predictions are triggered by a timer.
     * Lets say we want 5 updates (estimates) per second = update each 200 millis.
     */
    private static final long FILTER_TIME = 1000;
    ///////////////////////////////////////////////////////////////////////////////////////////////


    private KalmanLatLong kalmanLatLong;
    private int ACCURACY_DECAY_TIME =3; // meters per seconds

   public AndroidLocationInService(Context context) {

        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

       kalmanLatLong = new KalmanLatLong(ACCURACY_DECAY_TIME);

//       mKalmanLocationManager = new KalmanLocationManager(context);
//       mKalmanLocationManager.requestLocationUpdates(
//               KalmanLocationManager.UseProvider.GPS_AND_NET, FILTER_TIME, GPS_TIME, NET_TIME, mLocationListener, true);
    }

    public Observable<Location> getLocationUpdates() {
        return subject.doOnSubscribe(disposable -> {
            if (subscriberCount == 0) {
                startLocationUpdates();
            }
            subscriberCount += 1;
        }).doOnDispose(() -> {
            subscriberCount -= 1;
            if (subscriberCount == 0) {
                stopLocationUpdates();
            }
        });
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected(@Nullable Bundle bundle) {
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (currentLocation != null) {

            subject.onNext(getLocationMapped(currentLocation));
        }

        if (subscriberCount > 0) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        Log.e(TAG,"Location changed: "+ location.getLatitude() + " " + location.getLongitude());
        currentLocation = location;
        Location locationModel = getLocationMapped(location);
        if (kalmanLatLong!=null && kalmanLatLong.Process(locationModel)) {
            location.setLatitude(kalmanLatLong.get_lat());
            location.setLongitude(kalmanLatLong.get_lng());
            location.setAccuracy(kalmanLatLong.get_accuracy());
            Log.e(TAG, "LOCATION KALMAN2 " + location.getLatitude() + "  " + location.getLongitude());
            subject.onNext(locationModel);
        }
    }


    private Location getLocationMapped(android.location.Location object){
        Location location = null;
        if (object != null) {
            location = new Location();
            location.setLatitude(object.getLatitude());
            location.setLongitude(object.getLongitude());
            location.setAccuracy(object.getAccuracy());
            location.setTime(object.getTime());
            location.setHasAccuracy(object.hasAccuracy());
            location.setHasSpeed(object.hasSpeed());
            location.setSpeed(object.getSpeed());
            location.setProvider(object.getProvider());
        }
        return location;
    }

    /**
     * Location Source for google maps 'my location' layer.
     */
    private LocationSource mLocationSource = new LocationSource() {

        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {

        }

        @Override
        public void deactivate() {

        }
    };

    /**
     * Listener used to get updates from KalmanLocationManager (the good old Android LocationListener).
     */
    private android.location.LocationListener mLocationListener = new android.location.LocationListener() {

        @Override
        public void onLocationChanged(android.location.Location location) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            // GPS location
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                Log.e(TAG, "GPS_PROVIDER DATA: " + location.getLatitude() + "," + location.getLongitude());
            }

            // Network location
            if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                Log.e(TAG, "NETWORK_PROVIDER DATA: " + location.getLatitude() + "," + location.getLongitude());

            }

            // If Kalman location and google maps activated the supplied mLocationSource
            if (location.getProvider().equals(KalmanLocationManager.KALMAN_PROVIDER)) {
                Log.e(TAG, "KALMAN1 FILTER DATA: " + location.getLatitude() + "," + location.getLongitude());
                Location locationModel = getLocationMapped(location);
                subject.onNext(locationModel);

            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    public void removeKalmanFilterLocationUpdates(){
        if(mKalmanLocationManager!=null)
            mKalmanLocationManager.removeUpdates(mLocationListener);
    }
}
