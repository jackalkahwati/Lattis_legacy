package io.lattis.ellipse.sdk.security;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import io.lattis.ellipse.sdk.Ellipse.Command;
import io.lattis.ellipse.sdk.model.Status;
import io.lattis.ellipse.sdk.util.BluetoothUtil;
import io.reactivex.Emitter;

import static io.lattis.ellipse.sdk.Ellipse.Hardware.Characteristic.INFO;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.CHALLENGE_DATA;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.CHALLENGE_KEY;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.SIGNED_MESSAGE;

class SecurityHandlerV1 extends SecurityHandler {

    SecurityHandlerV1(Emitter emitter, String userId, String signedMessage) {
        super(emitter,userId, signedMessage);
    }

    @Override
    public Status onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Command.Status status = Command.Status.forValue(characteristic);
        if (status != null) {
            if (status.equals(Command.Status.WRITE_OK)) {
                if(CHALLENGE_KEY.get(gatt).getValue() == null){
                    handleOperationFailure(CHALLENGE_KEY.forceWrite(gatt, Encryption.getChallengeKey(userId)), CHALLENGE_KEY);
                } else if(SIGNED_MESSAGE.get(gatt).getValue() == null){
                    handleOperationFailure(SIGNED_MESSAGE.forceWrite(gatt, signedMessage), SIGNED_MESSAGE);
                }
            } else if (status.equals(Command.Status.OWNER_REQUEST)) {
                handleOperationFailure(CHALLENGE_DATA.forceRead(gatt), CHALLENGE_DATA);
                return Status.OWNER_REQUEST;
            } else if (status.equals(Command.Status.GUEST_REQUEST)) {
                handleOperationFailure(CHALLENGE_DATA.forceRead(gatt), CHALLENGE_DATA);
                return Status.GUEST_REQUEST;
            } else if (status.equals(Command.Status.OWNER_VERIFIED)) {
                INFO.read(gatt);
                BluetoothUtil.requestConnectionPriorityBalanced(gatt);
                return Status.OWNER_VERIFIED;
            } else if (status.equals(Command.Status.GUEST_VERIFIED)) {
                INFO.read(gatt);
                BluetoothUtil.requestConnectionPriorityBalanced(gatt);
                return Status.GUEST_VERIFIED;
            } else if (status.equals(Command.Status.SECURITY_STATE_INVALID_ACCESS_DENIED)) {
                gatt.disconnect();
                return Status.ACCESS_DENIED;
            }
        }
        return null;
    }
}
