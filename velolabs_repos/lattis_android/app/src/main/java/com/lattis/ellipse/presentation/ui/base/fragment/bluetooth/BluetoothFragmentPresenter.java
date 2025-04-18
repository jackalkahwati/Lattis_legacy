package com.lattis.ellipse.presentation.ui.base.fragment.bluetooth;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.lattis.ellipse.data.network.model.response.lock.SignedMessagePublicKeyResponse;
import com.lattis.ellipse.domain.interactor.bike.UpdateBikeMetaDatUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationSettingsUseCase;
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
import com.lattis.ellipse.domain.interactor.ride.GetRideUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StartActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StartLocationTrackInActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopGetTripDetailsThreadIfApplicableUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopLocationTrackInActiveTripUseCase;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.model.mapper.LockModelMapper;
import com.lattis.ellipse.presentation.model.mapper.ScannedLockModelMapper;
import com.lattis.ellipse.presentation.setting.IntPref;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.fragment.FragmentPresenter;
import com.lattis.ellipse.presentation.ui.bike.BikeDirectionFragmentPresenter;
import com.lattis.ellipse.presentation.ui.ride.service.util.UpdateTripData;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import hugo.weaving.DebugLog;
import io.lattis.ellipse.sdk.exception.BluetoothException;
import io.lattis.ellipse.sdk.exception.ConnectionException;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.lattis.ellipse.domain.model.Lock.Connection.Status.GUEST_VERIFIED;
import static com.lattis.ellipse.domain.model.Lock.Connection.Status.OWNER_VERIFIED;

public abstract class BluetoothFragmentPresenter<View extends BluetoothFragmentView> extends FragmentPresenter<View> {
    private final String TAG = BluetoothFragmentPresenter.class.getName();

    private CompositeDisposable getLocationSettingsSubscription = new CompositeDisposable();
    private Disposable connectToLastSubscription = null;
    private Disposable connectToSubscription = null;
    private Disposable positionSubscription = null;
    private Disposable connectionSubscription = null;
    private Disposable hardwareSubscription = null;
    private Disposable setPositionTimerSubscription =null;
    private Disposable setPositionSubscription = null;
    private Disposable getFirmwareVersionSubscription = null;
    private Disposable getFirmwareTimerSubscription =null;
    private Disposable startLocationTrackInActiveTripSubscription = null;
    private Disposable stopLocationTrackInActiveTripSubscription = null;
    private Disposable startActiveTripSubscription = null;
    private Disposable stopActiveTripSubscription = null;

    private ScannedLockModelMapper scannedLockModelMapper;
    private LockModelMapper lockModelMapper;

    private Ride ride = null;
    private IntPref rideTimeWalkThroughPref;
    private LockModel lockModel;
    private boolean isAccessDeniedForEllipse=false;
    private BikeDirectionFragmentPresenter.ConnectionState connectionState;
    private LockModel connectedLock;
    private Lock.Hardware.Position lastPositionRequested;
    private boolean setPositionRequest=true;
    private int setPositionRetry =0;
    private int MAX_SET_POSITION_RETRY = 4;
    private Boolean shackleJam=false;
    private Boolean isJammingUpdated=false;
    private boolean isBikeMetaDataUpdated=false;
    private Integer lock_battery=null;
    private String firmwareVersion=null;
    private static final int MILLISECONDS_FOR_SET_POSITION_TIMER = 2000;
    private static final int MILLISECONDS_FOR_FIRMWARE_TIMER = 10000;

    private ConnectToLockUseCase connectToLockUseCase;
    private ConnectToLastLockedLockUseCase connectToLastLockedLockUseCase;
    private SignedMessagePublicKeyUseCase signedMessagePublicKeyUseCase;
    private SetLockPositionUseCase setLockPositionUseCase;
    private ObserveLockPositionUseCase observeLockPositionUseCase;
    private ObserveConnectionStateUseCase observeConnectionStateUseCase;
    private ObserveHardwareStateUseCase observeHardwareStateUseCase;
    private GetRideUseCase getRideUseCase;
    private GetLockUseCase getLockUseCase;
    private DisconnectAllLockUseCase disconnectAllLockUseCase;
    private GetLockFirmwareVersionCase getLockFirmwareVersionUseCase;
    private UpdateBikeMetaDatUseCase updateBikeMetaDatUseCase;
    private StartActiveTripUseCase startActiveTripUseCase;
    private StartLocationTrackInActiveTripUseCase startLocationTrackInActiveTripUseCase;
    private StopLocationTrackInActiveTripUseCase stopLocationTrackInActiveTripUseCase;
    private StopActiveTripUseCase stopActiveTripUseCase;
    private StopGetTripDetailsThreadIfApplicableUseCase stopGetTripDetailsThreadIfApplicableUseCase;
    private GetLocationSettingsUseCase getLocationSettingsUseCase;


    public BluetoothFragmentPresenter(ConnectToLockUseCase connectToLockUseCase,
                                      SignedMessagePublicKeyUseCase signedMessagePublicKeyUseCase,
                                      ObserveConnectionStateUseCase observeConnectionStateUseCase,
                                      GetRideUseCase getRideUseCase,
                                      ScannedLockModelMapper scannedLockModelMapper,
                                      LockModelMapper lockModelMapper,
                                      DisconnectAllLockUseCase disconnectAllLockUseCase,
                                      GetLocationSettingsUseCase getLocationSettingsUseCase
               ){
        this.connectToLockUseCase = connectToLockUseCase;
        this.signedMessagePublicKeyUseCase = signedMessagePublicKeyUseCase;
        this.observeConnectionStateUseCase = observeConnectionStateUseCase;
        this.getRideUseCase = getRideUseCase;
        this.scannedLockModelMapper = scannedLockModelMapper;
        this.lockModelMapper = lockModelMapper;
        this.disconnectAllLockUseCase = disconnectAllLockUseCase;
        this.getLocationSettingsUseCase = getLocationSettingsUseCase;
    }

    public BluetoothFragmentPresenter(ConnectToLockUseCase connectToLockUseCase,
                               ConnectToLastLockedLockUseCase connectToLastLockedLockUseCase,
                               SignedMessagePublicKeyUseCase signedMessagePublicKeyUseCase,
                               SetLockPositionUseCase setLockPositionUseCase,
                               ObserveLockPositionUseCase observeLockPositionUseCase,
                               ObserveConnectionStateUseCase observeConnectionStateUseCase,
                               ObserveHardwareStateUseCase observeHardwareStateUseCase,
                                      GetRideUseCase getRideUseCase,
                                      IntPref rideTimeWalkThroughPref,
                                      ScannedLockModelMapper scannedLockModelMapper,
                                      LockModelMapper lockModelMapper,
                                      GetLockUseCase getLockUseCase,
                                      DisconnectAllLockUseCase disconnectAllLockUseCase,
                                      GetLockFirmwareVersionCase getLockFirmwareVersionUseCase,
                                      UpdateBikeMetaDatUseCase updateBikeMetaDatUseCase,
                                      StartActiveTripUseCase startActiveTripUseCase,
                                      StartLocationTrackInActiveTripUseCase startLocationTrackInActiveTripUseCase,
                                      StopLocationTrackInActiveTripUseCase stopLocationTrackInActiveTripUseCase,
                                      StopActiveTripUseCase stopActiveTripUseCase,
                                      StopGetTripDetailsThreadIfApplicableUseCase stopGetTripDetailsThreadIfApplicableUseCase,
                                      GetLocationSettingsUseCase getLocationSettingsUseCase

    ){
        this.connectToLockUseCase = connectToLockUseCase;
        this.connectToLastLockedLockUseCase = connectToLastLockedLockUseCase;
        this.signedMessagePublicKeyUseCase = signedMessagePublicKeyUseCase;
        this.setLockPositionUseCase = setLockPositionUseCase;
        this.observeLockPositionUseCase = observeLockPositionUseCase;
        this.observeConnectionStateUseCase = observeConnectionStateUseCase;
        this.observeHardwareStateUseCase = observeHardwareStateUseCase;
        this.getRideUseCase = getRideUseCase;
        this.rideTimeWalkThroughPref = rideTimeWalkThroughPref;
        this.scannedLockModelMapper = scannedLockModelMapper;
        this.lockModelMapper = lockModelMapper;
        this.getLockUseCase = getLockUseCase;
        this.disconnectAllLockUseCase = disconnectAllLockUseCase;
        this.getLockFirmwareVersionUseCase = getLockFirmwareVersionUseCase;
        this.updateBikeMetaDatUseCase = updateBikeMetaDatUseCase;
        this.startActiveTripUseCase = startActiveTripUseCase;
        this.startLocationTrackInActiveTripUseCase = startLocationTrackInActiveTripUseCase;
        this.stopLocationTrackInActiveTripUseCase = stopLocationTrackInActiveTripUseCase;
        this.stopActiveTripUseCase = stopActiveTripUseCase;
        this.stopGetTripDetailsThreadIfApplicableUseCase = stopGetTripDetailsThreadIfApplicableUseCase;
        this.getLocationSettingsUseCase = getLocationSettingsUseCase;
    }

    protected void onBluetoothEnabled(){
        getSignedMessagePublicKey();
    }

    protected void onLocationSettingsON(){
        getSignedMessagePublicKey();
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
                                if(view!=null) {
                                    view.onLocationSettingsON();
                                    onLocationSettingsON();
                                }
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

                                onLocationSettingsON();
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



    ////////////////////////// Bluetooth connection : start ///////////////////////////


    public void getRide() {
        subscriptions.add(getRideUseCase
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride newRide) {
                        super.onNext(newRide);
                        ride = newRide;
                        Log.e(TAG,"getRide::"+ride.toString());
                        view.OnRideSuccess(ride);
                        if(ride.getRideId()!=rideTimeWalkThroughPref.getValue()){
                            rideTimeWalkThroughPref.setValue(ride.getRideId());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.OnRideFailure();

                    }
                }));
    }

    @DebugLog
    public void connectToLastLockedLock() {
        if (connectToLastSubscription != null)
            connectToLastSubscription.dispose();
        isAccessDeniedForEllipse=false;

        if(lockModel ==null && ride!=null){
            lockModel = new LockModel();
            lockModel.setUserId(ride.getBike_bike_fleet_key());
            lockModel.setMacId(ride.getBike_mac_id());

        }

        subscriptions.add(connectToLastSubscription = connectToLastLockedLockUseCase.execute(new RxObserver<Lock.Connection.Status>() {

            @Override
            public void onStart() {
                super.onStart();
                //connectionState = BikeDirectionFragmentPresenter.ConnectionState.CONNECTING;
//                view.showConnecting();
            }

            @Override
            public void onNext(Lock.Connection.Status status) {
                super.onNext(status);
                if (status.isAuthenticated()) {
                    Log.e(TAG, "===========> connectToLastLockedLock::CONNECTED");
                    LockModel lastConnectedLockModel = lockModelMapper.mapIn(status.getLock());
                    if(ride!=null && lastConnectedLockModel!=null && lastConnectedLockModel.getMacId()!=null && lastConnectedLockModel.getMacId().equals(ride.getBike_mac_id())) {
                        connectionState = BikeDirectionFragmentPresenter.ConnectionState.CONNECTED;
                        view.onLockConnected(connectedLock=lastConnectedLockModel);
                    }else{
                        getSignedMessagePublicKey();
                    }
                } else if (status.equals(Lock.Connection.Status.DISCONNECTED)) {
                    connectionState = BikeDirectionFragmentPresenter.ConnectionState.DISCONNECTED;
                    Log.e(TAG, "===========> connectToLastLockedLock::DISCONNECTED");
                    if(!isAccessDeniedForEllipse){
                        view.onLockConnectionFailed();
                    }
                    stopLocationTrackInActiveTripService();
                } else if (status.equals(Lock.Connection.Status.ACCESS_DENIED)) {
                    Log.e(TAG, "===========> connectToLastLockedLock::ACCESS_DENIED");
                    connectionState = BikeDirectionFragmentPresenter.ConnectionState.CONNECTION_FAIL;
                    isAccessDeniedForEllipse=true;
                    view.onLockConnectionAccessDenied();
                    logCustomException(new Throwable("Acess denied: "+ lockModel.getMacId()));
                    stopLocationTrackInActiveTripService();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);

                if (throwable instanceof BluetoothException) {
                    BluetoothException exception = (BluetoothException) throwable;
                    if (exception != null) {
                        if(exception.getStatus()!=null){
                            if (exception.getStatus().equals(BluetoothException.Status.BLUETOOTH_DISABLED)) {
                                view.requestEnableBluetooth();
                            }
                        }
                    }
                } else  {
                    Log.e(TAG, "===========> connectToLastLockedLock::onError");
                    connectionState = BikeDirectionFragmentPresenter.ConnectionState.DISCONNECTED;
                    view.onLockConnectionFailed();
                }
            }
        }));
    }

    public void getSignedMessagePublicKey() {
        subscriptions.add(signedMessagePublicKeyUseCase
                .withBikeId(ride.getBikeId())
                .withMacId(ride.getBike_mac_id())
                .execute(new RxObserver<SignedMessagePublicKeyResponse>(view) {
                    @Override
                    public void onNext(SignedMessagePublicKeyResponse signedMessagePublicKeyResponse) {
                        super.onNext(signedMessagePublicKeyResponse);

                        view.onSignedMessagePublicKeySuccess(signedMessagePublicKeyResponse.getSignedMessagePublicKeyPayloadResponse().getSigned_message(), signedMessagePublicKeyResponse.getSignedMessagePublicKeyPayloadResponse().getPublic_key());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(e instanceof SocketTimeoutException || e instanceof UnknownHostException){
                            getLock();
                        }else{
                            view.onSignedMessagePublicKeyFailure();
                        }
                    }
                }));
    }

    public void getLock() {
        subscriptions.add(getLockUseCase
                .execute(new RxObserver<Lock>(view) {
                    @Override
                    public void onNext(Lock lock) {
                        super.onNext(lock);
                        if(lock!=null && lock.getSignedMessage()!=null && lock.getPublicKey()!=null){
                            view.onSignedMessagePublicKeySuccess(lock.getSignedMessage(),lock.getPublicKey());
                        }else{
                            view.onSignedMessagePublicKeyFailure();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onSignedMessagePublicKeyFailure();
                    }
                }));
    }



    public void disconnectAllLocks() {
        subscriptions.add(disconnectAllLockUseCase
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        connectTo();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        if (throwable instanceof BluetoothException) {
                            BluetoothException exception = (BluetoothException) throwable;
                            if (exception != null) {
                                if(exception.getStatus()!=null){
                                    if (exception.getStatus().equals(BluetoothException.Status.BLUETOOTH_DISABLED)) {
                                        view.requestEnableBluetooth();
                                    }
                                }
                            }
                        } else {
                            connectTo();
                        }
                    }
                }));
    }


    public void connectTo() {

        if (connectToLastSubscription != null)
            connectToLastSubscription.dispose();


        if (connectToSubscription != null)
            connectToSubscription.dispose();

        isAccessDeniedForEllipse=false;

        subscriptions.add(connectToSubscription = connectToLockUseCase.execute(lockModelMapper.mapOut(lockModel), new RxObserver<Lock.Connection.Status>(view, false) {
            @Override
            public void onStart() {
                super.onStart();
                //connectionState = BikeDirectionFragmentPresenter.ConnectionState.CONNECTING;
                view.showConnecting();
            }

            @Override
            public void onComplete() {
                super.onComplete();
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                if(e instanceof  BluetoothException){
                    if(((BluetoothException) e).getStatus()!=null) {
                        if (((BluetoothException) e).getStatus().equals(BluetoothException.Status.DEVICE_NOT_FOUND)) {
                            view.showConnectionTimeOut();
                        } else if (((BluetoothException) e).getStatus().equals(BluetoothException.Status.BLUETOOTH_DISABLED)) {
                            view.requestEnableBluetooth();
                        }
                    }
                }
            }

            @Override
            public void onNext(Lock.Connection.Status state) {
                if (state == OWNER_VERIFIED || state == GUEST_VERIFIED) {
                    super.onNext(state);
                    connectionState = BikeDirectionFragmentPresenter.ConnectionState.CONNECTED;

                    view.onLockConnected(connectedLock = lockModelMapper.mapIn(state.getLock()));
                } else if (state.equals(Lock.Connection.Status.DISCONNECTED)) {
                    connectionState = BikeDirectionFragmentPresenter.ConnectionState.DISCONNECTED;
                    if(!isAccessDeniedForEllipse){
                        view.onLockConnectionFailed();
                    }
                    stopLocationTrackInActiveTripService();
                } else if (state.equals(Lock.Connection.Status.ACCESS_DENIED)) {
                    connectionState = BikeDirectionFragmentPresenter.ConnectionState.CONNECTION_FAIL;
                    isAccessDeniedForEllipse=true;
                    logCustomException(new Throwable("Acess denied: "+ lockModel.getMacId()));
                    view.onLockConnectionAccessDenied();
                    stopLocationTrackInActiveTripService();
                }
            }
        }));
    }


    private void handleConnectionException(Throwable e){

        if(e instanceof ConnectionException) {
            ConnectionException connectionException = (ConnectionException) e;
            if (connectionException != null && connectionException.type == ConnectionException.Type.CONNECTION_NOT_FOUND && view != null) {
                resetSetPositionVariables();
                view.onLockConnectionFailed();
            }
        }
    }

    private void resetSetPositionVariables(){
        subscribeToSetPositionTimer(false);
        lastPositionRequested = null;
        setPositionRequest = false;
        setPositionRetry = 0;
    }

    public void observeLockPosition(@NonNull LockModel lockModel) {
        if (positionSubscription != null)
            positionSubscription.dispose();
        subscriptions.add(positionSubscription = observeLockPositionUseCase
                .forLock(lockModelMapper.mapOut(lockModel))
                .execute(new RxObserver<Lock.Hardware.Position>() {
                    @Override
                    public void onNext(Lock.Hardware.Position position) {
                        if (position == Lock.Hardware.Position.LOCKED) {
                            //Log.e(TAG, "######observeLockPosition: Lock is LOCKED");
                        }

                        if (position == Lock.Hardware.Position.UNLOCKED) {
                            //Log.e(TAG, "######observeLockPosition: Lock is UNLOCKED");
                        }

                        if (position == Lock.Hardware.Position.INVALID || position == Lock.Hardware.Position.BETWEEN_LOCKED_AND_UNLOCKED) {
                            view.showLockPositionError();
                            shackleJam=true;
                            if(!isJammingUpdated){
                                isJammingUpdated=true;
                                updateBikeMetaData();
                            }
                        } else {
                            setPosition(position);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        handleConnectionException(e);
                    }
                }));
    }




    public void observeConnectionState(@NonNull LockModel lockModel) {
        if (connectionSubscription != null)
            connectionSubscription.dispose();
        subscriptions.add(connectionSubscription = observeConnectionStateUseCase
                .forLock(lockModelMapper.mapOut(lockModel))
                .execute(new RxObserver<Lock.Connection.Status>() {
                    @Override
                    public void onNext(Lock.Connection.Status status) {
                        super.onNext(status);
                        if (status == Lock.Connection.Status.DISCONNECTED) {
                            Log.e(TAG, "######observeConnectionState: Lock is DISCONNECTED");
                            connectionState = BikeDirectionFragmentPresenter.ConnectionState.DISCONNECTED;
                        } else if (status == Lock.Connection.Status.OWNER_VERIFIED || status == Lock.Connection.Status.GUEST_VERIFIED ) {
                            observeLockPosition(lockModel);
                            observeHardwareState(lockModel);
                            isBikeMetaDataUpdated=false;
                            subscribeToFirmwareTimer(lockModel,true);
                        }
                        Log.e(TAG, "######observeConnectionState: Lock is " + status);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        handleConnectionException(e);
                    }
                }));
    }



    private void observeHardwareState(@NonNull LockModel lockModel) {

        if (hardwareSubscription != null) {
            hardwareSubscription.dispose();
            hardwareSubscription = null;
        }

        subscriptions.add(hardwareSubscription = observeHardwareStateUseCase
                .forLock(lockModelMapper.mapOut(lockModel))
                .execute(new RxObserver<Lock.Hardware.State>() {
                    @Override
                    public void onNext(Lock.Hardware.State state) {
                        super.onNext(state);
                        lock_battery = state.getBatteryLevel();
                        Lock.Hardware.Position position = state.getPosition();
                        if (position == Lock.Hardware.Position.INVALID || position == Lock.Hardware.Position.BETWEEN_LOCKED_AND_UNLOCKED) {
                            // observeLockPosition is handling this as invalid is received in that only.
                        } else {
                            setPosition(position);
                        }
                        updateBikeMetaData();
                    }
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        handleConnectionException(e);
                    }
                }));
    }

    public synchronized void subscribeToSetPositionTimer(boolean active) {
        Log.e(TAG,"subscribeToSetPositionTimer::"+active);
        if (active) {
            if (setPositionTimerSubscription == null) {
                setPositionTimerSubscription = Observable.timer(MILLISECONDS_FOR_SET_POSITION_TIMER, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            public void accept(Long aLong) {
                                Log.e(TAG,"subscribeToSetPositionTimer::"+active+" call");
                                subscribeToSetPositionTimer(false);
                                if(lastPositionRequested!=null) {
                                    setPosition(lastPositionRequested == Lock.Hardware.Position.LOCKED ? true : false, true);
                                }else{
                                    resetSetPositionVariables();
                                    if(view!=null) {
                                        view.onSetPositionFailure();
                                    }
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                if(throwable!=null && throwable.getLocalizedMessage()!=null)
                                    resetSetPositionVariables();
                                if(view!=null){
                                    view.onSetPositionFailure();
                                }
                            }
                        });
            }
        } else {
            if (setPositionTimerSubscription != null) {
                setPositionTimerSubscription.dispose();
                setPositionTimerSubscription = null;
            }
        }
    }

    public void setPosition( Boolean position, boolean isRetry) {

        if (connectionState == BikeDirectionFragmentPresenter.ConnectionState.DISCONNECTED) {
            getSignedMessagePublicKey();
            return;
        }

        if(!isRetry) {
            subscribeToSetPositionTimer(false);
            subscribeToSetPositionTimer(true);
            setPositionRetry = 0;
            setPositionRequest = true;
            lastPositionRequested = position ? Lock.Hardware.Position.LOCKED : Lock.Hardware.Position.UNLOCKED;
        }else{
            setPositionRetry++;
            if(setPositionRetry<MAX_SET_POSITION_RETRY){
                subscribeToSetPositionTimer(false);
                subscribeToSetPositionTimer(true);
            }else{
                resetSetPositionVariables();
                view.onSetPositionFailure();
                return;
            }
        }

        if(setPositionSubscription!=null){
            setPositionSubscription.dispose();
        }

        setPositionSubscription = setLockPositionUseCase
                .withState(position)
                .forLock(lockModelMapper.mapOut(lockModel))
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        view.onSetPositionStatus(status);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onSetPositionStatus(false);
                        handleConnectionException(e);
                    }
                });
    }



    private void setPosition(Lock.Hardware.Position position){
        isJammingUpdated=false;
        shackleJam=false;
        if((lastPositionRequested!=null && setPositionRequest && lastPositionRequested==position) ||
                lastPositionRequested==null ){
            resetSetPositionVariables();
            if(view!=null) {
                view.showLockPositionSuccess(position);
            }
        }
    }


    public synchronized void subscribeToFirmwareTimer(final LockModel lockModel, boolean active) {
        if (active) {
            if (getFirmwareTimerSubscription == null) {
                getFirmwareTimerSubscription = Observable.timer(MILLISECONDS_FOR_FIRMWARE_TIMER, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Long>() {
                            public void accept(Long aLong) {
                                subscribeToFirmwareTimer(null,false);
                                getLockFirmwareVersion(lockModel);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {

                            }
                        });
            }
        } else {
            if (getFirmwareTimerSubscription != null) {
                getFirmwareTimerSubscription.dispose();
                getFirmwareTimerSubscription = null;
            }
        }
    }

    public void getLockFirmwareVersion(@NonNull LockModel lockModel) {
        if (getFirmwareVersionSubscription != null)
            getFirmwareVersionSubscription.dispose();
        subscriptions.add(getFirmwareVersionSubscription =  getLockFirmwareVersionUseCase
                .forLock(lockModelMapper.mapOut(lockModel))
                .execute(new RxObserver<String>() {
                    @Override
                    public void onNext(String version) {
                        if (version!=null) {
                            firmwareVersion = version;
                            updateBikeMetaData();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        handleConnectionException(e);
                    }
                }));
    }

    public void updateBikeMetaData() {
        if(lock_battery==null || firmwareVersion == null || isBikeMetaDataUpdated){
            return;
        }


        subscriptions.add(updateBikeMetaDatUseCase
                .withFirmWare(firmwareVersion)
                .withShackleJamStatus(shackleJam)
                .withLockBattery(lock_battery)
                .withBikeBattery(-1)
                .forBike(ride.getBikeId())
                .execute(new RxObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        isBikeMetaDataUpdated=true;
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));
    }

    public Integer getLock_battery() {
        return lock_battery;
    }

    public void setLockModel(LockModel lockModel){
        this.lockModel = lockModel;
    }

    public LockModel getLockModel() {
        return lockModel;
    }


    public void unsubscribeAllSubscription() {
        if (connectToLastSubscription != null){
            connectToLastSubscription.dispose();
            connectToLastSubscription=null;
        }

        if (connectToSubscription != null){
            connectToSubscription.dispose();
            connectToSubscription=null;
        }


        if (positionSubscription != null){
            positionSubscription.dispose();
            positionSubscription=null;
        }

        if (connectionSubscription != null){
            connectionSubscription.dispose();
            connectionSubscription=null;
        }

        if(hardwareSubscription!=null){
            hardwareSubscription.dispose();
            hardwareSubscription=null;
        }

        if(getFirmwareVersionSubscription!=null){
            getFirmwareVersionSubscription.dispose();
            getFirmwareVersionSubscription=null;
        }

        if(setPositionSubscription!=null){
            setPositionSubscription.dispose();
            setPositionSubscription=null;
        }

        stopGetTripDetailsThreadIfApplicable();

        if(startActiveTripSubscription!=null){
            startActiveTripSubscription.dispose();
            startActiveTripSubscription=null;
        }

        if(stopActiveTripSubscription!=null){
            stopActiveTripSubscription.dispose();
            stopActiveTripSubscription=null;
        }

        if(startLocationTrackInActiveTripSubscription!=null){
            startLocationTrackInActiveTripSubscription.dispose();
            startLocationTrackInActiveTripSubscription=null;
        }

        if(stopLocationTrackInActiveTripSubscription!=null){
            stopLocationTrackInActiveTripSubscription.dispose();
            stopLocationTrackInActiveTripSubscription=null;
        }

        subscribeToFirmwareTimer(null,false);
        resetSetPositionVariables();

        firmwareVersion=null;
        lock_battery=null;
        isBikeMetaDataUpdated=false;
        shackleJam=true;
        isJammingUpdated =false;

    }


    public void startActiveTripService() {

        if(startActiveTripSubscription!=null)
            startActiveTripSubscription.dispose();

        subscriptions.add(startActiveTripSubscription= startActiveTripUseCase
                .withTripId(ride.getRideId())
                .execute(new RxObserver<UpdateTripData>(view) {
                    @Override
                    public void onNext(UpdateTripData updateTripData) {
                        super.onNext(updateTripData);
                        Log.e(TAG,"startUpdateTripService::"+updateTripData.toString());
                        view.setRideDurationAndCost(updateTripData);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                    }
                }));
    }


    public void stopLocationTrackInActiveTripService() {

        if(stopLocationTrackInActiveTripSubscription!=null){
            stopLocationTrackInActiveTripSubscription.dispose();
        }

        subscriptions.add(stopLocationTrackInActiveTripSubscription = stopLocationTrackInActiveTripUseCase

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

    public void startLocationTrackInActiveTripService() {

        if(startLocationTrackInActiveTripSubscription!=null){
            startLocationTrackInActiveTripSubscription.dispose();
        }

        subscriptions.add(startLocationTrackInActiveTripSubscription = startLocationTrackInActiveTripUseCase
                .withLockModel(lockModel)
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


    public void stopActiveTripService() {
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

    public void stopGetTripDetailsThreadIfApplicable() {

        if(stopGetTripDetailsThreadIfApplicableUseCase==null)
            return;

        stopGetTripDetailsThreadIfApplicableUseCase

                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean aVoid) {
                        super.onNext(aVoid);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });
    }




}
