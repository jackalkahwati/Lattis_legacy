package com.lattis.ellipse.presentation.ui.ride;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.lattis.ellipse.data.network.model.response.uploadImage.UploadImageResponse;
import com.lattis.ellipse.domain.interactor.location.GetLocationSettingsUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.interactor.lock.disconnect.DisconnectAllLockUseCase;
import com.lattis.ellipse.domain.interactor.ride.EndRideUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.uploadImage.UploadImageUseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.adapter.rxjava2.HttpException;

import static com.lattis.ellipse.presentation.ui.damagebike.DamageReportSuccessActivity.BIKE_DAMAGE;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.END_RIDE_ID_LOADING_STRING;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FORCE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LATITUDE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LOCK_BATTERY;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LONGITUDE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.PARKING_END_RIDE_ID;

/**
 * Created by ssd3 on 4/4/17.
 */

public class EndRideCheckListActivityPresenter extends ActivityPresenter<EndRideCheckListActivityView> {

    private final String TAG = EndRideCheckListActivityPresenter.class.getName();
    boolean checkListStatus;
    private EndRideUseCase endRideUseCase;
    private UploadImageUseCase uploadImageUseCase;
    private DisconnectAllLockUseCase disconnectAllLockUseCase;
    private final StopActiveTripUseCase stopActiveTripUseCase;
    private Disposable locationSubscription;
    private int trip_id;
    private Location currentLocation;
    private int parkingId = -1;
    private GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private boolean isDamageBike = false;
    private String loadingString=null;
    private Integer lock_battery=null;
    private Disposable uploadImageSubscription;
    private Disposable endRideSubscription;
    private CompositeDisposable getLocationSettingsSubscription = new CompositeDisposable();
    private GetLocationSettingsUseCase getLocationSettingsUseCase;

    private boolean forceEndRide =false;
    private String imageURL=null;


    @Inject
    public EndRideCheckListActivityPresenter(GetLocationUpdatesUseCase getLocationUpdatesUseCase,
                                             EndRideUseCase endRideUseCase,
                                             UploadImageUseCase uploadImageUseCase,
                                             DisconnectAllLockUseCase disconnectAllLockUseCase,
                                             StopActiveTripUseCase stopActiveTripUseCase,
                                             GetLocationSettingsUseCase getLocationSettingsUseCase) {

        this.endRideUseCase = endRideUseCase;
        this.uploadImageUseCase = uploadImageUseCase;
        this.disconnectAllLockUseCase = disconnectAllLockUseCase;
        this.getLocationUpdatesUseCase = getLocationUpdatesUseCase;
        this.stopActiveTripUseCase = stopActiveTripUseCase;
        this.getLocationSettingsUseCase = getLocationSettingsUseCase;
    }


    public boolean isForceEndRide() {
        return forceEndRide;
    }

    public void setForceEndRide(){
        forceEndRide = true;
    }


    @Override
    protected void updateViewState() {
        if(view!=null){
            view.checkForLocation();
        }
    }

    public void requestLocationUpdates() {
        subscriptions.add(locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {
            @Override
            public void onNext(Location location) {
                currentLocation=location;
                if(view!=null) {
                    view.setUserPosition(location);
                }
            }
        }));
    }

    public void requestStopLocationUpdates() {
        if (locationSubscription != null)
            locationSubscription.dispose();
    }


    @Override
    public void saveArguments(@NonNull Bundle arguments) {
        super.saveArguments(arguments);
        if (currentLocation != null) {
            arguments.putDouble(LATITUDE_END_RIDE_ID, currentLocation.getLatitude());
            arguments.putDouble(LONGITUDE_END_RIDE_ID, currentLocation.getLongitude());
            arguments.putInt(EndRideCheckListActivity.TRIP_ID, trip_id);
            arguments.putInt(EndRideCheckListActivity.PARKING_END_RIDE_ID, parkingId);
        }
    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null) {
            trip_id = arguments.getInt(EndRideCheckListActivity.TRIP_ID);
            isDamageBike = arguments.getBoolean(BIKE_DAMAGE);
            if (arguments.containsKey(LATITUDE_END_RIDE_ID) && arguments.containsKey(LONGITUDE_END_RIDE_ID)) {
                double latitude = arguments.getDouble(LATITUDE_END_RIDE_ID);
                double longitude = arguments.getDouble(LONGITUDE_END_RIDE_ID);
                currentLocation = new Location(latitude, longitude);
            }
            if (arguments.containsKey(PARKING_END_RIDE_ID)) {
                parkingId = arguments.getInt(EndRideCheckListActivity.PARKING_END_RIDE_ID);
            } else {
                parkingId = -1;
            }

            if (arguments.containsKey(LOCK_BATTERY)) {
                lock_battery = arguments.getInt(EndRideCheckListActivity.LOCK_BATTERY);
            }


            if (arguments.containsKey(FORCE_END_RIDE_ID)) {
                forceEndRide = arguments.getBoolean(FORCE_END_RIDE_ID);
                Log.e("EndRideCheckListPre","setup::"+forceEndRide);
            }else{
                Log.e("EndRideCheckListPre","setup::"+forceEndRide);
            }


            if (arguments.containsKey(END_RIDE_ID_LOADING_STRING)) {
                loadingString = arguments.getString(END_RIDE_ID_LOADING_STRING);
            }

        }
    }

    @DebugLog
    public void endRide() {
        Log.e("EndRideCheckListPre","endRide");
        if (currentLocation != null) {
            Log.e("EndRideCheckListPre","endRide::currentLocation not NULL");
            endRideSubscription = endRideUseCase
                    .withTripId(trip_id)
                    .withLocation(currentLocation)
                    .withImageURL(imageURL)
                    .withParkingId(parkingId)
                    .withReportDamage(isDamageBike)
                    .withLockBattery(lock_battery)
                    .execute(new RxObserver<Boolean>(view) {
                        @Override
                        public void onNext(Boolean status) {
                            super.onNext(status);

                            if(view!=null)
                                view.onEndTripSuccess();
                            Log.e("EndRideCheckListPre","endRide::onNext::success");
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            Log.e("EndRideCheckListPre","endRide::onError::"+e.getLocalizedMessage());

                            if (e instanceof HttpException && view!=null) {
                                HttpException exception = (HttpException) e;
                                if (exception.code() == 409) {
                                    view.onEndTripPaymentFailure();
                                }else if (exception.code() == 411) {
                                    view.onEndTripStripeConnectFailure();
                                }else{
                                    if(view!=null)
                                        view.onEndTripFailure();
                                }
                            }else{
                                if(view!=null)
                                    view.onEndTripFailure();
                            }


                        }
                    });
        } else {
            Log.e("EndRideCheckListPre","endRide::currentLocation NULL");
            requestLocationUpdates();
        }
    }

    public void uploadImage(String filePath) {
        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("parking", ""+new Date().getTime(), requestFile);
        uploadImageSubscription = uploadImageUseCase.withFile(body)
                .withUploadType("parking")
                .execute(new RxObserver<UploadImageResponse>() {
                    @Override
                    public void onNext(UploadImageResponse uploadImageResponse) {
                        super.onNext(uploadImageResponse);
                        imageURL = uploadImageResponse.uploadedUrl();
                        if(view!=null)
                            view.onUploadImageSuccess(uploadImageResponse.uploadedUrl());

                    }

                    @Override
                    public void onError(Throwable e) {
                        if(view!=null)
                            view.onUploadImageFailure();
                    }
                });
    }

    public void cancelAllSubscription(){

        requestStopLocationUpdates();


        if(uploadImageSubscription!=null){
            uploadImageSubscription.dispose();
            uploadImageSubscription=null;
        }


        if(endRideSubscription!=null){
            endRideSubscription.dispose();
            endRideSubscription=null;
        }
    }


    public void disconnectAllLocks() {
        subscriptions.add(disconnectAllLockUseCase
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        view.onLockDisconnectionSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onLockDisconnectionFail();
                    }
                }));
    }

    public void stopUpdateTripService() {
        subscriptions.add(stopActiveTripUseCase

                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean aVoid) {
                        super.onNext(aVoid);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));
    }

    public void setUserLocation(Location location) {
        this.currentLocation = location;
    }


    public void getLocationSetting(){
        cancelLocationSettingsSubscription();
        getLocationSettingsSubscription.add(getLocationSettingsUseCase
                .execute(new RxObserver<LocationSettingsResult>(view) {
                    @Override
                    public void onNext(LocationSettingsResult locationSettingsResult) {
                        super.onNext(locationSettingsResult);
                        cancelLocationSettingsSubscription();

                        switch (locationSettingsResult.getStatus()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied. The client can
                                // initialize location requests here.
                                if(view!=null)
                                    view.onLocationSettingsON();
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog.
                                if(view!=null)
                                    view.onLocationSettingsPermissionRequired(locationSettingsResult);
                                break;

                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way
                                // to fix the settings so we won't show the dialog.
                                // ...
                                if(view!=null)
                                    view.onLocationSettingsNotAvailable();
                                break;

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.onLocationSettingsNotAvailable();
                    }
                }));
    }

    private void cancelLocationSettingsSubscription(){
        if(getLocationSettingsSubscription!=null){
            getLocationSettingsSubscription.clear();
        }
    }

    public String getLoadingString() {
        return loadingString;
    }
}
