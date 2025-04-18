package io.lattis.ellipse.sdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import hugo.weaving.DebugLog;
import io.lattis.ellipse.sdk.Ellipse;
import io.lattis.ellipse.sdk.Ellipse.Hardware.Position;
import io.lattis.ellipse.sdk.callback.BlinkLedCallback;
import io.lattis.ellipse.sdk.callback.SetLedCallback;
import io.lattis.ellipse.sdk.connection.ConnectionPool;
import io.lattis.ellipse.sdk.model.Alert;
import io.lattis.ellipse.sdk.model.BluetoothLock;
import io.lattis.ellipse.sdk.model.FirmwareUpdateProgress;
import io.lattis.ellipse.sdk.model.Rssi;
import io.lattis.ellipse.sdk.model.Status;
import io.lattis.ellipse.sdk.notification.EllipseNotificationManager;
import io.lattis.ellipse.sdk.scanner.BluetoothScanner;
import io.lattis.ellipse.sdk.util.IntentUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class BluetoothService extends Service {

    private static final int FIND_DEVICE_TIMEOUT_MILLIS = 30000;

    private static final int NOTIFICATION_ID_ALERT = 2;

    private static final String ACTION_STOP_ALERT_MONITORING = "ACTION_STOP_ALERT_MONITORING";
    private static final String ACTION_STOP_AUTO_LOCK = "ACTION_STOP_AUTO_LOCK";
    private static final String ACTION_STOP_AUTO_UNLOCK = "ACTION_STOP_AUTO_UNLOCK";

    private IBinder mBinder = new BlueToothServiceBinder();
    private BluetoothScanner bluetoothScanner;
    private ConnectionPool connectionPool;
    private Disposable alertDisposable;
    private PublishSubject<FirmwareUpdateProgress> firmwareUpdateProgressPublishSubject = PublishSubject.create();
    private PublishSubject<Alert> alertPublishSubject = PublishSubject.create();
    private EllipseNotificationManager notificationManager;

    @DebugLog
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_STOP_ALERT_MONITORING:
                stopForeground(true);
                break;
            case ACTION_STOP_AUTO_LOCK:
                stopForeground(true);
                break;
            case ACTION_STOP_AUTO_UNLOCK:
                stopForeground(true);
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.bluetoothScanner = BluetoothScanner.get(this);
        this.connectionPool = new ConnectionPool(this, bluetoothScanner);
        this.notificationManager = new EllipseNotificationManager(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connectionPool.destroy();
    }

    public class BlueToothServiceBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @DebugLog
    private void onCrashAlert(Alert alert){
        startActivity(IntentUtil.getAlertIntent(this, alert));
        notificationManager.notifyAlert(this,alert);
    }

    @DebugLog
    private void onTheftAlert(Alert alert){
        notificationManager.notifyAlert(this,alert);
    }

    public synchronized Observable<BluetoothLock> startScan(int scanDurationMillis) {
        return bluetoothScanner.startScan(scanDurationMillis);
    }

    public synchronized Observable<List<BluetoothLock>> getConnectedLocks() {
        return connectionPool.getConnectedLocks();
    }

    public synchronized Observable<BluetoothLock> getLastConnectedLock() {
        return connectionPool.getLastConnectedLock();
    }

    public Observable<Status> connect(final BluetoothLock bluetoothLock) {
        return connectionPool.connect(bluetoothLock, FIND_DEVICE_TIMEOUT_MILLIS)
                .doOnNext(new Consumer<Status>() {
                    @Override
                    public void accept(Status status) throws Exception {
                        if(status.equals(Status.GUEST_VERIFIED) || status.equals(Status.OWNER_VERIFIED)){
                            if(bluetoothLock.getAlertMode()!= Alert.OFF){
                                setAlertMode(bluetoothLock, bluetoothLock.getAlertMode());
                            }
                        }
                    }
                });
    }

    public synchronized Observable<Boolean> disconnect(final BluetoothLock bluetoothLock) {
        return connectionPool.disconnect(bluetoothLock);
    }

    public synchronized Observable<Boolean> disconnectAllLocks() {
        return connectionPool.disconnectAllLocks();
    }

    public synchronized Observable<Boolean> isConnectedTo(final BluetoothLock lock) {
        return connectionPool.isConnectedTo(lock);
    }

    public synchronized Observable<Boolean> reset(final BluetoothLock bluetoothLock) {
        return connectionPool.reset(bluetoothLock);
    }

    public Observable<BluetoothLock> observeDisconnectedLock(){
        return connectionPool.observeDisconnectedLock();
    }

    public Observable<BluetoothLock> observeConnectedLock(){
        return connectionPool.observeConnectedLock();
    }

    public Observable<List<BluetoothLock>> observeConnectedLocks(){
        return connectionPool.observeConnectedLocks();
    }

    public Observable<Status> observeLocksStatus() {
        return connectionPool.observeConnectionStatus();
    }

    public Observable<Status> observeLockStatus(final BluetoothLock bluetoothLock) {
        return connectionPool.observeConnectionStatus(bluetoothLock);
    }

    public Observable<Boolean> setPosition(@NonNull BluetoothLock lock, final boolean locked) {
        return connectionPool.setPosition(lock, locked);
    }

    public Observable<Boolean> setPinCode(@NonNull BluetoothLock lock, final String pinCode) {
        return connectionPool.setPinCode(lock, pinCode);
    }

    public Observable<Alert> getAlertMode(@NonNull final BluetoothLock lock) {
        return connectionPool.getAlertMode(lock);
    }

    public Observable<Alert> setAlertMode(@NonNull final BluetoothLock lock,
                                                       @NonNull final Alert alertMode) {


        if(alertDisposable != null && !alertDisposable.isDisposed()){
            alertDisposable.dispose();
            alertDisposable = null;
        }

        alertDisposable = connectionPool.setAlertMode(lock, alertMode)
                .flatMap(updateAlertNotificationAndServiceState)
                .flatMap(new Function<Object, ObservableSource<Alert>>() {
                    @Override
                    public ObservableSource<Alert> apply(Object o) throws Exception {
                        return connectionPool.observeAlerts(lock);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Alert>() {
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onComplete() { }

                    @Override
                    public void onNext(Alert alert) {
                        alert.forActivity(alertMode.getActivity());
                        if(alert.equals(Alert.CRASH)){
                            onCrashAlert(alert);
                        } else if(alert.equals(Alert.THEFT)){
                            onTheftAlert(alert);
                        }
                    }
                });
        return Observable.just(alertMode);
    }

    public Observable<Boolean> setAutoLock(@NonNull final BluetoothLock lock,
                                                        final boolean active){
        return connectionPool.setAutoLock(lock, active);
    }

    public Observable<Boolean> setAutoUnlock(@NonNull final BluetoothLock lock,
                                                         final boolean active){
        return connectionPool.setAutoUnlock(lock, active);
    }

    private Function<Alert, Observable<Alert>> updateAlertNotificationAndServiceState = new Function<Alert, Observable<Alert>>() {
        @Override
        public Observable<Alert> apply(Alert alert) throws Exception {
            if(alert.equals(Alert.OFF)){
                stopForeground(true);
            } else {
                notificationManager.notifyAlertMode(BluetoothService.this, alert);
            }
            return Observable.just(alert);
        }
    };

    public synchronized Observable<Ellipse.Boot.Version> getFirmwareVersion(BluetoothLock lock) {
        return connectionPool.getFirmwareVersion(lock);
    }

    public synchronized Observable<String> getSerialNumber(BluetoothLock lock) {
        return connectionPool.getSerialNumber(lock);
    }

    public synchronized Observable<FirmwareUpdateProgress> updateFirmware(final BluetoothLock lock,
                                                                          final Ellipse.Boot.Version targetVersion,
                                                                          final List<String> firmwareUpdates) {
        connectionPool.updateFirmware(lock, targetVersion, firmwareUpdates)
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<FirmwareUpdateProgress>() {

                    @Override
                    public void onError(Throwable e) {
                        firmwareUpdateProgressPublishSubject.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        firmwareUpdateProgressPublishSubject.onComplete();
                    }

                    @Override
                    public void onNext(FirmwareUpdateProgress progress) {
                        notificationManager.notify(BluetoothService.this, null, progress);
                        firmwareUpdateProgressPublishSubject.onNext(progress);
                    }
                });
        return firmwareUpdateProgressPublishSubject;
    }

    public synchronized Observable<Position> observeLockPosition(final BluetoothLock lock) {
        return connectionPool.observeLockPosition(lock);
    }

    public synchronized Observable<Rssi> observeRssiLevel(final BluetoothLock lock, final int refreshIntervalMillis) {
        return connectionPool.observeRssiLevel(lock,refreshIntervalMillis);
    }

    public synchronized Observable<Ellipse.Hardware.State> observeHardwareState(final BluetoothLock lock) {
        return connectionPool.observeHardwareState(lock);
    }

    public synchronized Observable<Integer> observeBatteryLevel(final BluetoothLock lock) {
        return connectionPool.observeBatteryLevel(lock);
    }

    public synchronized Observable<Alert> observeAlerts(final BluetoothLock lock) {
        return alertPublishSubject;
    }

    public synchronized Observable<Boolean> setLedState(final String macAddress, final boolean ledOn){
        BluetoothLock lock = new BluetoothLock();
        lock.setMacAddress(macAddress);
        return bluetoothScanner.findDevice(lock,FIND_DEVICE_TIMEOUT_MILLIS).flatMap(new Function<Status, ObservableSource<Boolean>>() {
            @Override
            public ObservableSource<Boolean> apply(final Status status) throws Exception {
                return Observable.create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                        status.getBluetoothDevice().connectGatt(BluetoothService.this, false,
                                new SetLedCallback(emitter,ledOn));
                    }
                });
            }
        });
    }

    public synchronized Observable<Void> blinkLed(final String macAddress){
        BluetoothLock lock = new BluetoothLock();
        lock.setMacAddress(macAddress);
        return bluetoothScanner.findDevice(lock,FIND_DEVICE_TIMEOUT_MILLIS).flatMap(new Function<Status, ObservableSource<Void>>() {
            @Override
            public ObservableSource<Void> apply(final Status status) throws Exception {
                return Observable.create(new ObservableOnSubscribe<Void>() {
                    @Override
                    public void subscribe(ObservableEmitter<Void> emitter) throws Exception {
                        status.getBluetoothDevice().connectGatt(BluetoothService.this, false,
                                new BlinkLedCallback(emitter));
                    }
                });
            }
        });
    }
}
