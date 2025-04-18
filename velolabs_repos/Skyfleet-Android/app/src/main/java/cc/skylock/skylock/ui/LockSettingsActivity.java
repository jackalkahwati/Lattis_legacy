package cc.skylock.skylock.ui;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import cc.skylock.skylock.Bean.FirmwareUpdates;
import cc.skylock.skylock.Bean.SendMacIdAsParameter;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Bean.UserRegistrationResponse;
import cc.skylock.skylock.R;
import cc.skylock.skylock.bluetooth.BluetoothDeviceStatus;
import cc.skylock.skylock.bluetooth.SkylockBluetoothLEService;
import cc.skylock.skylock.bluetooth.SkylockCrashTheftAlert;
import cc.skylock.skylock.notification.NotificationView;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LockSettingsActivity extends AppCompatActivity implements BluetoothDeviceStatus {
    private TextView textView_detectionContent, textView_setpin, textView_header,
            textView_label_lockname, textView_lockname, textView_label_ownerName,
            textView_ownerName, textView_label_serialNumber,
            textView_serialNumber, textView_label_firmware, textView_firmware,
            textView_cv_label_firmware, textView_header_two, textView_label_theftdetection,
            textView_label_theftdetectionsettings, textView_label_crashdetection,
            textView_label_crashdetectionsettings, textView_header_three, textView_label_proximity_lock,
            textView_label_proximity_unlock, textView_label_pincode, textView_cv_label_delete, textView_toolbar_title;
    private CardView cardView_update, cardView_delete;
    private Context mContext;
    private PrefUtil mPrefUtil;
    private String serialNumber = "";
    private SkylockBluetoothLEService mService = null;
    private String macID;
    private ImageView arrow_setpin_ImageView;
    private ToggleButton crashToggleButton, theftToggleButton, proximityLock, proximityUnlock;
    public SkylockCrashTheftAlert crashTheftAlert;
    private RelativeLayout rl_progressbar, rl_lockdetails_name;
    private double currentVersion;
    private double latestversion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_settings);
        service_init();
        mContext = LockSettingsActivity.this;
        final int colorprimary = ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null);
        changeStatusBarColor(colorprimary);
        mPrefUtil = new PrefUtil(mContext);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        crashTheftAlert = new SkylockCrashTheftAlert(mContext);
        rl_progressbar = (RelativeLayout) findViewById(R.id.rl_progressbar);
        textView_toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        proximityLock = (ToggleButton) findViewById(R.id.toggleButton_proximity_lock);
        proximityUnlock = (ToggleButton) findViewById(R.id.toggleButton_proximity_unlock);
        crashToggleButton = (ToggleButton) findViewById(R.id.toggleButton_crash);
        theftToggleButton = (ToggleButton) findViewById(R.id.toggleButton_theft);
        arrow_setpin_ImageView = (ImageView) findViewById(R.id.iv_image_arrow);
        cardView_update = (CardView) findViewById(R.id.cv_update_button);
        rl_lockdetails_name = (RelativeLayout) findViewById(R.id.rl_lockdetails_name);
        cardView_update.setVisibility(View.GONE);
        cardView_delete = (CardView) findViewById(R.id.cv_delete_lock);
        textView_setpin = (TextView) findViewById(R.id.tv_setpincode);
        textView_detectionContent = (TextView) findViewById(R.id.tv_detection_content);
        textView_header = (TextView) findViewById(R.id.tv_header_label);
        textView_label_lockname = (TextView) findViewById(R.id.tv_label_name);
        textView_lockname = (TextView) findViewById(R.id.tv_lock_name);
        textView_label_ownerName = (TextView) findViewById(R.id.tv_label_registered_owner);
        textView_ownerName = (TextView) findViewById(R.id.tv_registered_owner);
        textView_label_serialNumber = (TextView) findViewById(R.id.tv_label_Serialnumber);
        textView_serialNumber = (TextView) findViewById(R.id.tv_Serialnumber);
        textView_label_firmware = (TextView) findViewById(R.id.tv_label_Firmware);
        textView_firmware = (TextView) findViewById(R.id.tv_Firmware);
        textView_cv_label_firmware = (TextView) findViewById(R.id.cv_update_Firmware);
        textView_header_two = (TextView) findViewById(R.id.tv_header_two);
        textView_label_theftdetection = (TextView) findViewById(R.id.tv_label_Theft_detection);
        textView_label_theftdetectionsettings = (TextView) findViewById(R.id.tv_label_Theft_detection_settings);
        textView_label_crashdetection = (TextView) findViewById(R.id.tv_label_Crash_detection);
        textView_label_crashdetectionsettings = (TextView) findViewById(R.id.tv_label_Crash_detection_settings);
        textView_header_two = (TextView) findViewById(R.id.tv_header_two);
        textView_cv_label_delete = (TextView) findViewById(R.id.tv_delete_button);
        textView_label_proximity_lock = (TextView) findViewById(R.id.tv_label_proximity_lock);
        textView_label_proximity_unlock = (TextView) findViewById(R.id.tv_label_proximity_unlock);
        textView_label_pincode = (TextView) findViewById(R.id.tv_pincode);
        textView_header_three = (TextView) findViewById(R.id.tv_header_three);
        textView_cv_label_delete.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_pincode.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_theftdetectionsettings.setTypeface(UtilHelper.getTypface(mContext));
        textView_header_two.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_theftdetection.setTypeface(UtilHelper.getTypface(mContext));
        textView_header.setTypeface(UtilHelper.getTypface(mContext));
        textView_serialNumber.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_crashdetection.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_proximity_unlock.setTypeface(UtilHelper.getTypface(mContext));
        textView_firmware.setTypeface(UtilHelper.getTypface(mContext));
        textView_detectionContent.setTypeface(UtilHelper.getTypface(mContext));
        textView_header_three.setTypeface(UtilHelper.getTypface(mContext));
        textView_cv_label_firmware.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_lockname.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_crashdetectionsettings.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_ownerName.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_serialNumber.setTypeface(UtilHelper.getTypface(mContext));
        textView_setpin.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_firmware.setTypeface(UtilHelper.getTypface(mContext));
        textView_header_three.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_proximity_lock.setTypeface(UtilHelper.getTypface(mContext));
        textView_serialNumber.setTypeface(UtilHelper.getTypface(mContext));
        textView_ownerName.setTypeface(UtilHelper.getTypface(mContext));
        textView_lockname.setTypeface(UtilHelper.getTypface(mContext));
        textView_toolbar_title.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_crashdetectionsettings.setVisibility(View.GONE);
        final String first = "<font color='#9B9B9B'>" + getString(R.string.action_settings_detection_content_first) + "</font>";
        final String next = "<font color='#57D8FF'>" + " " + getString(R.string.action_settings_detection_content_next) + "</font>";
        textView_detectionContent.setText(Html.fromHtml(first + next));
        final String one = "<font color='#9B9B9B'>No PIN code.</font>";
        final String two = "<font color='#57D8FF'> Set one now</font>";
        textView_setpin.setText(Html.fromHtml(one + two));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        textView_firmware.setText(mPrefUtil.getStringPref(macID + SkylockConstant.FW_VERSION, ""));
        textView_serialNumber.setText(mPrefUtil.getStringPref(macID + SkylockConstant.BOARD_MANUFACTURING_NUMBER, ""));
        rl_progressbar.setVisibility(View.VISIBLE);
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            macID = bundle.getString("MAC_ID");
            final String lockname = mPrefUtil.getStringPref(macID, "");
            textView_lockname.setText(lockname);
        }
        final String userdetailsJson = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_DETAILS, "");
        if (!userdetailsJson.equals("")) {
            Gson gson = new Gson();
            UserRegistrationResponse lockList = gson.fromJson(userdetailsJson, UserRegistrationResponse.class);
            final String firstName = lockList.getPayload().getFirst_name();
            final String lastName = lockList.getPayload().getLast_name();
            if (firstName != null && lastName != null) {
                final String ownerName = firstName + " " + lastName;
                if (!Objects.equals(ownerName, "")) {
                    textView_ownerName.setText(ownerName);
                }
            }
        }
        textView_detectionContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(LockSettingsActivity.this, HomePageActivity.class);
                intent.putExtra("typeOfNotification", 2);
                startActivity(intent);
            }
        });
        if (mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_PROXIMITY_UNLOCK_ENABLE, false))
            proximityUnlock.setChecked(true);
        if (mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_PROXIMITY_LOCK_ENABLE, false))
            proximityLock.setChecked(true);

        handleCrashAndTheft(macID);
        if (mPrefUtil.getBooleanPref(Myconstants.KEY_USER_LOCK_SET_PIN, false)) {
            arrow_setpin_ImageView.setVisibility(View.VISIBLE);
            textView_setpin.setVisibility(View.GONE);
        } else {
            arrow_setpin_ImageView.setVisibility(View.GONE);
            textView_setpin.setVisibility(View.VISIBLE);
        }
        cardView_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(LockSettingsActivity.this, FirmwareUpdateActivity.class)
                        .putExtra("LOCK_MACID", macID));
            }
        });
        textView_setpin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                callAddlockActivity();
            }
        });
        arrow_setpin_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //        finish();
                callAddlockActivity();
            }
        });
        rl_lockdetails_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LockSettingsActivity.this, UpdateLockNameActivity.class).putExtra("MAC_ID", macID));
            }
        });
        textView_label_theftdetectionsettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LockSettingsActivity.this, CrashTheftSettingsActivity.class).putExtra("MAC_ID", macID));
            }
        });
        textView_label_crashdetectionsettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //      startActivity(new Intent(LockSettingsActivity.this, CrashTheftSettingsActivity.class));
            }
        });
        crashToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String ecList = mPrefUtil.getStringPref(SkylockConstant.PREF_EMERGENCY_CONTACT_LIST, "");
                if (!Objects.equals(ecList, "") && ecList != null) {
                    try {
                        JSONArray ecListJson = new JSONArray(ecList);
                        if (ecListJson.length() >= 1) {
                            if (!SkylockBluetoothLEService.mBluetoothGattDescriptorEnable) {
                                enableCrashAndTheft(SkylockConstant.SKYLOCK_CRASHSELECTION);
                                crashToggleButton.setChecked(true);
                                crashTheftAlert.flagCrash();
                                mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_CRASH_ENABLE, true);
                            } else {
                                disableCrashAlert();

                            }
                        } else {
                            if (!SkylockBluetoothLEService.mBluetoothGattDescriptorEnable)
                                showAlertDialog();
                            else
                                disableCrashAlert();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    crashToggleButton.setChecked(false);
                    showAlertDialog();
                }


            }
        });

        cardView_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.isNetworkAvailable(mContext) && macID != null) {
                    startActivity(new Intent(mContext, DeleteAccountActivity.class)
                            .putExtra("DELETION_TYPE", 1)
                            .putExtra("LOCK_MACID", macID)
                            .putExtra("CURRENTLY_CONNECTED", true)
                    );


                } else
                    Toast.makeText(mContext, "No network connection", Toast.LENGTH_SHORT).show();
            }
        });


        proximityLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (proximityLock.isChecked()) {
                    mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_PROXIMITY_LOCK_ENABLE, true);
                } else {
                    mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_PROXIMITY_LOCK_ENABLE, false);
                }
            }

        });
        proximityUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (proximityUnlock.isChecked()) {
                    mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_PROXIMITY_UNLOCK_ENABLE, true);
                } else {
                    mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_PROXIMITY_UNLOCK_ENABLE, false);
                }
            }
        });
        theftToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SkylockBluetoothLEService.mBluetoothGattDescriptorEnable) {
                    enableCrashAndTheft(SkylockConstant.SKYLOCK_THEFTSELECTION);
                    theftToggleButton.setChecked(true);
                    crashTheftAlert.flagTheft(mPrefUtil.getIntPref(macID + SkylockConstant.PREF_LOCK_THEFT_SENSITIVITY, 2));
                    mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_THEFT_ENABLE, true);
                } else {
                    if (!crashTheftAlert.isCrash()) {
                        disableCrashAndTheft(0);
                    }
                    theftToggleButton.setChecked(false);
                    mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_THEFT_ENABLE, false);
                    crashTheftAlert.disableCrashTheft();
                }
            }
        });


    }

    public void stopTimer() {
        if (mService != null)
            mService.stopTimer();
    }

    private void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    public void deleteLockCall(String macAddress) {
        rl_progressbar.setVisibility(View.VISIBLE);
        macAddress = macAddress.replace(":", "");
        final SendMacIdAsParameter sendMacIdAsParameter = new SendMacIdAsParameter();
        if (macAddress != null)
            sendMacIdAsParameter.setMac_id(macAddress);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);

        Call<SuccessResponse> delete = lockWebServiceApi.DeleteLock(sendMacIdAsParameter);

        delete.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                if (response.code() == 200) {
                    Toast.makeText(mContext, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    rl_progressbar.setVisibility(View.GONE);
                    try {
                        LockSettingsActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {
                rl_progressbar.setVisibility(View.GONE);
                Toast.makeText(mContext, "Deletion failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showAlertDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.alert_add_ec);
        dialog.setCancelable(false);
        final TextView textView_label_cancel = (TextView) dialog.findViewById(R.id.tv_title);
        final TextView textView_label_Locate = (TextView) dialog.findViewById(R.id.tv_description);
        final CardView cv_ok = (CardView) dialog.findViewById(R.id.cv_yes_button);
        final CardView cv_cancel = (CardView) dialog.findViewById(R.id.cv_cancel_button);
        textView_label_cancel.setTypeface(UtilHelper.getTypface(this));
        textView_label_Locate.setTypeface(UtilHelper.getTypface(this));
        cv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        cv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void disableCrashAlert() {
        if (!crashTheftAlert.isTheft()) {
            disableCrashAndTheft(0);
        }
        mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_CRASH_ENABLE, false);
        crashToggleButton.setChecked(false);

        crashTheftAlert.disableCrashTheft();
    }

    private void handleCrashAndTheft(String macID) {
        if (macID != null) {
            if (mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_CRASH_ENABLE, false)) {
                enableCrashAndTheft(SkylockConstant.SKYLOCK_CRASHSELECTION);
                crashToggleButton.setChecked(true);
                crashTheftAlert.flagCrash();
            } else if (mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_THEFT_ENABLE, false)) {
                enableCrashAndTheft(SkylockConstant.SKYLOCK_THEFTSELECTION);
                theftToggleButton.setChecked(true);
                crashTheftAlert.flagTheft(mPrefUtil.getIntPref(macID + SkylockConstant.PREF_LOCK_THEFT_SENSITIVITY, 2));
            }
        }
    }

    public void enableCrashAndTheft(int crashTheftselection) {
        if (mService != null)
            mService.enableCrashAndTheft(crashTheftselection);
    }

    public void disableCrashAndTheft(int crashTheftselection) {
        if (mService != null)
            mService.disableCrashAndTheft(crashTheftselection);
    }

    private void callAddlockActivity() {
        final Intent intent = new Intent(LockSettingsActivity.this, AddLockActivity.class);
        intent.putExtra("ADD_LOCK", "SET_CAP_PIN");
        intent.putExtra("Lock_ID", macID);
        startActivity(intent);
    }

    public void putShippingMode() {
        if (mService != null) {
            mService.putShippingMode();
        }
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, SkylockBluetoothLEService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            mService.registerBluetoothDeviceStatusListener(LockSettingsActivity.this);
            mService.getLockSerialNumber();

        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mService.stopSelf();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mService != null) {
            mService.registerBluetoothDeviceStatusListener(LockSettingsActivity.this);
        }
        final String lockname = mPrefUtil.getStringPref(macID, "");
        textView_lockname.setText(lockname);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mService.enableHardwareNotification();
        finish();
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {

    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onConnectionTimeOut() {

    }


    @Override
    public void onDeviceDisconnected(boolean shippingModeEnabled) {

    }


    @Override
    public void onBoardFailed() {

    }

    @Override
    public void onBoardCompleted(BluetoothGatt mBluetoothGatt, String mode) {

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
        callVibrator();
        NotificationView.showNotification(LockSettingsActivity.this, "Crash", "Alert", 0, CrashAlert.class);
        Intent crashAlert = new Intent(this, CrashAlert.class);
        crashAlert.putExtra("MAC_ID", macID);
        startActivity(crashAlert);
        UtilHelper.analyticTrackUserAction("Crash Alert", "Custom", "", null, "ANDROID");
    }

    @Override
    public void onTheft(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        callVibrator();
        NotificationView.showNotification(LockSettingsActivity.this, "Theft", "Alert", 1, TheftAlert.class);
        Intent theftAlert = new Intent(this, TheftAlert.class);
        startActivity(theftAlert);
        UtilHelper.analyticTrackUserAction("Theft Alert", "Custom", "", null, "ANDROID");
    }

    @Override
    public void onDeviceStatus(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    private void callVibrator() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(1000);
            }
        });
    }

    @Override
    public void onCrashedAndTheft(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        crashTheftAlert.putCharacterstic(gatt, characteristic);
    }


    @Override
    public void onScanFailed() {

    }

    @Override
    public void onScanedDevice(HashSet<BluetoothDevice> device) {

    }

    @Override
    public void onGetRSSi(BluetoothGatt gatt, int rssi) {

    }

    @Override
    public void getFWinfo(final String version) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPrefUtil.setStringPref(macID + SkylockConstant.FW_VERSION, version);
                currentVersion = Double.parseDouble(version);
                textView_firmware.setText(mPrefUtil.getStringPref(macID + SkylockConstant.FW_VERSION, ""));
                if (NetworkUtil.isNetworkAvailable(mContext)) {
                    getLockMetaData();
                } else {
                    rl_progressbar.setVisibility(View.GONE);
                }


            }
        });
    }

    @Override
    public void readSerialNumber(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic != null) {
            mService.callFWInfo();
            macID = UtilHelper.getLockMacIDFromName(gatt.getDevice().getName());
            byte[] mode = characteristic.getValue();
            for (int i = 0; i < mode.length; i++) {
                // decimal to hex
                String hex = Integer.toString(mode[i], 16);
                // hexToString
                String result = UtilHelper.hexToString(hex);
                serialNumber = serialNumber + result;
            }
            mPrefUtil.setStringPref(macID + SkylockConstant.BOARD_MANUFACTURING_NUMBER, "" + serialNumber);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView_serialNumber.setText(serialNumber);
                }
            });
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

    }

    private void getLockMetaData() {

        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<FirmwareUpdates> mLockMetaData = lockWebServiceApi.GetLatestFirmwareVersion();
        mLockMetaData.enqueue(new Callback<FirmwareUpdates>() {
            @Override
            public void onResponse(Call<FirmwareUpdates> call, Response<FirmwareUpdates> mLockMetaData) {
                if (mLockMetaData.code() == 200) {
                    try {
                        List<String> fWVersion = mLockMetaData.body().getPayload();
                        int lastIndex = fWVersion.size() - 1;
                        latestversion = Double.parseDouble(fWVersion.get(lastIndex));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    rl_progressbar.setVisibility(View.GONE);
                    if (currentVersion < latestversion) {
                        cardView_update.setVisibility(View.VISIBLE);

                    } else {
                        cardView_update.setVisibility(View.GONE);

                    }
                }

            }

            @Override
            public void onFailure(Call<FirmwareUpdates> call, Throwable t) {
                rl_progressbar.setVisibility(View.GONE);
            }
        });

    }


}
