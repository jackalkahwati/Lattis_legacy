package io.lattis.ellipse.sdk.connection;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hugo.weaving.DebugLog;
import io.lattis.ellipse.sdk.Ellipse;
import io.lattis.ellipse.sdk.Ellipse.Boot.Version;
import io.lattis.ellipse.sdk.exception.BluetoothException;
import io.lattis.ellipse.sdk.exception.EllipseException;
import io.lattis.ellipse.sdk.model.Alert;
import io.lattis.ellipse.sdk.model.BluetoothLock;
import io.lattis.ellipse.sdk.model.FirmwareUpdateProgress;
import io.lattis.ellipse.sdk.model.Rssi;
import io.lattis.ellipse.sdk.model.Status;
import io.lattis.ellipse.sdk.scanner.BluetoothScanner;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static io.lattis.ellipse.sdk.model.Status.DEVICE_FOUND;
import static io.lattis.ellipse.sdk.model.Status.DISCONNECTED;
import static io.lattis.ellipse.sdk.model.Status.GUEST_VERIFIED;
import static io.lattis.ellipse.sdk.model.Status.OWNER_VERIFIED;

public class ConnectionPool {

    private CompositeDisposable connectionsSubscription = new CompositeDisposable();
    private HashMap<String, Connection> connections =  new HashMap<>();
    private HashMap<String, Connection> pendingConnections =  new HashMap<>();

    private PublishSubject<Alert> alertSubject =  PublishSubject.create();
    private PublishSubject<Status> connectionStatusSubject =  PublishSubject.create();
    private PublishSubject<BluetoothLock> connectedLockSubject =  PublishSubject.create();
    private PublishSubject<BluetoothLock> disconnectedLockSubject =  PublishSubject.create();
    private PublishSubject<List<BluetoothLock>> connectedLocksSubject =  PublishSubject.create();

    private Context context;
    private BluetoothScanner bluetoothScanner;

    public ConnectionPool(Context context, BluetoothScanner bluetoothScanner) {
        this.context = context;
        this.bluetoothScanner = bluetoothScanner;
    }

    private Observable<Boolean> checkBluetoothEnableOrThrow() {
        return bluetoothScanner.checkBluetoothEnableOrThrow();
    }

    @DebugLog
    private void subscribeToEstablishConnection(final Connection connection, final int timeout){
        Observable.interval(timeout * 2, TimeUnit.MILLISECONDS)
                .flatMap(new Function<Long, ObservableSource<Status>>() {
                    @Override
                    public ObservableSource<Status> apply(Long aLong) throws Exception {
                        Toast.makeText(context,"Scanning",Toast.LENGTH_LONG).show();
                        return bluetoothScanner.findDevice(connection.getBluetoothLock(), timeout, true);
                    }
                }).subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Status>() {

                    @DebugLog
                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context, e.getMessage() ,Toast.LENGTH_LONG).show();
                        connection.getConnectionStatusSubject().onNext(DISCONNECTED.forBluetoothLock(connection.getBluetoothLock()));
                    }

                    @Override
                    public void onComplete() {

                    }

                    @DebugLog
                    @Override
                    public void onNext(Status status) {
                        if(status.equals(DEVICE_FOUND)){
                            Toast.makeText(context,"DEVICE_FOUND",Toast.LENGTH_LONG).show();
                            BluetoothLock bluetoothLock = connection.getBluetoothLock();
                            connections.remove(bluetoothLock.getMacId());
                            connect(bluetoothLock, timeout);
                            dispose();
                        } else {
                            connection.getConnectionStatusSubject().onNext(status);
                        }
                    }
                });
    }

    @DebugLog
    public Observable<Status> connect(final BluetoothLock bluetoothLock, final int timeout) {
        if(!connections.containsKey(bluetoothLock.getMacId())) {
            final Connection connection = new Connection(bluetoothLock);
            connections.put(bluetoothLock.getMacId(), connection);
            connectionsSubscription.add(bluetoothScanner.findDevice(bluetoothLock, timeout)
                    .flatMap(new Function<Status, ObservableSource<Status>>() {
                        @Override
                        public ObservableSource<Status> apply(Status scanningStatus) throws Exception {
                            connection.getConnectionStatusSubject().onNext(scanningStatus);
                            if(scanningStatus.equals(DEVICE_FOUND)){
                                return connection.create(context, false, scanningStatus.getBluetoothDevice());
                            }
                            return Observable.just(scanningStatus);
                        }
                    }).subscribeWith(new DisposableObserver<Status>() {

                        @DebugLog
                        @Override
                        public void onError(Throwable e) {
                            disconnectedLockSubject.onNext(bluetoothLock);
                            connections.remove(bluetoothLock.getMacId());
                            connection.getConnectionStatusSubject().onError(e);
                        }

                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void onNext(Status status) {
                            if(status == OWNER_VERIFIED || status == GUEST_VERIFIED){
                                connectedLockSubject.onNext(bluetoothLock);
                                connectedLocksSubject.onNext(getConnectedLockList());
                            } else if(status == DISCONNECTED){
                                if(!connection.mustBeKeptAlive()){
                                    connections.remove(bluetoothLock.getMacId());
                                } else {
                                    subscribeToEstablishConnection(connection,timeout);
                                }
                                connectionsSubscription.remove(this);
                                connectedLocksSubject.onNext(getConnectedLockList());
                                disconnectedLockSubject.onNext(bluetoothLock);
                            }
                        }
                    }));
        }
        if(connections.containsKey(bluetoothLock.getMacId())){
            return connections.get(bluetoothLock.getMacId()).getConnectionStatusSubject();
        } else {
            return Observable.error(new BluetoothException(BluetoothException.Status.BLUETOOTH_DISABLED));
        }
    }

    public synchronized Observable<Boolean> disconnectAllLocks(){
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                if(connections.size() > 0){
                    for(Connection connection: connections.values()){
                        connection.complete();
                    }
                    connections.clear();
                    connectionsSubscription.clear();
                    emitter.onNext(true);
                } else {
                    emitter.onNext(false);
                }
            }
        });
    }

    public Observable<Boolean> disconnect(final BluetoothLock bluetoothLock) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                if(connections.containsKey(bluetoothLock.getMacId())){
                    Connection connection = connections.get(bluetoothLock.getMacId());
                    connection.complete();
                    connections.remove(bluetoothLock.getMacId());
                    emitter.onNext(true);
                } else {
                    emitter.onNext(false);
                }
            }
        });
    }

    public Observable<BluetoothLock> observeDisconnectedLock(){
        return disconnectedLockSubject;
    }

    public Observable<BluetoothLock> observeConnectedLock(){
        return connectedLockSubject;
    }

    public Observable<List<BluetoothLock>> observeConnectedLocks(){
        return connectedLocksSubject;
    }

    private List<BluetoothLock> getConnectedLockList(){
        List<BluetoothLock> locks = new ArrayList<>(connections.size());
        if(connections.size() > 0){
            for(Connection connection:connections.values()){
                locks.add(connection.getBluetoothLock());
            }
        }
        return locks;
    }

    public Observable<List<BluetoothLock>> getConnectedLocks(){
        return Observable.create(new ObservableOnSubscribe<List<BluetoothLock>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BluetoothLock>> emitter) throws Exception {
                if(connections.size() > 0){
                    emitter.onNext(getConnectedLockList());
                } else emitter.onError(new EllipseException("No lock connected"));
            }
        });
    }

    public Observable<BluetoothLock> getLastConnectedLock(){
        return Observable.create(new ObservableOnSubscribe<BluetoothLock>() {
            @Override
            public void subscribe(ObservableEmitter<BluetoothLock> emitter) throws Exception {
                if(connections.size() > 0){
                    if(connections.size() > 1){
                        String macId = null;
                        //TODO
                        /*Date connected;
                        for(Connection connection:connections.values()){
                            if(macId != null && ){

                            } else {
                                macId = connection.getBluetoothLock().getMacId();
                            }
                        }*/
                        emitter.onNext(connections.values().iterator().next().getBluetoothLock());
                    } else {
                        emitter.onNext(connections.values().iterator().next().getBluetoothLock());
                    }

                }else emitter.onError(new EllipseException("No lock connected"));
            }
        });
    }

    private Observable<Connection> getConnection(final BluetoothLock bluetoothLock){
        return checkBluetoothEnableOrThrow().flatMap(new Function<Boolean, ObservableSource<Connection>>() {
            @Override
            public ObservableSource<Connection> apply(Boolean aBoolean) throws Exception {
                return Observable.create(new ObservableOnSubscribe<Connection>() {
                    @Override
                    public void subscribe(ObservableEmitter<Connection> emitter) throws Exception {
                        if(connections.containsKey(bluetoothLock.getMacId())){
                            emitter.onNext(connections.get(bluetoothLock.getMacId()));
                        } else {
                            connect(bluetoothLock, 30000).flatMap(new Function<Status, ObservableSource<Connection>>() {
                                @Override
                                public ObservableSource<Connection> apply(final Status status) throws Exception {
                                    return Observable.create(new ObservableOnSubscribe<Connection>() {
                                        @Override
                                        public void subscribe(ObservableEmitter<Connection> emitter1) throws Exception {
                                            if(status == OWNER_VERIFIED || status == GUEST_VERIFIED){
                                                emitter1.onNext(connections.get(bluetoothLock.getMacId()));
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });

        
    }

    @NonNull
    public Observable<Status> observeConnectionStatus(BluetoothLock lock) {
        return getConnection(lock).flatMap(new Function<Connection, ObservableSource<Status>>() {
            @Override
            public ObservableSource<Status> apply(Connection connection) throws Exception {
                return connection.getConnectionStatusSubject();
            }
        });
    }

    @NonNull
    public Observable<Status> observeConnectionStatus() {
        return connectionStatusSubject;
    }

    @NonNull
    public Observable<Boolean> setPosition(final BluetoothLock lock, final boolean locked) {
        return getConnection(lock).flatMap(new Function<Connection, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(Connection connection) throws Exception {
                return connection.setPosition(locked);
            }
        });
    }

    @NonNull
    public Observable<Boolean> setPinCode(final BluetoothLock lock, final String pinCode) {
        return getConnection(lock).flatMap(new Function<Connection, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(Connection connection) throws Exception {
                return connection.setPinCode(pinCode);
            }
        });
    }

    @NonNull
    public Observable<Alert> setAlertMode(BluetoothLock lock, @NonNull final Alert alert) {
        return getConnection(lock).flatMap(new Function<Connection, Observable<Alert>>() {
            @Override
            public Observable<Alert> apply(Connection connection) throws Exception {
                return connection.setAlertMode(alert);
            }
        });
    }

    @NonNull
    public Observable<Boolean> setAutoLock(BluetoothLock lock, final boolean active) {
        return getConnection(lock).flatMap(new Function<Connection, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(Connection connection) throws Exception {
                return connection.setAutoLock(active);
            }
        });
    }

    @NonNull
    public Observable<Boolean> setAutoUnlock(BluetoothLock lock, final boolean active) {
        return getConnection(lock).flatMap(new Function<Connection, ObservableSource<Boolean>>() {
            @Override
            public ObservableSource<Boolean> apply(Connection connection) throws Exception {
                return connection.setAutoUnlock(active);
            }
        });
    }

    @NonNull
    public Observable<Alert> getAlertMode(BluetoothLock lock) {
        return getConnection(lock).flatMap(new Function<Connection, Observable<Alert>>() {
            @Override
            public Observable<Alert> apply(Connection connection) throws Exception {
                return Observable.just(connection.getAlertMode());
            }
        });
    }

    @NonNull
    public synchronized Observable<Version> getFirmwareVersion(BluetoothLock lock) {
        return getConnection(lock).flatMap(new Function<Connection, ObservableSource<Version>>() {
            @Override
            public ObservableSource<Version> apply(Connection connection) throws Exception {
                return connection.getVersionObservable();
            }
        });
    }

    @NonNull
    public synchronized Observable<String> getSerialNumber(BluetoothLock lock) {
        return getConnection(lock).flatMap(new Function<Connection, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Connection connection) throws Exception {
                return connection.getSerialNumberObservable();
            }
        });
    }

    @NonNull
    public synchronized Observable<FirmwareUpdateProgress> updateFirmware(final BluetoothLock lock,
                                                                          final Version targetVersion,
                                                                          final List<String> firmwareUpdates) {
        return getConnection(lock).flatMap(new Function<Connection, ObservableSource<FirmwareUpdateProgress>>() {
            @Override
            public ObservableSource<FirmwareUpdateProgress> apply(Connection connection) throws Exception {
                return connection.updateVersionObservable(targetVersion,firmwareUpdates);
            }
        });
    }

    @NonNull
    public synchronized Observable<Ellipse.Hardware.State> observeHardwareState(BluetoothLock lock) {
        return getConnection(lock).flatMap(new Function<Connection, ObservableSource<Ellipse.Hardware.State>>() {
            @Override
            public ObservableSource<Ellipse.Hardware.State> apply(Connection connection) throws Exception {
                return connection.getHardwareStateObservable();
            }
        });
    }

    @NonNull
    public synchronized Observable<Rssi> observeRssiLevel(BluetoothLock lock, final int refreshIntervalMillis) {
        return getConnection(lock).flatMap(new Function<Connection, ObservableSource<Rssi>>() {
            @Override
            public ObservableSource<Rssi> apply(Connection connection) throws Exception {
                return connection.getRssiLevelObservable(refreshIntervalMillis);
            }
        });
    }

    @NonNull
    public Observable<Integer> observeBatteryLevel(BluetoothLock lock) {
        return getConnection(lock).flatMap(new Function<Connection, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Connection connection) throws Exception {
                return connection.getBatteryLevelObservable();
            }
        });
    }

    @NonNull
    public Observable<Ellipse.Hardware.Position> observeLockPosition(BluetoothLock lock) {
        return getConnection(lock).flatMap(new Function<Connection, ObservableSource<Ellipse.Hardware.Position>>() {
            @Override
            public ObservableSource<Ellipse.Hardware.Position> apply(Connection connection) throws Exception {
                return connection.getLockPositionObservable();
            }
        });
    }

    @NonNull
    public Observable<Alert> observeAlerts() {
        return alertSubject;
    }

    @NonNull
    public Observable<Alert> observeAlerts(BluetoothLock lock) {
        return getConnection(lock).flatMap(new Function<Connection, ObservableSource<Alert>>() {
            @Override
            public ObservableSource<Alert> apply(Connection connection) throws Exception {
                return connection.getAlertsObservable();
            }
        });
    }

    public void destroy() {
        connectionsSubscription.clear();
        connections.clear();
    }

    @NonNull
    public Observable<Boolean> reset(BluetoothLock bluetoothLock) {
        return getConnection(bluetoothLock).flatMap(new Function<Connection, ObservableSource<Boolean>>() {
            @Override
            public ObservableSource<Boolean> apply(Connection connection) throws Exception {
                return connection.reset();
            }
        });
    }

    public Observable<Boolean> isConnectedTo(BluetoothLock lock) {
        return Observable.just(connections.containsKey(lock.getMacId()));
    }
}
