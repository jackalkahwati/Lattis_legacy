package cc.skylock.skylock.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by admin on 05/05/16.
 */
@TargetApi(21)
public abstract class BluetoothScaner {
    private static final long SCAN_PERIOD = 5000;
    private android.bluetooth.le.BluetoothLeScanner mLEScanner;
    private android.bluetooth.le.ScanSettings settings;
    private List<android.bluetooth.le.ScanFilter> filters;
    private HashSet<BluetoothDevice> deviceList = new HashSet<BluetoothDevice>();
    private Handler mHandler = new Handler();
    private final static String TAG = BluetoothScaner.class.getSimpleName();
    public BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    public static Context context;
    public String mCalledMode = null;
    BluetoothScanCallback bluetoothScanCallback;

    BluetoothScaner(Context context) {
        this.context = context;
        initialize();

    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through

        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            filters = new ArrayList<android.bluetooth.le.ScanFilter>();
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new android.bluetooth.le.ScanSettings.Builder()
                    .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            bluetoothScanCallback = BluetoothScanCallback.getInstance();
            bluetoothScanCallback.bluetoothScaner = this;
        }

        return true;
    }

    public void scanLeDevice(boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        if (mLEScanner != null && mBluetoothAdapter.isEnabled())
                            mLEScanner.stopScan(bluetoothScanCallback);
                    }
                    scanedList(deviceList);
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                if (mLEScanner != null&& mBluetoothAdapter.isEnabled())
                    mLEScanner.startScan(filters, settings, bluetoothScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                if (mLEScanner != null && mBluetoothAdapter.isEnabled())
                    mLEScanner.stopScan(bluetoothScanCallback);
            }
            scanedList(deviceList);
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {

                    Log.i("onLeScan", device.toString());
                    addListOfBluetoothDevices(device);
                }
            };

    public void addListOfBluetoothDevices(BluetoothDevice device) {
        if (device.getName() != null) {
            if (device.getName().contains("Ellipse")) {
                deviceList.add(device);
            }

        }
    }

    public abstract void scanedList(HashSet<BluetoothDevice> bluetoothDevices);


}
