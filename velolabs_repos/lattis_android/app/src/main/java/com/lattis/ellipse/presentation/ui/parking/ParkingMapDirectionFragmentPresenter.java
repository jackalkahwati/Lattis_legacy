package com.lattis.ellipse.presentation.ui.parking;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.lattis.ellipse.data.network.model.response.lock.SignedMessagePublicKeyResponse;
import com.lattis.ellipse.data.platform.mapper.LocationToMapBoxMapper;
import com.lattis.ellipse.domain.interactor.bike.UpdateBikeMetaDatUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationSettingsUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.interactor.lock.SignedMessagePublicKey.SignedMessagePublicKeyUseCase;
import com.lattis.ellipse.domain.interactor.lock.connect.ConnectToLastLockedLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.connect.ConnectToLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.disconnect.DisconnectAllLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.firmware.GetLockFirmwareVersionCase;
import com.lattis.ellipse.domain.interactor.lock.observe.ObserveConnectionStateUseCase;
import com.lattis.ellipse.domain.interactor.lock.observe.ObserveHardwareStateUseCase;
import com.lattis.ellipse.domain.interactor.lock.observe.ObserveLockPositionUseCase;
import com.lattis.ellipse.domain.interactor.lock.realm.GetLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.realm.SaveLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.scanner.ScanForLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.setter.SetLockPositionUseCase;
import com.lattis.ellipse.domain.interactor.map.GetMapBoxRouteForNavigationUseCase;
import com.lattis.ellipse.domain.interactor.map.GetMapBoxRouteUseCase;
import com.lattis.ellipse.domain.interactor.ride.GetRideUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StartActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StartLocationTrackInActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopGetTripDetailsThreadIfApplicableUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopLocationTrackInActiveTripUseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.model.ScannedLock;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.model.mapper.LockModelMapper;
import com.lattis.ellipse.presentation.model.mapper.ScannedLockModelMapper;
import com.lattis.ellipse.presentation.setting.IntPref;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BluetoothFragmentPresenter;
import com.lattis.ellipse.presentation.ui.bike.BikeDirectionFragmentPresenter;
import com.lattis.ellipse.presentation.ui.ride.service.util.UpdateTripData;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import hugo.weaving.DebugLog;
import io.lattis.ellipse.sdk.exception.BluetoothException;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.lattis.ellipse.domain.model.Lock.Connection.Status.GUEST_VERIFIED;
import static com.lattis.ellipse.domain.model.Lock.Connection.Status.OWNER_VERIFIED;
import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_RIDE_COUNT;
import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_RIDE_WALK_THROUGH_STRING;

/**
 * Created by ssd3 on 3/28/17.
 */

public class ParkingMapDirectionFragmentPresenter extends BluetoothFragmentPresenter<ParkingMapDirectionFragmentView> {

    private final String TAG = ParkingMapDirectionFragmentPresenter.class.getName();
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private Location currentUserLocation;
    private Disposable locationSubscription;
    private IntPref rideCountPref;


    @Inject
    ParkingMapDirectionFragmentPresenter(GetLocationUpdatesUseCase getLocationUpdatesUseCase,
                                GetRideUseCase getRideUseCase,
                                ScanForLockUseCase scanForLockUseCase,
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
    protected void onLocationSettingsON() {
        super.onLocationSettingsON();
        getSignedMessagePublicKey();
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
