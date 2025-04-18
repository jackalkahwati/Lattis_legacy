package io.lattis.ellipse.sdk.security;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import io.lattis.ellipse.sdk.Ellipse;
import io.lattis.ellipse.sdk.Ellipse.Command;
import io.lattis.ellipse.sdk.model.Status;
import io.lattis.ellipse.sdk.util.BluetoothUtil;
import io.reactivex.Emitter;

import static io.lattis.ellipse.sdk.Ellipse.Command.Status.WRITE_OK;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.Characteristic.INFO;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.CHALLENGE_DATA;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.CHALLENGE_KEY;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.PUBLIC_KEY;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.SIGNED_MESSAGE;

class SecurityHandlerV2 extends SecurityHandler {

    SecurityHandlerV2(Emitter emitter, String userId, String signedMessage) {
        super(emitter,userId, signedMessage);
    }

    @Override
    public Status onCharacteristicChanged(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic gattCharacteristic) {
        Command.Status status = Command.Status.forValue(gattCharacteristic);
        if (status != null) {
            Ellipse.Security.Characteristic characteristic = status.getCharacteristic();
            if(characteristic != null){
                if (status.equals(WRITE_OK)) {
                    if (characteristic.equals(PUBLIC_KEY)) {
                        handleOperationFailure(CHALLENGE_KEY.forceWrite(gatt, Encryption.getChallengeKey(userId)),
                                CHALLENGE_KEY);
                    } else if (characteristic.equals(CHALLENGE_KEY)) {
                        handleOperationFailure(SIGNED_MESSAGE.forceWrite(gatt, signedMessage), SIGNED_MESSAGE);
                    }
                } else if (characteristic.equals(SIGNED_MESSAGE)) {

                    handleOperationFailure(CHALLENGE_DATA.forceRead(gatt), CHALLENGE_DATA);
                    if (status.equals(Command.Status.OWNER_REQUEST)) {
                        return Status.OWNER_REQUEST;
                    } else if(status.equals(Command.Status.GUEST_REQUEST)){
                        return Status.GUEST_REQUEST;
                    }

                } else if (characteristic.equals(CHALLENGE_DATA)) {

                    INFO.read(gatt);
                    BluetoothUtil.requestConnectionPriorityBalanced(gatt);

                    if (status.equals(Command.Status.GUEST_VERIFIED)) {
                        return Status.GUEST_VERIFIED;
                    } else {
                        return Status.OWNER_VERIFIED;
                    }

                } else if (status.equals(Command.Status.SECURITY_STATE_INVALID_ACCESS_DENIED)) {
                    gatt.disconnect();
                    return Status.ACCESS_DENIED;
                }
            }
        }
        return null;
    }
}
