package com.lattis.ellipse.presentation.ui.bike;

import android.util.Log;

import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.data.network.model.response.lock.SignedMessagePublicKeyResponse;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.data.network.model.response.ride.UpdateTripResponse;
import com.lattis.ellipse.data.platform.mapper.LocationToMapBoxMapper;
import com.lattis.ellipse.domain.interactor.bike.BikeDetailUseCase;
import com.lattis.ellipse.domain.interactor.bike.CancelReserveBikeUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationSettingsUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.interactor.lock.SignedMessagePublicKey.SignedMessagePublicKeyUseCase;
import com.lattis.ellipse.domain.interactor.lock.connect.ConnectToLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.disconnect.DisconnectAllLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.getter.GetLockConnectionStatus;
import com.lattis.ellipse.domain.interactor.lock.observe.ObserveConnectionStateUseCase;
import com.lattis.ellipse.domain.interactor.lock.realm.DeleteLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.realm.SaveLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.setter.BlinkLedUseCase;
import com.lattis.ellipse.domain.interactor.map.GetMapBoxRouteForNavigationUseCase;
import com.lattis.ellipse.domain.interactor.map.GetMapBoxRouteMatcherUseCase;
import com.lattis.ellipse.domain.interactor.map.GetMapBoxRouteUseCase;
import com.lattis.ellipse.domain.interactor.ride.GetRideUseCase;
import com.lattis.ellipse.domain.interactor.ride.RideSummaryUseCase;
import com.lattis.ellipse.domain.interactor.ride.SaveRideUseCase;
import com.lattis.ellipse.domain.interactor.ride.StartRideUseCase;
import com.lattis.ellipse.domain.interactor.ride.UpdateRideUseCase;
import com.lattis.ellipse.domain.interactor.user.GetCurrentUserStatusUseCase;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.dagger.qualifier.FleetId;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.model.mapper.BikeModelMapper;
import com.lattis.ellipse.presentation.model.mapper.LockModelMapper;
import com.lattis.ellipse.presentation.model.mapper.ScannedLockModelMapper;
import com.lattis.ellipse.presentation.setting.IntPref;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.fragment.bluetooth.BluetoothFragmentPresenter;
import com.mapbox.api.directions.v5.models.DirectionsRoute;

import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;

import hugo.weaving.DebugLog;
import io.lattis.ellipse.BuildConfig;
import io.lattis.ellipse.sdk.exception.BluetoothException;
import io.reactivex.disposables.Disposable;
import retrofit2.adapter.rxjava2.HttpException;

import static com.lattis.ellipse.domain.model.Lock.Connection.Status.ACCESS_DENIED;
import static com.lattis.ellipse.domain.model.Lock.Connection.Status.DEVICE_FOUND;
import static com.lattis.ellipse.domain.model.Lock.Connection.Status.DISCONNECTED;
import static com.lattis.ellipse.domain.model.Lock.Connection.Status.GUEST_VERIFIED;
import static com.lattis.ellipse.domain.model.Lock.Connection.Status.OWNER_VERIFIED;
import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_RIDE_COUNT;


public class BikeDirectionFragmentPresenter extends BluetoothFragmentPresenter<BikeDirectionFragmentView> {
    private final String TAG = BikeDirectionFragmentPresenter.class.getName();

    private final LocationToMapBoxMapper locationToMapBoxMapper;
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private final ConnectToLockUseCase connectToLockUseCase;
    private final SignedMessagePublicKeyUseCase signedMessagePublicKeyUseCase;
    private final StartRideUseCase startRideUseCase;
    private final SaveLockUseCase saveLockUseCase;
    private final DeleteLockUseCase deleteLockUseCase;
    private final DisconnectAllLockUseCase disconnectAllLockUseCase;
    private final GetCurrentUserStatusUseCase getCurrentUserStatusUseCase;
    private final RideSummaryUseCase rideSummaryUseCase;
    private final BikeDetailUseCase bikeDetailUseCase;
    private final BikeModelMapper bikeModelMapper;
    private final SaveRideUseCase saveRideUseCase;
    private final UpdateRideUseCase updateRideUseCase;
    private final BlinkLedUseCase blinkLedUseCase;
    private final ObserveConnectionStateUseCase observeConnectionStateUseCase;
    private Location currentUserLocation;
    private Disposable locationSubscription;
    private Disposable connectionSubscription;
    private Disposable observeConnectionSubscription;
    private Disposable startScanSubscription;
    private CancelReserveBikeUseCase cancelReserveBikeUseCase;
    private GetLockConnectionStatus getLockConnectionStatus;
    private ScannedLockModelMapper scannedLockModelMapper;
    private LockModelMapper lockModelMapper;
    private ConnectionState connectionState;
    private GetMapBoxRouteMatcherUseCase getMapBoxRouteMatcherUseCase;
    private GetMapBoxRouteUseCase getMapBoxRouteUseCase;
    private GetMapBoxRouteForNavigationUseCase getMapBoxRouteForNavigationUseCase;
    private boolean isAccessDeniedForEllipse=false;
    private boolean isStartRideInProgress = false;
    private final String fleetId;
    private IntPref rideCountPref;
    private LockModel lockModel;
    private  boolean isRideStartingRequiredAfterLocationUpdate=false;
    private Bike bike;



    private boolean isRideStarted;

    public Bike getBike() {
        return bike;
    }

    public void setBike(Bike bike) {
        this.bike = bike;
    }

    public boolean isRideStarted() {
        return isRideStarted;
    }

    public void setRideStarted(boolean rideStarted) {
        isRideStarted = rideStarted;
    }


    public boolean isDisconnectRequiredForApp=false;

    //private Ride ride;

    @Inject
    BikeDirectionFragmentPresenter(LocationToMapBoxMapper locationToMapBoxMapper,
                                   StartRideUseCase startRideUseCase,
                                   GetLocationUpdatesUseCase getLocationUpdatesUseCase,
                                   ConnectToLockUseCase connectToLockUseCase,
                                   SignedMessagePublicKeyUseCase signedMessagePublicKeyUseCase,
                                   SaveLockUseCase saveLockUseCase,
                                   DisconnectAllLockUseCase disconnectAllLockUseCase,
                                   BlinkLedUseCase blinkLedUseCase,
                                   GetMapBoxRouteUseCase getMapBoxRouteUseCase,
                                   GetMapBoxRouteMatcherUseCase getMapBoxRouteMatcherUseCase,
                                   GetMapBoxRouteForNavigationUseCase getMapBoxRouteForNavigationUseCase,
                                   DeleteLockUseCase deleteLockUseCase,
                                   GetRideUseCase getRideUseCase,
                                   @FleetId String fleetId,
                                   GetCurrentUserStatusUseCase getCurrentUserStatusUseCase,
                                   RideSummaryUseCase rideSummaryUseCase,
                                   BikeDetailUseCase bikeDetailUseCase,
                                   BikeModelMapper bikeModelMapper,
                                   SaveRideUseCase saveRideUseCase,
                                   UpdateRideUseCase updateRideUseCase,
                                   CancelReserveBikeUseCase cancelReserveBikeUseCase,
                                   @Named(KEY_RIDE_COUNT) IntPref rideCountPref,
                                   GetLockConnectionStatus getLockConnectionStatus,
                                   ObserveConnectionStateUseCase observeConnectionStateUseCase,
                                   GetLocationSettingsUseCase getLocationSettingsUseCase,
                                   ScannedLockModelMapper scannedLockModelMapper,
                                   LockModelMapper lockModelMapper) {

        super(connectToLockUseCase,
                signedMessagePublicKeyUseCase,
                observeConnectionStateUseCase,
                getRideUseCase,
                scannedLockModelMapper,
                lockModelMapper,
                disconnectAllLockUseCase,
                getLocationSettingsUseCase
               );
        this.scannedLockModelMapper =scannedLockModelMapper;
        this.lockModelMapper = lockModelMapper;
        this.getLocationUpdatesUseCase = getLocationUpdatesUseCase;
        this.locationToMapBoxMapper = locationToMapBoxMapper;
        this.startRideUseCase = startRideUseCase;
        this.connectToLockUseCase = connectToLockUseCase;
        this.signedMessagePublicKeyUseCase = signedMessagePublicKeyUseCase;
        this.saveLockUseCase = saveLockUseCase;
        this.disconnectAllLockUseCase = disconnectAllLockUseCase;
        this.blinkLedUseCase = blinkLedUseCase;
        this.getMapBoxRouteMatcherUseCase = getMapBoxRouteMatcherUseCase;
        this.getMapBoxRouteUseCase = getMapBoxRouteUseCase;
        this.getMapBoxRouteForNavigationUseCase = getMapBoxRouteForNavigationUseCase;
        this.deleteLockUseCase = deleteLockUseCase;
        this.getCurrentUserStatusUseCase = getCurrentUserStatusUseCase;
        this.rideSummaryUseCase = rideSummaryUseCase;
        this.fleetId = fleetId;
        this.bikeDetailUseCase = bikeDetailUseCase;
        this.bikeModelMapper =bikeModelMapper;
        this.saveRideUseCase = saveRideUseCase;
        this.updateRideUseCase = updateRideUseCase;
        this.cancelReserveBikeUseCase = cancelReserveBikeUseCase;
        this.rideCountPref = rideCountPref;
        this.getLockConnectionStatus = getLockConnectionStatus;
        this.observeConnectionStateUseCase = observeConnectionStateUseCase;
    }

    public LocationToMapBoxMapper getLocationToMapBoxMapper() {
        return locationToMapBoxMapper;
    }

    public void setDisconnectRequiredForApp(boolean disconnectRequiredForApp) {
        isDisconnectRequiredForApp = disconnectRequiredForApp;
    }


    public void requestLocationUpdates() {
        requestStopLocationUpdates();
        subscriptions.add(locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {
            @Override
            public void onNext(Location location) {
                currentUserLocation = location;
                if(isRideStartingRequiredAfterLocationUpdate){
                    isRideStartingRequiredAfterLocationUpdate=false;
                    startRide();
                }
                view.setUserPosition(location);
            }
        }));
    }

    public void requestStopLocationUpdates() {
        if (locationSubscription != null)
            locationSubscription.dispose();
    }


    public void saveLock(LockModel lockModel) {
        subscriptions.add(saveLockUseCase
                .withLock(lockModelMapper.mapOut(lockModel))
                .execute(new RxObserver<Lock>(view) {
                    @Override
                    public void onNext(Lock lock) {
                        super.onNext(lock);
                        view.onSaveLockSuccess(lock);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onSaveLockFailure();
                    }
                }));
    }


    public void deleteLock() {
        subscriptions.add(deleteLockUseCase
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));
    }

    public void startRide() {

        if (currentUserLocation == null) {
            isRideStartingRequiredAfterLocationUpdate=true;
            requestLocationUpdates();
            return;
        }

        if(isStartRideInProgress){
            return;
        }

        Log.e(TAG, "Starting Ride@@@@@@@@@@@@@@");
        isStartRideInProgress=true;

        subscriptions.add(startRideUseCase
                .withBike(bike)
                .withLocation(currentUserLocation)
                .withFirstLockConnect(!isRideStarted)
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);
                        isStartRideInProgress=false;
                        if(BuildConfig.FLAVOR!="lattisDev"){
                            int currentRideCount = rideCountPref.getValue();
                            if(currentRideCount<5){
                                rideCountPref.setValue(++currentRideCount);
                            }
                        }
                        view.onStartRideSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        isStartRideInProgress=false;
                        view.onStartRideFail();
                    }
                }));
    }





    public void getSignedMessagePublicKey(Bike bike) {
        subscriptions.add(signedMessagePublicKeyUseCase
                .withBikeId(bike.getBike_id())
                .withMacId(bike.getMac_id())
                .execute(new RxObserver<SignedMessagePublicKeyResponse>(view) {
                    @Override
                    public void onNext(SignedMessagePublicKeyResponse signedMessagePublicKeyResponse) {
                        super.onNext(signedMessagePublicKeyResponse);

                        view.OnSignedMessagePublicKeySuccess(signedMessagePublicKeyResponse.getSignedMessagePublicKeyPayloadResponse().getSigned_message(), signedMessagePublicKeyResponse.getSignedMessagePublicKeyPayloadResponse().getPublic_key());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.OnSignedMessagePublicKeyFailure();

                    }
                }));
    }

    @DebugLog
    public void connectTo() {
        if(lockModel==null)
            return;
        cancelConnectionSubscription();
        isAccessDeniedForEllipse=false;
        connectionState = ConnectionState.NOT_CONNECTED;
        subscriptions.add(connectionSubscription= connectToLockUseCase.execute(lockModelMapper.mapOut(lockModel), new RxObserver<Lock.Connection.Status>(view, false) {
            @Override
            public void onStart() {
                super.onStart();
                connectionState = ConnectionState.CONNECTING;
            }

            @Override
            public void onComplete() {
                super.onComplete();
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                e.printStackTrace();
                if(e instanceof  BluetoothException){
                    if(((BluetoothException) e).getStatus()!=null) {
                        if (((BluetoothException) e).getStatus().equals(BluetoothException.Status.DEVICE_NOT_FOUND)) {
                            disconnectAllLocks(false);
                            Log.e(TAG, "BikeDirectionFragment::connect::onError::DEVICE_NOT_FOUND#####" );
                        } else if (((BluetoothException) e).getStatus().equals(BluetoothException.Status.BLUETOOTH_DISABLED)) {
                            view.requestEnableBluetooth();
                            Log.e(TAG, "ConnectionPool::connect::onError::BLUETOOTH_DISABLED#####" );
                        }
                    }
                }
            }

            @Override
            public void onNext(Lock.Connection.Status state) {
                if (state == OWNER_VERIFIED || state == GUEST_VERIFIED) {
                    super.onNext(state);
                    connectionState = ConnectionState.CONNECTED;
                    view.onLockConnected(lockModelMapper.mapIn(state.getLock()));
                } else if (state == DEVICE_FOUND) {
                    view.showConnecting();
                } else if (state == ACCESS_DENIED) {
                    super.onNext(state);
                    connectionState = ConnectionState.CONNECTION_FAIL;
                    isAccessDeniedForEllipse=true;
                    logCustomException(new Throwable("Acess denied: "+ lockModel.getMacId()));
                    view.onLockConnectionAccessDenied();
                }else if (state == DISCONNECTED) {
                    super.onNext(state);
                    connectionState = ConnectionState.DISCONNECTED;
                    if(!isAccessDeniedForEllipse){
                        view.onLockConnectionFailed();
                    }
                }
            }
        }));
    }

    public void cancelConnectionSubscription(){
        if(connectionSubscription!=null){
            connectionSubscription.dispose();
            connectionSubscription=null;
        }

        if(startScanSubscription!=null){
            startScanSubscription.dispose();
            startScanSubscription=null;
        }

        if(observeConnectionSubscription!=null){
            observeConnectionSubscription.dispose();
            observeConnectionSubscription=null;
        }

    }

    @Override
    protected void onBluetoothEnabled() {
        disconnectAllLocks();
    }

    @Override
    protected void onLocationSettingsON(){
        requestLocationUpdates();
        getSignedMessagePublicKey(bike);
    }

    @DebugLog
    public void disconnectAllLocks(boolean endRide) {
        subscriptions.add(disconnectAllLockUseCase
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        if(!isDisconnectRequiredForApp) {
                            connectTo();
                        }
                        else{
                            if(view!=null)
                                view.OnDisconnectSuccess(endRide);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        if (throwable instanceof BluetoothException) {
                            BluetoothException exception = (BluetoothException) throwable;
                            if (exception != null) {
                                if (exception.getStatus() != null) {
                                    if (exception.getStatus().equals(BluetoothException.Status.BLUETOOTH_DISABLED)) {
                                        if(view!=null)
                                            view.requestEnableBluetooth();
                                    }
                                }
                            }
                        } else {

                            if(!isDisconnectRequiredForApp) {
                                connectTo();
                            }
                            else {
                                if(view!=null)
                                    view.OnDisconnectFailure(endRide);
                            }
                        }
                    }
                }));
    }

    @DebugLog
    void blinkLed(LockModel lockModel) {
        subscriptions.add(blinkLedUseCase.withMacAddress(lockModel.getMacId())
                .execute(new RxObserver<>(view, false)));
    }


    @Override
    protected void updateViewState() {

    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public enum ConnectionState {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        SCANNING,
        CONNECTION_FAIL
    }


    public void getCurrentUserStatus() {
        Log.e(TAG,"getCurrentUserStatus");
        subscriptions.add(getCurrentUserStatusUseCase
                .execute(new RxObserver<GetCurrentUserStatusResponse>(view) {
                    @Override
                    public void onNext(GetCurrentUserStatusResponse getCurrentUserStatusResponse) {
                        super.onNext(getCurrentUserStatusResponse);
                        view.onGetCurrentUserStatusSuccess(getCurrentUserStatusResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e instanceof UnknownHostException) {
                        }else{
                            view.onGetCurrentUserStatusFailure();
                        }
                    }
                }));
    }

    public void getRideSummary(int trip_id) {
        subscriptions.add(rideSummaryUseCase
                .withTripId(trip_id)
                .execute(new RxObserver<RideSummaryResponse>(view) {
                    @Override
                    public void onNext(RideSummaryResponse rideSummaryResponse) {
                        super.onNext(rideSummaryResponse);
                        view.onGetRideSummarySuccess(rideSummaryResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onGetRideSummaryFailure();
                    }
                }));
    }
    public String getFleetId() {
        return fleetId;
    }

    public BikeModelMapper getBikeModelMapper() {
        return bikeModelMapper;
    }


    public void getBikeDetails(int bike_id) {
        subscriptions.add(bikeDetailUseCase
                .withBikeId(bike_id)
                .withQRCodeId(-1)
                .execute(new RxObserver<Bike>(view) {
                    @Override
                    public void onNext(Bike bike) {
                        super.onNext(bike);
                        if(bike!=null){
                            view.onBikeDetailsSuccess(bike);

                        }else{
                            view.onBikeDetailsFailure();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e instanceof HttpException) {
                            HttpException exception = (HttpException) e;
                            if (exception.code() == 401) {
                                view.onBikeUnAuthorised();
                            }else if (exception.code() == 404) {
                                view.onBikeNotFound();
                            }else if (exception.code() == 409) {
                                view.onBikeAlreadyRented();
                            }else{
                                view.onBikeDetailsFailure();
                            }
                        }else{
                            view.onBikeDetailsFailure();
                        }
                    }
                }));
    }


    public void saveRide(Ride ride) {
        subscriptions.add(saveRideUseCase
                .withRide(ride)
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);
                        view.onSaveRideSuccess(ride);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onSaveRideFailure();
                    }
                }));
    }

    @DebugLog
    public void updateTrip(Ride ride) {

        if(ride==null)
            return;

        subscriptions.add(updateRideUseCase
                .withTripId(ride.getRideId())
                .withSteps(getSteps())
                .execute(new RxObserver<UpdateTripResponse>() {
                    @Override
                    public void onNext(UpdateTripResponse updateTripResponse) {
                        super.onNext(updateTripResponse);
                        if(updateTripResponse!=null)
                            view.onTripDataSuccess(updateTripResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));
    }

    public void cancelBikeReservation(Bike bike, boolean isBikeDamage, boolean lockIssue) {
        subscriptions.add(cancelReserveBikeUseCase
                .withBikeId(bike.getBike_id())
                .withDamage(isBikeDamage)
                .withLockIssue(lockIssue)
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        view.onCancelBikeSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onCancelBikeFail();
                    }
                }));
    }


    double[][] getSteps() {
            return new double[][]{
                    new double[]{}
            };
    }


    public void setLockModel(LockModel lockModel){
        this.lockModel = lockModel;
    }

    public LockModel getLockModel() {
        return lockModel;
    }


    public void isLockConnected() {

        if(lockModel==null){
            view.onLockConnectionStatus(false);
            return;
        }
        subscriptions.add(getLockConnectionStatus
                .forLock(lockModelMapper.mapOut(lockModel))
                .execute(new RxObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean status) {
                        view.onLockConnectionStatus(status);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onLockConnectionStatus(false);
                    }
                }));
    }


    public void observeConnectionState() {
        if (observeConnectionSubscription != null)
            observeConnectionSubscription.dispose();
        subscriptions.add(observeConnectionSubscription = observeConnectionStateUseCase
                .forLock(lockModelMapper.mapOut(lockModel))
                .execute(new RxObserver<Lock.Connection.Status>() {
                    @Override
                    public void onNext(Lock.Connection.Status status) {
                        super.onNext(status);
                        if (status == Lock.Connection.Status.DISCONNECTED) {
                            view.onLockConnectionFailed();
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));
    }

    public void setRideStartingRequiredAfterLocationUpdate(boolean rideStartingRequiredAfterLocationUpdate) {
        isRideStartingRequiredAfterLocationUpdate = rideStartingRequiredAfterLocationUpdate;
    }


}
