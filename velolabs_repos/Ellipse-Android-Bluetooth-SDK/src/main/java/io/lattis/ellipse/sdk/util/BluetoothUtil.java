package io.lattis.ellipse.sdk.util;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.bluetooth.BluetoothGatt.GATT_CONNECTION_CONGESTED;
import static android.bluetooth.BluetoothGatt.GATT_FAILURE;
import static android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION;
import static android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION;
import static android.bluetooth.BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
import static android.bluetooth.BluetoothGatt.GATT_INVALID_OFFSET;
import static android.bluetooth.BluetoothGatt.GATT_READ_NOT_PERMITTED;
import static android.bluetooth.BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGatt.GATT_WRITE_NOT_PERMITTED;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTING;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTING;

public class BluetoothUtil {

    public static String getConnectionStateName(int connectionState){
        switch (connectionState){
            case STATE_CONNECTED: return "CONNECTED";
            case STATE_DISCONNECTED: return "DISCONNECTED";
            case STATE_CONNECTING: return "CONNECTING";
            case STATE_DISCONNECTING :return "DISCONNECTING";
            default:return "UNKNOWN";
        }
    }

    public static String getConnectionStatusName(int connectionStatus){
        switch (connectionStatus){
            case GATT_CONNECTION_CONGESTED: return "CONNECTION_CONGESTED";
            case GATT_FAILURE: return "FAILURE";
            case GATT_INSUFFICIENT_AUTHENTICATION: return "INSUFFICIENT_AUTHENTICATION";
            case GATT_INSUFFICIENT_ENCRYPTION :return "INSUFFICIENT_ENCRYPTION";
            case GATT_INVALID_ATTRIBUTE_LENGTH :return "INVALID_ATTRIBUTE_LENGTH";
            case GATT_INVALID_OFFSET :return "INVALID_OFFSET";
            case GATT_READ_NOT_PERMITTED :return "READ_NOT_PERMITTED";
            case GATT_REQUEST_NOT_SUPPORTED :return "REQUEST_NOT_SUPPORTED";
            case GATT_SUCCESS :return "SUCCESS";
            case GATT_WRITE_NOT_PERMITTED :return "WRITE_NOT_PERMITTED";
            default:return "UNKNOWN";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static List<ScanFilter> getScanFiltersFor(String macAddress){
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setDeviceAddress(macAddress).build());
        return filters;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static ScanSettings getScanSettings(){
        return new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
    }

    public static String getMacIdFromName(String name) {
        return name.contains("-") ? name.split("-")[1] : name.split(" ")[1];
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        Log.i("bytesToHex", "" + new String(hexChars));
        return new String(hexChars);
    }

    public static byte[] encodeMessage(String message){
        int len = message.length();
        byte[] byteMessage = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            byteMessage[i / 2] = (byte) ((Character.digit(message.charAt(i), 16) << 4)
                    + Character.digit(message.charAt(i + 1), 16));
        }
        return byteMessage;
    }

    public static void requestConnectionPriorityHigh(BluetoothGatt bluetoothGatt){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            bluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        }
    }

    public static void requestConnectionPriorityBalanced(BluetoothGatt bluetoothGatt){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            bluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
        }
    }
}
