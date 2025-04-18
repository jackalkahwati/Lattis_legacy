package com.lattis.ellipse.presentation.ui.home.fragment;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.Task;
import com.lattis.ellipse.Utils.FirebaseUtil;
import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.domain.interactor.bike.BikeDetailUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationSettingsUseCase;
import com.lattis.ellipse.domain.interactor.lock.disconnect.DisconnectAllLockUseCase;
import com.lattis.ellipse.domain.interactor.ride.DeleteRideUseCase;
import com.lattis.ellipse.domain.interactor.ride.GetRideUseCase;
import com.lattis.ellipse.domain.interactor.ride.RideSummaryUseCase;
import com.lattis.ellipse.domain.interactor.ride.SaveRideUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.user.GetCurrentUserStatusUseCase;
import com.lattis.ellipse.domain.interactor.user.GetLocalUserUseCase;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.dagger.qualifier.FleetId;
import com.lattis.ellipse.presentation.model.mapper.BikeModelMapper;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.fragment.FragmentPresenter;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;


public class HomeMapFragmentPresenter extends FragmentPresenter<HomeMapFragmentView> {

    private static final String TAG = HomeMapFragmentPresenter.class.getSimpleName();
    private final GetCurrentUserStatusUseCase getCurrentUserStatusUseCase;
    private final SaveRideUseCase saveRideUseCase;
    private final BikeDetailUseCase bikeDetailUseCase;
    private final RideSummaryUseCase rideSummaryUseCase;
    private final GetRideUseCase getRideUseCase;
    private final String fleetId;
    private User user;
    private AppUpdateManager appUpdateManager;
    private final BikeModelMapper bikeModelMapper;
    private DeleteRideUseCase deleteRideUseCase;
    private StopActiveTripUseCase stopActiveTripUseCase;
    private GetLocalUserUseCase getLocalUserUseCase;
    private GetLocationSettingsUseCase getLocationSettingsUseCase;
    private final DisconnectAllLockUseCase disconnectAllLockUseCase;
    private CompositeDisposable getCurrentStatusSubscription = new CompositeDisposable();
    private CompositeDisposable getLocationSettingsSubscription = new CompositeDisposable();
    private CompositeDisposable getRideSummarySubscription = new CompositeDisposable();
    private CompositeDisposable saveRideSubscription = new CompositeDisposable();
    private CompositeDisposable getBikeDetailsSubscription = new CompositeDisposable();
    private CompositeDisposable deleteRideSubscription = new CompositeDisposable();
    private CompositeDisposable disconnectedAllLocksSubscription = new CompositeDisposable();
    private CompositeDisposable getRideSubscription = new CompositeDisposable();


    @Inject
    HomeMapFragmentPresenter(GetCurrentUserStatusUseCase getCurrentUserStatusUseCase, SaveRideUseCase saveRideUseCase,
                             BikeDetailUseCase bikeDetailUseCase,
                             RideSummaryUseCase rideSummaryUseCase, @FleetId String fleetId, BikeModelMapper bikeModelMapper,
                             DeleteRideUseCase deleteRideUseCase,
                             StopActiveTripUseCase stopActiveTripUseCase,
                             DisconnectAllLockUseCase disconnectAllLockUseCase,
                             GetRideUseCase getRideUseCase,
                             GetLocalUserUseCase getLocalUserUseCase,
                             GetLocationSettingsUseCase getLocationSettingsUseCase,
                             AppUpdateManager appUpdateManager) {
        this.getCurrentUserStatusUseCase = getCurrentUserStatusUseCase;
        this.saveRideUseCase = saveRideUseCase;
        this.bikeDetailUseCase = bikeDetailUseCase;
        this.rideSummaryUseCase = rideSummaryUseCase;
        this.fleetId = fleetId;
        this.bikeModelMapper = bikeModelMapper;
        this.deleteRideUseCase = deleteRideUseCase;
        this.stopActiveTripUseCase = stopActiveTripUseCase;
        this.disconnectAllLockUseCase = disconnectAllLockUseCase;
        this.getRideUseCase = getRideUseCase;
        this.getLocalUserUseCase = getLocalUserUseCase;
        this.getLocationSettingsUseCase = getLocationSettingsUseCase;
        this.appUpdateManager = appUpdateManager;
    }

    public BikeModelMapper getBikeModelMapper() {
        return bikeModelMapper;
    }

    @Override
    protected void updateViewState() {
    }

    public void getCurrentUserStatus() {

        clearGetCurrentStatusSubscription();

        getCurrentStatusSubscription.add(getCurrentUserStatusUseCase
                .execute(new RxObserver<GetCurrentUserStatusResponse>(view) {
                    @Override
                    public void onNext(GetCurrentUserStatusResponse getCurrentUserStatusResponse) {
                        super.onNext(getCurrentUserStatusResponse);
                        clearGetCurrentStatusSubscription();
                        if(view!=null)
                            view.onGetCurrentUserStatusSuccess(getCurrentUserStatusResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        clearGetCurrentStatusSubscription();
                        if(e instanceof SocketTimeoutException || e instanceof UnknownHostException){
                            getRide();
                        }else{
                            if(view!=null)
                                view.onGetCurrentUserStatusFailure();
                        }


                    }
                }));
    }

    public void saveRide(Ride ride) {
        saveRideSubscription.add(saveRideUseCase
                .withRide(ride)
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);
                        if(view!=null)
                            view.onSaveRideSuccess(ride);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.onSaveRideFailure();
                    }
                }));
    }

    public void getBikeDetails(int bike_id) {
        getBikeDetailsSubscription.add(bikeDetailUseCase
                .withBikeId(bike_id)
                .withQRCodeId(-1)
                .execute(new RxObserver<Bike>(view) {
                    @Override
                    public void onNext(Bike bike) {
                        super.onNext(bike);
                        if(view!=null)
                            view.onBikeDetailsSuccess(bike);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.onBikeDetailsFailure();
                    }
                }));
    }

    public void getRideSummary(int trip_id) {
        getRideSummarySubscription.add(rideSummaryUseCase
                .withTripId(trip_id)
                .execute(new RxObserver<RideSummaryResponse>(view) {
                    @Override
                    public void onNext(RideSummaryResponse rideSummaryResponse) {
                        super.onNext(rideSummaryResponse);
                        if(view!=null)
                            view.onGetRideSummarySuccess(rideSummaryResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.onGetRideSummaryFailure();
                    }
                }));
    }

    public String getFleetId() {
        return fleetId;
    }


    public enum CurrentStatus {
        NO_BIKE_NO_TRIP,
        NO_TRIP_BUT_ACTIVE_BIKE_BOOKING,
        ACTIVE_TRIP,
        INVALID
    }


    public void deleteRide() {
        deleteRideSubscription.add(deleteRideUseCase
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        Log.e("HomeMapFragPresenter","deleteRide::onNext::"+status);
                        if(view!=null)
                            view.onRideDeleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e("HomeMapFragPresenter","deleteRide::onError::"+e.getLocalizedMessage());
                        if(view!=null)
                            view.onRideDeleted();
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

    public void clearGetCurrentStatusSubscription(){
        if(getCurrentStatusSubscription!=null){
            getCurrentStatusSubscription.clear();
        }
    }

    public void clearAllSubscriptions(){
        getCurrentStatusSubscription.clear();
        getLocationSettingsSubscription.clear();
        getRideSummarySubscription.clear();
        saveRideSubscription.clear();
        getBikeDetailsSubscription.clear();
        deleteRideSubscription.clear();
        disconnectedAllLocksSubscription.clear();
        getRideSubscription.clear();
    }


    public void disconnectAllLocks() {
        disconnectedAllLocksSubscription.add(disconnectAllLockUseCase
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                    }
                }));
    }

    public void getRide() {
        getRideSubscription.add(getRideUseCase
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);
                        Log.e(TAG,"getRide::"+ride.toString());
                        if(ride!=null && ride.getRideId()!=0 && view!=null){  // this will ensure that active trip
                            view.onRideSuccess(ride);
                        }else if(view!=null){
                            view.onRideFailure();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.onRideFailure();
                    }
                }));
    }

    public void getLocalUser(){
        subscriptions.add(getLocalUserUseCase.execute(new RxObserver<User>(view, false) {
            @Override
            public void onNext(User currUser) {
                if(currUser!=null){
                    user = currUser;
                    FirebaseUtil.getInstance().addUserIdAndEmail(currUser.getId(),currUser.getEmail());
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);

            }
        }));
    }

    public User getUser() {
        return user;
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

    public void checkForNewVersion(){

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Request the update.
                if(view!=null){
                    view.onAppUpdateAvailable(appUpdateManager,appUpdateInfo);
                }
            }else{
                if(view!=null){
                    view.onAppUpdateNotAvailable();
                }
            }
        });

        appUpdateInfoTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(view!=null){
                    view.onAppUpdateNotAvailable();
                }
            }
        });
    }


    public void checkIfNewVersionGettingInstalled(){
        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Request the update.
                if(view!=null){
                    view.onAppUpdateAvailable(appUpdateManager,appUpdateInfo);
                }
            }
        });
    }


}
