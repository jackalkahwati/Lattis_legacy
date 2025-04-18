package io.lattis.ellipse.sdk.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import hugo.weaving.DebugLog;
import io.lattis.ellipse.sdk.Ellipse;
import io.lattis.ellipse.sdk.Ellipse.Boot.Version;
import io.lattis.ellipse.sdk.Ellipse.Hardware.Position;
import io.lattis.ellipse.sdk.Ellipse.Security;
import io.lattis.ellipse.sdk.alert.CrashAlertHandler;
import io.lattis.ellipse.sdk.alert.TheftAlertHandler;
import io.lattis.ellipse.sdk.exception.BluetoothException;
import io.lattis.ellipse.sdk.exception.EllipseException;
import io.lattis.ellipse.sdk.model.AccelerometerData;
import io.lattis.ellipse.sdk.model.Alert;
import io.lattis.ellipse.sdk.model.BluetoothLock;
import io.lattis.ellipse.sdk.model.FirmwareUpdateProgress;
import io.lattis.ellipse.sdk.model.Rssi;
import io.lattis.ellipse.sdk.model.Status;
import io.lattis.ellipse.sdk.security.Encryption;
import io.lattis.ellipse.sdk.security.SecurityHandler;
import io.lattis.ellipse.sdk.util.BluetoothUtil;
import io.lattis.ellipse.sdk.util.StringUtil;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
import static io.lattis.ellipse.sdk.Ellipse.Boot.Characteristic.CODE_VERSION;
import static io.lattis.ellipse.sdk.Ellipse.Boot.Characteristic.WRITE_DATA;
import static io.lattis.ellipse.sdk.Ellipse.Boot.DUMMY_VALUE;
import static io.lattis.ellipse.sdk.Ellipse.Command.Status.LOCK_UNLOCK_FAILED;
import static io.lattis.ellipse.sdk.Ellipse.Command.Status.WRITE_OK;
import static io.lattis.ellipse.sdk.Ellipse.Configuration.Characteristic.BUTTON_LOCK_SEQUENCE;
import static io.lattis.ellipse.sdk.Ellipse.Configuration.Characteristic.RESET;
import static io.lattis.ellipse.sdk.Ellipse.Configuration.Characteristic.SERIAL_NUMBER;
import static io.lattis.ellipse.sdk.Ellipse.Configuration.Mode.FACTORY;
import static io.lattis.ellipse.sdk.Ellipse.Configuration.Mode.SHIPPING;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.Characteristic.ACCELEROMETER;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.Characteristic.INFO;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.Characteristic.LOCK;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.Position.INVALID;
import static io.lattis.ellipse.sdk.Ellipse.Hardware.Position.LOCKED;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.CHALLENGE_DATA;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.PUBLIC_KEY;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.SIGNED_MESSAGE;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.STATE;
import static io.lattis.ellipse.sdk.model.FirmwareUpdateProgress.Status.IMAGE_INVALID;
import static io.lattis.ellipse.sdk.model.FirmwareUpdateProgress.Status.IMAGE_VALID;
import static io.lattis.ellipse.sdk.model.Status.DISCONNECTED;
import static io.lattis.ellipse.sdk.model.Status.DISCOVER_SERVICE;
import static io.lattis.ellipse.sdk.model.Status.SERVICE_DISCOVERED;

public class DeviceConnectionCallback extends AbstractBluetoothCallBack<Status> {

    private static final String TAG = DeviceConnectionCallback.class.getSimpleName();

    private final BehaviorSubject<Status> connectionState = BehaviorSubject.create();

    private final BehaviorSubject<Integer> temperature =  BehaviorSubject.create();
    private final BehaviorSubject<Integer> batteryLevel =  BehaviorSubject.create();
    private final BehaviorSubject<Position> lockPosition =  BehaviorSubject.create();
    private final BehaviorSubject<Ellipse.Hardware.State> hardwareStateSubject =  BehaviorSubject.create();

    private final PublishSubject<Version> versionSubject =  PublishSubject.create();
    private final PublishSubject<Rssi> rssiLevel =  PublishSubject.create();
    private PublishSubject<Rssi> rssiLiveLevel;
    private final PublishSubject<String> serialNumberSubject =  PublishSubject.create();
    private final PublishSubject<FirmwareUpdateProgress> updateVersion =  PublishSubject.create();
    private final PublishSubject<Boolean> pinCodeSubject =  PublishSubject.create();
    private final PublishSubject<Alert> alertSubject = PublishSubject.create();


    private SecurityHandler securityHandler;
    private final CrashAlertHandler crashAlertHandler = new CrashAlertHandler();
    private final TheftAlertHandler theftAlertHandler = new TheftAlertHandler();

    private Alert alertMode = Alert.OFF;
    private List<String> firmwareUpdates;
    private Version targetVersion;
    private int updatePosition = 0;
    private final BluetoothLock bluetoothLock;
    private Ellipse.Hardware.State lastHardwareState;

    public DeviceConnectionCallback(BluetoothLock bluetoothLock){
        super(null);
        this.bluetoothLock = bluetoothLock;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, "onConnectionStateChange " + BluetoothUtil.getConnectionStateName(newState)
                + " " + BluetoothUtil.getConnectionStatusName(status));
        super.onConnectionStateChange(gatt, status, newState);
        if (newState == STATE_CONNECTED) {
            if (status == GATT_SUCCESS) {
                gatt.discoverServices();
                publishState(DISCOVER_SERVICE);
            }
        } else if (newState == STATE_DISCONNECTED) {
            publishState(DISCONNECTED);
            connectionState.onComplete();
        }
    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
        Log.d(TAG, "onServicesDiscovered " + BluetoothUtil.getConnectionStatusName(status));
        publishState(SERVICE_DISCOVERED);
        STATE.setNotification(gatt, true);
        STATE.write(gatt, true);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicWrite " + characteristic.getUuid() + " status " + BluetoothUtil.getConnectionStatusName(status));
        if(LOCK.equal(characteristic)){
            INFO.read(gatt);
        } else if(CODE_VERSION.equal(characteristic)){
            CODE_VERSION.forceRead(gatt);
        } else if(BUTTON_LOCK_SEQUENCE.equal(characteristic)){
            pinCodeSubject.onNext(true);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        Log.d(TAG, "onCharacteristicRead " + characteristic.getUuid() + " status " + BluetoothUtil.getConnectionStatusName(status));
        if (status == GATT_SUCCESS) {
            if (STATE.equal(characteristic)) {
                Ellipse.Configuration.Mode mode = Ellipse.Configuration.Mode.forValue(characteristic);
                BluetoothUtil.requestConnectionPriorityHigh(gatt);
                if (mode != null) {
                    if (mode.equals(FACTORY) || mode.equals(SHIPPING)) {
                        handleOperationFailure(PUBLIC_KEY.forceWrite(gatt, bluetoothLock.getPublicKey()),emitter, PUBLIC_KEY);
                    } else {
                        handleOperationFailure(SIGNED_MESSAGE.forceWrite(gatt, bluetoothLock.getSignedMessage()),emitter, SIGNED_MESSAGE);
                    }
                } else {
                    Log.e(TAG,"Mode is null");
                }
            } else if (CHALLENGE_DATA.equal(characteristic)) {
                CHALLENGE_DATA.forceWrite(gatt, Encryption.getResult(bluetoothLock.getUserId(), characteristic.getValue()));
            } else if (CODE_VERSION.equal(characteristic)) {
                Version version = Version.forValue(characteristic);
                if(targetVersion!= null && version.isEqualToTarget(targetVersion)){
                    updateVersion.onNext(new FirmwareUpdateProgress(IMAGE_VALID));
                    RESET.write(gatt, DUMMY_VALUE);
                    targetVersion = null;
                } else {
                    if(targetVersion == null){
                        versionSubject.onNext(version);
                    } else {
                        updateVersion.onNext(new FirmwareUpdateProgress(IMAGE_INVALID));
                    }
                }
            } else if (INFO.equal(characteristic)){
                lastHardwareState = Ellipse.Hardware.State.forValue(characteristic);
                hardwareStateSubject.onNext(lastHardwareState);
                lockPosition.onNext(lastHardwareState.getPosition());
                batteryLevel.onNext(lastHardwareState.getBatteryLevel());
                rssiLevel.onNext(Rssi.LOCK.withValue(lastHardwareState.getRssiLevel()));
                temperature.onNext(lastHardwareState.getTemperature());
            } else if(SERIAL_NUMBER.equal(characteristic)){
                serialNumberSubject.onNext(StringUtil.extractSerialNumber(Ellipse.Configuration.getSerialNumber(characteristic)));
            }
        }
    }

    private void handleOperationFailure(boolean success,
                                        Emitter emitter,
                                        Security.Characteristic characteristic){
        if(!success){
           // subscriber.onError(new BluetoothException(SECURITY_CHALLENGE_FAILURE).forCharacteristic(characteristic));
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (STATE.equal(characteristic)) {
            Log.d(TAG, "onCharacteristicChanged " + characteristic.getUuid());

            if(securityHandler == null){
                try {
                    securityHandler = SecurityHandler.get(characteristic,
                            emitter,
                            bluetoothLock.getUserId(),
                            bluetoothLock.getSignedMessage());
                } catch (EllipseException e) {
                    e.printStackTrace();
                }
            }

            Ellipse.Command.Status commandStatus;

            if(Ellipse.Command.isVersion1(characteristic)){
                commandStatus = Ellipse.Command.Status.forCommandV1(characteristic);
            } else {
                commandStatus = Ellipse.Command.Status.forCommandV2(characteristic);
            }

            if(firmwareUpdates == null){
                Status state = securityHandler.onCharacteristicChanged(gatt,characteristic);
                if(state!=null){
                    //connectionState.onNext(state.forBluetoothLock(bluetoothLock));
                    publishState(state);
                }
            }

            if (commandStatus != null) {
                Log.d(TAG, "Command.Status : " + commandStatus.name());
                if (commandStatus.equals(LOCK_UNLOCK_FAILED)) {
                    lockPosition.onNext(INVALID);
                } else if(commandStatus == WRITE_OK && firmwareUpdates != null){
                    updateVersion.onNext(new FirmwareUpdateProgress(firmwareUpdates.size(), updatePosition));
                    updatePosition++;
                    if(updatePosition < firmwareUpdates.size()){
                        int position = updatePosition;
                        if(position % 15 == 0){
                            BluetoothUtil.requestConnectionPriorityHigh(gatt);
                        }
                        WRITE_DATA.writeFirmware(gatt, firmwareUpdates.get(updatePosition));
                    } else {
                        firmwareUpdates = null;
                        updatePosition = 0;
                        CODE_VERSION.write(gatt, DUMMY_VALUE);
                    }
                } else if(LOCK.equal(characteristic)){
                    INFO.read(gatt);
                }
            } else {
                Log.e(TAG, "Status null");
            }
        } else if(ACCELEROMETER.equal(characteristic)){
            onReceiveAccelerometer(Ellipse.Hardware.Accelerometer.forValue(characteristic));
        }
    }

    @DebugLog
    private void onReceiveAccelerometer(AccelerometerData data){
        if(alertMode != Alert.OFF){
            switch (alertMode){
                case THEFT:
                    theftAlertHandler.add(data);
                    if(theftAlertHandler.isInAlert()){
                        onTheftAlert(data);
                    }
                    break;
                case CRASH:
                    crashAlertHandler.add(data);
                    if(crashAlertHandler.isInAlert()){
                        alertSubject.onNext(Alert.CRASH
                                .forLockId(bluetoothLock.getLockId())
                                .withAccelerometerData(data));
                    }
                    break;
            }
        } else {
            //TODO
        }
    }

    @DebugLog
    private void onTheftAlert(AccelerometerData data){
        alertSubject.onNext(Alert.THEFT
                .forLockId(bluetoothLock.getLockId())
                .withAccelerometerData(data));
    }

    @DebugLog
    private void publishState(Status state){
        publishState(state,null);
    }

    @DebugLog
    private void publishState(Status state, @Nullable BluetoothDevice bluetoothDevice){
        if(bluetoothDevice!= null){
            bluetoothLock.setMacAddress(bluetoothDevice.getAddress());
        }
        state.forBluetoothLock(bluetoothLock);
        connectionState.onNext(state);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        STATE.forceRead(gatt);
    }

    @Override
    public void onReadRemoteRssi(final BluetoothGatt gatt, final int rssi, final int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            rssiLevel.onNext(Rssi.ANDROID.withValue(rssi));
            if(rssiLiveLevel != null){
                rssiLiveLevel.onNext(Rssi.ANDROID.withValue(rssi));
            }
        }
    }

    @NonNull
    public BehaviorSubject<Ellipse.Hardware.State> getHardwareStateSubject() {
        return hardwareStateSubject;
    }

    @NonNull
    public BehaviorSubject<Status> getConnectionStatusSubject() {
        return connectionState;
    }

    @NonNull
    public PublishSubject<Rssi> getRssiLevel() {
        return rssiLevel;
    }

    @NonNull
    public Observable<Rssi> readAndroidRssiLevel(BluetoothGatt bluetoothGatt) {
        if(bluetoothGatt.readRemoteRssi()){
            return rssiLiveLevel = PublishSubject.create();
        } else {
            return Observable.error(new BluetoothException("Unable to read rssi"));
        }
    }

    @NonNull
    public Observable<Rssi> readLockRssiLevel(BluetoothGatt bluetoothGatt) {
        INFO.read(bluetoothGatt);
        rssiLiveLevel = PublishSubject.create();
        return rssiLiveLevel;
    }

    @NonNull
    public BehaviorSubject<Integer> getBatteryLevel() {
        return batteryLevel;
    }

    @NonNull
    public BehaviorSubject<Position> getLockPosition() {
        return lockPosition;
    }

    @NonNull
    public PublishSubject<Version> getVersion() {
        return versionSubject;
    }

    @NonNull
    public PublishSubject<String> getSerialNumber() {
        return serialNumberSubject;
    }

    @NonNull
    public PublishSubject<FirmwareUpdateProgress> getUpdateVersionSubject(Version targetVersion,
                                                            List<String> firmwareUpdates) {
        this.targetVersion = targetVersion;
        this.firmwareUpdates = firmwareUpdates;
        return updateVersion;
    }

    public PublishSubject<Alert> getAlertSubject() {
        return alertSubject;
    }

    public void setAlertMode(@NonNull Alert alertMode) {
        this.alertMode = alertMode;
    }

    public @NonNull Alert getAlertMode() {
        return alertMode;
    }

    public PublishSubject<Boolean> getPinCodeSubject() {
        return pinCodeSubject;
    }

    public BluetoothLock getBluetoothLock() {
        return bluetoothLock;
    }

    public boolean isLocked(){
        return lastHardwareState != null && lastHardwareState.getPosition() == LOCKED;
    }
}