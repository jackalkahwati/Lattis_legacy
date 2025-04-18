package com.lattis.ellipse.presentation.ui.parking;

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
import com.lattis.ellipse.domain.interactor.lock.setter.SetLockPositionUseCase;
import com.lattis.ellipse.domain.interactor.parking.GetParkingZoneUseCase;
import com.lattis.ellipse.domain.interactor.parking.GetParkingsForFleetUseCase;
import com.lattis.ellipse.domain.interactor.ride.GetRideUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StartActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StartLocationTrackInActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopGetTripDetailsThreadIfApplicableUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopLocationTrackInActiveTripUseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.model.mapper.LockModelMapper;
import com.lattis.ellipse.presentation.model.mapper.ScannedLockModelMapper;
import com.lattis.ellipse.presentation.setting.IntPref;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.fragment.FragmentPresenter;
import com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BluetoothFragmentPresenter;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.disposables.Disposable;

import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_RIDE_COUNT;
import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_RIDE_WALK_THROUGH_STRING;

/**
 * Created by ssd3 on 3/27/17.
 */

public class FindParkingFragmentPresenter extends BluetoothFragmentPresenter<FindParkingFragmentView> {


    private final String TAG = FindParkingFragmentPresenter.class.getName();
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private Location currentUserLocation;
    private GetParkingZoneUseCase getParkingZoneUseCase;
    private GetParkingsForFleetUseCase getParkingsForFleetUseCase;
    private GetRideUseCase getRideUseCase;
    private Disposable locationSubscription = null;
    private float rangeInMiles = 1000;
    private IntPref rideCountPref;

    @Inject
    FindParkingFragmentPresenter(GetLocationUpdatesUseCase getLocationUpdatesUseCase,
                                 GetParkingZoneUseCase getParkingZoneUseCase,
                                 GetParkingsForFleetUseCase getParkingsForFleetUseCase,
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
                                 LockModelMapper lockModelMapper) {


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
        this.getParkingZoneUseCase = getParkingZoneUseCase;
        this.getParkingsForFleetUseCase = getParkingsForFleetUseCase;
        this.getRideUseCase = getRideUseCase;
        this.rideCountPref = rideCountPref;
    }

    @Override
    protected void updateViewState() {
    }


    public Location getCurrentUserLocation() {
        return currentUserLocation;
    }

    public void setCurrentUserLocation(Location currentUserLocation) {
        this.currentUserLocation = currentUserLocation;
    }

    public void findParkingsFromFleetId(int fleetId) {
        subscriptions.add(getParkingsForFleetUseCase
                .withFleetId(fleetId)
                .execute(new RxObserver<List<Parking>>() {
                    @Override
                    public void onNext(List<Parking> parkings) {
                        if (parkings != null) {
                            view.onFindingParkingSuccess(parkings);
                        }else{
                            view.onFindingParkingFailure();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onFindingParkingFailure();
                    }
                }));
    }

    public void getParkingZone(int fleetId) {

        subscriptions.add(getParkingZoneUseCase
                .withFleetID(fleetId)
                .execute(new RxObserver<List<ParkingZone>>() {
                    @Override
                    public void onNext(List<ParkingZone> parkingZone) {
                        if(parkingZone!=null){
                            view.onFindingZoneSuccess(parkingZone);
                        }
                        findParkingsFromFleetId(fleetId);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        findParkingsFromFleetId(fleetId);
                    }
                }));

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
        if (locationSubscription != null){
            locationSubscription.dispose();
            locationSubscription =null;
        }

    }

    public boolean doesToolTipForUnlockNeedsToBeShown() {
        return rideCountPref.getValue()<=3?true:false;
    }

}
