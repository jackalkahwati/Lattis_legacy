package io.lattis.ellipse.sdk.callback;

import android.bluetooth.BluetoothGatt;

import java.util.Timer;
import java.util.TimerTask;

import io.lattis.ellipse.sdk.exception.BluetoothException;
import io.reactivex.ObservableEmitter;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.Characteristic.LED;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.WRITE_LED_BLINK;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.WRITE_LED_OFF;

public class BlinkLedCallback extends AbstractBluetoothCallBack<Void> {

    public BlinkLedCallback(ObservableEmitter<? super Void> disposableObserver) {
        super(disposableObserver);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if(newState == STATE_CONNECTED){
            if(status == GATT_SUCCESS){
                gatt.discoverServices();
            }
        }
    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
        boolean success = LED.write(gatt, WRITE_LED_BLINK);
        if(success){
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    LED.write(gatt, WRITE_LED_OFF);
                    emitter.onNext(null);
                    gatt.disconnect();
                    timer.cancel();
                    emitter.onComplete();
                }
            },5000);
        } else {
            emitter.onError(new BluetoothException("unable to blink led"));
        }
    }
}
