package com.lattis.ellipse.presentation.ui.bike.bikeList;

import android.util.Log;

import com.lattis.ellipse.data.network.model.response.lock.SignedMessagePublicKeyResponse;
import com.lattis.ellipse.domain.interactor.bike.BikeDetailUseCase;
import com.lattis.ellipse.domain.interactor.bike.CancelReserveBikeUseCase;
import com.lattis.ellipse.domain.interactor.bike.ReserveBikeUseCase;
import com.lattis.ellipse.domain.interactor.card.GetCardUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.interactor.lock.SignedMessagePublicKey.SignedMessagePublicKeyUseCase;
import com.lattis.ellipse.domain.interactor.lock.connect.ConnectToLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.disconnect.DisconnectAllLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.realm.DeleteLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.realm.SaveLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.scanner.ScanForLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.setter.BlinkLedUseCase;
import com.lattis.ellipse.domain.interactor.lock.setter.SetLockPositionUseCase;
import com.lattis.ellipse.domain.interactor.ride.StartRideUseCase;
import com.lattis.ellipse.domain.interactor.user.GetUserUseCase;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.model.ScannedLock;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.dagger.qualifier.FleetId;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.model.mapper.LockModelMapper;
import com.lattis.ellipse.presentation.model.mapper.ScannedLockModelMapper;
import com.lattis.ellipse.presentation.setting.IntPref;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.bluetooth.BaseBluetoothActivityPresenter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import hugo.weaving.DebugLog;
import io.lattis.ellipse.BuildConfig;
import io.lattis.ellipse.sdk.exception.BluetoothException;
import io.reactivex.disposables.Disposable;
import retrofit2.adapter.rxjava2.HttpException;

import static com.lattis.ellipse.domain.model.Lock.Connection.Status.ACCESS_DENIED;
import static com.lattis.ellipse.domain.model.Lock.Connection.Status.DISCONNECTED;
import static com.lattis.ellipse.domain.model.Lock.Connection.Status.GUEST_VERIFIED;
import static com.lattis.ellipse.domain.model.Lock.Connection.Status.OWNER_VERIFIED;
import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_RIDE_COUNT;

/**
 * Created by ssd3 on 9/5/17.
 */

public class ScanBikeQRCodeActivityPresenter extends BaseBluetoothActivityPresenter<ScanBikeQRCodeActivityView> {



    private final BikeDetailUseCase bikeDetailUseCase;
    private int current_qr_code_id=-1;


    private final String TAG = ScanBikeQRCodeActivityPresenter.class.getName();
    private final ScanForLockUseCase scanForLockUseCase;
    private final ConnectToLockUseCase connectToLockUseCase;
    private final SignedMessagePublicKeyUseCase signedMessagePublicKeyUseCase;
    private final StartRideUseCase startRideUseCase;
    private final ReserveBikeUseCase reserveBikeUseCase;
    private final SaveLockUseCase saveLockUseCase;
    private final DeleteLockUseCase deleteLockUseCase;
    private final DisconnectAllLockUseCase disconnectAllLockUseCase;
    private final BlinkLedUseCase blinkLedUseCase;
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private final SetLockPositionUseCase setLockPositionUseCase;
    private final CancelReserveBikeUseCase cancelReserveBikeUseCase;
    private final GetCardUseCase getCardUseCase;
    private GetUserUseCase getUserUseCase;
    private Disposable connectionSubscription;
    private Disposable startScanSubscription;
    private Disposable setPositionSubscription;
    private Disposable locationSubscription;
    private Disposable cancelReserveBikeSubscription;
    private boolean isAccessDeniedForEllipse=false;
    private boolean isStartRideInProgress = false;
    private final String fleetId;
    private Location currentUserLocation;
    private boolean isDisconnectRequiredForApp=false;
    private boolean isRideNeedsToBeStartedAfterLocation=false;
    private boolean isCreateBookingNeedsToBeDoneAfterLocation=false;
    private QRScanProgress qrScanProgress=QRScanProgress.NOTHING;
    private IntPref rideCountPref;
    private LockModel lockModel;
    private boolean isReconnection;
    private boolean isPhoneNumberCheckRequired =true;
    private boolean isPhoneNumberOK;
    private Disposable getUserSubscription = null;

    @Inject
    public ScannedLockModelMapper scannedLockModelMapper;
    @Inject
    public LockModelMapper lockModelMapper;
    private Bike bike;

    @Inject
    ScanBikeQRCodeActivityPresenter(BikeDetailUseCase bikeDetailUseCase,
                                    ScanForLockUseCase scanForLockUseCase,
                                    ConnectToLockUseCase connectToLockUseCase,
                                    SignedMessagePublicKeyUseCase signedMessagePublicKeyUseCase,
                                    SaveLockUseCase saveLockUseCase,
                                    DisconnectAllLockUseCase disconnectAllLockUseCase,
                                    BlinkLedUseCase blinkLedUseCase,
                                    StartRideUseCase startRideUseCase,
                                    DeleteLockUseCase deleteLockUseCase,
                                    GetLocationUpdatesUseCase getLocationUpdatesUseCase,
                                    @FleetId String fleetId,
                                    ReserveBikeUseCase reserveBikeUseCase,
                                    SetLockPositionUseCase setLockPositionUseCase,
                                    CancelReserveBikeUseCase cancelReserveBikeUseCase,
                                    GetCardUseCase getCardUseCase,
                                    @Named(KEY_RIDE_COUNT) IntPref rideCountPref,
                                    GetUserUseCase getUserUseCase)
    {
        this.bikeDetailUseCase = bikeDetailUseCase;
        this.scanForLockUseCase = scanForLockUseCase;
        this.connectToLockUseCase = connectToLockUseCase;
        this.signedMessagePublicKeyUseCase = signedMessagePublicKeyUseCase;
        this.saveLockUseCase=saveLockUseCase;
        this.disconnectAllLockUseCase = disconnectAllLockUseCase;
        this.blinkLedUseCase = blinkLedUseCase;
        this.deleteLockUseCase = deleteLockUseCase;
        this.startRideUseCase=startRideUseCase;
        this.getLocationUpdatesUseCase = getLocationUpdatesUseCase;
        this.fleetId = fleetId;
        this.reserveBikeUseCase = reserveBikeUseCase;
        this.setLockPositionUseCase = setLockPositionUseCase;
        this.cancelReserveBikeUseCase = cancelReserveBikeUseCase;
        this.getCardUseCase = getCardUseCase;
        this.rideCountPref = rideCountPref;
        this.getUserUseCase = getUserUseCase;
    }



    public void RequestToAddBikeFromQRCode(String qrCodeDatatString) {
        if (qrCodeDatatString != null) {
            try {
                JSONObject qrCodeJSONObject = new JSONObject(qrCodeDatatString);
                if (qrCodeJSONObject != null) {
                    Integer qr_code_id = qrCodeJSONObject.getInt("qr_id");
                    String bike_name = qrCodeJSONObject.getString("bike_name");
                    if (qr_code_id != null && bike_name!=null) {
                        getBikeDetails(qr_code_id);
                        return;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        current_qr_code_id=-1;
        view.onInvalidQRCode();
    }


    public void getBikeDetails(int qr_code_id) {

        if(current_qr_code_id==qr_code_id){
            view.restartScanner();
            return;
        }


        subscriptions.add(bikeDetailUseCase
                .withBikeId(-1)
                .withQRCodeId(qr_code_id)
                .execute(new RxObserver<Bike>(view) {
                    @Override
                    public void onNext(Bike bike) {
                        super.onNext(bike);
                        if(bike!=null){
                            current_qr_code_id = qr_code_id;
                            if(bike.getStatus()!=null && bike.getCurrent_status()!=null){
                                if(bike.getStatus().equalsIgnoreCase("active") && bike.getCurrent_status().equalsIgnoreCase("parked")) {
                                    view.onBikeDetailsSuccess(bike);
                                    return;
                                }
                            }

                            current_qr_code_id =-1;
                            view.onBikeNotAvailable();

                        }else{
                            current_qr_code_id =-1;
                            view.onBikeDetailsFailure();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        current_qr_code_id =-1;
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

    public void getCards() {
        subscriptions.add(getCardUseCase
                .execute(new RxObserver<List<Card>>() {
                    @Override
                    public void onNext(List<Card> cards) {
                        view.onGetCardSuccess(cards);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onGetCardFailure();
                    }
                }));
    }


    ////////////////////////////////// RESERVE BIKE CODE : START //////////////////////////////////////////////////


    public void reserveBike(Bike bike) {
        this.bike=bike;

        if(currentUserLocation==null){
            requestLocationUpdates();
            isCreateBookingNeedsToBeDoneAfterLocation=true;
            return;
        }

        subscriptions.add(reserveBikeUseCase
                .withBike(bike)
                .withScanStatus(true)
                .withLatitude(currentUserLocation.getLatitude())
                .withLongitude(currentUserLocation.getLongitude())
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);
                        qrScanProgress=QRScanProgress.BIKE_RESERVE;
                        view.OnReserveBikeSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e instanceof HttpException) {
                            HttpException exception = (HttpException) e;
                            if (exception.code() == 404) {
                                view.OnReserveBikeNotFound();
                            } else {
                                view.OnReserveBikeFail();
                            }
                        } else {
                            view.OnReserveBikeFail();
                        }
                    }


                }));
    }

    public void cancelBikeReservation(Bike bike){
        cancelCancelReserveBikeSubscription();
        qrScanProgress = QRScanProgress.NOTHING;
        cancelReserveBikeSubscription = cancelReserveBikeUseCase
                .withBikeId(bike.getBike_id())
                .withDamage(false)
                .execute(new RxObserver<Boolean>(view){
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        if(view!=null)
                            view.onCancelBikeSuccess();
                    }
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.onCancelBikeFail();
                    }
                });
    }





    ////////////////////////////////// START RIDE CODE : START //////////////////////////////////////////////////

    public void startRide(Bike bike, boolean isRideStarted) {
        Log.e(TAG,"startRide::");

        if (currentUserLocation == null) {
            Log.e(TAG,"startRide::location is null");
            requestLocationUpdates();
            isRideNeedsToBeStartedAfterLocation=true;
            return;
        }


        Log.e(TAG,"startRide::location is NOT null");

        qrScanProgress = QRScanProgress.RIDE_STARTED;

        subscriptions.add(startRideUseCase
                .withBike(bike)
                .withLocation(currentUserLocation)
                .withFirstLockConnect(!isRideStarted)
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);
                        Log.e(TAG,"startRide::onNext");
                        qrScanProgress = QRScanProgress.RIDE_STARTED;
                        if(BuildConfig.FLAVOR!="lattisDev"){
                            int currentRideCount = rideCountPref.getValue();
                            if(currentRideCount<5){
                                rideCountPref.setValue(++currentRideCount);
                            }
                        }


                        if(view!=null)
                            view.onStartRideSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e(TAG,"startRide::onError");
                        if(view!=null)
                            view.onStartRideFail();
                    }
                }));
    }



    ////////////////////////////////// LOCK CONNECTION CODE : START //////////////////////////////////////////////////

    public void requestLocationUpdates() {
        requestStopLocationUpdates();
        subscriptions.add(locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {
            @Override
            public void onNext(Location location) {
                requestStopLocationUpdates();
                currentUserLocation = location;
                if(isRideNeedsToBeStartedAfterLocation){
                    isRideNeedsToBeStartedAfterLocation=false;
                    view.startRide();
                }else if(isCreateBookingNeedsToBeDoneAfterLocation && bike!=null){
                    isCreateBookingNeedsToBeDoneAfterLocation=false;
                    reserveBike(bike);
                }
            }
        }));
    }

    public void requestStopLocationUpdates() {
        if (locationSubscription != null) {
            locationSubscription.dispose();
        }
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

    public void setPosition( Boolean position) {

        cancelSetPositionSubscription();

        subscriptions.add(setPositionSubscription=setLockPositionUseCase
                .withState(position)
                .forLock(lockModelMapper.mapOut(lockModel))
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        view.OnSetPositionStatus(status);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.OnSetPositionStatus(false);

                    }
                }));
    }

    public void cancelSetPositionSubscription(){
        if(setPositionSubscription!=null){
            setPositionSubscription.dispose();
            setPositionSubscription=null;
        }
    }

    public void cancelCancelReserveBikeSubscription(){
        if(cancelReserveBikeSubscription!=null){
            cancelReserveBikeSubscription.dispose();
            cancelReserveBikeSubscription=null;
        }
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
    void startScan() {

        if(startScanSubscription!=null){
            startScanSubscription.dispose();
            startScanSubscription=null;
        }

        subscriptions.add(startScanSubscription= scanForLockUseCase.execute(new RxObserver<ScannedLock>() {
            @Override
            public void onStart() {
                super.onStart();
                view.onScanStart();
            }

            @Override
            public void onComplete() {
                super.onComplete();
                view.onScanStop();
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                if (throwable instanceof BluetoothException) {
                    BluetoothException exception = (BluetoothException) throwable;
                    if (exception != null) {
                        if (exception.getStatus() != null) {
                            if (exception.getStatus().equals(BluetoothException.Status.BLUETOOTH_DISABLED)) {
                                view.requestEnableBluetooth();
                            }
                        }
                    }

                } else {
                    view.onScanStop();
                }
            }

            @Override
            public void onNext(ScannedLock lock) {
                super.onNext(lock);
                view.onLockScanned(scannedLockModelMapper.mapIn(lock));
            }
        }));
    }

    void connectTo() {
        cancelConnectionSubscription();
        isAccessDeniedForEllipse=false;
        subscriptions.add(connectionSubscription= connectToLockUseCase.execute(lockModelMapper.mapOut(lockModel), new RxObserver<Lock.Connection.Status>(view, false) {
            @Override
            public void onStart() {
                super.onStart();
                view.showConnecting(!isReconnection);
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
                            isReconnection=true;
                            connectTo();
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
                    qrScanProgress = QRScanProgress.LOCK_CONNECTED;
                    view.onLockConnected(lockModelMapper.mapIn(state.getLock()));
                } else if (state == ACCESS_DENIED) {
                    super.onNext(state);
                    isAccessDeniedForEllipse=true;
                    view.onLockConnectionAccessDenied();
                }else if (state == DISCONNECTED) {
                    super.onNext(state);
                    if(!isAccessDeniedForEllipse){
                        view.onLockConnectionFailed();
                    }
                }
            }
        }));
    }

    private void cancelConnectionSubscription(){
        if(connectionSubscription!=null){
            connectionSubscription.dispose();
            connectionSubscription=null;
        }

        if(startScanSubscription!=null){
            startScanSubscription.dispose();
            startScanSubscription=null;
        }
        isRideNeedsToBeStartedAfterLocation =false;
        isCreateBookingNeedsToBeDoneAfterLocation=false;
    }

    public void cancelAllSubscription(){
        requestStopLocationUpdates();
        isRideNeedsToBeStartedAfterLocation =false;
        isCreateBookingNeedsToBeDoneAfterLocation=false;
        cancelConnectionSubscription();
        cancelSetPositionSubscription();
        cancelCancelReserveBikeSubscription();
        cancelGetUserSubscription();
    }

    @Override
    protected void onBluetoothEnabled() {
        disconnectAllLocks();
    }


    public void disconnectAllLocks() {
        subscriptions.add(disconnectAllLockUseCase
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        if(!isDisconnectRequiredForApp) {
                            isReconnection=false;
                            connectTo();
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
                                isReconnection=false;
                                connectTo();
                            }
                        }
                    }
                }));
    }
    public void setDisconnectRequiredForApp(boolean disconnectRequiredForApp) {
        isDisconnectRequiredForApp = disconnectRequiredForApp;
    }

    @DebugLog
    void blinkLed(LockModel lockModel) {
        subscriptions.add(blinkLedUseCase.withMacAddress(lockModel.getMacAddress())
                .execute(new RxObserver<>(view, false)));
    }

    enum QRScanProgress{
        NOTHING,
        BIKE_RESERVE,
        LOCK_CONNECTED,
        RIDE_STARTED
    }


    public QRScanProgress getQrScanProgress() {
        return qrScanProgress;
    }


    public void setLockModel(LockModel lockModel){
        this.lockModel = lockModel;
    }

    public LockModel getLockModel() {
        return lockModel;
    }


    public void getUserProfile(){
        isPhoneNumberOK=false;
        cancelGetUserSubscription();
        getUserSubscription = getUserUseCase.execute(new RxObserver<User>(view, false) {
            @Override
            public void onNext(User currUser) {
                if(currUser!=null){
                    isPhoneNumberOK = (currUser.getPhoneNumber()!=null && !currUser.getPhoneNumber().equals("")) ? true : false;
                }
                view.onGetUserProfile();
            }
            @Override
            public void onError(Throwable e) {
                super.onError(e);
                view.onGetUserProfile();
            }
        });
    }


    public boolean phoneNumberCheckPassed(Bike bike){
        return bike.isRequire_phone_number() ? isPhoneNumberOK : true;
    }

    public void cancelGetUserSubscription(){
        if(getUserSubscription!=null){
            getUserSubscription.dispose();
            getUserSubscription=null;
        }
    }

}



