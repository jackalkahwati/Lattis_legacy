package cc.skylock.myapplication.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.List;

/**
 * Created by Velo Labs Android on 25-05-2016.
 */
@TargetApi(23)
public class BluetoothScanCallback extends ScanCallback {
    cc.skylock.myapplication.bluetooth.BluetoothScaner bluetoothScaner;
    static BluetoothScanCallback bluetoothScanCallback;
    void BluetoothDevice( ){

    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        if (results != null) {
                for (ScanResult sr : results) {
                    Log.i("ScanResult - Results", sr.toString());
                }
            }

    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        if (result != null) {
                Log.i("callbackType", String.valueOf(callbackType));
                Log.i("result", result.toString());
                BluetoothDevice btDevice = result.getDevice();
                if (btDevice != null) {
                   bluetoothScaner.addListOfBluetoothDevices(btDevice);
                } else
                    Log.i("Device btDevice : ", "null");

            }
    }
static BluetoothScanCallback getInstance(){
    if(bluetoothScanCallback == null){
        bluetoothScanCallback = new BluetoothScanCallback();
    }
    return bluetoothScanCallback;
}
}
