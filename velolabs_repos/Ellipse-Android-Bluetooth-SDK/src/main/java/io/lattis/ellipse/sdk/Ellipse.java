package io.lattis.ellipse.sdk;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanFilter;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.lattis.ellipse.sdk.model.AccelerometerData;
import io.lattis.ellipse.sdk.model.Coordinate;
import io.lattis.ellipse.sdk.util.BluetoothUtil;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;
import static android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
import static io.lattis.ellipse.sdk.Ellipse.Configuration.Characteristic.SERIAL_NUMBER;
import static io.lattis.ellipse.sdk.Ellipse.Security.Characteristic.STATE;

public class Ellipse {

    public static class Security {

        static final UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        public static class Service {

            private static final UUID UUID_SERVICE = UUID.fromString("d3995e00-fa57-11e4-ae59-0002a5d5c51b");

            @NonNull
            private static BluetoothGattService get(@NonNull BluetoothGatt bluetoothGatt){
                return bluetoothGatt.getService(UUID_SERVICE);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public static List<ScanFilter> getScanFilters(){
                List<ScanFilter> scanFilters = new ArrayList<>();
                scanFilters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID_SERVICE)).build());
                return scanFilters;
            }
        }

        public enum Characteristic {

            SIGNED_MESSAGE(24065, UUID.fromString("d3995e01-fa57-11e4-ae59-0002a5d5c51b")),
            PUBLIC_KEY(24066, UUID.fromString("d3995e02-fa57-11e4-ae59-0002a5d5c51b")),
            CHALLENGE_KEY(24067, UUID.fromString("d3995e03-fa57-11e4-ae59-0002a5d5c51b")),
            CHALLENGE_DATA(24068, UUID.fromString("d3995e04-fa57-11e4-ae59-0002a5d5c51b")),
            STATE(24069, UUID.fromString("d3995e05-fa57-11e4-ae59-0002a5d5c51b"));

            int id;
            UUID uuid;

            Characteristic(int id, UUID uuid) {
                this.id = id;
                this.uuid = uuid;
            }

            public BluetoothGattCharacteristic get(BluetoothGatt bluetoothGatt){
                return Service.get(bluetoothGatt).getCharacteristic(uuid);
            }

            public boolean equal(BluetoothGattCharacteristic characteristic){
                return characteristic.getUuid()!= null && characteristic.getUuid().equals(uuid);
            }

            @SuppressWarnings("unused")
            public boolean read(@NonNull BluetoothGatt bluetoothGatt){
                return bluetoothGatt.readCharacteristic(get(bluetoothGatt));
            }

            public boolean forceRead(@NonNull BluetoothGatt bluetoothGatt){
                return forceRead(bluetoothGatt,10);
            }

            public boolean forceRead(@NonNull BluetoothGatt bluetoothGatt, int numberAttempts){
                boolean valid;
                do{
                    valid = bluetoothGatt.readCharacteristic(get(bluetoothGatt));
                    if(!valid){
                        Log.e(Ellipse.class.getSimpleName(),"Unable to read characteristic "+name());
                    }
                    numberAttempts--;
                }while (!valid && numberAttempts > 0);
                return valid;
            }

            public boolean write(@NonNull BluetoothGatt bluetoothGatt, boolean notificationOn){
                return writeDescriptor(bluetoothGatt, getDescriptor(bluetoothGatt), notificationOn);
            }

            @NonNull
            private BluetoothGattDescriptor getDescriptor(@NonNull BluetoothGatt bluetoothGatt){
                return get(bluetoothGatt).getDescriptor(Security.UUID_DESCRIPTOR);
            }

            @SuppressWarnings("unused")
            public boolean write(@NonNull BluetoothGatt bluetoothGatt, String value){
                return writeSecurityCharacteristic(bluetoothGatt,get(bluetoothGatt), value);
            }

            public boolean forceWrite(@NonNull BluetoothGatt bluetoothGatt, String value){
                return forceWrite(bluetoothGatt, value, 10);
            }

            public boolean forceWrite(@NonNull BluetoothGatt bluetoothGatt, String value, int numberAttempts){
                //bluetoothGatt.beginReliableWrite();
                boolean valid;
                do{
                    valid = writeSecurityCharacteristic(bluetoothGatt,get(bluetoothGatt), value);
                    if(!valid){
                        Log.e(Ellipse.class.getSimpleName(),"Unable to write characteristic "+name());
                    }
                    numberAttempts--;
                }while (!valid && numberAttempts > 0);
                return valid;
            }

            public boolean setNotification(@NonNull BluetoothGatt bluetoothGatt, boolean on){
                return bluetoothGatt.setCharacteristicNotification(get(bluetoothGatt), on);
            }

            private static boolean writeSecurityCharacteristic(@NonNull BluetoothGatt bluetoothGatt,
                                                               final BluetoothGattCharacteristic characteristic,
                                                               final String value){
                return Ellipse.writeCharacteristic(bluetoothGatt, characteristic,
                        BluetoothUtil.encodeMessage(value));
            }

            public static Characteristic forValue(int id){
                for(Characteristic characteristic:values()){
                    if(characteristic.id == id){
                        return characteristic;
                    }
                }
                return null;
            }
        }
    }

    public static class Command {

        public static boolean isVersion1(@NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic){
            return STATE.equal(bluetoothGattCharacteristic) && bluetoothGattCharacteristic.getValue().length == 1;
        }

        public enum Status {

            WRITE_OK(new byte[]{(byte) 0x00}),
            GUEST_REQUEST(new byte[]{(byte) 0x01}),
            OWNER_REQUEST(new byte[]{(byte) 0x02}),
            GUEST_VERIFIED(new byte[]{(byte) 0x03}),
            OWNER_VERIFIED(new byte[]{(byte) 0x04}),
            IN_PROGRESS(new byte[]{(byte) 0xFF}),
            WRITE_IGNORED_INVALID_LENGTH(new byte[]{(byte) 0x80}),
            SECURITY_STATE_INVALID_ACCESS_DENIED(new byte[]{(byte) 0x81}),
            LOCK_UNLOCK_FAILED(new byte[]{(byte) 0x82}),
            INVALID_OFFSET(new byte[]{(byte) 0x83}),
            INVALID_WRITE_LENGTH(new byte[]{(byte) 0x84}),
            INVALID_PARAMETER(new byte[]{(byte) 0x85});

            private static final int OFFSET_STATUS = 1;

            private byte[] value;
            private Ellipse.Security.Characteristic characteristic;

            Status(byte[] value) {
                this.value = value;
            }

            public @Nullable Security.Characteristic getCharacteristic() {
                return characteristic;
            }

            public static @Nullable Status forCommandV1(@NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic){
                for(Status status:values()){
                    if(status.value[0] == bluetoothGattCharacteristic.getValue()[0]){
                        return status;
                    }
                }
                return null;
            }

            public static @Nullable Status forCommandV2(@NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic){
                for(Status status:values()){
                    if(status.value[0] == bluetoothGattCharacteristic.getValue()[0]){
                        status.characteristic = Ellipse.Security.Characteristic.forValue(bluetoothGattCharacteristic.getIntValue(FORMAT_UINT16, OFFSET_STATUS));
                        return status;
                    }
                }
                return null;
            }

            public static @Nullable Status forValue(@NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic){
                for(Status status:values()){
                    if(status.value[0] == bluetoothGattCharacteristic.getValue()[0]){
                        int value;
                        if(bluetoothGattCharacteristic.getValue().length > OFFSET_STATUS){
                            value = bluetoothGattCharacteristic.getIntValue(FORMAT_UINT16, OFFSET_STATUS);
                        } else {
                            value = bluetoothGattCharacteristic.getIntValue(FORMAT_UINT8,0);
                        }
                        status.characteristic = Ellipse.Security.Characteristic.forValue(value);
                        return status;
                    }
                }
                return null;
            }
        }
    }

    public static class Hardware {

        public static final byte[] WRITE_POSITION_UNLOCKED = {(byte) 0x00};
        public static final byte[] WRITE_POSITION_LOCKED = {(byte) 0x01};
        public static final byte[] WRITE_POSITION_DELAYED_LOCK = {(byte) 0xFF};

        public static final byte[] WRITE_LED_ON = {(byte) 0xFF};
        public static final byte[] WRITE_LED_OFF = {(byte) 0x00};
        public static final byte[] WRITE_LED_BLINK = {(byte) 0xCF,
                (byte) 0xCF,(byte) 0x00,(byte) 0x01,(byte) 0x00};

        @SuppressWarnings("unused")
        public static class State {

            private Position position;

            private int batteryLevel;

            private int rssiLevel;

            private int temperature;

            public State(Position position,
                         int batteryLevel,
                         int rssiLevel,
                         int temperature) {
                this.position = position;
                this.batteryLevel = batteryLevel;
                this.rssiLevel = rssiLevel;
                this.temperature = temperature;
            }

            public Position getPosition() {
                return position;
            }

            public int getBatteryLevel() {
                return batteryLevel;
            }

            public int getRssiLevel() {
                return rssiLevel;
            }

            public int getTemperature() {
                return temperature;
            }

            public static State forValue(BluetoothGattCharacteristic characteristic){
                return new State(Position.forValue(characteristic),
                                 Voltage.forValue(characteristic),
                                 Rssi.forValue(characteristic),
                                 Temperature.forValue(characteristic));
            }
        }

        static class Voltage {

            private static final int OFFSET_VOLTAGE = 0;

            static int forValue(BluetoothGattCharacteristic characteristic){
                return characteristic.getIntValue(FORMAT_UINT16, OFFSET_VOLTAGE);
            }
        }

        static class Temperature {

            private static final int OFFSET_TEMPERATURE = 2;

            static int forValue(BluetoothGattCharacteristic characteristic){
                return characteristic.getIntValue(FORMAT_UINT8, OFFSET_TEMPERATURE);
            }
        }

        static class Rssi {

            private static final int OFFSET_RSSI = 3;

            static int forValue(BluetoothGattCharacteristic characteristic){
                return characteristic.getIntValue(FORMAT_UINT8, OFFSET_RSSI);
            }
        }

        public enum Position {

            UNLOCKED(0),
            LOCKED(1),
            BETWEEN_LOCKED_UNLOCKED(2),
            INVALID(3);

            private static final int OFFSET_POSITION = 4;

            int value;

            Position(int value) {
                this.value = value;
            }

            public static Position forValue(BluetoothGattCharacteristic characteristic){
                return forValue(characteristic.getIntValue(FORMAT_UINT8, OFFSET_POSITION));
            }

            private static Position forValue(int value){
                for(Position position:values()){
                    if(position.value == value){
                        return position;
                    }
                }
                return null;
            }
        }

        @SuppressWarnings("unused")
        public static class Magnetometer {

            private static final int OFFSET_MAGNETOMETER_COORDINATE_X = 0;
            private static final int OFFSET_MAGNETOMETER_COORDINATE_Y = 2;
            private static final int OFFSET_MAGNETOMETER_COORDINATE_Z = 4;

            public static Coordinate forValue(BluetoothGattCharacteristic characteristic){
                return new Coordinate(
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_MAGNETOMETER_COORDINATE_X),
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_MAGNETOMETER_COORDINATE_Y),
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_MAGNETOMETER_COORDINATE_Z));
            }
        }

        @SuppressWarnings("unused")
        public static class Accelerometer {

            private static final int OFFSET_ACCELEROMETER_COORDINATE_X = 0;
            private static final int OFFSET_ACCELEROMETER_COORDINATE_Y = 2;
            private static final int OFFSET_ACCELEROMETER_COORDINATE_Z = 4;
            private static final int OFFSET_ACCELEROMETER_COORDINATE_DEVIATION_X = 6;
            private static final int OFFSET_ACCELEROMETER_COORDINATE_DEVIATION_Y = 8;
            private static final int OFFSET_ACCELEROMETER_COORDINATE_DEVIATION_Z = 10;
            private static final int OFFSET_ACCELEROMETER_SENSITIVITY = 12;

            public static AccelerometerData forValue(BluetoothGattCharacteristic characteristic){
                AccelerometerData data = new AccelerometerData();
                data.setMav(new Coordinate(
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_ACCELEROMETER_COORDINATE_X),
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_ACCELEROMETER_COORDINATE_Y),
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_ACCELEROMETER_COORDINATE_Z)));
                data.setDeviation(new Coordinate(
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_ACCELEROMETER_COORDINATE_DEVIATION_X),
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_ACCELEROMETER_COORDINATE_DEVIATION_Y),
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_ACCELEROMETER_COORDINATE_DEVIATION_Z)));
                data.setSensitivity(characteristic.getIntValue(FORMAT_UINT8,OFFSET_ACCELEROMETER_SENSITIVITY));
                return data;
            }
        }

        static class Service {

            private static final UUID UUID_SERVICE = UUID.fromString("d3995e40-fa57-11e4-ae59-0002a5d5c51b");

            @NonNull
            public static BluetoothGattService get(@NonNull BluetoothGatt bluetoothGatt){
                return bluetoothGatt.getService(UUID_SERVICE);
            }
        }

        public enum Characteristic {

            LED(24129, UUID.fromString("d3995e41-fa57-11e4-ae59-0002a5d5c51b")), //READ/WRITE
            LOCK(24130, UUID.fromString("d3995e42-fa57-11e4-ae59-0002a5d5c51b")),//READ/WRITE
            INFO(24131, UUID.fromString("d3995e43-fa57-11e4-ae59-0002a5d5c51b")),
            MAGNETOMETER(24132, UUID.fromString("d3995e44-fa57-11e4-ae59-0002a5d5c51b")),//NOTIFY
            CONNECTION(24133, UUID.fromString("d3995e45-fa57-11e4-ae59-0002a5d5c51b")),//READ/WRITE
            ACCELEROMETER(24134, UUID.fromString("d3995e46-fa57-11e4-ae59-0002a5d5c51b"));//NOTIFY

            int id;
            UUID uuid;

            Characteristic(int value, UUID uuid) {
                this.id = value;
                this.uuid = uuid;
            }

            public BluetoothGattCharacteristic get(BluetoothGatt bluetoothGatt){
                return Hardware.Service.get(bluetoothGatt).getCharacteristic(uuid);
            }

            public boolean equal(BluetoothGattCharacteristic characteristic){
                return characteristic.getUuid().equals(uuid);
            }

            public boolean read(@NonNull BluetoothGatt bluetoothGatt){
                return bluetoothGatt.readCharacteristic(get(bluetoothGatt));
            }

            public boolean write(@NonNull BluetoothGatt bluetoothGatt, byte[] value){
                BluetoothGattCharacteristic characteristic = get(bluetoothGatt);
                characteristic.setValue(value);
                return bluetoothGatt.writeCharacteristic(characteristic);
            }

            public boolean notify(@NonNull BluetoothGatt bluetoothGatt, boolean notificationOn){
                return writeDescriptor(bluetoothGatt, getDescriptor(bluetoothGatt), notificationOn);
            }

            @NonNull
            private BluetoothGattDescriptor getDescriptor(@NonNull BluetoothGatt bluetoothGatt){
                return get(bluetoothGatt).getDescriptor(Security.UUID_DESCRIPTOR);
            }

            public boolean setNotification(@NonNull BluetoothGatt bluetoothGatt, boolean on){
                return bluetoothGatt.setCharacteristicNotification(get(bluetoothGatt), on);
            }
        }
    }

    public static class Configuration {

        private static final byte[] WRITE_RESET_SHIPPING_MODE = {(byte) 0xBC};
        public static final byte[] WRITE_RESET_FACTORY_MODE = {(byte) 0xBD};
        public static final byte[] WRITE_RESET_DEVELOPMENT_MODE = {(byte) 0xBE};

        static class Service {

            private static final UUID UUID_SERVICE = UUID.fromString("d3995e80-fa57-11e4-ae59-0002a5d5c51b");

            @NonNull
            public static BluetoothGattService get(@NonNull BluetoothGatt bluetoothGatt){
                return bluetoothGatt.getService(UUID_SERVICE);
            }
        }

        public enum Characteristic {

            RESET(24193, UUID.fromString("d3995e81-fa57-11e4-ae59-0002a5d5c51b")),
            LOCK_ADJUST(24194, UUID.fromString("d3995e82-fa57-11e4-ae59-0002a5d5c51b")),
            SERIAL_NUMBER(24195, UUID.fromString("d3995e83-fa57-11e4-ae59-0002a5d5c51b")),
            BUTTON_LOCK_SEQUENCE(24196, UUID.fromString("d3995e84-fa57-11e4-ae59-0002a5d5c51b")),
            CODE_VERSION(23809, UUID.fromString("d3995d01-fa57-11e4-ae59-0002a5d5c51b")),
            WRITE_DATA(23810, UUID.fromString("d3995d02-fa57-11e4-ae59-0002a5d5c51b"));

            int value;
            UUID uuid;

            Characteristic(int value, UUID uuid) {
                this.value = value;
                this.uuid = uuid;
            }

            public BluetoothGattCharacteristic get(BluetoothGatt bluetoothGatt){
                return Configuration.Service.get(bluetoothGatt).getCharacteristic(uuid);
            }

            public boolean equal(BluetoothGattCharacteristic characteristic){
                return characteristic.getUuid().equals(uuid);
            }

            public boolean read(@NonNull BluetoothGatt bluetoothGatt){
                return bluetoothGatt.readCharacteristic(get(bluetoothGatt));
            }

            public boolean write(@NonNull BluetoothGatt bluetoothGatt, byte[] value){
                BluetoothGattCharacteristic characteristic = get(bluetoothGatt);
                characteristic.setValue(value);
                return bluetoothGatt.writeCharacteristic(characteristic);
            }

            public boolean writePinCode(@NonNull BluetoothGatt bluetoothGatt,
                                        String pinCode) {
                for (int i = pinCode.length(); i < 16; i++) {
                    pinCode = pinCode + "0";
                }
                BluetoothGattCharacteristic characteristic = get(bluetoothGatt);
                return BUTTON_LOCK_SEQUENCE.equal(characteristic)
                        && Ellipse.writeCharacteristic(
                        bluetoothGatt, characteristic, BluetoothUtil.encodeMessage(pinCode));
            }
        }

        public static String getSerialNumber(BluetoothGattCharacteristic characteristic){
            return SERIAL_NUMBER.equal(characteristic) ? characteristic.getStringValue(0) : "Wrong Serial Number Characteristic";
        }

        public enum Mode {

            UNKNOWN(0),
            FACTORY(5),
            SHIPPING(6);

            private static final int OFFSET_MODE = 0;

            int value;

            Mode(int value) {
                this.value = value;
            }

            public static Mode forValue(BluetoothGattCharacteristic characteristic){
                return forValue(characteristic.getIntValue(FORMAT_UINT8, OFFSET_MODE));
            }

            private static Mode forValue(int value){
                for(Configuration.Mode mode:values()){
                    if(mode.value == value){
                        return mode;
                    }
                }
                return null;
            }
        }
    }

    public static class Boot {

        public static final byte[] DUMMY_VALUE = {(byte) 0xFF};

        static class Service {

            @NonNull
            public static BluetoothGattService get(@NonNull BluetoothGatt bluetoothGatt){
                return bluetoothGatt.getService(Configuration.Service.UUID_SERVICE);
            }
        }

        @SuppressWarnings("unused")
        public enum DownloadStatus {

            DATA_RECEIVED(new byte[] {(byte) 0x01}),
            DOWNLOAD_EVENT_VALID_IMAGE(new byte[] {(byte) 0x02}),
            DOWNLOAD_EVENT_VALID_IMAGE_ALREADY_USED(new byte[] {(byte) 0x03}),
            DOWNLOAD_EVENT_INVALID_IMAGE_BAD_HEADER(new byte[] {(byte) 0x04}),
            DOWNLOAD_EVENT_INVALID_IMAGE_BAD_MATCH_WITH_SOFT_DEVICE(new byte[] {(byte) 0x05}),
            DOWNLOAD_EVENT_INVALID_IMAGE_BAD_SIGNATURE(new byte[] {(byte) 0x06}),
            DOWNLOAD_EVENT_INVALID_DATA_SIZE(new byte[] {(byte) 0x0A}),
            DOWNLOAD_EVENT_INVALID_DATA_OFFSET(new byte[] {(byte) 0x0B}),
            CODE_VERSION_UPDATED(new byte[] {(byte) 0x20});

            private final byte[] value;

            DownloadStatus(byte[] value) {
                this.value = value;
            }
        }

        public enum Characteristic {

            CODE_VERSION(23809, UUID.fromString("d3995d01-fa57-11e4-ae59-0002a5d5c51b")),
            WRITE_DATA(23810, UUID.fromString("d3995d02-fa57-11e4-ae59-0002a5d5c51b")),
            STATUS(23811, UUID.fromString("d3995d03-fa57-11e4-ae59-0002a5d5c51b")),
            DOWNLOAD_DONE(23812, UUID.fromString("d3995d04-fa57-11e4-ae59-0002a5d5c51b")),;

            int value;
            UUID uuid;

            Characteristic(int value, UUID uuid) {
                this.value = value;
                this.uuid = uuid;
            }

            public BluetoothGattCharacteristic get(BluetoothGatt bluetoothGatt){
                return Boot.Service.get(bluetoothGatt).getCharacteristic(uuid);
            }

            public boolean equal(BluetoothGattCharacteristic characteristic){
                return characteristic.getUuid()!= null && characteristic.getUuid().equals(uuid);
            }

            public boolean read(@NonNull BluetoothGatt bluetoothGatt){
                return bluetoothGatt.readCharacteristic(get(bluetoothGatt));
            }

            public boolean forceRead(@NonNull BluetoothGatt bluetoothGatt){
                return forceRead(bluetoothGatt, 20);
            }

            public boolean forceRead(@NonNull BluetoothGatt bluetoothGatt, int numberAttempts){
                boolean valid;
                do{
                    valid = bluetoothGatt.readCharacteristic(get(bluetoothGatt));
                    if(!valid){
                        Log.e(Ellipse.class.getSimpleName(),"Unable to read characteristic "+name());
                    }
                    numberAttempts--;
                }while (!valid && numberAttempts > 0);
                return valid;
            }

            public boolean write(@NonNull BluetoothGatt bluetoothGatt, byte[] value){
                BluetoothGattCharacteristic characteristic = get(bluetoothGatt);
                characteristic.setValue(value);
                return bluetoothGatt.writeCharacteristic(characteristic);
            }

            public boolean setNotification(@NonNull BluetoothGatt bluetoothGatt, boolean on){
                return bluetoothGatt.setCharacteristicNotification(get(bluetoothGatt), on);
            }

            public static Boot.Characteristic forValue(int value){
                for(Boot.Characteristic characteristic:values()){
                    if(characteristic.value == value){
                        return characteristic;
                    }
                }
                return null;
            }

            public boolean writeFirmware(@NonNull BluetoothGatt bluetoothGatt,
                                        final String version) {
                BluetoothGattCharacteristic characteristic = get(bluetoothGatt);
                return WRITE_DATA.equal(characteristic)
                        && Ellipse.writeCharacteristic(bluetoothGatt, characteristic, BluetoothUtil.encodeMessage(version));
            }
        }

        public static class Version {

            private static final int INVALID_IMAGE_TYPE = 0;
            private static final int IMAGE_TYPE_BOOTLOADER = 1;
            private static final int IMAGE_TYPE_APPLICATION = 2;

            private static final int OFFSET_SOFT_DEVICE_INFO_LINK_LAYER_VERSION = 0;
            private static final int OFFSET_SOFT_DEVICE_INFO_MANUFACTURER_ID = 1;
            private static final int OFFSET_SOFT_DEVICE_INFO_LINK_LAYER_SUB_VERSION = 3;
            private static final int OFFSET_BOOT_LOADER_VERSION = 5;
            private static final int OFFSET_BOOT_LOADER_REVISION = 7;
            private static final int OFFSET_FIRMWARE_VERSION = 9;
            private static final int OFFSET_FIRMWARE_REVISION = 11;
            private static final int OFFSET_IMAGE_TYPE = 13;
            private static final int OFFSET_IMAGE_VERSION = 14;
            private static final int OFFSET_IMAGE_REVISION = 16;

            int bootLoaderVersion;
            int bootLoaderRevision;
            int applicationVersion;
            int applicationRevision;
            int imageType;
            int imageVersion;
            int imageRevision;

            Version(int applicationVersion, int applicationRevision) {
                this.applicationVersion = applicationVersion;
                this.applicationRevision = applicationRevision;
            }

            Version(int bootLoaderVersion,
                    int bootLoaderRevision,
                    int applicationVersion,
                    int applicationRevision,
                    int imageType,
                    int imageVersion,
                    int imageRevision) {
                this.bootLoaderVersion = bootLoaderVersion;
                this.bootLoaderRevision = bootLoaderRevision;
                this.applicationVersion = applicationVersion;
                this.applicationRevision = applicationRevision;
                this.imageType = imageType;
                this.imageVersion = imageVersion;
                this.imageRevision = imageRevision;
            }

            public static Version forValue(BluetoothGattCharacteristic characteristic) {
                return new Version(characteristic.getIntValue(FORMAT_UINT16, OFFSET_BOOT_LOADER_VERSION),
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_BOOT_LOADER_REVISION),
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_FIRMWARE_VERSION),
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_FIRMWARE_REVISION),
                        characteristic.getIntValue(FORMAT_UINT8,  OFFSET_IMAGE_TYPE),
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_IMAGE_VERSION),
                        characteristic.getIntValue(FORMAT_UINT16, OFFSET_IMAGE_REVISION));
            }

            @SuppressWarnings("unused")
            public int getBootLoaderVersion() {
                return bootLoaderVersion;
            }

            @SuppressWarnings("unused")
            public int getBootLoaderRevision() {
                return bootLoaderRevision;
            }

            @SuppressWarnings("unused")
            public int getApplicationVersion() {
                return applicationVersion;
            }

            @SuppressWarnings("unused")
            public int getApplicationRevision() {
                return applicationRevision;
            }

            @SuppressWarnings("unused")
            public int getImageType() {
                return imageType;
            }

            @SuppressWarnings("unused")
            public int getImageVersion() {
                return imageVersion;
            }

            @SuppressWarnings("unused")
            public int getImageRevision() {
                return imageRevision;
            }

            public boolean isEqualToTarget(Version version){
                return this.imageVersion == version.applicationVersion && this.imageRevision == version.applicationRevision;
            }

            public static Version forCompare(int version, int revision){
                return new Version(version,revision);
            }
        }
    }

    private static boolean writeDescriptor(@NonNull BluetoothGatt bluetoothGatt,
                                           @NonNull BluetoothGattDescriptor descriptor,
                                           boolean notificationOn){
        descriptor.setValue(notificationOn ? ENABLE_NOTIFICATION_VALUE : DISABLE_NOTIFICATION_VALUE);
        return bluetoothGatt.writeDescriptor(descriptor);
    }

    private static boolean writeCharacteristic(@NonNull BluetoothGatt bluetoothGatt,
                                               final BluetoothGattCharacteristic characteristic,
                                               final byte[] value){
        characteristic.setValue(value);
        return bluetoothGatt.writeCharacteristic(characteristic);
    }
}
