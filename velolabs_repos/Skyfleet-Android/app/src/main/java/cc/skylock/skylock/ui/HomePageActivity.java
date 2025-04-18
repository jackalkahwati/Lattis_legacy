package cc.skylock.skylock.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.R;
import cc.skylock.skylock.bluetooth.BluetoothDeviceStatus;
import cc.skylock.skylock.bluetooth.SkylockBluetoothLEService;
import cc.skylock.skylock.bluetooth.SkylockCrashTheftAlert;
import cc.skylock.skylock.notification.NotificationView;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.service.LocationService;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.ui.fragment.EmergencyContacts;
import cc.skylock.skylock.ui.fragment.FindMyEllipsesFragment;
import cc.skylock.skylock.ui.fragment.GetLockInfo;
import cc.skylock.skylock.ui.fragment.MyEllipsesFragment;
import cc.skylock.skylock.ui.fragment.SharingChildFragment_LockList;
import cc.skylock.skylock.ui.fragment.TermsAndConditionFragment;
import cc.skylock.skylock.ui.fragment.UserProfileFragment;
import cc.skylock.skylock.ui.fragment.WebviewFragment;
import cc.skylock.skylock.utils.LockDetailsHelper;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UserDetailsHelper;
import cc.skylock.skylock.utils.UtilHelper;
import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BluetoothDeviceStatus,
        View.OnClickListener {
    public static BluetoothGatt mCurrentlyconnectedGatt = null;
    private BluetoothAdapter mBtAdapter = null;
    private static final int REQUEST_ENABLE_BT = 2;
    private SkylockBluetoothLEService mService = null;
    public static final String TAG = "ellipses";
    private PrefUtil mPrefUtil;
    public static String connectedMacAddress = null;
    private Fragment myEllipsesFragment = null, findMyEllipsesFragment = null,
            profileFragment = null, mEmergencyContactFragment = null,
            sharingChildFragment_LockList = null, termsAndConditionFragment = null;
    private String lock_Name = null;
    public static GetLockInfo getLockInfo;
    private HashSet<BluetoothDevice> mBluetoothDeviceList;
    private ArrayList<HashMap<String, String>> myLockAndShareLockListData;
    public SkylockCrashTheftAlert crashTheftAlert;
    private Context mContext;
    private RelativeLayout relativeLayout_LockUnlock;
    private ImageView imageView_battery, imageView_tower, crashAlert_ImageView,
            theftAlert_ImageView;
    private TextView textView_lockName, textView_header, textView_Lock_status, textView_status,
            textView_content1, textView_content2, textView_userProfile, mEmergencyContactBt,
            textView_help, textView_order, textView_termsandcondions;
    private RelativeLayout relativeLayout_toolsLayout;
    private static boolean lockStatus = false;
    private Toolbar toolbar;
    private int colorprimary = 0;
    private int colorapptheme = 0;
    private DrawerLayout drawer;
    private RelativeLayout relativeLayout_content;
    private MenuItem addnewMenu, refreshMenu;
    private ProgressBar mProgressBar;
    private Rect bounds;
    public AudioManager audioManager;
    private String SCANNING = "";
    private String CONNECTING = "";
    private String LOCKED = "";
    private String UNLOCKED = "";
    private String UNLOCKING = "";
    private String LOCKING = "";
    private String NOT_CONNECTED = "";
    private String TAP_LOCK = "";
    private String TAP_UNLOCK = "";
    private boolean isFirstTime = false;
    private Bundle bundle;
    private MediaPlayer mp;
    private Handler mHandler;
    private boolean proximityFunctionsUnlockDone = false;
    private boolean proximityFunctionsLockDone = false;
    private NavigationView navigationView;
    public static String macID = null;
    private String lockListJson = "";
    private boolean isCurrentScreen = false;
    private boolean isBindService = false;
    private BroadcastReceiver mBroadcastReceiver;
    private final int showECAlert = 1;
    private final int showProfileSettingAlert = 2;
    private boolean autoLock = false, autoUnlock = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        mHandler = new Handler();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        colorprimary = ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null);
        colorapptheme = ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_white);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        myLockAndShareLockListData = new ArrayList<>();
        final TextView tv_label_crash = (TextView) findViewById(R.id.tv_label_crash);
        final TextView tv_label_crashdetection = (TextView) findViewById(R.id.tv_label_crashdetection);
        final TextView tv_label_crashon = (TextView) findViewById(R.id.tv_label_crashon);
        final TextView tv_label_thefton = (TextView) findViewById(R.id.tv_label_thefton);
        final TextView tv_label_theftdetection = (TextView) findViewById(R.id.tv_label_theftdetection);
        final TextView tv_label_theft = (TextView) findViewById(R.id.tv_label_theft);
        tv_label_crash.setTypeface(UtilHelper.getTypface(this));
        assert tv_label_crashdetection != null;
        tv_label_crashdetection.setTypeface(UtilHelper.getTypface(this));
        assert tv_label_crashon != null;
        tv_label_crashon.setTypeface(UtilHelper.getTypface(this));
        tv_label_thefton.setTypeface(UtilHelper.getTypface(this));
        tv_label_theftdetection.setTypeface(UtilHelper.getTypface(this));
        tv_label_theft.setTypeface(UtilHelper.getTypface(this));
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        textView_header = (TextView) findViewById(R.id.tv_header);
        mPrefUtil = new PrefUtil(this);
        mContext = HomePageActivity.this;
        mBluetoothDeviceList = new HashSet<>();
        textView_header.setText("");
        changeStatusBarColor(colorapptheme);
        SCANNING = getResources().getString(R.string.action_scanning);
        CONNECTING = getResources().getString(R.string.action_connecting);
        LOCKED = getResources().getString(R.string.action_locked);
        LOCKING = getResources().getString(R.string.action_locking);
        UNLOCKED = getResources().getString(R.string.action_unlocked);
        UNLOCKING = getResources().getString(R.string.action_unlocking);
        NOT_CONNECTED = getResources().getString(R.string.action_notconnected);
        TAP_LOCK = getResources().getString(R.string.action_tap_lock);
        TAP_UNLOCK = getResources().getString(R.string.action_tap_unlock);
        mPrefUtil.setBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, true);
        relativeLayout_LockUnlock = (RelativeLayout) findViewById(R.id.rl_lockunlocklayout);
        relativeLayout_toolsLayout = (RelativeLayout) findViewById(R.id.rl_toolslayout);
        imageView_battery = (ImageView) findViewById(R.id.iv_battery);
        textView_lockName = (TextView) findViewById(R.id.tv_lockName);
        textView_content1 = (TextView) findViewById(R.id.tv_content1);
        textView_content2 = (TextView) findViewById(R.id.tv_content2);
        imageView_tower = (ImageView) findViewById(R.id.iv_tower);
        textView_status = (TextView) findViewById(R.id.tv_status);
        textView_termsandcondions = (TextView) findViewById(R.id.tv_terms);
        crashAlert_ImageView = (ImageView) findViewById(R.id.iv_crash);
        theftAlert_ImageView = (ImageView) findViewById(R.id.iv_theft);
        textView_Lock_status = (TextView) findViewById(R.id.tv_status1);
        relativeLayout_content = (RelativeLayout) findViewById(R.id.rl_content);
        textView_userProfile = (TextView) findViewById(R.id.tv_profile);
        mEmergencyContactBt = (TextView) findViewById(R.id.tv_ec);
        textView_help = (TextView) findViewById(R.id.tv_help);
        textView_order = (TextView) findViewById(R.id.tv_rate);
        textView_userProfile.setOnClickListener(this);
        mEmergencyContactBt.setOnClickListener(this);
        textView_termsandcondions.setOnClickListener(this);
        textView_help.setOnClickListener(this);
        textView_order.setOnClickListener(this);
        relativeLayout_content.setVisibility(View.GONE);
        textView_status.setVisibility(View.GONE);
        relativeLayout_toolsLayout.setVisibility(View.GONE);
        mProgressBar = (ProgressBar) findViewById(R.id.home_progressBar);
        mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.notconnected_progress, null));
        bounds = mProgressBar.getIndeterminateDrawable().getBounds();
        relativeLayout_LockUnlock.setEnabled(false);
        textView_header.setText("");
        textView_Lock_status.setText(NOT_CONNECTED);
        textView_Lock_status.setTypeface(UtilHelper.getTypface(mContext));
        textView_header.setTypeface(UtilHelper.getTypface(mContext));
        textView_status.setTypeface(UtilHelper.getTypface(mContext));
        textView_lockName.setTypeface(UtilHelper.getTypface(mContext));
        textView_content2.setTypeface(UtilHelper.getTypface(mContext));
        textView_content1.setTypeface(UtilHelper.getTypface(mContext));
        SkylockConstant.userToken = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_TOKEN, "");
        bundle = getIntent().getExtras();
        crashTheftAlert = new SkylockCrashTheftAlert(mContext);
        myEllipsesFragment = MyEllipsesFragment.newInstance();
        findMyEllipsesFragment = FindMyEllipsesFragment.newInstance();
        profileFragment = UserProfileFragment.newInstance();
        mEmergencyContactFragment = EmergencyContacts.newInstance();
        termsAndConditionFragment = TermsAndConditionFragment.newInstance();
        sharingChildFragment_LockList = SharingChildFragment_LockList.newInstance();
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (mp != null) {
            mp.release();
        }

        textView_content2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePageActivity.this, AddLockActivity.class)
                        .putExtra("ADD_LOCK", "SCAN"));
            }
        });

        if (SkylockConstant.SKYLOCK_CRASHSELECTION == SkylockBluetoothLEService.crashTheftselection) {
            crashAlert_ImageView.setImageResource(R.drawable.icon_crash_select);
        } else if (SkylockConstant.SKYLOCK_THEFTSELECTION == SkylockBluetoothLEService.crashTheftselection) {
            theftAlert_ImageView.setImageResource(R.drawable.icon_theft_select);
        } else {
            theftAlert_ImageView.setImageResource(R.drawable.icon_theft_unselect);
            crashAlert_ImageView.setImageResource(R.drawable.icon_crash_unselect);
        }
        crashAlert_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!crashTheftAlert.isTheft()) {
                    if (!UserDetailsHelper.isUserDetailsPresent(mContext)) {
                        showAlertDialogForEC(showProfileSettingAlert);
                        return;
                    }

                    final String ecList = mPrefUtil.getStringPref(SkylockConstant.PREF_EMERGENCY_CONTACT_LIST, "");
                    if (ecList == null || Objects.equals(ecList, "")) {
                        showAlertDialogForEC(showECAlert);
                    }

                    try {
                        JSONArray ecListJson = new JSONArray(ecList);
                        if (ecListJson.length() >= 1) {
                            if (!SkylockBluetoothLEService.mBluetoothGattDescriptorEnable) {
                                enableCrashAndTheft(SkylockConstant.SKYLOCK_CRASHSELECTION);
                                crashAlert_ImageView.setImageResource(R.drawable.icon_crash_select);
                                crashTheftAlert.flagCrash();
                                mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_CRASH_ENABLE, true);
                            } else {
                                disableCrashAlert();

                            }
                        } else {
                            if (!SkylockBluetoothLEService.mBluetoothGattDescriptorEnable) {
                                if (UserDetailsHelper.isUserDetailsPresent(mContext))
                                    showAlertDialogForEC(showECAlert);
                                else
                                    showAlertDialogForEC(showProfileSettingAlert);
                            } else
                                disableCrashAlert();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        theftAlert_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!crashTheftAlert.isCrash()) {
                    if (!SkylockBluetoothLEService.mBluetoothGattDescriptorEnable) {
                        enableCrashAndTheft(SkylockConstant.SKYLOCK_THEFTSELECTION);
                        theftAlert_ImageView.setImageResource(R.drawable.icon_theft_select);
                        crashTheftAlert.flagTheft(mPrefUtil.getIntPref(macID + SkylockConstant.PREF_LOCK_THEFT_SENSITIVITY, 2));
                        mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_THEFT_ENABLE, true);
                    } else {
                        if (!crashTheftAlert.isCrash()) {
                            disableCrashAndTheft(0);
                        }
                        theftAlert_ImageView.setImageResource(R.drawable.icon_theft_unselect);
                        mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_THEFT_ENABLE, false);
                        crashTheftAlert.disableCrashTheft();
                    }
                }
            }
        });

        relativeLayout_LockUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView_status.setVisibility(View.GONE);
                if (lockStatus) {
                    unLock();
                } else {
                    lock();
                }
            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("USER_DELETE_ACCOUNT")) {
                    mCurrentlyconnectedGatt = null;
                    finish();
                    context.startActivity(new Intent(HomePageActivity.this, LoginMenuActivity.class));
                } else if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            disConnectionUI();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            if (mService != null) {
                                mService.stopTimer();
                                closeConnection();
                            }
                            break;
                        case BluetoothAdapter.STATE_ON:
                            doScanning();
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            break;
                    }
                } else if (intent.getAction().equals(SkylockConstant.ACTION_GATT_DISCONNECTED)) {
                    disConnectionUI();
                } else if (intent.getAction().equals(SkylockConstant.ACTION_GATT_CONNECTED)) {
                    connectionUI(lock_Name);
                    getHWInfo();
                } else if (intent.getAction().equals("INVOKE_SHARE_LOCK")) {
                    getLockListUpdateShareLock();

                }

            }
        };
    }

    private void handleRevokeLockAccess() {
        closeConnection();
    }

    private void disableCrashAlert() {
        if (!crashTheftAlert.isTheft()) {
            disableCrashAndTheft(0);
        }
        mPrefUtil.setBooleanPref(macID + SkylockConstant.SKYLOCK_CRASH_ENABLE, false);
        crashAlert_ImageView.setImageResource(R.drawable.icon_crash_unselect);

        crashTheftAlert.disableCrashTheft();
    }

    private void showAlertDialogForEC(final int alertType) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.alert_add_ec);
        dialog.setCancelable(false);
        final TextView textView_label_cancel = (TextView) dialog.findViewById(R.id.tv_title);
        final TextView textView_label_Locate = (TextView) dialog.findViewById(R.id.tv_description);
        final CardView cv_ok = (CardView) dialog.findViewById(R.id.cv_yes_button);
        final CardView cv_cancel = (CardView) dialog.findViewById(R.id.cv_cancel_button);
        textView_label_cancel.setTypeface(UtilHelper.getTypface(this));
        textView_label_Locate.setTypeface(UtilHelper.getTypface(this));
        textView_label_cancel.setText(getResources().getString(R.string.warning));
        if (alertType == showECAlert) {
            textView_label_Locate.setText(getResources().getString(R.string.ec_contact_description));
        } else {
            textView_label_Locate.setText(getResources().getString(R.string.first_last_name_missing_alert));
        }
        cv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        cv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alertType == showECAlert) {
                    callEmergencyContacts();
                } else if (alertType == showProfileSettingAlert) {
                    callProfileFragment();
                }
                dialog.cancel();
            }
        });
        dialog.show();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    public void lock() {
        if (mService != null) {
            showProgress(LOCKING);
            mProgressBar.setIndeterminate(false);
            Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds(); // get current bounds before change drawable
            mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.lock_progress_background, null));
            mProgressBar.getIndeterminateDrawable().setBounds(bounds); // set bounds back
            mProgressBar.setProgress(50);
            autoLock = mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_PROXIMITY_LOCK_ENABLE, false);
            mService.lock(false);
        }
    }

    private void showProgress(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                textView_Lock_status.setText(status);
            }
        });
    }

    public void getHWInfo() {
        if (mService != null) {
            mService.getHardwareInfo();
        }
    }

    public void showAddNewIcon(boolean enable) {
        if (addnewMenu != null)
            addnewMenu.setVisible(enable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        addnewMenu = menu.findItem(R.id.action_addnew);
        refreshMenu = menu.findItem(R.id.action_refresh);
        addnewMenu.setVisible(false);
        refreshMenu.setVisible(false);
        return true;

    }

    private void changeHamburgerIconColor(int color) {
        final PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            final View v = toolbar.getChildAt(i);

            if (v instanceof ImageButton) {
                ((ImageButton) v).setColorFilter(colorFilter);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_addnew) {
            Intent intent = new Intent(HomePageActivity.this, AddLockActivity.class)
                    .putExtra("ADD_LOCK", "HOME");
            startActivity(intent);
            final FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.getFragments() != null) {
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                    hideHeaderLayout();
                    addnewMenu.setVisible(false);
                }
            }
            return true;
        } else if (id == R.id.action_refresh) {
            refreshMenu.setVisible(false);
            doScanning();
            return true;
        } else if (id == android.R.id.home) {
            drawer.openDrawer(Gravity.LEFT);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doScanning() {
        if (mBtAdapter.isEnabled()) {
            showProgress(SCANNING);
            relativeLayout_content.setVisibility(View.GONE);
            final Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds(); // get current bounds before change drawable
            mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.lock_progress_background, null));
            mProgressBar.getIndeterminateDrawable().setBounds(bounds); // set bounds back
            intializeBluetoothLE();
        } else
            Toast.makeText(this, "Please turn on your bluetooth", Toast.LENGTH_LONG).show();

    }

    public void unLock() {
        if (mService != null) {
            showProgress(UNLOCKING);
            final Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds(); // get current bounds before change drawable
            mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.unlock_progress_background, null));
            mProgressBar.getIndeterminateDrawable().setBounds(bounds); // set bounds back
            mProgressBar.setProgress(50);
            mService.unLock();
        }
    }


    public void putShippingMode() {
        if (mService != null) {
            mService.putShippingMode();
        }
    }

    private void service_init() {

        Intent bindIntent = new Intent(this, SkylockBluetoothLEService.class);
        getApplicationContext().bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        isBindService = true;

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
            if (mService == null) {
                service_init();
            }
            mService.registerBluetoothDeviceStatusListener(HomePageActivity.this);
            if (mBtAdapter.isEnabled()) {

                final Intent intent = getIntent();
                if (intent != null)
                    onNewIntent(intent);
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService.unregisterBluetoothDeviceStatusListener();
            mService = null;
        }
    };

    private void handleExtras(String lockNameFromOnBoardprocess) {
        isFirstTime = true;
        if (lockNameFromOnBoardprocess != null) {
            lock_Name = mPrefUtil.getStringPref(lockNameFromOnBoardprocess, "");
            final Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds(); // get current bounds before change drawable
            mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.lock_progress, null));
            mProgressBar.getIndeterminateDrawable().setBounds(bounds);
            connectionUI(lock_Name);
            saveCurrentLocation();
            final int lockPosition = bundle.getInt("Lock_State");
            if (lockPosition == 0) {
                unlockUI();
            } else {
                lockUI();
            }
            if (mService != null) {
                mService.getRssiValue();
                isFirstTime = true;
                getHWInfo();
            }
        }

    }


    @Override
    public void onBackPressed() {
        customMenuOption(null);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            hideHeaderLayout();
            drawer.closeDrawer(GravityCompat.START);
        } else {
            hideHeaderLayout();
        }
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getFragments() != null) {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
                //   hideHeaderLayout();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }


    public void hideHeaderLayout() {
        addnewMenu.setVisible(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_white);
        textView_header.setText("");
        changeHamburgerIconColor(Color.WHITE);
        toolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.app_background, null));
        changeStatusBarColor(colorapptheme);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        customMenuOption(null);
        int id = item.getItemId();
        item.setCheckable(true);
        if (id == R.id.nav_ellipses) {
            isCurrentScreen = false;
            changeHeaderUI("ELLIPSES", colorprimary, Color.WHITE);
            addnewMenu.setVisible(true);
            try {
                if (myEllipsesFragment != null && myEllipsesFragment instanceof MyEllipsesFragment) {
                    if (!myEllipsesFragment.isAdded()) {
                        setFragment(myEllipsesFragment, true, "MyEllipsesFragment");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_fme) {
            isCurrentScreen = false;
            addnewMenu.setVisible(false);
            changeHeaderUI("FIND MY ELLIPSE", Color.parseColor("#efefef"), colorapptheme);
            try {
                if (findMyEllipsesFragment != null && findMyEllipsesFragment instanceof FindMyEllipsesFragment) {
                    if (!findMyEllipsesFragment.isAdded()) {
                        setFragment(findMyEllipsesFragment, true, "FindMyEllipsesFragment");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (id == R.id.nav_share) {
            if (weHavePermissionToReadContacts()) {
                changeStatusBarColor(colorprimary);
                showAlertDialogForSharing();
            }


        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean weHavePermissionToReadContacts() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                this.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 123);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    private void showAlertDialogForSharing() {

        final Dialog dialog = new Dialog(mContext, android.R.style.Theme_Holo_Light_NoActionBar);
        dialog.setContentView(R.layout.alert_sharing_home);
        dialog.setCancelable(false);
        final ImageView imageView_close = (ImageView) dialog.findViewById(R.id.iv_close);
        final TextView textView_label_content_one = (TextView) dialog.findViewById(R.id.tv_content1);
        final TextView textView_cv_label = (TextView) dialog.findViewById(R.id.tv_label_startbutton);
        final CardView cardView_startbutton = (CardView) dialog.findViewById(R.id.cv_start_button);
        textView_label_content_one.setTypeface(UtilHelper.getTypface(mContext));
        textView_cv_label.setTypeface(UtilHelper.getTypface(mContext));
        final String one = "<font color='#57D8FF'>" + "HOW DO I SHARE MY ELLIPSE? " + "</font>";
        final String two = "<font color='#9B9B9B'>" + "<br/><br/> You can share your Ellipse with any of your phone contacts.  Just choose a contact and weʼll SMS them an invitation. " + "</font>";
        final String three = "<font color='#57D8FF'>" + "<br/><br/><br/> WHAT DO MY FRIENDS NEED TO DO?" + "</font>";
        final String four = "<font color='#9B9B9B'>" + "<br/><br/> Theyʼll need to install the Ellipse app and once theyʼve accepted your invitation, they can start using your Ellipse.  Theyʼll be able to lock and unlock it just like you can but they wonʼt be able to change any of your settings.";
        if (Build.VERSION.SDK_INT >= 24) {
            textView_label_content_one.setText(Html.fromHtml((one + two + three + four), 0));
        } else {
            textView_label_content_one.setText(Html.fromHtml((one + two + three + four)));
        }


        cardView_startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard(mContext);

                callSharingFragment();
                dialog.cancel();
            }
        });
        imageView_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatusBarColor(colorapptheme);
                dialog.cancel();
            }

        });
        dialog.show();

    }

    private void callSharingFragment() {
        addnewMenu.setVisible(false);
        changeStatusBarColor(colorprimary);
        changeHeaderUI("SHARING", colorprimary, Color.WHITE);
        refreshMenu.setVisible(false);

        if (sharingChildFragment_LockList != null && sharingChildFragment_LockList instanceof SharingChildFragment_LockList) {
            if (!sharingChildFragment_LockList.isAdded()) {
                getSupportFragmentManager().popBackStack();
                setFragment(sharingChildFragment_LockList, true, "SharingChildFragment_LockList");
            }
        }
    }


    public void changeHeaderUI(String headerName,
                               int statusBarcolor, int headerTextColor) {
        try {
            if (headerTextColor != Color.WHITE) {
                toolbar.setBackgroundColor(Color.WHITE);
            } else {
                toolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                        R.color.colorPrimary, null));
            }
            textView_header.setText(headerName);
            textView_header.setTextColor(headerTextColor);
            changeStatusBarColor(statusBarcolor);
            changeHamburgerIconColor(headerTextColor);
            refreshMenu.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void intializeBluetoothLE() {
        if (mService == null) {
            service_init();
        } else if (!mService.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }
    }

    @Override
    protected void onResume() {
        changeHamburgerIconColor(Color.WHITE);
        if (!Fabric.isInitialized()) {
            Fabric.with(this, new Crashlytics());
        }
        isCurrentScreen = true;
        lockListJson = mPrefUtil.getStringPref(SkylockConstant.PREF_LOCK_LIST, "");
        if (!lockListJson.equals("")) {
            myLockAndShareLockListData = LockDetailsHelper.convertJsonToGson(mContext, lockListJson);
        } else {
            disConnectionUI();
        }

        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mService == null) {
                service_init();
            } else {
                mService.registerBluetoothDeviceStatusListener(HomePageActivity.this);
            }
        }

        registerReceiver(mBroadcastReceiver, makeGattUpdateIntentFilter());

        if (mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_CRASH_ENABLE, false)) {
            crashAlert_ImageView.setImageResource(R.drawable.icon_crash_select);
            crashTheftAlert.flagCrash();
        } else {
            crashAlert_ImageView.setImageResource(R.drawable.icon_crash_unselect);
        }
        if (mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_THEFT_ENABLE, false)) {
            theftAlert_ImageView.setImageResource(R.drawable.icon_theft_select);
            crashTheftAlert.flagTheft(mPrefUtil.getIntPref(macID + SkylockConstant.PREF_LOCK_THEFT_SENSITIVITY, 2));
        } else {
            theftAlert_ImageView.setImageResource(R.drawable.icon_theft_unselect);
        }
        if (mService != null && macID != null) {
            autoLock = mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_PROXIMITY_LOCK_ENABLE, false);
            autoUnlock = mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_PROXIMITY_UNLOCK_ENABLE, false);
            if (autoLock || autoUnlock)
                mService.getRssiValue();
        }
        if (addnewMenu != null)
            hideHeaderLayout();
        super.onResume();

    }

    public void enableCrashAndTheft(int crashTheftselection) {
        if (mService != null)
            mService.enableCrashAndTheft(crashTheftselection);
    }

    public void disableCrashAndTheft(int crashTheftselection) {
        if (mService != null)
            mService.disableCrashAndTheft(crashTheftselection);
    }

    public void lockUI() {
        lockStatus = true;
        try {
            final Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds(); // get current bounds before change drawable
            mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.lock_progress, null));
            mProgressBar.getIndeterminateDrawable().setBounds(bounds); // set bounds back
            mProgressBar.setProgress(50);
            textView_status.setVisibility(View.VISIBLE);
            textView_status.setText(TAP_UNLOCK);
            showProgress(LOCKED);
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                // Play the sound
                final int resId = R.raw.locksound;
                mp = MediaPlayer.create(HomePageActivity.this, resId);
                mp.start();
            }
            textView_lockName.setText(lock_Name);
            UtilHelper.analyticTrackUserAction("Locked", "Custom", "", null, "ANDROID");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unlockUI() {

        lockStatus = false;
        try {
            Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds(); // get current bounds before change drawable
            mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.unlock_progress, null));
            mProgressBar.getIndeterminateDrawable().setBounds(bounds); // set bounds back
            mProgressBar.setProgress(50);
            textView_status.setVisibility(View.VISIBLE);
            showProgress(UNLOCKED);
            textView_status.setText(TAP_LOCK);
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                // Play the sound
                final int resId = R.raw.unlocksound;
                mp = MediaPlayer.create(HomePageActivity.this, resId);
                mp.start();
            }
            textView_lockName.setText(lock_Name);
            UtilHelper.analyticTrackUserAction("Unlocked", "Custom", "", null, "ANDROID");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void connectionUI(final String lockName) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                relativeLayout_LockUnlock.setEnabled(true);
                textView_lockName.setText(lockName);
                relativeLayout_toolsLayout.setVisibility(View.VISIBLE);
                relativeLayout_content.setVisibility(View.GONE);
                UtilHelper.analyticTrackUserAction("Lock Paired", "Custom", "", null, "ANDROID");
            }
        });

    }

    public void disConnectionUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCurrentlyconnectedGatt = null;
                final Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds(); // get current bounds before change drawable
                mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.notconnected_progress, null));
                mProgressBar.getIndeterminateDrawable().setBounds(bounds); // set bounds back
                relativeLayout_content.setVisibility(View.VISIBLE);
                relativeLayout_LockUnlock.setEnabled(false);
                textView_lockName.setText("");
                relativeLayout_toolsLayout.setVisibility(View.GONE);
                showProgress(NOT_CONNECTED);
                textView_status.setVisibility(View.GONE);
                if (mService != null)
                    mService.stopRssiValue();
            }
        });
        if (myEllipsesFragment != null && getLockInfo != null) {
            getLockInfo.onBleDisconnect();
        }
        if (findMyEllipsesFragment != null && getLockInfo != null) {
            getLockInfo.onBleDisconnect();
        }
//        saveCurrentLocation();
//        UtilHelper.analyticTrackUserAction("Lock Unpaired", "Custom", "", null, "ANDROID");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            ((UserProfileFragment) profileFragment).beginCrop(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            ((UserProfileFragment) profileFragment).handleCrop(resultCode, data);
        }
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            } else {
                if (mService == null) {
                    service_init();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showAlertDialogForSharing();
        } else {
            return;
        }
    }

    @Override
    public void onDestroy() {
        if (null != mServiceConnection && isBindService) {
            isBindService = false;
            getApplicationContext().unbindService(mServiceConnection);
            getApplicationContext().stopService(new Intent(this, SkylockBluetoothLEService.class));
        }
        if (null != mp) {
            try {
                mp.stop(); //error
                mp.reset();
                mp.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }


    @Override
    protected void onStop() {
        stopTimer();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void stopTimer() {
        if (mService != null) {
            mService.stopTimer();
            mService.stopRssiValue();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AddLockActivity.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction("USER_DELETE_ACCOUNT");
        intentFilter.addAction(SkylockConstant.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction("INVOKE_SHARE_LOCK");
        return intentFilter;
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
        mService.getRssiValue();
        lock_Name = mPrefUtil.getStringPref(UtilHelper.getLockMacIDFromName(device.getName()), "");
    }

    @Override
    public void onConnect() {
        isFirstTime = true;
        showProgress(CONNECTING);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                relativeLayout_content.setVisibility(View.GONE);
                mProgressBar.setIndeterminate(false);
                Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds(); // get current bounds before change drawable
                mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.initial_progress_background, null));
                mProgressBar.getIndeterminateDrawable().setBounds(bounds); // set bounds back
                mProgressBar.setProgress(50);
            }
        });

    }

    @Override
    public void onConnectionTimeOut() {
        disConnectionUI();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getLockInfo != null) {
                    if (myEllipsesFragment != null && getLockInfo != null)
                        getLockInfo.onConnectionTimeOut();
                    else if (findMyEllipsesFragment != null && findMyEllipsesFragment != null)
                        getLockInfo.onConnectionTimeOut();
                }
                final String warning = getResources().getString(R.string.warning);
                final String timeoutError = getResources().getString(R.string.out_of_range_alert);
                CentralizedAlertDialog.showDialog(mContext, warning, timeoutError, 0);
            }
        });
    }


    @Override
    public void onDeviceDisconnected(boolean shippingModeEnabled) {
        isFirstTime = false;
        proximityFunctionsUnlockDone = false;
        disConnectionUI();
        if (mService != null)
            mService.stopRssiValue();
        if (myEllipsesFragment != null && getLockInfo != null) {
            getLockInfo.onBleDisconnect();
        }
        if (findMyEllipsesFragment != null && getLockInfo != null) {
            getLockInfo.onBleDisconnect();
        }

    }


    @Override
    public void onBoardFailed() {
        mCurrentlyconnectedGatt = null;
        if (myEllipsesFragment != null && getLockInfo != null)
            getLockInfo.onBoardFailed();
        else if (findMyEllipsesFragment != null && getLockInfo != null)
            getLockInfo.onBoardFailed();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(NOT_CONNECTED);
                Toast.makeText(HomePageActivity.this, "Access denied. Please try again.", Toast.LENGTH_SHORT).show();
                disConnectionUI();
            }
        });

    }

    @Override
    public void onBoardCompleted(BluetoothGatt mBluetoothGatt, String mode) {

        showProgress(CONNECTING);
        mCurrentlyconnectedGatt = mBluetoothGatt;
        macID = UtilHelper.getLockMacIDFromName(mBluetoothGatt.getDevice().getName());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                relativeLayout_content.setVisibility(View.GONE);
                mProgressBar.setIndeterminate(false);
                Rect bounds = mProgressBar.getIndeterminateDrawable().getBounds(); // get current bounds before change drawable
                mProgressBar.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.lock_progress_background, null));
                mProgressBar.getIndeterminateDrawable().setBounds(bounds); // set bounds back
                mProgressBar.setProgress(50);
            }
        });
        lock_Name = mPrefUtil.getStringPref(UtilHelper.getLockMacIDFromName(mBluetoothGatt.getDevice().getName()), "");
        connectionUI(lock_Name);
        saveCurrentLocation();
    }

    private void saveCurrentLocation() {
        try {
            final LocationService location = new LocationService(HomePageActivity.this);
            final LatLng currentLocation = location.updateCoordinates();
            if (currentLocation != null) {
                mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LOCATION, currentLocation.latitude + "," + currentLocation.longitude);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCrashAndTheft(String macID) {
        if (mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_CRASH_ENABLE, false)) {
            enableCrashAndTheft(SkylockConstant.SKYLOCK_CRASHSELECTION);
            crashTheftAlert.flagCrash();
            crashAlert_ImageView.setImageResource(R.drawable.icon_crash_select);
        } else if (mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_THEFT_ENABLE, false)) {
            enableCrashAndTheft(SkylockConstant.SKYLOCK_THEFTSELECTION);
            crashTheftAlert.flagTheft(mPrefUtil.getIntPref(macID + SkylockConstant.PREF_LOCK_THEFT_SENSITIVITY, 2));
            theftAlert_ImageView.setImageResource(R.drawable.icon_theft_select);
        }
    }

    @Override
    public void onGetHardwareInfo(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        connectionUI(lock_Name);
        mCurrentlyconnectedGatt = gatt;
        HomePageActivity.connectedMacAddress = UtilHelper.getLockMacIDFromName(gatt.getDevice().getName());
        if (getLockInfo != null) {
            if (myEllipsesFragment != null && getLockInfo != null)
                getLockInfo.onGetHardwareInfo(gatt, characteristic);
            else if (findMyEllipsesFragment != null && findMyEllipsesFragment != null)
                getLockInfo.onGetHardwareInfo(gatt, characteristic);
        }
        if (isCurrentScreen) {
            final int position = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);
            if (!lockStatus && position == 1 || position == 2) {
                lockStatus = true;
                onLocked();
            } else if (lockStatus && position == 0) {
                lockStatus = false;
                onUnLocked();
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lock_Name = mPrefUtil.getStringPref(connectedMacAddress, "");
                textView_lockName.setText(lock_Name);
            }
        });
        if (isFirstTime) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleCrashAndTheft(macID);
                }
            });
            showBatteryVolt(characteristic);
            mService.increaseTxPower();
            mService.getRssiValue();
        }
    }

    private void showBatteryVolt(BluetoothGattCharacteristic characteristic) {
        final int batvoltage = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
        isFirstTime = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //batvoltage in millivolt
                if (3300 <= batvoltage && batvoltage <= 3600) {
                    imageView_battery.setImageResource(R.drawable.icon_batt_100);
                } else if (3200 <= batvoltage && batvoltage <= 3300) {
                    imageView_battery.setImageResource(R.drawable.icon_batt_75);

                } else if (3100 <= batvoltage && batvoltage <= 3200) {
                    imageView_battery.setImageResource(R.drawable.icon_batt_50);

                } else if (3000 <= batvoltage && batvoltage <= 3100) {
                    imageView_battery.setImageResource(R.drawable.icon_batt_25);
                    CentralizedAlertDialog.showDialog(HomePageActivity.this, getResources().getString(R.string.warning),
                            getResources().getString(R.string.battery_low_alert), 0);
                }
            }
        });

    }

    @Override
    public void onLocked() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lockStatus = true;
                lockUI();
                saveCurrentLocation();
            }
        });

    }

    @Override
    public void onUnLocked() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lockStatus = false;
                unlockUI();
                saveCurrentLocation();
            }
        });


    }

    @Override
    public void onLockMalfunctioned() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CentralizedAlertDialog.showDialog(mContext,
                        getResources().getString(R.string.warning),
                        getResources().getString(R.string.lock_malfunction), 0);
            }
        });
    }

    @Override
    public void onCrashed(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        callVibrator();
        NotificationView.showNotification(HomePageActivity.this, "Crash", "Alert", 0, CrashAlert.class);
        Intent crashAlert = new Intent(this, CrashAlert.class);
        crashAlert.putExtra("MAC_ID", macID);
        startActivity(crashAlert);
        UtilHelper.analyticTrackUserAction("Crash Alert", "Custom", "", null, "ANDROID");
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
    public void onTheft(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        callVibrator();
        NotificationView.showNotification(HomePageActivity.this, "Theft", "Alert", 1, TheftAlert.class);
        Intent theftAlert = new Intent(this, TheftAlert.class);
        startActivity(theftAlert);
        UtilHelper.analyticTrackUserAction("Theft Alert", "Custom", "", null, "ANDROID");
    }


    @Override
    public void onDeviceStatus(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {


    }


    @Override
    public void onCrashedAndTheft(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        crashTheftAlert.putCharacterstic(gatt, characteristic);
    }


    @Override
    public void onScanFailed() {
        showProgress(NOT_CONNECTED);
        disConnectionUI();
    }


    @Override
    public void onScanedDevice(HashSet<BluetoothDevice> device) {
        if (device != null && device.size() > 0) {
            for (BluetoothDevice bluetoothDevices : device) {
                if (UtilHelper.getDeviceMode(bluetoothDevices) != null && UtilHelper.getDeviceMode(bluetoothDevices).equals("ON_BOARDED_MODE")) {
                    mBluetoothDeviceList.add(bluetoothDevices);
                }
                SkylockConstant.mLockMacIdList.add(UtilHelper.getLockMacIDFromName(bluetoothDevices.getName()));
            }
            if (mBluetoothDeviceList != null && mBluetoothDeviceList.size() > 0) {
                boolean isConnectionSuccess = false;
                for (BluetoothDevice bluetoothDevices : mBluetoothDeviceList) {

                    if (myLockAndShareLockListData != null && myLockAndShareLockListData.size() > 0) {
                        for (int i = 0; i < myLockAndShareLockListData.size(); i++) {
                            if (bluetoothDevices != null && myLockAndShareLockListData.get(i).get("LOCK_MACID")
                                    .equals(UtilHelper.getLockMacIDFromName(bluetoothDevices.getName()))
                                    && myLockAndShareLockListData.get(i).get("LOCK_MACID")
                                    .equals(mPrefUtil.getStringPref(SkylockConstant.SKYLOCK_PRIMARY, ""))) {
                                final String key = UtilHelper.getMD5Hash(myLockAndShareLockListData.get(i).get("USER_ID"));
                                isConnectionSuccess = connectBleDevice(bluetoothDevices, key);
                                break;
                            }
                        }
                    } else {
                        disConnectionUI();
                    }

                }
                if (!isConnectionSuccess)
                    disConnectionUI();
            } else {
                disConnectionUI();
            }
        }
    }

    private boolean connectBleDevice(BluetoothDevice bluetoothDevices, String key) {
        return mService.connect(bluetoothDevices, true, key, false);
    }

    public void clearBackStack() {
        hideHeaderLayout();
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public void onGetRSSi(BluetoothGatt gatt, int rssiValue) {
        if (getLockInfo != null) {
            if (myEllipsesFragment != null && getLockInfo != null)
                getLockInfo.onGetBLESignal(gatt, rssiValue);
            else if (findMyEllipsesFragment != null && getLockInfo != null)
                getLockInfo.onGetBLESignal(gatt, rssiValue);
        }
        macID = UtilHelper.getLockMacIDFromName(gatt.getDevice().getName());
        autoUnlock = mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_PROXIMITY_UNLOCK_ENABLE, false);
        autoLock = mPrefUtil.getBooleanPref(macID + SkylockConstant.SKYLOCK_PROXIMITY_LOCK_ENABLE, false);
        if (rssiValue < -75) {
            if (autoLock) {
                if (mService != null && !proximityFunctionsLockDone) {
                    mService.lock(false);
                    proximityFunctionsLockDone = true;
                }
            }

        } else {
            proximityFunctionsLockDone = false;
        }
        if (rssiValue > -60) {
            if (autoUnlock) {
                if (mService != null && !proximityFunctionsUnlockDone) {
                    mService.unLock();
                    proximityFunctionsUnlockDone = true;
                }
            }
        } else {
            proximityFunctionsUnlockDone = false;
        }
        showRSSIStrength(rssiValue);
    }

    @Override
    public void getFWinfo(String version) {
    }

    @Override
    public void readSerialNumber(BluetoothGatt gatt, BluetoothGattCharacteristic
            characteristic) {
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                  int status) {
    }

    private void showRSSIStrength(final int value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value >= -50) {
                    imageView_tower.setImageResource(R.drawable.icon_tower_4);
                } else if (-50 >= value && value >= -70) {
                    imageView_tower.setImageResource(R.drawable.icon_tower_3);
                } else if (-70 >= value && value >= -90) {
                    imageView_tower.setImageResource(R.drawable.icon_tower_2);
                } else if (-90 >= value) {
                    imageView_tower.setImageResource(R.drawable.icon_tower_1);
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_profile:
                customMenuOption(v);
                try {
                    addnewMenu.setVisible(false);
                    callProfileFragment();
                    drawer.closeDrawer(GravityCompat.START);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.tv_ec:
                customMenuOption(v);
                if (profileFragment != null) {
                    ((UserProfileFragment) profileFragment).saveUserDetails();
                }
                if (UserDetailsHelper.isUserDetailsPresent(mContext))
                    callEmergencyContacts();
                else
                    showAlertDialogForEC(showProfileSettingAlert);
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.tv_help:
                customMenuOption(v);
                callWebView("HELP", 1);

                break;
            case R.id.tv_rate:
                customMenuOption(v);
                callWebView("ORDER YOUR ELLIPSE NOW", 2);
                break;
            case R.id.tv_terms:
                customMenuOption(v);
                try {
                    addnewMenu.setVisible(false);
                    changeHeaderUI("TERMS AND CONDITIONS", colorprimary, Color.WHITE);
                    if (!mEmergencyContactFragment.isAdded()) {
                        getSupportFragmentManager().popBackStack();
                        setFragment(termsAndConditionFragment, true, "TermsAndConditionFragment");

                    }
                    drawer.closeDrawer(GravityCompat.START);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    private void callProfileFragment() {
        changeHeaderUI("MY PROFILE", colorprimary, Color.WHITE);
        if (profileFragment != null && profileFragment instanceof UserProfileFragment) {
            if (!profileFragment.isAdded()) {
                getSupportFragmentManager().popBackStack();
                setFragment(profileFragment, true, "UserProfileFragment");
            }
        }
    }

    private void callWebView(String header, int urlId) {
        try {
            addnewMenu.setVisible(false);
            changeHeaderUI(header, colorprimary, Color.WHITE);
            final WebviewFragment mWebviewFragment = new WebviewFragment();
            if (mWebviewFragment != null && mWebviewFragment instanceof WebviewFragment) {
                if (!mWebviewFragment.isAdded()) {
                    getSupportFragmentManager().popBackStack();
                    Bundle bundle = new Bundle();
                    bundle.putInt("URL", urlId);
                    mWebviewFragment.setArguments(bundle);
                    setFragment(mWebviewFragment, true, "WebviewFragment");
                }
            }
            drawer.closeDrawer(GravityCompat.START);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callEmergencyContacts() {
        try {

            addnewMenu.setVisible(false);
            changeHeaderUI("EMERGENCY CONTACTS", colorprimary, Color.WHITE);

            if (!mEmergencyContactFragment.isAdded()) {
                getSupportFragmentManager().popBackStack();
                setFragment(mEmergencyContactFragment, true, "EmergencyContacts");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean bleConnection(String macId, final String generatekey) {
        try {
            if (mService != null) {
                closeConnection();

                if (!mBtAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    return false;
                } else {
                    if (SkylockConstant.mLockMacIdList != null && SkylockConstant.mLockMacIdList.size() > 0 && SkylockConstant.mLockMacIdList.contains(macId)) {
                        for (BluetoothDevice bluetoothDevice : mBluetoothDeviceList) {
                            if (UtilHelper.getLockMacIDFromName(bluetoothDevice.getName()).equals(macId)) {
                                final BluetoothDevice mBluetoothDevice = mBtAdapter.getRemoteDevice(bluetoothDevice.getAddress());
                                if (mBluetoothDevice != null) {
                                    mPrefUtil.setStringPref(SkylockConstant.SKYLOCK_PRIMARY, macId);
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            isCurrentScreen = true;
                                            showProgress(CONNECTING);
                                            mService.connect(mBluetoothDevice, true, generatekey, false);

                                        }
                                    }, 1000);
                                    return true;
                                }
                                break;
                            }
                        }
                        return false;
                    } else {
                        onConnectionTimeOut();
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection() {
        if (mService != null) {
            mService.close();
        }
    }

    /**
     * Set the given fragment to be visible.
     *
     * @param fragment Fragment to be shown
     */
    public void setFragment(final Fragment fragment, final boolean isAddToBackStack, final String tag) {
        mHandler.post(new Runnable() {
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
                    } else {
                        fragmentTransaction.addToBackStack(null);
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        mService.registerBluetoothDeviceStatusListener(HomePageActivity.this);
        bundle = getIntent().getExtras();
        if (bundle != null) {
            final String lockNameFromOnBoardprocess = bundle.getString("Lock_ID");
            final int notoficationType = bundle.getInt("typeOfNotification");
            if (lockNameFromOnBoardprocess != null) {
                handleExtras(lockNameFromOnBoardprocess);
            } else if (notoficationType == 1) {
                findMyEllipsesFragment = FindMyEllipsesFragment.newInstance();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setFragment(findMyEllipsesFragment, true, "FindMyEllipsesFragment");
                        changeHeaderUI("FIND MY ELLIPSE", colorapptheme, colorapptheme);
                    }
                });
            } else if (notoficationType == 2) {
                callWebView("HELP", 1);
            } else if (notoficationType == 3) {
                final String sharedMacid = bundle.getString("MAC_ID");
                final String generateKey = bundle.getString("GENERATE_KEY");
                if (sharedMacid != null && generateKey != null) {
                    bleConnection(sharedMacid, generateKey);
                    macID = sharedMacid;
                }
            }
        } else {
            if (mService != null) {
                if (myLockAndShareLockListData != null &&
                        myLockAndShareLockListData.size() > 0) {
                    if (mCurrentlyconnectedGatt != null) {
                        isFirstTime = true;
                        macID = UtilHelper.getLockMacIDFromName(mCurrentlyconnectedGatt.getDevice().getName());
                        lock_Name = mPrefUtil.getStringPref(macID, "");
                        getHWInfo();
                    } else
                        doScanning();
                } else {
                    disConnectionUI();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    public void customMenuOption(View view) {
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
        textView_userProfile.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        textView_userProfile.setTextColor(Color.WHITE);
        mEmergencyContactBt.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        mEmergencyContactBt.setTextColor(Color.WHITE);
        textView_help.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        textView_help.setTextColor(Color.WHITE);
        textView_order.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        textView_order.setTextColor(Color.WHITE);
        textView_termsandcondions.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        textView_termsandcondions.setTextColor(Color.WHITE);
        if (view != null) {
            ((TextView) view).setBackgroundColor(colorprimary);
            ((TextView) view).setTextColor(colorapptheme);
        }

    }

    /**
     * Get tag for top Fragment
     *
     * @return fragment's tag
     */
    public void getTopFragmentTag() {

        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        hideHeaderLayout();
    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    private void getLockListUpdateShareLock() {
        SkylockConstant.userToken = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<LockList> getLockList = lockWebServiceApi.GetLockData();
        getLockList.enqueue(new Callback<LockList>() {
            @Override
            public void onResponse(Call<LockList> call, Response<LockList> response) {
                if (response.code() == 200) {

                    if (response.body().getStatus() == 200) {
                        LockList payloadEntity = response.body();
                        Gson gson = new Gson();
                        String lockJson = gson.toJson(payloadEntity);
                        mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, lockJson);
                        if (mCurrentlyconnectedGatt != null && mCurrentlyconnectedGatt.getDevice() != null) {
                            String currentConnectDeviceMac = UtilHelper.getLockMacIDFromName(mCurrentlyconnectedGatt.getDevice().getName());
                            boolean shareLockRevoked = true;
                            if (!lockJson.equals("")) {
                                myLockAndShareLockListData = LockDetailsHelper.convertJsonToGson(mContext, lockJson);
                                for (HashMap<String, String> myLockList : myLockAndShareLockListData) {
                                    if (currentConnectDeviceMac.equals(myLockList.get("LOCK_MACID"))) {
                                        shareLockRevoked = false;
                                    }
                                }
                                if (shareLockRevoked) {
                                    handleRevokeLockAccess();
                                    mPrefUtil.setStringPref(currentConnectDeviceMac, "");
                                    mPrefUtil.setStringPref(currentConnectDeviceMac + SkylockConstant.SKYLOCK_PUBLIC_KEYS, "");
                                    mPrefUtil.setStringPref(currentConnectDeviceMac + SkylockConstant.SKYLOCK_SIGNED_MESSAGES, "");
                                    mPrefUtil.setStringPref(currentConnectDeviceMac + SkylockConstant.LAST_CONNECTED_TIMESTAMP, "");
                                }
                            }
                        }

                    } else {
                        mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, "");

                    }
                } else if (response.code() == 403) {
                    Toast.makeText(mContext, "You are not the Owner for this Lock", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<LockList> call, Throwable t) {
                Log.e("There are some problem", t.toString());
            }
        });
    }
}
