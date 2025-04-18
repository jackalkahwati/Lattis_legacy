package cc.skylock.myapplication.bluetooth;

import android.bluetooth.BluetoothDevice;

import java.util.HashSet;

/**
 * Created by admin on 04/06/16.
 */
public interface SkylockBluetoothScanCallback {
    public void onScaningDevice(BluetoothDevice device);
    public void onError();
}
