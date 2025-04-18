package cc.skylock.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import cc.skylock.myapplication.Uitls.FileUtils;
import cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService;

public class FirmwareUpdateActivity extends AppCompatActivity implements cc.skylock.myapplication.bluetooth.BluetoothDeviceStatus, View.OnClickListener {

    private SkylockBluetoothLEService mService;
    private ImageView imageView_bleConnectionStatus;
    private TextView updateNowButton;
    private ProgressBar progressBar;
    private TextView progress_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_update);
        updateNowButton = (TextView) findViewById(R.id.textView_label_update_now);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_update);
        progress_title = (TextView) findViewById(R.id.tv_loading_status);
        updateNowButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        service_init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unbindService(mServiceConnection);
        getApplicationContext().stopService(new Intent(this, SkylockBluetoothLEService.class));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textView_label_update_now: {

                updateNowButton.setEnabled(false);
                getFirmwareBytes();
            }
            break;
        }
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            if (mService == null) {
                service_init();
            }
            mService.registerBluetoothDeviceStatusListener(FirmwareUpdateActivity.this);
            mService.enableSecurityNotificationCharaterisitics(true);
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService.unregisterBluetoothDeviceStatusListener();
            mService = null;
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, cc.skylock.myapplication.bluetooth.SkylockBluetoothLEService.class);
        getApplicationContext().bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }


    private void getFirmwareBytes() {
        byte[] fileBytes = FileUtils.readFirmwareFile(this);
//        int position=0;
//        while(position*132 < fileBytes.length){
//            byte[] subBytes = subArray(fileBytes,position*132,132);
//            position++;
//        }


        mService.updateFirmware(fileBytes);
    }


    @Override
    public void onScanFailed() {

    }


    @Override
    public void onConnect() {

    }

    @Override
    public void onConnected(BluetoothGatt mBluetoothGatt) {

    }

    @Override
    public void onConnectionFailed(boolean shippingModeEnabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAlertDialog(FWUpgradeProgress.FWProgress.FW_FAIL);
            }
        });
    }

    @Override
    public void onDisConnect() {

    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }

    @Override
    public void onGetHardWareInfo(BluetoothGatt mBluetoothGatt, final BluetoothGattCharacteristic mCharacteristic) {

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt mBluetoothGatt) {
        if (mService != null)
            mService.unLock();
    }

    @Override
    public void onDeviceStatus(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {

    }

    @Override
    public void onGetSerialInfo(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onLedBlink(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onScanedDevice(BluetoothDevice device) {

    }

    @Override
    public void onFWUpgradeProgress(final FWUpgradeProgress fwUpgradeProgress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fwUpgradeProgress != null && fwUpgradeProgress.fwProgress == FWUpgradeProgress.FWProgress.FW_IN_PROGRESS) {
                    progress_title.setText("Firmware upgrade in progress");
                    progressBar.setMax(fwUpgradeProgress.maxProgress);
                    progressBar.setProgress(fwUpgradeProgress.currentProgress);
                }else if (fwUpgradeProgress.fwProgress == FWUpgradeProgress.FWProgress.FW_SUCCESS || fwUpgradeProgress.fwProgress == FWUpgradeProgress.FWProgress.FW_FAIL) {
                    updateNowButton.setEnabled(true);
                    mService.unregisterBluetoothDeviceStatusListener();
                    showAlertDialog(fwUpgradeProgress.fwProgress);
                }
            }
        });
    }


    private void showAlertDialog(FWUpgradeProgress.FWProgress fwProgress) {
        AlertDialog.Builder builder;


        String title = "";
        String message = "";
        if (fwProgress == FWUpgradeProgress.FWProgress.FW_FAIL) {
            title = "FW";
            message = "FW failed";
            progress_title.setText("Firmware upgrade failed");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light);
            } else {
                builder = new AlertDialog.Builder(this);
            }
        } else {
            title = "FW";
            message = "FW success";
            progress_title.setText("Firmware upgrade succeed");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog);
            } else {
                builder = new AlertDialog.Builder(this);
            }
        }

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
