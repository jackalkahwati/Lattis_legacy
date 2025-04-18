package cc.skylock.myapplication.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by admin on 05/05/16.
 */
@TargetApi(21)
public abstract class BluetoothScaner {
    private static final long SCAN_PERIOD = 40000;
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
    cc.skylock.myapplication.bluetooth.BluetoothScanCallback bluetoothScanCallback;
    private static UUID UUID_SERVICE = UUID.fromString("d3995e00-fa57-11e4-ae59-0002a5d5c51b");


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
            filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID_SERVICE)).build());
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new android.bluetooth.le.ScanSettings.Builder()
                    .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            bluetoothScanCallback = cc.skylock.myapplication.bluetooth.BluetoothScanCallback.getInstance();
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
                        if (mBluetoothAdapter != null)
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        if (mLEScanner != null)
                            mLEScanner.stopScan(bluetoothScanCallback);
                    }
//                    scanedList(deviceList);
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (mBluetoothAdapter != null)
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                if (mLEScanner != null)
                    mLEScanner.startScan(filters, settings, bluetoothScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (mBluetoothAdapter != null)
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                if (mLEScanner != null)
                    mLEScanner.stopScan(bluetoothScanCallback);
            }
//            scanedList(deviceList);
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
//            if (device.getName().contains("Ellipse")||device.getName().contains("Skylock")) {
                deviceList.add(device);
//            }

            scanedList(device);

        }
    }

    public abstract void scanedList(BluetoothDevice bluetoothDevice);


}
