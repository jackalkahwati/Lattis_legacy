package cc.skylock.skylock.ui;

import android.bluetooth.BluetoothDevice;

import java.util.HashSet;

/**
 * Created by admin on 25/05/16.
 */
public interface GetBluetoothScanedDevices {

        public void scanedDevices(HashSet<BluetoothDevice> bluetoothDevices);

        public void deviceConnected();
}
