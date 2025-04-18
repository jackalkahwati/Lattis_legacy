package cc.skylock.skylock.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

import cc.skylock.skylock.Bean.FirmwareUpdates;
import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.R;
import cc.skylock.skylock.bluetooth.BluetoothDeviceStatus;
import cc.skylock.skylock.bluetooth.SkylockBLEFirmwareUpdateStatus;
import cc.skylock.skylock.bluetooth.SkylockBluetoothLEService;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirmwareUpdateActivity extends AppCompatActivity implements BluetoothDeviceStatus, SkylockBLEFirmwareUpdateStatus {
    private SkylockBluetoothLEService mService = null;
    ProgressBar loading_ProgressBar;
    int progressStatus = 0;
    RelativeLayout progress_RelativeLayout;
    TextView loadingStatus_TextView, textView_label_title, textView_label_later,
            textView_label_update, textView_Description, textView_label_update_now;
    CardView cv_ok, cv_update, cv_update_now;
    boolean isFirmwareDone = false;
    PrefUtil mPrefUtil;
    private Intent mIntent;
    int fromActivity = 0;
    LinearLayout mLinearLayout;
    private String macAddress = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware_update);
        service_init();
        mPrefUtil = new PrefUtil(this);
        mIntent = getIntent();
        if (mIntent != null) {
            fromActivity = mIntent.getIntExtra("ACTIVITY_ID", 0);
            macAddress = mIntent.getStringExtra("LOCK_MACID");
        }
        textView_label_later = (TextView) findViewById(R.id.tv_ok_later);
        textView_label_update = (TextView) findViewById(R.id.textView_label_update);
        textView_label_title = (TextView) findViewById(R.id.tv_title);
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_buttons);
        cv_ok = (CardView) findViewById(R.id.cv_ok_later);
        cv_update = (CardView) findViewById(R.id.cv_update);
        cv_update_now = (CardView) findViewById(R.id.cv_update_now);
        loadingStatus_TextView = (TextView) findViewById(R.id.tv_loading_status);
        textView_label_update_now = (TextView) findViewById(R.id.textView_label_update_now);
        loading_ProgressBar = (ProgressBar) findViewById(R.id.progressBar_update);
        progress_RelativeLayout = (RelativeLayout) findViewById(R.id.progressBar_relativeLayout);
        textView_Description = (TextView) findViewById(R.id.tv_description);
        textView_label_update_now.setTypeface(UtilHelper.getTypface(this));
        textView_Description.setTypeface(UtilHelper.getTypface(this));
        textView_label_title.setTypeface(UtilHelper.getTypface(this));
        textView_label_later.setTypeface(UtilHelper.getTypface(this));
        textView_label_update.setTypeface(UtilHelper.getTypface(this));
        loadingStatus_TextView.setTypeface(UtilHelper.getTypface(this));
        loadingStatus_TextView.setVisibility(View.GONE);
        progress_RelativeLayout.setVisibility(View.GONE);
        loading_ProgressBar.setVisibility(View.GONE);
        if (NetworkUtil.isNetworkAvailable(this))
            getFirmwareLogs();
        else {
            final String network_error_header = getResources().getString(R.string.network_error);
            final String no_internet_alert_description = getResources().getString(R.string.no_internet_alert);
            CentralizedAlertDialog.showDialog(this, network_error_header, no_internet_alert_description, 0);

        }
        textView_label_title.setText(R.string.firmware_update_available);
        if (fromActivity == 4) {
            cv_update_now.setVisibility(View.VISIBLE);
            mLinearLayout.setVisibility(View.INVISIBLE);

        } else {
            cv_update_now.setVisibility(View.INVISIBLE);
            mLinearLayout.setVisibility(View.VISIBLE);
        }
        cv_update_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFirmwareDone) {
                    cv_update_now.setClickable(false);
                    cv_update_now.setEnabled(false);
                    mService.callBootService();
                    textView_label_title.setText(getResources().getString(R.string.firmware_update_progress));
                    textView_label_update_now.setText(getResources().getString(R.string.updating));
                    cv_update_now.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.accent, null));
                    textView_label_update.setTextColor(Color.parseColor("#BCBBBB"));
                    progress_RelativeLayout.setVisibility(View.VISIBLE);

                } else {
                    finish();
                    final Intent intent = new Intent(FirmwareUpdateActivity.this, HomePageActivity.class);
                    intent.putExtra("Lock_ID", macAddress);
                    intent.putExtra("Lock_State", 0);
                    startActivityForResult(intent, 100);

                }
            }
        });
        cv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFirmwareDone) {
                    cv_update.setClickable(false);
                    cv_ok.setClickable(false);
                    mService.callBootService();
                    cv_ok.setVisibility(View.INVISIBLE);
                    textView_label_update.setText(getResources().getString(R.string.updating));
                    cv_update.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                    textView_label_update.setTextColor(Color.WHITE);
                    progress_RelativeLayout.setVisibility(View.VISIBLE);

                } else {
                    finish();
                    final Intent intent = new Intent(FirmwareUpdateActivity.this, HomePageActivity.class);
                    intent.putExtra("Lock_ID", macAddress);
                    intent.putExtra("Lock_State", 0);
                    startActivityForResult(intent, 100);
                }
            }
        });
        cv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    private void service_init() {
        Intent bindIntent = new Intent(this, SkylockBluetoothLEService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mService != null) {
            mService.registerBluetoothDeviceStatusListener(FirmwareUpdateActivity.this);
            mService.registerFirmwareUpdateListener(FirmwareUpdateActivity.this);
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            mService.registerBluetoothDeviceStatusListener(FirmwareUpdateActivity.this);
            mService.registerFirmwareUpdateListener(FirmwareUpdateActivity.this);

        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    @Override
    public void onDeviceConnected(BluetoothDevice device) {

    }

    @Override
    public void onConnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingStatus_TextView.setText(getString(R.string.connectiing));
            }
        });


    }

    @Override
    public void onConnectionTimeOut() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (mService != null) {
                    mService.initialize();
                }
            }
        });
    }


    @Override
    public void onDeviceDisconnected(boolean shippingModeEnabled) {

    }


    @Override
    public void onBoardFailed() {

    }

    @Override
    public void onBoardCompleted(final BluetoothGatt mBluetoothGatt, String mode) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                macAddress = UtilHelper.getLockMacIDFromName(mBluetoothGatt.getDevice().getName());
                final Intent intent = new Intent(SkylockConstant.ACTION_GATT_CONNECTED);
                sendBroadcast(intent);
                completeFWUI();

            }
        });

    }

    @Override
    public void onGetHardwareInfo(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onLocked() {

    }

    @Override
    public void onUnLocked() {

    }

    @Override
    public void onLockMalfunctioned() {

    }

    @Override
    public void onCrashed(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onTheft(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onDeviceStatus(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onCrashedAndTheft(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }


    @Override
    public void onScanFailed() {

    }

    @Override
    public void onScanedDevice(HashSet<BluetoothDevice> device) {
        final ArrayList<String> mLockMacIdList = new ArrayList<>();

        for (BluetoothDevice mBluetoothDevice : device) {
            mLockMacIdList.add(UtilHelper.getLockMacIDFromName(mBluetoothDevice.getName()));
        }
        if (mLockMacIdList.contains(macAddress)) {
            for (BluetoothDevice mBluetoothDevice : device) {
                final String scanLockMacID = UtilHelper.getLockMacIDFromName(mBluetoothDevice.getName());
                if (scanLockMacID.equals(macAddress)) {
                    final String macId = mBluetoothDevice.getAddress().replace(":", "");
                    macAddress = macId;
                    bleConnection(macId, true);
                    break;
                }
            }
        } else {
            if (mService != null)
                mService.initialize();
        }

    }

    @Override
    public void onGetRSSi(BluetoothGatt gatt, int rssi) {

    }

    @Override
    public void getFWinfo(String version) {

    }

    @Override
    public void readSerialNumber(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

    }

    @Override
    public void doUpdateFirmware() {

    }

    @Override
    public void onGetFirmwareImageData(final FirmwareUpdates mFirmwareUpdates) {
        if (mFirmwareUpdates.getPayload() != null) {
            progressStatus = mFirmwareUpdates.getPayload().size();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView_Description.setText("");
                    textView_label_update.setTextColor(Color.parseColor("#BCBBBB"));
                    cv_update.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.accent, null));
                    loadingStatus_TextView.setVisibility(View.VISIBLE);
                    loading_ProgressBar.setVisibility(View.VISIBLE);
                    progress_RelativeLayout.setVisibility(View.GONE);
                    loading_ProgressBar.setMax(progressStatus);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress_RelativeLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onUpdateFirmwareImage(final int mprogressStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading_ProgressBar.setProgress(mprogressStatus);
            }
        });
    }

    @Override
    public void oncompleteFirmwareImage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isFirmwareDone = true;
                textView_label_title.setText(getResources().getString(R.string.firmware_update_progress));
                loadingStatus_TextView.setVisibility(View.VISIBLE);
                loadingStatus_TextView.setText(R.string.rebooting);
                mService.stopTimer();
                mService.initialize();
            }
        });

    }

    public boolean bleConnection(String macId, final boolean isRefreshBle) {
        try {
            if (mService != null) {
                final String generatekey = UtilHelper.getMD5Hash("" + mPrefUtil.getIntPref(SkylockConstant.PREF_USER_ID, 0));

                final BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                final BluetoothDevice mBluetoothDevice = mBtAdapter.getRemoteDevice(UtilHelper.macAddColon(macId));
                if (mBluetoothDevice != null) {
                    mPrefUtil.setStringPref(SkylockConstant.SKYLOCK_PRIMARY, macAddress);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mService.close();
                            mService.connect(mBluetoothDevice, true, generatekey, isRefreshBle);
                        }
                    },15000);
                } else {
                    Toast.makeText(this, "No ellipse found", Toast.LENGTH_LONG).show();
                }
                return true;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void completeFWUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cv_update_now.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                cv_update.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                cv_update.setClickable(true);
                cv_update_now.setClickable(true);
                cv_update_now.setEnabled(true);
                cv_update.setEnabled(true);
                loadingStatus_TextView.setVisibility(View.GONE);
                loading_ProgressBar.setVisibility(View.GONE);
                textView_label_update.setTextColor(Color.WHITE);
                textView_label_update_now.setText(getResources().getString(R.string.finished));
                textView_label_update.setText(getResources().getString(R.string.finished));
                textView_label_title.setText(R.string.firmware_update_completed);
                mService.increaseTxPower();
                CentralizedAlertDialog.showDialog(FirmwareUpdateActivity.this,
                        getResources().getString(R.string.success),
                        getResources().getString(R.string.firmware_update_completed), 0);
            }
        });
    }

    @Override
    public void onCompleteFirmwareWithExisitingVersion() {
        completeFWUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mService.stopSelf();

    }

    @Override
    public void onBackPressed() {

    }

    public void getFirmwareLogs() {
        progress_RelativeLayout.setVisibility(View.VISIBLE);
        final LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<SuccessResponse> getLockList = lockWebServiceApi.GetFirmwareUpdationInfo();
        getLockList.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                if (response.code() == 200)
                    progress_RelativeLayout.setVisibility(View.GONE);
                final String[] infoList = response.body().getPayload();
                String info = "";
                if (infoList != null) {
                    for (String value : infoList) {
                        info = info + "-\t" + value + "\n";
                    }
                }
                textView_Description.setText(info);
            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {
                progress_RelativeLayout.setVisibility(View.GONE);
            }
        });


    }
}
