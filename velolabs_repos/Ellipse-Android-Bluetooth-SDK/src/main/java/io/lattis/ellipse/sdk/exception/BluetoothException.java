package io.lattis.ellipse.sdk.exception;

import io.lattis.ellipse.sdk.Ellipse;

public class BluetoothException extends Throwable {

    private Ellipse.Security.Characteristic characteristic;

    public enum Status {
        BLUETOOTH_DISABLED,
        NO_DEVICE_FOUND,
        DEVICE_NOT_FOUND,
        SECURITY_CHALLENGE_FAILURE;
    }

    private Status status;


    public BluetoothException(Status status) {
        this.status = status;
    }

    public BluetoothException(String message) {
        super(message);
    }

    public Status getStatus() {
        return status;
    }

    public BluetoothException forCharacteristic(Ellipse.Security.Characteristic characteristic) {
        this.characteristic = characteristic;
        return this;
    }

    public Ellipse.Security.Characteristic getCharacteristic() {
        return characteristic;
    }

    @Override
    public String toString() {
        return "BluetoothException{" +
                "characteristic=" + characteristic +
                ", status=" + status +
                '}';
    }
}
