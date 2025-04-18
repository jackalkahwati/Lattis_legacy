package io.lattis.ellipse.sdk.scanner;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.lattis.ellipse.sdk.Ellipse;
import io.lattis.ellipse.sdk.exception.BluetoothException;
import io.lattis.ellipse.sdk.model.BluetoothLock;
import io.lattis.ellipse.sdk.model.Status;
import io.lattis.ellipse.sdk.util.BluetoothUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;

import static io.lattis.ellipse.sdk.exception.BluetoothException.Status.BLUETOOTH_DISABLED;
import static io.lattis.ellipse.sdk.exception.BluetoothException.Status.DEVICE_NOT_FOUND;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class
LollipopBluetoothScanner extends BluetoothScanner {

    private static final String TAG = LollipopBluetoothScanner.class.getSimpleName();
    private ScanSettings settings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build();
    private ScanCallback scanCallback;
    private BluetoothLeScanner bluetoothLeScanner;

    LollipopBluetoothScanner(Context context) {
        super(context);
        this.bluetoothLeScanner = bluetoothManager.getAdapter().getBluetoothLeScanner();
    }

    private Function<Boolean, Observable<BluetoothLeScanner>> getBluetoothScanner = new Function<Boolean, Observable<BluetoothLeScanner>>() {
        @Override
        public Observable<BluetoothLeScanner> apply(final Boolean isBluetoothEnabled) throws Exception {
            return Observable.create(new ObservableOnSubscribe<BluetoothLeScanner>() {
                @Override
                public void subscribe(ObservableEmitter<BluetoothLeScanner> emitter) throws Exception {
                    if(scanCallback!=null){
                        bluetoothLeScanner.stopScan(scanCallback);
                        scanCallback = null;
                    }
                    if(isBluetoothEnabled){
                        emitter.onNext(bluetoothLeScanner = bluetoothManager.getAdapter().getBluetoothLeScanner());
                    } else {
                        emitter.onError(new BluetoothException(BLUETOOTH_DISABLED));
                    }
                }
            });
        }
    };

    @Override
    public Observable<BluetoothLock> startScan(final int scanDurationMillis) {
        return checkBluetoothEnableOrThrow()
                .flatMap(getBluetoothScanner)
                .flatMap(new Function<BluetoothLeScanner, Observable<BluetoothLock>>() {
                    @Override
                    public Observable<BluetoothLock> apply(final BluetoothLeScanner bluetoothLeScanner) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<BluetoothLock>() {
                            @Override
                            public void subscribe(final ObservableEmitter<BluetoothLock> emitter) throws Exception {
                                scannedDevices.clear();
                                bluetoothLeScanner.startScan(Ellipse.Security.Service.getScanFilters(),
                                        settings, scanCallback = new ScanCallback() {
                                            @Override
                                            public void onScanResult(int callbackType, ScanResult result) {
                                                super.onScanResult(callbackType, result);
                                                BluetoothLock lock = lockMapper.mapOut(result.getDevice());
                                                if(!scannedDevices.containsKey(lock.getMacAddress())){
                                                    scannedDevices.put(lock.getMacAddress(), result.getDevice());
                                                    emitter.onNext(lock);
                                                }
                                            }

                                            @Override
                                            public void onScanFailed(int errorCode) {
                                                super.onScanFailed(errorCode);
                                                emitter.onError(new BluetoothException("Scan Failed "+errorCode));
                                            }
                                        });

                            }
                }).timeout(scanDurationMillis, TimeUnit.MILLISECONDS,
                                Observable.create(new ObservableOnSubscribe<BluetoothLock>() {
                                    @Override
                                    public void subscribe(ObservableEmitter<BluetoothLock> emitter) throws Exception {
                                        emitter.onComplete();
                                    }
                })).doOnDispose(new Action() {
                            @Override
                            public void run() throws Exception {
                                if(scanCallback!=null){
                                    bluetoothLeScanner.stopScan(scanCallback);
                                    scanCallback = null;
                                }
                            }
                        });
            }
        });
    }

    @Override
    public Observable<Status> findDevice(final BluetoothLock lock, final int timeoutMillis) {
        return findDevice(lock, timeoutMillis,false);
    }

    @Override
    public Observable<Status> findDevice(final BluetoothLock lock, int timeoutMillis, boolean skipCache) {
        if(lock.getMacAddress() != null && scannedDevices.containsKey(lock.getMacAddress()) && !skipCache) {
            return Observable.just(Status.DEVICE_FOUND.forBluetoothDevice(scannedDevices.get(lock.getMacAddress())));
        }
        return checkBluetoothEnableOrThrow()
                .flatMap(getBluetoothScanner)
                .flatMap(new Function<BluetoothLeScanner, Observable<Status>>() {
                    @Override
                    public Observable<Status> apply(final BluetoothLeScanner bluetoothLeScanner) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<Status>() {
                            @Override
                            public void subscribe(final ObservableEmitter<Status> emitter) throws Exception {
                                emitter.onNext(Status.SCANNING);
                                bluetoothLeScanner.startScan(
                                        //macAddress != null ? BluetoothUtil.getScanFiltersFor(macAddress) :
                                        Ellipse.Security.Service.getScanFilters(),
                                        BluetoothUtil.getScanSettings(), new ScanCallback() {
                                            @Override
                                            public void onScanResult(int callbackType, ScanResult result) {
                                                super.onScanResult(callbackType, result);
                                                if(lock.getMacAddress() != null && lock.getMacAddress().equals(result.getDevice().getAddress())){
                                                    Log.d(TAG,"Device found with macAddress "+lock.getMacAddress()+" Rssi:" +result.getRssi());
                                                    bluetoothLeScanner.stopScan(this);
                                                    scannedDevices.put(lock.getMacAddress(),result.getDevice());
                                                    emitter.onNext(Status.DEVICE_FOUND.forBluetoothDevice(result.getDevice()));
                                                } else if(lock.getMacId()!= null && BluetoothUtil.getMacIdFromName(result.getDevice().getName()).equals(lock.getMacId())){
                                                    Log.d(TAG,"Device found with macId "+lock.getMacId() +" Rssi:" +result.getRssi());
                                                    bluetoothLeScanner.stopScan(this);
                                                    scannedDevices.put(lock.getMacAddress(),result.getDevice());
                                                    emitter.onNext(Status.DEVICE_FOUND.forBluetoothDevice(result.getDevice()));
                                                }
                                            }

                                            @Override
                                            public void onBatchScanResults(List<ScanResult> results) {
                                                super.onBatchScanResults(results);
                                            }

                                            @Override
                                            public void onScanFailed(int errorCode) {
                                                super.onScanFailed(errorCode);
                                                Log.d(TAG,"Scan Failed with errorCode "+errorCode);
                                                emitter.onError(new Throwable("Scan Failed with errorCode "+errorCode));
                                            }
                                        });
                            }
                        });
                    }
                }).timeout(timeoutMillis, TimeUnit.MILLISECONDS, Observable.create(new ObservableOnSubscribe<Status>() {
                    @Override
                    public void subscribe(ObservableEmitter<Status> emitter) throws Exception {
                        if(!scannedDevices.containsKey(lock.getMacAddress())){
                            emitter.onError(new BluetoothException(DEVICE_NOT_FOUND));
                        }
                    }
                })).doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        if(scanCallback!=null){
                            bluetoothLeScanner.stopScan(scanCallback);
                            scanCallback = null;
                        }
                    }
                });
    }
}
