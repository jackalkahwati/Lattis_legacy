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
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import cc.skylock.skylock.Bean.FirmwareUpdates;
import cc.skylock.skylock.Bean.LockKeyGen;
import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.Bean.UpdateLockNameResponse;
import cc.skylock.skylock.Bean.UpdateLockNameParameter;
import cc.skylock.skylock.Bean.SendMacIdAsParameter;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Bean.TouchPadSequence;
import cc.skylock.skylock.R;
import cc.skylock.skylock.bluetooth.BluetoothDeviceStatus;
import cc.skylock.skylock.bluetooth.SkylockBluetoothLEService;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.ui.fragment.AddLockHome;
import cc.skylock.skylock.ui.fragment.LockStepOne;
import cc.skylock.skylock.ui.fragment.LockStepThree;
import cc.skylock.skylock.ui.fragment.LockStepTwo;
import cc.skylock.skylock.ui.fragment.SeTPinFragment;
import cc.skylock.skylock.utils.LockDetailsHelper;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddLockActivity extends AppCompatActivity implements BluetoothDeviceStatus {
    private PrefUtil mPrefUtil;
    RelativeLayout progress_RelativeLayout;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    private int REQUEST_ENABLE_BT = 1;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothManager mbluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String macAddress = null;
    private SkylockBluetoothLEService mService = null;
    public static final String TAG = "AddlockActivity";
    Toolbar toolbar;
    MenuItem refreshMenu;
    TextView textView_header;
    Context mContext;
    boolean mlockDisConnected = false;
    int lockStatus;
    private double currentFWversion = 0, latestFWversion = 0;

    private Fragment seTPinFragment = null, addLockHomeFragment = null, lockStepOne = null, lockStepTwo = null, lockStepThree = null;
    BroadcastReceiver connectivityBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Objects.equals(action, "android.net.conn.CONNECTIVITY_CHANGE")) {
                String status = NetworkUtil.getConnectivityStatusString(context);
                if (status != null) {
                    CentralizedAlertDialog.showDialog(mContext, getResources().getString(R.string.network_error),
                            getResources().getString(R.string.no_internet_alert), 0);
                }

            }
        }
    };
    private CoordinatorLayout coordinatorLayout;
    private ArrayList<HashMap<String, String>> myLockAndShareLockListData;
    private String lockListJson = "";
    private LockList lockList;
    private String generatekey = null;
    private String setPinFromLockSettings = null;
    private boolean isEnableBackPress = true;
    private boolean isForceFirmwareUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lock);
        mbluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mbluetoothManager.getAdapter();
        addLockHomeFragment = AddLockHome.newInstance();
        lockStepTwo = LockStepTwo.newInstance();
        lockStepOne = LockStepOne.newInstance();
        lockStepThree = LockStepThree.newInstance();
        seTPinFragment = SeTPinFragment.newInstance();
        textView_header = (TextView) findViewById(R.id.toolbar_title);
        progress_RelativeLayout = (RelativeLayout) findViewById(R.id.progressBar_relativeLayout);
        mContext = AddLockActivity.this;
        mPrefUtil = new PrefUtil(mContext);
        final int colorprimary = ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null);
        changeStatusBarColor(colorprimary);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        myLockAndShareLockListData = new ArrayList<>();
        textView_header.setTypeface(UtilHelper.getTypface(mContext));
        textView_header.setText(getResources().getString(R.string.addlock1_toolbar_title));
        SkylockConstant.userToken = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_TOKEN, "");
        final Intent intent = getIntent();
        if (intent != null)
            onNewIntent(intent);
        getLockMetaData();
        mPrefUtil.setBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, true);
        progress_RelativeLayout.setVisibility(View.GONE);
        lockListJson = mPrefUtil.getStringPref(SkylockConstant.PREF_LOCK_LIST, "");
        if (!lockListJson.equals("")) {
            myLockAndShareLockListData = LockDetailsHelper.convertJsonToGson(mContext, lockListJson);
        }

        progress_RelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    /**
     * Set the given fragment to be visible.
     *
     * @param fragment Fragment to be shown
     */
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
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
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


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            setPinFromLockSettings = bundle.getString("ADD_LOCK");
            if (setPinFromLockSettings != null && setPinFromLockSettings.equals("SET_CAP_PIN")) {
                macAddress = bundle.getString("Lock_ID");
                callSetPinFragment();
            } else if (setPinFromLockSettings != null && setPinFromLockSettings.equals("HOME")) {
                setFragment(addLockHomeFragment, true, "AddLockHome");
            }
        }
        intializeBluetoothLE();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            mService.registerBluetoothDeviceStatusListener(AddLockActivity.this);
            if (mBluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT >= 21) {
                    mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                    filters = new ArrayList<ScanFilter>();
                    checkLocationListener();
                    final Bundle bundle = getIntent().getExtras();
                    if (bundle != null) {
                        final String doScan = bundle.getString("ADD_LOCK");
                        if (doScan != null && doScan.equals("SCAN")) {
                            selectPage(2);
                        }
                    }
                }

            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    private boolean intializeBluetoothLE() {
        if (mBluetoothAdapter.isEnabled()) {
            if (mService == null) {
                service_init();
            } else if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
                return false;
            }
        }
        return true;
    }


    public void selectPage(int pageID) {

        if (pageID == 1) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    setFragment(lockStepOne, true, "LockStepOne");
                }
            });

        } else if (pageID == 2) {

            if (mService != null) {
                mService.close();
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                if (mService == null) {
                    service_init();
                } else if (!mService.initialize()) {
                }
            }
            progress_RelativeLayout.setVisibility(View.GONE);
            textView_header.setText(R.string.searching_for_ellipses);
            if (refreshMenu != null)
                refreshMenu.setVisible(true);
            if (intializeBluetoothLE())
                setFragment(lockStepTwo, true, "LockStepTwo");
        } else {
            refreshMenu.setVisible(false);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addlock_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        refreshMenu = menu.findItem(R.id.action_refresh);
        refreshMenu.setVisible(false);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            intializeBluetoothLE();
            progress_RelativeLayout.setVisibility(View.GONE);
            LockStepTwo.newInstance().showProgress();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {

            unregisterReceiver(connectivityBroadcastReceiver);
            Log.d(TAG, "onDestroy()");
            unbindService(mServiceConnection);
            mService.stopSelf();
        }

    }

    @Override
    public void onBackPressed() {
        if (isEnableBackPress) {
            super.onBackPressed();
            if (mlockDisConnected) {
                final Intent intent = new Intent(SkylockConstant.ACTION_GATT_DISCONNECTED);
                mContext.sendBroadcast(intent);
            }
            finish();
        }
    }

    @Override
    protected void onResume() {
        registerReceiver(connectivityBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if (!NetworkUtil.isNetworkAvailable(mContext))
            Toast.makeText(AddLockActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
        super.onResume();


    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    private void checkLocationListener() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(AddLockActivity.this);
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
        } else {
            service_init();
        }
    }

    public void addNameToPreference(final String lockName) {
        progress_RelativeLayout.setVisibility(View.VISIBLE);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                updateLockNameCall(lockName);

            }
        });

    }

    public void setCapPin(String pinCode, String[] touchPadSequence) {
        if (mService != null) {
            mService.setCapPin(pinCode);
            setPinTouchSequenceCall(touchPadSequence);
        }
        if (setPinFromLockSettings != null && setPinFromLockSettings.equals("SET_CAP_PIN")) {
            finish();
        } else if (isForceFirmwareUpdate) {
            finish();
            if (currentFWversion < latestFWversion)
                startActivity(new Intent(AddLockActivity.this, FirmwareUpdateActivity.class).putExtra("ACTIVITY_ID", 4).putExtra("LOCK_MACID", macAddress));
            else
                callHomePageActivity();
        } else {
            callHomePageActivity();
        }
    }

    private void callHomePageActivity() {
        finish();
        final Intent intent = new Intent(AddLockActivity.this, HomePageActivity.class);
        intent.putExtra("Lock_ID", macAddress);
        intent.putExtra("Lock_State", lockStatus);
        startActivityForResult(intent, 100);
    }

    public boolean bleConnection(BluetoothDevice mBluetoothDevice, boolean doConnection) {
        if (mService != null) {
            final String currentLockMacId = UtilHelper.getLockMacIDFromName(mBluetoothDevice.getName());
            mPrefUtil.setStringPref(SkylockConstant.SKYLOCK_PRIMARY, currentLockMacId);
            mService.close();
            progress_RelativeLayout.setVisibility(View.VISIBLE);
            if (doConnection) {

                if (myLockAndShareLockListData != null && myLockAndShareLockListData.size() > 0) {
                    final ArrayList<String> listOfMacid = new ArrayList<>();
                    for (HashMap<String, String> myLockList : myLockAndShareLockListData) {
                        listOfMacid.add(myLockList.get("LOCK_MACID"));
                    }
                    if (listOfMacid.contains(currentLockMacId)) {
                        for (HashMap<String, String> myLockList : myLockAndShareLockListData) {
                            if (myLockList.get("LOCK_MACID").equals(currentLockMacId)) {
                                generatekey = UtilHelper.getMD5Hash("" + myLockList.get("USER_ID"));
                            }
                        }
                        bleConnectionWithLock(mBluetoothDevice, doConnection, generatekey);
                    } else {
                        registerLockWithOval(mBluetoothDevice, doConnection, currentLockMacId);
                    }

                } else {
                    registerLockWithOval(mBluetoothDevice, doConnection, currentLockMacId);
                }
            } else {
                bleConnectionWithLock(mBluetoothDevice, doConnection, generatekey);
            }

        }

        return false;
    }


    private boolean bleConnectionWithLock(BluetoothDevice mBluetoothDevice, boolean blinkLed, String generatekey) {
        if (mBluetoothDevice != null && mBluetoothAdapter.isEnabled()) {
            mService.connect(mBluetoothDevice, blinkLed, generatekey, false);
            return true;
        } else {
            progress_RelativeLayout.setVisibility(View.GONE);
            Toast.makeText(AddLockActivity.this, "Please turn on your bluetooth", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void registerLockWithOval(final BluetoothDevice mBluetoothDevice, final boolean blinkLed, final String currentLockMacId) {
        final SendMacIdAsParameter sendMacIdAsParameter = new SendMacIdAsParameter();
        sendMacIdAsParameter.setMac_id(currentLockMacId);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<LockKeyGen> mlockKeyGen = lockWebServiceApi.AddLock(sendMacIdAsParameter);
        mlockKeyGen.enqueue(new Callback<LockKeyGen>() {
            @Override
            public void onResponse(Call<LockKeyGen> call, Response<LockKeyGen> response) {
                if (response.code() == 200) {
                    macAddress = currentLockMacId;
                    mPrefUtil.setIntPref(SkylockConstant.PREF_LOCK_ID + currentLockMacId, response.body().getPayload().getLock_id());
                    generatekey = UtilHelper.getMD5Hash("" + mPrefUtil.getIntPref(SkylockConstant.PREF_USER_ID, 0));
                    bleConnectionWithLock(mBluetoothDevice, blinkLed, generatekey);
                    isForceFirmwareUpdate = true;
                } else {
                    CentralizedAlertDialog.showDialog(mContext,getResources().getString(R.string.warning),
                            getResources().getString(R.string.lock_registered_failure), 0);
                    progress_RelativeLayout.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(Call<LockKeyGen> call, Throwable t) {
                progress_RelativeLayout.setVisibility(View.GONE);

            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    private void getLockList() {
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<LockList> getLockList = lockWebServiceApi.GetLockData();
        getLockList.enqueue(new Callback<LockList>() {
            @Override
            public void onResponse(Call<LockList> call, Response<LockList> response) {
                if (response.code() == 200) {
                    final LockList payloadEntity = response.body();
                    final Gson gson = new Gson();
                    final String lockJson = gson.toJson(payloadEntity);
                    mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, lockJson);
                }
            }

            @Override
            public void onFailure(Call<LockList> call, Throwable t) {
                Log.e("There are some problem", t.toString());
            }
        });
    }

    private void setPinTouchSequenceCall(String[] touchPadSequence) {
        final TouchPadSequence mTouchPadSequence = new TouchPadSequence();
        try {
            mTouchPadSequence.setPin_code(touchPadSequence);
            mTouchPadSequence.setMac_id(macAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<SuccessResponse> setTouchPad = lockWebServiceApi.SaveTouchPinCode(mTouchPadSequence);
        setTouchPad.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {

            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {

            }
        });
    }

    private void updateLockNameCall(final String lockName) {
        final UpdateLockNameParameter.PropertiesEntity mPropertiesEntity = new UpdateLockNameParameter.PropertiesEntity();
        mPropertiesEntity.setLock_id(mPrefUtil.getIntPref(SkylockConstant.PREF_LOCK_ID + macAddress, 0));
        mPropertiesEntity.setName(lockName);
        final UpdateLockNameParameter mUpdateLockNameParameter = new UpdateLockNameParameter();
        mUpdateLockNameParameter.setProperties(mPropertiesEntity);
        final LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<UpdateLockNameResponse> getLockList = lockWebServiceApi.AddLockName(mUpdateLockNameParameter);
        getLockList.enqueue(new Callback<UpdateLockNameResponse>() {
            @Override
            public void onResponse(Call<UpdateLockNameResponse> call, Response<UpdateLockNameResponse> response) {
                if (response.code() == 200) {
                    if (mService != null)
                        mService.callFWInfo();
                    getLockList();
                    mPrefUtil.setStringPref(macAddress, lockName);
                } else {
                    Toast.makeText(AddLockActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateLockNameResponse> call, Throwable t) {
                Log.e("There are some problem", t.toString());
                progress_RelativeLayout.setVisibility(View.GONE);
            }
        });

    }

    private void callSetPinFragment() {
        textView_header.setText(R.string.enter_a_pin_code);
        toolbar.setBackgroundColor(Color.WHITE);
        textView_header.setTextColor(Color.parseColor("#829cb2"));
        setFragment(seTPinFragment, true, "SeTPinFragment");
        textView_header.setTextSize(24);
        textView_header.setPadding(0, 25, 0, 0);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            } else {
                checkLocationListener();
                return;
            }


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
        if (device != null)
            macAddress = UtilHelper.getLockMacIDFromName(device.getName());
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onConnectionTimeOut() {
        deleteLockCall(macAddress);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress_RelativeLayout.setVisibility(View.GONE);
            }
        });
        Toast.makeText(AddLockActivity.this, "Connection time out. Try again.", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onDeviceDisconnected(boolean shippingModeEnabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress_RelativeLayout.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onBoardFailed() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AddLockActivity.this, "Access denied", Toast.LENGTH_LONG).show();
                mService.close();
                deleteLockCall(macAddress);
                progress_RelativeLayout.setVisibility(View.GONE);

            }
        });
    }

    @Override
    public void onBoardCompleted(BluetoothGatt mBluetoothGatt, final String mode) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mode.equals("00")) {
                    callHomePageActivity();
                } else {
                    progress_RelativeLayout.setVisibility(View.GONE);
                    setFragment(lockStepThree, true, "LockStepThree");
                    refreshMenu.setVisible(false);
                    isEnableBackPress = false;
                }
            }
        });
    }

    @Override
    public void onGetHardwareInfo(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt != null) {
            macAddress = UtilHelper.getLockMacIDFromName(gatt.getDevice().getName());
            mService.increaseTxPower();
            final int position = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);
            if (position == 1 || position == 2) {
                lockStatus = position;
            } else if (position == 0) {
                lockStatus = 0;
            }

        }
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
    public void onDeviceStatus(BluetoothGatt gatt, BluetoothGattCharacteristic mode) {
        if (gatt != null)
            macAddress = UtilHelper.getLockMacIDFromName(gatt.getDevice().getName());
    }

    @Override
    public void onCrashedAndTheft(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }


    @Override
    public void onScanFailed() {

        LockStepTwo.newInstance().hideProgress();

    }

    @Override
    public void onScanedDevice(HashSet<BluetoothDevice> device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView_header.setText(R.string.ellipses);
                if (refreshMenu != null)
                    refreshMenu.setVisible(true);
            }
        });
        for (BluetoothDevice bluetoothDevices : device) {
            SkylockConstant.mLockMacIdList.add(UtilHelper.getLockMacIDFromName(bluetoothDevices.getName()));
        }
        if (device != null) {
            mlockDisConnected = true;
            LockStepTwo.newInstance().scanedDevices(device);

        }
    }

    @Override
    public void onGetRSSi(BluetoothGatt gatt, int rssi) {

    }

    @Override
    public void getFWinfo(final String version) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentFWversion = Double.parseDouble(version);
                progress_RelativeLayout.setVisibility(View.GONE);
                callSetPinFragment();
            }
        });


    }

    @Override
    public void readSerialNumber(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

    }


    public void deleteLockCall(String macAddress) {
        final SendMacIdAsParameter sendMacIdAsParameter = new SendMacIdAsParameter();
        if (macAddress != null)
            sendMacIdAsParameter.setMac_id(macAddress);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);

        Call<SuccessResponse> delete = lockWebServiceApi.DeleteLock(sendMacIdAsParameter);

        delete.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                mPrefUtil.setStringPref(sendMacIdAsParameter.getMac_id() + SkylockConstant.SKYLOCK_PUBLIC_KEYS, "");
                mPrefUtil.setStringPref(sendMacIdAsParameter.getMac_id() + SkylockConstant.SKYLOCK_SIGNED_MESSAGES, "");
                UtilHelper.analyticTrackUserAction("Delete lock", "Custom", "Ellipses", null, "ANDROID");
            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {

            }
        });

    }

    private void getLockMetaData() {

        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<FirmwareUpdates> mLockMetaData = lockWebServiceApi.GetLatestFirmwareVersion();
        mLockMetaData.enqueue(new Callback<FirmwareUpdates>() {
            @Override
            public void onResponse(Call<FirmwareUpdates> call, Response<FirmwareUpdates> mLockMetaData) {
                if (mLockMetaData.code() == 200) {
                    try {
                        final List<String> fWVersion = mLockMetaData.body().getPayload();
                        final int lastIndex = fWVersion.size() - 1;
                        latestFWversion = Double.parseDouble(fWVersion.get(lastIndex));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<FirmwareUpdates> call, Throwable t) {
            }
        });

    }


}
