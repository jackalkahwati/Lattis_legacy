package io.lattis.ellipse.sdk.model;

import android.bluetooth.BluetoothDevice;

public enum Status {

    SCANNING,
    DEVICE_FOUND,
    DISCOVER_SERVICE,

    DISCONNECTED,

    SERVICE_DISCOVERED,
    OWNER_REQUEST,
    GUEST_REQUEST,
    OWNER_VERIFIED,
    GUEST_VERIFIED,
    ACCESS_DENIED,
    FIRMWARE_VERSION,
    UPDATING_FIRMWARE,
    ERROR;

    BluetoothLock bluetoothLock;

    BluetoothDevice bluetoothDevice;

    public Status forBluetoothLock(BluetoothLock bluetoothLock) {
        this.bluetoothLock = bluetoothLock;
        return this;
    }

    public BluetoothLock getBluetoothLock() {
        return bluetoothLock;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public Status forBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        return this;
    }
}
