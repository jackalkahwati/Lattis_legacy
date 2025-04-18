package io.lattis.ellipse.sdk.callback;

import android.bluetooth.BluetoothGatt;

import io.reactivex.ObservableEmitter;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.Characteristic.LED;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.WRITE_LED_OFF;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.WRITE_LED_ON;

public class SetLedCallback extends AbstractBluetoothCallBack<Boolean> {

    private final boolean ledOn;

    public SetLedCallback(ObservableEmitter<? super Boolean> emitter, boolean ledOn) {
        super(emitter);
        this.ledOn = ledOn;
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
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        emitter.onNext(LED.write(gatt, ledOn ? WRITE_LED_ON : WRITE_LED_OFF));
        emitter.onComplete();
        gatt.disconnect();
    }
}
