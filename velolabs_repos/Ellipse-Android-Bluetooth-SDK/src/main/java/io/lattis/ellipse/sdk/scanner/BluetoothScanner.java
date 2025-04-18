package io.lattis.ellipse.sdk.scanner;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;

import java.util.HashMap;

import io.lattis.ellipse.sdk.exception.BluetoothException;
import io.lattis.ellipse.sdk.mapper.BluetoothLockMapper;
import io.lattis.ellipse.sdk.model.BluetoothLock;
import io.lattis.ellipse.sdk.model.Status;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

import static io.lattis.ellipse.sdk.exception.BluetoothException.Status.BLUETOOTH_DISABLED;

public abstract class BluetoothScanner {

    BluetoothManager bluetoothManager;
    HashMap<String, BluetoothDevice> scannedDevices = new HashMap<>();
    BluetoothLockMapper lockMapper = new BluetoothLockMapper();

    public static BluetoothScanner get(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return new LollipopBluetoothScanner(context);
        } else {
            return new KitkatBluetoothScanner(context);
        }
    }

    BluetoothScanner(Context context) {
        this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public abstract Observable<BluetoothLock> startScan(final int scanDurationMillis);

    public abstract Observable<Status> findDevice(final BluetoothLock lock, int timeoutMillis);

    public abstract Observable<Status> findDevice(final BluetoothLock lock, int timeoutMillis, boolean skipCache);

    private Observable<Boolean> isBluetoothEnable(){
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                emitter.onNext(bluetoothManager.getAdapter().isEnabled());
            }
        });
    }

    public Observable<Boolean> checkBluetoothEnableOrThrow() {
        return isBluetoothEnable().flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
            @Override
            public ObservableSource<Boolean> apply(Boolean enabled) throws Exception {
                if(enabled){
                    return Observable.just(true);
                } else {
                    return Observable.error(new BluetoothException(BLUETOOTH_DISABLED));
                }
            }
        });
    }
}