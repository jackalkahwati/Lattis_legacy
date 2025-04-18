package com.lattis.ellipse.presentation.ui.ride.fee;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.lattis.ellipse.data.network.model.response.parking.GetParkingFeeForFleetDataResponse;
import com.lattis.ellipse.data.network.model.response.parking.GetParkingFeeForFleetResponse;
import com.lattis.ellipse.domain.interactor.location.GetLocationSettingsUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.interactor.parking.GetParkingFeeForFleetUseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.ACCURACY_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FLEET_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.FLEET_TYPE;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.HAS_ACCURACY_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LATITUDE_END_RIDE_ID;
import static com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity.LONGITUDE_END_RIDE_ID;

/**
 * Created by ssd3 on 8/1/17.
 */

public class ParkingFeeActivityPresenter extends ActivityPresenter<ParkingFeeActivityView> {
    private final GetParkingFeeForFleetUseCase getParkingFeeForFleetUseCase;
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private GetParkingFeeForFleetDataResponse getParkingFeeForFleetDataResponse;
    private static String ARGS_GET_PARKING_DATA = "ARGS_GET_PARKING_DATA";
    private Location currentLocation;
    private int fleet_id;
    private Disposable locationSubscription;
    private String fleet_type;
    private GetLocationSettingsUseCase getLocationSettingsUseCase;
    private CompositeDisposable getLocationSettingsSubscription = new CompositeDisposable();





    public final static String ARGS_END_RIDE = "END_RIDE";
    public final static boolean ARGS_END_RIDE_VALUE = true;

    public final static String ARGS_FIND_ZONES = "FIND_ZONES";
    public final static boolean ARGS_FIND_ZONES_VALUE = true;


    @Inject
    public ParkingFeeActivityPresenter(GetParkingFeeForFleetUseCase getParkingFeeForFleetUseCase,
                                       GetLocationUpdatesUseCase getLocationUpdatesUseCase,
                                       GetLocationSettingsUseCase getLocationSettingsUseCase){
        this.getParkingFeeForFleetUseCase = getParkingFeeForFleetUseCase;
        this.getLocationUpdatesUseCase = getLocationUpdatesUseCase;
        this.getLocationSettingsUseCase = getLocationSettingsUseCase;
    }

    @Override
    protected void updateViewState() {
        if(currentLocation==null){
            requestLocationUpdates();
        }else if(fleet_id==-1){
            view.showServerErrorUI();
        }else{
            if(view!=null){
                view.checkForLocation();
            }
        }
    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null) {
            if (arguments.containsKey(LATITUDE_END_RIDE_ID) && arguments.containsKey(LONGITUDE_END_RIDE_ID)) {
                double latitude = arguments.getDouble(LATITUDE_END_RIDE_ID);
                double longitude = arguments.getDouble(LONGITUDE_END_RIDE_ID);
                currentLocation = new Location(latitude, longitude);
            }

            if (arguments.containsKey(HAS_ACCURACY_END_RIDE_ID) && arguments.containsKey(ACCURACY_END_RIDE_ID)) {
                if(currentLocation!=null) {
                    currentLocation.setHasAccuracy(arguments.getBoolean(HAS_ACCURACY_END_RIDE_ID));
                    currentLocation.setAccuracy(arguments.getFloat(ACCURACY_END_RIDE_ID));
                }
            }

            if (arguments.containsKey(FLEET_ID)) {
                fleet_id = arguments.getInt(FLEET_ID);
            } else {
                fleet_id = -1;
            }

            if (arguments.containsKey(FLEET_TYPE)) {
                fleet_type = arguments.getString(FLEET_TYPE);
            }
        }
    }


    public void getParkingFee() {
        if (currentLocation != null) {
            subscriptions.add(getParkingFeeForFleetUseCase
                    .withFleetId(fleet_id)
                    .withLocation(currentLocation)
                    .execute(new RxObserver<GetParkingFeeForFleetResponse>(view) {
                        @Override
                        public void onNext(GetParkingFeeForFleetResponse getParkingFeeForFleetResponse) {
                            super.onNext(getParkingFeeForFleetResponse);
                            view.hideOperationLoading();

                            if(getParkingFeeForFleetResponse!=null){
                                getParkingFeeForFleetDataResponse = getParkingFeeForFleetResponse.GetParkingFeeForFleetData();
                            }
                            showAppropiateUI();
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            view.hideOperationLoading();
                            view.showServerErrorUI();
                        }
                    }));
        } else {
            requestLocationUpdates();
        }
    }


    private void showAppropiateUI(){

        if(getParkingFeeForFleetDataResponse==null){
            view.showServerErrorUI();
        }else if(getParkingFeeForFleetDataResponse.isNot_allowed()){
            view.showRestrictedParkingUI();
        }else if(getParkingFeeForFleetDataResponse.isNot_allowed()==false){

            if(getParkingFeeForFleetDataResponse.getFee()>0){
                view.showOutOfParkingZoneUI(getParkingFeeForFleetDataResponse.getFee(),getParkingFeeForFleetDataResponse.getCurrency());
            }else if(getParkingFeeForFleetDataResponse.isOutside()){
                view.showDisplinaryActionForZoneUI();
            }else{
                view.showNormalEndRide();
            }

        }
    }

    public void requestLocationUpdates() {
        subscriptions.add(locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {
            @Override
            public void onNext(Location location) {
                requestStopLocationUpdates();
                currentLocation = location;
                getParkingFee();
            }
        }));
    }

    public void requestStopLocationUpdates() {
        if (locationSubscription != null)
            locationSubscription.dispose();
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


}
