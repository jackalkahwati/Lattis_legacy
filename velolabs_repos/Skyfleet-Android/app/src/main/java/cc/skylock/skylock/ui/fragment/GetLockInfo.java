package cc.skylock.skylock.ui.fragment;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public interface GetLockInfo {
    public void onGetHardwareInfo(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    public void onGetBLESignal(BluetoothGatt gatt, int value);

    public void onBleDisconnect();

    public  void onBoardFailed();

    public void onConnectionTimeOut();

}


