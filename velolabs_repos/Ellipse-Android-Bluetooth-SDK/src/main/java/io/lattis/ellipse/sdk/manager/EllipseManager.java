package io.lattis.ellipse.sdk.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

import java.util.List;

import io.lattis.ellipse.sdk.Ellipse;
import io.lattis.ellipse.sdk.model.Alert;
import io.lattis.ellipse.sdk.model.BluetoothLock;
import io.lattis.ellipse.sdk.model.FirmwareUpdateProgress;
import io.lattis.ellipse.sdk.model.Rssi;
import io.lattis.ellipse.sdk.model.Status;
import io.lattis.ellipse.sdk.service.BluetoothService;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class EllipseManager implements IEllipseManager {

    public static final String EXTRA_LOCK_ID = "EXTRA_LOCK_ID";
    public static final String EXTRA_ACCELEROMETER_DATA_MAV_X = "EXTRA_ACCELEROMETER_DATA_MAV_X";
    public static final String EXTRA_ACCELEROMETER_DATA_MAV_Y= "EXTRA_ACCELEROMETER_DATA_MAV_Y";
    public static final String EXTRA_ACCELEROMETER_DATA_MAV_Z= "EXTRA_ACCELEROMETER_DATA_MAV_Z";
    public static final String EXTRA_ACCELEROMETER_DATA_DEVIATION_X= "EXTRA_ACCELEROMETER_DATA_DEVIATION_X";
    public static final String EXTRA_ACCELEROMETER_DATA_DEVIATION_Y= "EXTRA_ACCELEROMETER_DATA_DEVIATION_Y";
    public static final String EXTRA_ACCELEROMETER_DATA_DEVIATION_Z= "EXTRA_ACCELEROMETER_DATA_DEVIATION_Z";
    public static final String ACTION_ALERT = "ACTION_CRASH_ALERT";

    protected Context context;
    private BluetoothService bluetoothService;

    private EllipseManager(Context context) {
        this.context = context;
    }

    public static IEllipseManager newInstance(@NonNull Context context) {
        return new EllipseManager(context);
    }

    @Override
    public Observable<Void> blinkLed(final String macAddress) {
        return getBluetoothService().flatMap(new Function<BluetoothService, ObservableSource<Void>>() {
            @Override
            public ObservableSource<Void> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.blinkLed(macAddress);
            }
        });
    }

    @Override
    public Observable<Boolean> setLedState(final String macAddress, final boolean ledOn) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.setLedState(macAddress, ledOn);
            }
        });
    }

    @Override
    public Observable<Boolean> isConnectedTo(final BluetoothLock bluetoothLock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.isConnectedTo(bluetoothLock);
            }
        });
    }

    @Override
    public Observable<Status> connect(final BluetoothLock lock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Status>>() {
            @Override
            public Observable<Status> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.connect(lock);
            }
        });
    }

    @Override
    public Observable<Boolean> disconnect(final BluetoothLock bluetoothLock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.disconnect(bluetoothLock);
            }
        });
    }

    @Override
    public Observable<Boolean> disconnectAllLocks() {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.disconnectAllLocks();
            }
        });
    }

    @Override
    public Observable<Boolean> reset(final BluetoothLock bluetoothLock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.reset(bluetoothLock);
            }
        });
    }

    @Override
    public Observable<String> getSerialNumber(final BluetoothLock lock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<String>>() {
            @Override
            public Observable<String> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.getSerialNumber(lock);
            }
        });
    }

    @Override
    public Observable<Ellipse.Boot.Version> getFirmwareVersion(final BluetoothLock lock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Ellipse.Boot.Version>>() {
            @Override
            public Observable<Ellipse.Boot.Version> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.getFirmwareVersion(lock);
            }
        });
    }

    @Override
    public Observable<FirmwareUpdateProgress> updateFirmware(final BluetoothLock lock,
                                                             final Ellipse.Boot.Version targetVersion,
                                                             final List<String> firmwareUpdates) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<FirmwareUpdateProgress>>() {
            @Override
            public Observable<FirmwareUpdateProgress> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.updateFirmware(lock, targetVersion, firmwareUpdates);
            }
        });
    }

    @Override
    public Observable<Boolean> setPosition(final BluetoothLock lock, final boolean locked) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.setPosition(lock,locked);
            }
        });
    }

    @Override
    public Observable<Boolean> setPinCode(final BluetoothLock lock, final String pinCode) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.setPinCode(lock, pinCode);
            }
        });
    }

    @Override
    public Observable<Alert> getAlertMode(final BluetoothLock bluetoothLock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Alert>>() {
            @Override
            public Observable<Alert> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.getAlertMode(bluetoothLock);
            }
        });
    }

    @Override
    public Observable<Alert> setAlertMode(final BluetoothLock bluetoothLock,
                                          @NonNull final Alert alert) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Alert>>() {
            @Override
            public Observable<Alert> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.setAlertMode(bluetoothLock, alert);
            }
        });
    }

    @Override
    public Observable<Boolean> setAutoLock(final BluetoothLock bluetoothLock, final boolean active) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.setAutoLock(bluetoothLock,active);
            }
        });
    }

    @Override
    public Observable<Boolean> setAutoUnlock(final BluetoothLock bluetoothLock, final boolean active) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.setAutoUnlock(bluetoothLock, active);
            }
        });
    }

    @Override
    public Observable<BluetoothLock> observeDisconnectedLock() {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<BluetoothLock>>() {
            @Override
            public Observable<BluetoothLock> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.observeDisconnectedLock();
            }
        });
    }

    @Override
    public Observable<BluetoothLock> observeConnectedLock() {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<BluetoothLock>>() {
            @Override
            public Observable<BluetoothLock> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.observeConnectedLock();
            }
        });
    }

    @Override
    public Observable<List<BluetoothLock>> observeConnectedLocks() {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<List<BluetoothLock>>>() {
            @Override
            public Observable<List<BluetoothLock>> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.observeConnectedLocks();
            }
        });
    }

    @Override
    public Observable<BluetoothLock> startScan(final int scanDurationMillis) {
         return getBluetoothService().flatMap(new Function<BluetoothService, Observable<BluetoothLock>>() {
             @Override
             public Observable<BluetoothLock> apply(BluetoothService bluetoothService) throws Exception {
                 return bluetoothService.startScan(scanDurationMillis);
             }
         });
    }

    @Override
    public Observable<BluetoothLock> getLastConnectedLock() {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<BluetoothLock>>() {
            @Override
            public Observable<BluetoothLock> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.getLastConnectedLock();
            }
        });
    }

    @Override
    public Observable<List<BluetoothLock>> getConnectLocks() {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<List<BluetoothLock>>>() {
            @Override
            public Observable<List<BluetoothLock>> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.getConnectedLocks();
            }
        });
    }

    @Override
    public Observable<Ellipse.Hardware.State> observeHardwareState(final BluetoothLock bluetoothLock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Ellipse.Hardware.State>>() {
            @Override
            public Observable<Ellipse.Hardware.State> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.observeHardwareState(bluetoothLock);
            }
        });
    }

    @Override
    public Observable<Ellipse.Hardware.Position> observeLockPosition(final BluetoothLock bluetoothLock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Ellipse.Hardware.Position>>() {
            @Override
            public Observable<Ellipse.Hardware.Position> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.observeLockPosition(bluetoothLock);
            }
        });
    }

    @Override
    public Observable<Boolean> observeLedState(String macAddress) {
        return null;
    }

    @Override
    public Observable<Rssi> observeRssiLevel(final BluetoothLock bluetoothLock, final int refreshIntervalMillis) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Rssi>>() {
            @Override
            public Observable<Rssi> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.observeRssiLevel(bluetoothLock,refreshIntervalMillis);
            }
        });
    }

    @Override
    public Observable<Integer> observeBatteryLevel(final BluetoothLock bluetoothLock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Integer>>() {
            @Override
            public Observable<Integer> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.observeBatteryLevel(bluetoothLock);
            }
        });
    }

    @Override
    public Observable<Status> observeLockState(final BluetoothLock bluetoothLock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Status>>() {
            @Override
            public Observable<Status> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.observeLockStatus(bluetoothLock);
            }
        });
    }

    @Override
    public Observable<Status> observeLockStatus(BluetoothLock bluetoothLock) {
        return null;
    }

    @Override
    public Observable<Alert> observeAlerts(final BluetoothLock bluetoothLock) {
        return getBluetoothService().flatMap(new Function<BluetoothService, Observable<Alert>>() {
            @Override
            public Observable<Alert> apply(BluetoothService bluetoothService) throws Exception {
                return bluetoothService.observeAlerts(bluetoothLock);
            }
        });
    }

    private Observable<BluetoothService> getBluetoothService() {
        return Observable.create(new ObservableOnSubscribe<BluetoothService>() {
            @Override
            public void subscribe(final ObservableEmitter<BluetoothService> emitter) throws Exception {
                if(bluetoothService == null){
                    ServiceConnection serviceConnection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder binder) {
                            emitter.onNext(bluetoothService = ((BluetoothService.BlueToothServiceBinder)binder).getService());
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                            bluetoothService = null;
                            emitter.onComplete();
                        }
                    };
                    context.bindService(new Intent(context, BluetoothService.class),
                            serviceConnection, Context.BIND_AUTO_CREATE);
                } else {
                    emitter.onNext(bluetoothService);
                }
            }
        });
    }
}
