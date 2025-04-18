package com.lattis.ellipse.data.platform;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.lattis.ellipse.data.platform.mapper.LocationMapper;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.domain.model.map.PlaceAutocomplete;
import com.lattis.ellipse.domain.repository.LocationRepository;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.lattis.ellipse.R;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;


public class AndroidLocationRepository extends LocationCallback implements LocationRepository, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = AndroidLocationRepository.class.getSimpleName();

    private PublishSubject<Location> subject = PublishSubject.create();
    private PublishSubject<LocationSettingsResult>locationSettingsSubject= PublishSubject.create();
    private int subscriberCount = 0;


    private GoogleApiClient googleApiClient = null;
    private PlacesClient placesClient;
    private LocationRequest locationRequest = null;
    private LocationMapper locationMapper;
    private Context context;
    private android.location.Location currentLocation;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private FusedLocationProviderClient fusedLocationProviderClient; //Global variable;

    private static final LatLngBounds BOUNDS_WORLD = new LatLngBounds(
            new LatLng(-0, 0), new LatLng(0, 0));
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.NORMAL);

    @Inject
    public AndroidLocationRepository(Context context,
                                     LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
        this.context = context;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Places.initialize(context, context.getString(R.string.google_maps_key));
        placesClient = Places.createClient(context);

        googleApiClient.connect();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context); //initiate in onCreate

    }

    @Override
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
//        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener( new OnSuccessListener<android.location.Location>() {
            @Override
            public void onSuccess(android.location.Location location) {
               currentLocation = location;
                if (currentLocation != null) {
                    subject.onNext(locationMapper.mapIn(currentLocation));
                }
            }
        });


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
        subject.onError(new Throwable(connectionResult.getErrorMessage()));
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,this,Looper.getMainLooper());
//            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
//        if (googleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
//        }
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(this);
        }
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        if (locationResult == null) {
            return;
        }
        for (android.location.Location location : locationResult.getLocations()) {
            if (location != null) {
                Log.e(TAG, "Location changed normal: " + location.getLatitude() + " " + location.getLongitude() + "  "+ location.getAccuracy());
                currentLocation = location;
                subject.onNext(locationMapper.mapIn(location));
            }
        }
    }

    public Observable<LocationSettingsResult> getLocationSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        Task<LocationSettingsResponse> task =
                LocationServices.getSettingsClient(context).checkLocationSettings(builder.build());

        task.addOnSuccessListener( new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.i(TAG, "All location settings are satisfied.");
                LocationSettingsResult locationSettingsResult = new LocationSettingsResult();
                locationSettingsResult.setStatus(LocationSettingsStatusCodes.SUCCESS);
                locationSettingsSubject.onNext(locationSettingsResult);
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LocationSettingsResult locationSettingsResult = new LocationSettingsResult();
                locationSettingsResult.setStatus(((ApiException)e).getStatusCode());
                locationSettingsResult.setApiException((ApiException)e);
                locationSettingsSubject.onNext(locationSettingsResult);
            }
        });
        return locationSettingsSubject;
    }




    @Override
    public Observable<List<Bike>> getDistanceForBikes(Location currentLocation, List<Bike> bikes) {
        if (bikes == null || currentLocation == null) {
            return Observable.just(null);
        }
        final LatLng sourceLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        Map<Bike, Double> returnMap = new HashMap<>();
        return Observable.create(emitter -> {
                for (Bike bike : bikes) {
                    if (bike != null) {
                        returnMap.put(bike, CalculationByDistance(sourceLatLng, new LatLng(bike.getLatitude(), bike.getLongitude())));
                    }
                }
            emitter.onNext(sortBikeFromDistance(returnMap, true));
        });

    }

    private List<Bike> sortBikeFromDistance(Map<Bike, Double> unsortMap, final boolean order) {

        if (unsortMap == null) {
            return new ArrayList<>();
        }


        List<Map.Entry<Bike, Double>> list = new LinkedList<Map.Entry<Bike, Double>>(unsortMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Bike, Double>>() {
            public int compare(Map.Entry<Bike, Double> o1,
                               Map.Entry<Bike, Double> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });
        List<Bike> bikes = new ArrayList<>();
        for (Map.Entry<Bike, Double> entry : list) {
            bikes.add(entry.getKey());
            Log.e("AndroidLocRepository", "Distance is " + (Double) (entry.getValue()));
        }
        return bikes;
    }


    public double CalculationByDistance(LatLng StartP, LatLng EndP) {


        int Radius = 6371;//radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.e("Radius Value", "" + valueResult + "   KM  " + kmInDec + " Meter   " + meterInDec);

        return Radius * c;
    }


    @Override
    public Observable<ArrayList<PlaceAutocomplete>> getPlaces(String constraint) {
        return Observable.create(emitter -> {
            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
            RectangularBounds bounds = RectangularBounds.newInstance(BOUNDS_WORLD);
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
//                    .setLocationBias(bounds)
//                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setSessionToken(token)
                    .setQuery(constraint)
                    .build();
            placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener((response) -> {
                        ArrayList resultList = new ArrayList<>(response.getAutocompletePredictions().size());
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                            PlaceAutocomplete placeAutocomplete = new PlaceAutocomplete();
                            placeAutocomplete.setPlaceId(prediction.getPlaceId());
                            placeAutocomplete.setAddress1(prediction.getPrimaryText(STYLE_BOLD));
                            placeAutocomplete.setAddress2(prediction.getSecondaryText(STYLE_BOLD));
                            resultList.add(placeAutocomplete);
                        }
                        emitter.onNext(resultList);
                    }).addOnFailureListener((exception) -> {
                        emitter.onError(new Throwable("MATCH_NOT_FOUND"));
                    }
            );
        });
    }

    @Override
    public Observable<Place> getPlaceBuffer(String placeId) {
        return Observable.create(emitter -> {
            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
            FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                Log.e(TAG, "Place found: " + place.getName());
                emitter.onNext(place);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + exception.getMessage());

                }
                emitter.onError(new Throwable("MATCH_NOT_FOUND"));
            });
        });
    }

}
