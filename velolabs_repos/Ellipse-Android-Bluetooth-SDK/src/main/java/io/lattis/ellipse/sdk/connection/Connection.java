package io.lattis.ellipse.sdk.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.lattis.ellipse.sdk.Ellipse;
import io.lattis.ellipse.sdk.callback.DeviceConnectionCallback;
import io.lattis.ellipse.sdk.model.Alert;
import io.lattis.ellipse.sdk.model.BluetoothLock;
import io.lattis.ellipse.sdk.model.FirmwareUpdateProgress;
import io.lattis.ellipse.sdk.model.Rssi;
import io.lattis.ellipse.sdk.model.Status;
import io.lattis.ellipse.sdk.util.BluetoothUtil;
import io.lattis.ellipse.sdk.util.RssiUtil;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import static io.lattis.ellipse.sdk.Ellipse.Boot.Characteristic.CODE_VERSION;
import static io.lattis.ellipse.sdk.Ellipse.Boot.Characteristic.WRITE_DATA;
import static io.lattis.ellipse.sdk.Ellipse.Boot.DUMMY_VALUE;
import static io.lattis.ellipse.sdk.Ellipse.Configuration.Characteristic.BUTTON_LOCK_SEQUENCE;
import static io.lattis.ellipse.sdk.Ellipse.Configuration.Characteristic.RESET;
import static io.lattis.ellipse.sdk.Ellipse.Configuration.Characteristic.SERIAL_NUMBER;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.Characteristic.ACCELEROMETER;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.Characteristic.INFO;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.Characteristic.LOCK;

class Connection {

    private static final int AUTO_UNLOCK_REFRESH_INTERVAL_MILLIS = 5000;

    private static final int AUTO_LOCK_ANDROID_RSSI_THRESHOLD = 200;//-70;
    private static final int AUTO_UNLOCK_ANDROID_RSSI_THRESHOLD = 200;//-70;

    private static final int AUTO_LOCK_RSSI_THRESHOLD = 200;
    private static final int AUTO_UNLOCK_RSSI_THRESHOLD = 200;

    private DeviceConnectionCallback callback;
    private BluetoothGatt bluetoothGatt;
    private boolean autoLockActive = false;
    private boolean autoUnlockActive = false;

    private Disposable rssiLevelSubscription;

    Connection(BluetoothLock bluetoothLock) {
        this.autoUnlockActive = bluetoothLock.isAutoUnLockActive();
        this.autoLockActive = bluetoothLock.isAutoLockActive();
        this.callback = new DeviceConnectionCallback(bluetoothLock);
    }

    public Observable<Status> create(final Context context,
                                     final boolean autoConnect,
                                     final BluetoothDevice bluetoothDevice) {
        return callback.getConnectionStatusSubject()
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        if(bluetoothGatt == null){
                            bluetoothGatt = bluetoothDevice.connectGatt(context, autoConnect, callback);
                        }
                    }
                }).doOnNext(new Consumer<Status>() {
                    @Override
                    public void accept(Status status) throws Exception {
                        if(status == Status.OWNER_VERIFIED || status == Status.GUEST_VERIFIED){
                            if(autoLockActive || autoUnlockActive){
                                if(autoLockActive) {
                                    LOCK.write(bluetoothGatt, Ellipse.Hardware.WRITE_POSITION_DELAYED_LOCK);
                                }
                                subscribeToRssiLevel(true);
                            }
                        }
                    }
                });
    }

    @NonNull
    BehaviorSubject<Status> getConnectionStatusSubject() {
        return callback.getConnectionStatusSubject();
    }

    @NonNull
    Observable<Ellipse.Hardware.State> getHardwareStateObservable() {
        return callback.getHardwareStateSubject();
    }

    @NonNull
    Observable<Rssi> getRssiLevelObservable(final int refreshIntervalMillis) {
        return callback.getRssiLevel();
    }

    @NonNull
    Observable<Integer> getBatteryLevelObservable() {
        INFO.read(bluetoothGatt);
        return callback.getBatteryLevel();
    }

    @NonNull
    Observable<Alert> getAlertsObservable() {
        return callback.getAlertSubject();
    }

    @NonNull
    Observable<Alert> setAlertMode(@NonNull Alert alertMode) {
        ACCELEROMETER.setNotification(bluetoothGatt, alertMode != Alert.OFF);
        ACCELEROMETER.notify(bluetoothGatt, alertMode != Alert.OFF);
        this.callback.setAlertMode(alertMode);
        return Observable.just(alertMode);
    }

    @NonNull
    Observable<Boolean> setAutoLock(boolean active) {
        autoLockActive = active;
        if(active){
            LOCK.write(bluetoothGatt, Ellipse.Hardware.WRITE_POSITION_DELAYED_LOCK);
        }
        subscribeToRssiLevel(active);
        return Observable.just(active);
    }

    @NonNull
    Observable<Boolean> setAutoUnlock(boolean active) {
        autoUnlockActive = active;
        subscribeToRssiLevel(active);
        return Observable.just(active);
    }

    public boolean mustBeKeptAlive(){
        return autoUnlockActive || callback.getAlertMode() == Alert.THEFT;
    }

    private Observable<Rssi> subscribeToRssiLevel(){
        return Observable.interval(AUTO_UNLOCK_REFRESH_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .flatMap(requestRssiLevel);
    }

    private Function<Long, Observable<Rssi>> requestRssiLevel = new Function<Long, Observable<Rssi>>() {
        @Override
        public Observable<Rssi> apply(Long aLong) throws Exception {
            return callback.readAndroidRssiLevel(bluetoothGatt);
        }
    };

    private synchronized void subscribeToRssiLevel(boolean active){
        if(active){
            if (rssiLevelSubscription == null){

                rssiLevelSubscription = subscribeToRssiLevel()
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableObserver<Rssi>() {

                            @Override
                            public void onError(Throwable e) {}

                            @Override
                            public void onComplete() {

                            }

                            @Override
                            public void onNext(Rssi rssi) {
                                Log.d(Connection.class.getSimpleName(),"Rssi "+rssi);
                                Log.d(Connection.class.getSimpleName(),"Distance "+ RssiUtil.calculateDistance(-60, rssi.getValue()));
                                if(rssi.getValue() < AUTO_LOCK_ANDROID_RSSI_THRESHOLD &&
                                   autoLockActive && !callback.isLocked()){
                                    setPosition(true).subscribe();
                                } else if(rssi.getValue() > AUTO_UNLOCK_ANDROID_RSSI_THRESHOLD &&
                                          autoUnlockActive && callback.isLocked()){
                                    setPosition(false).subscribe();
                                }

                                if(!autoUnlockActive && !autoLockActive){
                                    complete();
                                }
                            }
                        });
                }
        } else {
            if(!autoLockActive && !autoUnlockActive && rssiLevelSubscription != null){
                rssiLevelSubscription.dispose();
                rssiLevelSubscription = null;
            }
        }
    }

    @NonNull
    Alert getAlertMode() {
        return this.callback.getAlertMode();
    }

    @NonNull
    Observable<Ellipse.Hardware.Position> getLockPositionObservable() {
        return callback.getLockPosition();
    }

    @NonNull
    Observable<Ellipse.Boot.Version> getVersionObservable() {
        CODE_VERSION.forceRead(bluetoothGatt);
        return callback.getVersion();
    }

    @NonNull
    Observable<String> getSerialNumberObservable() {
        SERIAL_NUMBER.read(bluetoothGatt);
        return callback.getSerialNumber();
    }

    @NonNull
    Observable<FirmwareUpdateProgress> updateVersionObservable(final Ellipse.Boot.Version targetVersion,
                                                               final List<String> firmwareVersions) {
        BluetoothUtil.requestConnectionPriorityHigh(bluetoothGatt);
        WRITE_DATA.writeFirmware(bluetoothGatt,firmwareVersions.get(0));
        return callback.getUpdateVersionSubject(targetVersion,firmwareVersions);
    }

    Observable<Boolean> setPosition(final boolean locked){
        return Observable.just(LOCK.write(bluetoothGatt, locked ?
                Ellipse.Hardware.WRITE_POSITION_LOCKED : Ellipse.Hardware.WRITE_POSITION_UNLOCKED));
    }

    BluetoothLock getBluetoothLock(){
        return callback.getBluetoothLock();
    }

    void complete() {
        if(bluetoothGatt!=null){
            bluetoothGatt.disconnect();
        }
    }

    Observable<Boolean> setPinCode(String pinCode) {
        BUTTON_LOCK_SEQUENCE.writePinCode(bluetoothGatt, pinCode);
        return callback.getPinCodeSubject();
    }

    public Observable<Boolean> reset(){
        return Observable.just(RESET.write(bluetoothGatt, DUMMY_VALUE));
    }
}
