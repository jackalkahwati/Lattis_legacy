package io.bikes.sandypedals.presentation.ui.ride;

import io.bikes.sandypedals.domain.interactor.bike.UpdateBikeMetaDatUseCase;
import io.bikes.sandypedals.domain.interactor.location.GetLocationSettingsUseCase;
import io.bikes.sandypedals.domain.interactor.location.GetLocationUpdatesUseCase;
import io.bikes.sandypedals.domain.interactor.lock.SignedMessagePublicKey.SignedMessagePublicKeyUseCase;
import io.bikes.sandypedals.domain.interactor.lock.connect.ConnectToLastLockedLockUseCase;
import io.bikes.sandypedals.domain.interactor.lock.connect.ConnectToLockUseCase;
import io.bikes.sandypedals.domain.interactor.lock.disconnect.DisconnectAllLockUseCase;
import io.bikes.sandypedals.domain.interactor.lock.firmware.GetLockFirmwareVersionCase;
import io.bikes.sandypedals.domain.interactor.lock.observe.ObserveConnectionStateUseCase;
import io.bikes.sandypedals.domain.interactor.lock.observe.ObserveHardwareStateUseCase;
import io.bikes.sandypedals.domain.interactor.lock.observe.ObserveLockPositionUseCase;
import io.bikes.sandypedals.domain.interactor.lock.realm.GetLockUseCase;
import io.bikes.sandypedals.domain.interactor.lock.setter.SetLockPositionUseCase;
import io.bikes.sandypedals.domain.interactor.ride.GetRideUseCase;
import io.bikes.sandypedals.domain.interactor.updatetrip.StartActiveTripUseCase;
import io.bikes.sandypedals.domain.interactor.updatetrip.StartLocationTrackInActiveTripUseCase;
import io.bikes.sandypedals.domain.interactor.updatetrip.StopActiveTripUseCase;
import io.bikes.sandypedals.domain.interactor.updatetrip.StopGetTripDetailsThreadIfApplicableUseCase;
import io.bikes.sandypedals.domain.interactor.updatetrip.StopLocationTrackInActiveTripUseCase;
import io.bikes.sandypedals.domain.model.Location;
import io.bikes.sandypedals.presentation.model.mapper.LockModelMapper;
import io.bikes.sandypedals.presentation.model.mapper.ScannedLockModelMapper;
import io.bikes.sandypedals.presentation.setting.IntPref;
import io.bikes.sandypedals.presentation.ui.base.RxObserver;
import io.bikes.sandypedals.presentation.ui.base.fragment.bluetooth.BluetoothFragmentPresenter;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.disposables.Disposable;

import static io.bikes.sandypedals.presentation.dagger.module.SettingModule.KEY_RIDE_COUNT;
import static io.bikes.sandypedals.presentation.dagger.module.SettingModule.KEY_RIDE_WALK_THROUGH_STRING;

/**
 * Created by ssd3 on 3/30/17.
 */

public class ActiveRideFragmentPresenter extends BluetoothFragmentPresenter<ActiveRideFragmentView> {

    private final String TAG = ActiveRideFragmentPresenter.class.getName();
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private Location currentUserLocation;
    private Disposable locationSubscription;
    private IntPref rideCountPref;


    @Inject
    ActiveRideFragmentPresenter(GetLocationUpdatesUseCase getLocationUpdatesUseCase,
                                GetRideUseCase getRideUseCase,
                                ConnectToLockUseCase connectToLockUseCase,
                                ConnectToLastLockedLockUseCase connectToLastLockedLockUseCase,
                                SignedMessagePublicKeyUseCase signedMessagePublicKeyUseCase,
                                SetLockPositionUseCase setLockPositionUseCase,
                                ObserveLockPositionUseCase observeLockPositionUseCase,
                                ObserveConnectionStateUseCase observeConnectionStateUseCase,
                                DisconnectAllLockUseCase disconnectAllLockUseCase,
                                @Named(KEY_RIDE_WALK_THROUGH_STRING) IntPref rideTimeWalkThroughPref,
                                StartActiveTripUseCase startActiveTripUseCase,
                                StopActiveTripUseCase stopActiveTripUseCase,
                                ObserveHardwareStateUseCase observeHardwareStateUseCase,
                                UpdateBikeMetaDatUseCase updateBikeMetaDatUseCase,
                                GetLockFirmwareVersionCase getLockFirmwareVersionCase,
                                GetLockUseCase getLockUseCase,
                                @Named(KEY_RIDE_COUNT) IntPref rideCountPref,
                                GetLocationSettingsUseCase getLocationSettingsUseCase,
                                StartLocationTrackInActiveTripUseCase startLocationTrackInActiveTripUseCase,
                                StopLocationTrackInActiveTripUseCase stopLocationTrackInActiveTripUseCase,
                                StopGetTripDetailsThreadIfApplicableUseCase stopGetTripDetailsThreadIfApplicableUseCase,
                                ScannedLockModelMapper scannedLockModelMapper,
                                LockModelMapper lockModelMapper
                                ) {

        super(connectToLockUseCase,
                connectToLastLockedLockUseCase,
                signedMessagePublicKeyUseCase,
                setLockPositionUseCase,
                observeLockPositionUseCase,
                observeConnectionStateUseCase,
                observeHardwareStateUseCase,
                getRideUseCase,
                rideTimeWalkThroughPref,
                scannedLockModelMapper,
                lockModelMapper,
                getLockUseCase,
                disconnectAllLockUseCase,
                getLockFirmwareVersionCase,
                updateBikeMetaDatUseCase,
                startActiveTripUseCase,
                startLocationTrackInActiveTripUseCase,
                stopLocationTrackInActiveTripUseCase,
                stopActiveTripUseCase,
                stopGetTripDetailsThreadIfApplicableUseCase,
                getLocationSettingsUseCase);

        this.getLocationUpdatesUseCase = getLocationUpdatesUseCase;
        this.rideCountPref = rideCountPref;
    }


    @Override
    protected void updateViewState() {
        super.updateViewState();
    }

    public Location getCurrentUserLocation() {
        return currentUserLocation;
    }

    public void requestLocationUpdates() {
        requestStopLocationUpdates();
        locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {
            @Override
            public void onNext(Location location) {
                currentUserLocation = location;
                if(view!=null)
                    view.setUserPosition(location);
            }
        });
    }
    public void requestStopLocationUpdates() {
        if (locationSubscription != null)
            locationSubscription.dispose();
    }



    @Override
    protected void onBluetoothEnabled() {
        super.onBluetoothEnabled();
        requestLocationUpdates();
    }





    public void unsubscribeAllSubscription() {
        super.unsubscribeAllSubscription();

    }

    public boolean doesToolTipForUnlockNeedsToBeShown() {
        return rideCountPref.getValue()<=3?true:false;
    }


}
