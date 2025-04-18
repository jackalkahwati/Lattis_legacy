package io.lattis.ellipse.sdk.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.concurrent.TimeUnit;

import io.lattis.ellipse.sdk.exception.BluetoothException;
import io.lattis.ellipse.sdk.model.BluetoothLock;
import io.lattis.ellipse.sdk.model.Status;
import io.lattis.ellipse.sdk.util.BluetoothUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;


class KitkatBluetoothScanner extends BluetoothScanner {

    private static final String TAG = KitkatBluetoothScanner.class.getSimpleName();

    Context context;

    BroadcastReceiver receiver;

    BluetoothAdapter bluetoothAdapter;

    Function<Boolean, ObservableSource<Boolean>> clearScannedDevices = aBoolean -> {
        scannedDevices.clear();
        return Observable.just(aBoolean);
    };

    Function<Boolean, ObservableSource<Intent>> registerReceiver = new Function<Boolean, ObservableSource<Intent>>() {
        @Override
        public ObservableSource<Intent> apply(Boolean aBoolean) throws Exception {
            return Observable.create(new ObservableOnSubscribe<Intent>() {
                @Override
                public void subscribe(ObservableEmitter<Intent> e) throws Exception {
                    receiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            e.onNext(intent);
                        }
                    };

                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    context.registerReceiver(receiver, filter);
                }
            });
        }
    };

    KitkatBluetoothScanner(Context context) {
        super(context);
        this.context = context;
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public Observable<BluetoothLock> startScan(int scanDurationMillis) {
        return checkBluetoothEnableOrThrow()
                .flatMap(clearScannedDevices)
                .flatMap(registerReceiver)
                .flatMap(new Function<Intent, ObservableSource<BluetoothLock>>() {
                    @Override
                    public ObservableSource<BluetoothLock> apply(Intent intent) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<BluetoothLock>() {
                            @Override
                            public void subscribe(ObservableEmitter<BluetoothLock> e) throws Exception {
                                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_NAME);
                                BluetoothLock lock = lockMapper.mapOut(bluetoothDevice);
                                if (!scannedDevices.containsKey(lock.getMacAddress())) {
                                    scannedDevices.put(lock.getMacAddress(), bluetoothDevice);
                                    e.onNext(lock);
                                }
                            }
                        })
                        .timeout(scanDurationMillis, TimeUnit.MILLISECONDS,
                                Observable.create(new ObservableOnSubscribe<BluetoothLock>() {
                                    @Override
                                    public void subscribe(ObservableEmitter<BluetoothLock> e) throws Exception {
                                        e.onComplete();
                                    }
                                }))
                        .doOnDispose(new Action() {
                            @Override
                            public void run() throws Exception {
                                context.unregisterReceiver(receiver);
                                receiver = null;
                            }
                        });
                    }
                });
    }

    @Override
    public Observable<Status> findDevice(BluetoothLock lock, int timeoutMillis) {
        return findDevice(lock, timeoutMillis, false);
    }

    @Override
    public Observable<Status> findDevice(BluetoothLock lock, int timeoutMillis, boolean skipCache) {
        if (lock.getMacAddress() != null && scannedDevices.containsKey(lock.getMacAddress()) && !skipCache) {
            return Observable.just(Status.DEVICE_FOUND.forBluetoothDevice(scannedDevices.get(lock.getMacAddress())));
        }
        return checkBluetoothEnableOrThrow()
                .flatMap(clearScannedDevices)
                .flatMap(registerReceiver)
                .flatMap(new Function<Intent, ObservableSource<Status>>() {
                    @Override
                    public ObservableSource<Status> apply(Intent intent) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<Status>() {
                            @Override
                            public void subscribe(ObservableEmitter<Status> e) throws Exception {
                                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_NAME);
                                if (!scannedDevices.containsKey(lock.getMacAddress())) {
                                    scannedDevices.put(lock.getMacAddress(), bluetoothDevice);

                                    if (lock.getMacAddress() != null && lock.getMacAddress().equals(bluetoothDevice.getAddress())) {
                                        e.onNext(Status.DEVICE_FOUND.forBluetoothDevice(bluetoothDevice));
                                        e.onComplete();
                                    } else if (lock.getMacId() != null && BluetoothUtil.getMacIdFromName(bluetoothDevice.getName()).equals(lock.getMacId())) {
                                        e.onNext(Status.DEVICE_FOUND.forBluetoothDevice(bluetoothDevice));
                                        e.onComplete();
                                    }
                                }
                            }
                        });
                    }
                })
                .timeout(timeoutMillis, TimeUnit.MILLISECONDS, Observable.create(new ObservableOnSubscribe<Status>() {
                    @Override
                    public void subscribe(ObservableEmitter<Status> e) throws Exception {
                        if (!scannedDevices.containsKey(lock.getMacAddress())) {
                            e.onError(new BluetoothException(BluetoothException.Status.DEVICE_NOT_FOUND));
                        }
                    }
                }))
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        context.unregisterReceiver(receiver);
                        receiver = null;
                    }
                });
    }

}
