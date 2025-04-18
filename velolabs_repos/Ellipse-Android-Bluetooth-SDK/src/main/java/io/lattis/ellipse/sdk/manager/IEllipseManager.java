package io.lattis.ellipse.sdk.manager;

import java.util.List;

import io.lattis.ellipse.sdk.Ellipse;
import io.lattis.ellipse.sdk.model.Alert;
import io.lattis.ellipse.sdk.model.BluetoothLock;
import io.lattis.ellipse.sdk.model.FirmwareUpdateProgress;
import io.lattis.ellipse.sdk.model.Rssi;
import io.lattis.ellipse.sdk.model.Status;
import io.reactivex.Observable;

public interface IEllipseManager {

    Observable<Void> blinkLed(String macAddress);

    Observable<Boolean> setLedState(final String macAddress,
                                    final boolean ledOn);

    Observable<Boolean> isConnectedTo(final BluetoothLock bluetoothLock);

    Observable<Status> connect(final BluetoothLock bluetoothLock);

    Observable<Boolean> disconnect(final BluetoothLock bluetoothLock);

    Observable<Boolean> disconnectAllLocks();

    Observable<Boolean> reset(final BluetoothLock bluetoothLock);

    Observable<Ellipse.Boot.Version> getFirmwareVersion(final BluetoothLock bluetoothLock);

    Observable<String> getSerialNumber(final BluetoothLock lock);

    Observable<FirmwareUpdateProgress> updateFirmware(final BluetoothLock bluetoothLock,
                                                      final Ellipse.Boot.Version targetVersion,
                                                      final List<String> firmwareUpdates);

    Observable<Boolean> setPosition(final BluetoothLock bluetoothLock,
                                    final boolean locked);

    Observable<Boolean> setPinCode(final BluetoothLock bluetoothLock,
                                   final String pinCode);

    Observable<BluetoothLock> startScan(int scanDurationMillis);

    Observable<BluetoothLock> getLastConnectedLock();

    Observable<List<BluetoothLock>> getConnectLocks();

    Observable<Ellipse.Hardware.State> observeHardwareState(final BluetoothLock bluetoothLock);

    Observable<Boolean> observeLedState(String macAddress);

    Observable<Rssi> observeRssiLevel(final BluetoothLock bluetoothLock, int refreshInterval);

    Observable<Integer> observeBatteryLevel(final BluetoothLock bluetoothLock);

    Observable<Status> observeLockState(final BluetoothLock bluetoothLock);

    Observable<Ellipse.Hardware.Position> observeLockPosition(final BluetoothLock bluetoothLock);

    Observable<Status> observeLockStatus(final BluetoothLock bluetoothLock);

    Observable<Alert> observeAlerts(final BluetoothLock bluetoothLock);

    Observable<Alert> getAlertMode(final BluetoothLock bluetoothLock);

    Observable<Alert> setAlertMode(final BluetoothLock bluetoothLock, Alert alert);

    Observable<Boolean> setAutoLock(final BluetoothLock bluetoothLock, boolean active);

    Observable<Boolean> setAutoUnlock(final BluetoothLock bluetoothLock, boolean active);

    Observable<BluetoothLock> observeDisconnectedLock();

    Observable<BluetoothLock> observeConnectedLock();

    Observable<List<BluetoothLock>> observeConnectedLocks();
}
