package io.lattis.ellipse.sdk.security;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.Nullable;

import io.lattis.ellipse.sdk.Ellipse;
import io.lattis.ellipse.sdk.exception.BluetoothException;
import io.lattis.ellipse.sdk.exception.EllipseException;
import io.lattis.ellipse.sdk.model.Status;
import io.reactivex.Emitter;

import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.STATE;
import static io.lattis.ellipse.sdk.exception.BluetoothException.Status.SECURITY_CHALLENGE_FAILURE;

public abstract class SecurityHandler {

    String userId;
    String signedMessage;
    private Emitter emitter;

    SecurityHandler(Emitter emitter, String userId, String signedMessage) {
        this.emitter = emitter;
        this.userId = userId;
        this.signedMessage = signedMessage;
    }

    public static SecurityHandler get(BluetoothGattCharacteristic characteristic,
                                      Emitter emitter,
                                      String userId,
                                      String signedMessage) throws EllipseException {
        if(STATE.equal(characteristic)){
            if(Ellipse.Command.isVersion1(characteristic)){
                return new SecurityHandlerV1(emitter,userId, signedMessage);
            } else {
                return new SecurityHandlerV2(emitter,userId, signedMessage);
            }
        } else {
            throw new EllipseException("You can get the Security Handler only from the Security STATE characteristic");
        }
    }

    public abstract @Nullable
    Status onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    void handleOperationFailure(boolean success, Ellipse.Security.Characteristic characteristic){
        if(!success){
            emitter.onError(new BluetoothException(SECURITY_CHALLENGE_FAILURE).forCharacteristic(characteristic));
        }
    }
}