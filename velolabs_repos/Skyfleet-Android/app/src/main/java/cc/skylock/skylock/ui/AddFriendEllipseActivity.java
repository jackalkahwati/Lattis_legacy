package cc.skylock.skylock.ui;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.soundcloud.android.crop.Crop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cc.skylock.skylock.Bean.AcceptSharing;
import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.Bean.PasswordHintParameter;
import cc.skylock.skylock.R;
import cc.skylock.skylock.bluetooth.BluetoothDeviceStatus;
import cc.skylock.skylock.bluetooth.SkylockBluetoothLEService;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.ui.alert.SharingSuccessAlert;
import cc.skylock.skylock.ui.fragment.ConnectFriendEllipseFragment;
import cc.skylock.skylock.ui.fragment.EnterShareCodeFragment;
import cc.skylock.skylock.ui.fragment.UserProfileFragment;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFriendEllipseActivity extends AppCompatActivity implements BluetoothDeviceStatus {
    Context mContext;
    PrefUtil mPrefUtil;
    RelativeLayout progress_RelativeLayout;
    ImageView imageView_close;
    SharingSuccessAlert dialogFragment = null;
    Fragment mConnectFriendEllipseFragment = null, mEnterShareCodeFragment;
    private int REQUEST_ENABLE_BT = 1;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    BluetoothManager mbluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    private SkylockBluetoothLEService mService = null;
    public static final String TAG = "AddFriendEllipse";
    BroadcastReceiver connectivityBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                String status = NetworkUtil.getConnectivityStatusString(context);
                if (status != null) {
                    final String network_error_header = getResources().getString(R.string.network_error);
                    final String no_internet_alert_description = getResources().getString(R.string.no_internet_alert);
                    CentralizedAlertDialog.showDialog(mContext, network_error_header, no_internet_alert_description, 0);
                }

            }
        }
    };
    private String invalidCodeText = null;
    private String invalidCodeMessage = null;
    private boolean isSecretCodeEntered = false;
    public static String sharedMacIdSuccess;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_ellipse);
        final int colorprimary = ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null);
        changeStatusBarColor(colorprimary);
        mEnterShareCodeFragment = EnterShareCodeFragment.newInstance();
        setFragment(mEnterShareCodeFragment, true, "EnterShareCodeFragment");
        mContext = AddFriendEllipseActivity.this;
        mPrefUtil = new PrefUtil(this);
        service_init();
        invalidCodeText = getResources().getString(R.string.invalid_code);
        invalidCodeMessage = getResources().getString(R.string.invalid_code_message);
        mConnectFriendEllipseFragment = ConnectFriendEllipseFragment.newInstance();
        imageView_close = (ImageView) findViewById(R.id.iv_close);
        progress_RelativeLayout = (RelativeLayout) findViewById(R.id.progressBar_relativeLayout);
        progress_RelativeLayout.setVisibility(View.GONE);
        mbluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mbluetoothManager.getAdapter();
        progress_RelativeLayout.setVisibility(View.GONE);
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
                checkLocationListener();
            }
        }


    }

    private void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, SkylockBluetoothLEService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            mService.registerBluetoothDeviceStatusListener(AddFriendEllipseActivity.this);
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    private void intializeBluetoothLE() {

        if (!mService.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }
    }

    public void callConnectFragment() {
        intializeBluetoothLE();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                setFragment(mConnectFriendEllipseFragment, true, "ConnectFriendEllipseFragment");
            }
        });
    }

    @Override
    protected void onResume() {
        registerReceiver(connectivityBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        super.onResume();


    }

    public void bleConnection(String macId, final String generateKey) {
        if (macId != null && generateKey != null) {
            final Intent intent = new Intent(AddFriendEllipseActivity.this, HomePageActivity.class);
            intent.putExtra("typeOfNotification", 3);
            intent.putExtra("MAC_ID", macId);
            intent.putExtra("GENERATE_KEY", generateKey);
            startActivity(intent);
            finish();
        }
    }

    public void acceptSharingCall(String mSendShareCode) {
        progress_RelativeLayout.setVisibility(View.VISIBLE);
        SkylockConstant.userToken = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        final PasswordHintParameter mPasswordHintParameter = new PasswordHintParameter();
        mPasswordHintParameter.setPassword_hint(mSendShareCode);
        Call<AcceptSharing> mAcceptSharing = lockWebServiceApi.AcceptSharing(mPasswordHintParameter);
        mAcceptSharing.enqueue(new Callback<AcceptSharing>() {
            @Override
            public void onResponse(Call<AcceptSharing> call, Response<AcceptSharing> response) {
                if (response.code() == 200) {
                    sharedMacIdSuccess = response.body().getPayload().getMac_id();
                    final String sharedLockName = response.body().getPayload().getName();
                    mPrefUtil.setStringPref(sharedMacIdSuccess, sharedLockName);
                    isSecretCodeEntered = true;
                    getLockList();
                    UtilHelper.analyticTrackUserAction("Lock Borrowed", "Share", "Add new lock", null, "ANDROID");

                } else {
                    CentralizedAlertDialog.showDialog(AddFriendEllipseActivity.this, invalidCodeText, invalidCodeMessage, 1);
                    progress_RelativeLayout.setVisibility(View.GONE);
                    UtilHelper.analyticTrackUserAction("Lock Borrowed Failed", "Share", "Add new lock", null, "ANDROID");
                }

            }

            @Override
            public void onFailure(Call<AcceptSharing> call, Throwable t) {
                progress_RelativeLayout.setVisibility(View.GONE);
            }
        });
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
    public void onBoardCompleted(BluetoothGatt gatt, String mode) {

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConnectFriendEllipseFragment.newInstance().hideProgress();
            }
        });
    }

    @Override
    public void onScanedDevice(HashSet<BluetoothDevice> device) {
        for (BluetoothDevice bluetoothDevices : device) {
            SkylockConstant.mLockMacIdList.add(UtilHelper.getLockMacIDFromName(bluetoothDevices.getName()));
        }
        if (device != null) {
            ConnectFriendEllipseFragment.newInstance().scanedDevices(device);
        } else {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ConnectFriendEllipseFragment.newInstance().hideProgress();
                }
            });

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


    public void setFragment(final Fragment fragment, final boolean isAddToBackStack, final String tag) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
        /*Create a Fragment Manager Object*/
                    FragmentManager fragmentManager;
        /*Create a Fragment Transaction Object*/
                    FragmentTransaction fragmentTransaction;
        /*Assign the fragment manager to the support fragment manager of the android.support.v4 package */
                    fragmentManager = getSupportFragmentManager();
        /*Begin the Transaction.*/
                    fragmentTransaction = fragmentManager.beginTransaction();
        /*Add the parametrised fragment to the fragment transaction. */
                    fragmentTransaction.replace(
                            R.id.containerView, fragment, tag);
        /*Add the fragment to the back stack*/
                    if (isAddToBackStack) {
                        fragmentTransaction.addToBackStack(tag);
                    }
        /*Commit the transaction.*/
                    fragmentTransaction.commit();
//        fragmentManager.executePendingTransactions();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!isSecretCodeEntered) {
            startActivity(new Intent(AddFriendEllipseActivity.this, AddLockActivity.class)
                    .putExtra("ADD_LOCK", "HOME")
            );
        }
        finish();
        super.onBackPressed();
    }

    private void getLockList() {

        SkylockConstant.userToken = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<LockList> getLockList = lockWebServiceApi.GetLockData();
        getLockList.enqueue(new Callback<LockList>() {
            @Override
            public void onResponse(Call<LockList> call, Response<LockList> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus() == 200) {
                        try {
                            LockList payloadEntity = response.body();
                            final Gson gson = new Gson();
                            String lockJson = gson.toJson(payloadEntity);
                            mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, lockJson);
                            progress_RelativeLayout.setVisibility(View.GONE);
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            dialogFragment = SharingSuccessAlert.newInstance(sharedMacIdSuccess);
                            dialogFragment.show(ft, "SharingSuccessAlert");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, "");

                    }
                }
            }

            @Override
            public void onFailure(Call<LockList> call, Throwable t) {
                Log.e("There are some problem", t.toString());
                progress_RelativeLayout.setVisibility(View.GONE);
            }
        });
    }


    private void checkLocationListener() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(AddFriendEllipseActivity.this);
            //   android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("Please enable Location Services and GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectivityBroadcastReceiver);
        unbindService(mServiceConnection);
        mService.stopSelf();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            } else {
                intializeBluetoothLE();
            }
        }
    }

}
