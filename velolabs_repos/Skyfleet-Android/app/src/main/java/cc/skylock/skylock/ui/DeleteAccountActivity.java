package cc.skylock.skylock.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;

import java.io.File;
import java.sql.SQLException;

import cc.skylock.skylock.Bean.SendMacIdAsParameter;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Database.Dbfunction;
import cc.skylock.skylock.R;
import cc.skylock.skylock.bluetooth.SkylockBluetoothLEService;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.operation.UserApiService;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteAccountActivity extends AppCompatActivity {
    private TextView textView_header, textView_title, textView_label_deleteaccount;
    private Toolbar toolbar;
    private Context mContext;
    private CardView cardView_deleteAccount;
    private PrefUtil mPrefUtil;
    private Profile profile;
    private RelativeLayout rl_progressbar;
    private SkylockBluetoothLEService mService = null;
    private int deletionType = 0;
    private String macAddress = null;
    private boolean isCurrentlyConnected = false;
    private final int DELETE_LOCK = 1;
    private final int DELETE_ACCOUNT = 0;
    private Dbfunction dbfunction;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("USER_DELETE_ACCOUNT")) {
                finish();
                context.startActivity(new Intent(DeleteAccountActivity.this, LoginMenuActivity.class));
            } else if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        finish();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if (mService != null) {
                            mService.stopTimer();
                            finish();
                        }
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);
        mContext = this;
        mPrefUtil = new PrefUtil(mContext);
        service_init();
        final int colorprimary = ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null);
        changeStatusBarColor(colorprimary);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        dbfunction = new Dbfunction(mContext);
        final Intent intent = getIntent();
        if (intent.getExtras() != null) {
            deletionType = intent.getExtras().getInt("DELETION_TYPE");
            macAddress = intent.getExtras().getString("LOCK_MACID");
            isCurrentlyConnected = intent.getExtras().getBoolean("CURRENTLY_CONNECTED");
        }
        textView_header = (TextView) findViewById(R.id.toolbar_title);
        textView_title = (TextView) findViewById(R.id.tv_label_title);
        textView_label_deleteaccount = (TextView) findViewById(R.id.textView_label_delete_account);
        cardView_deleteAccount = (CardView) findViewById(R.id.cv_delete_account);
        setSupportActionBar(toolbar);
        rl_progressbar = (RelativeLayout) findViewById(R.id.rl_progressbar);
        rl_progressbar.setVisibility(View.GONE);
        textView_header.setTypeface(UtilHelper.getTypface(mContext));
        textView_title.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_deleteaccount.setTypeface(UtilHelper.getTypface(mContext));
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        FacebookSdk.sdkInitialize(mContext.getApplicationContext());
        profile = Profile.getCurrentProfile();
        if (deletionType == DELETE_ACCOUNT) {
            textView_header.setText(getResources().getString(R.string.deletemyaccount));
            textView_title.setText(getResources().getString(R.string.title_activity_delete_account));
            textView_label_deleteaccount.setText(getResources().getString(R.string.action_textView_label_delete_account));
        } else {
            textView_header.setText(getResources().getString(R.string.action_header_delete_ellipse));
            textView_title.setText(getResources().getString(R.string.label_delete_lock));
            textView_label_deleteaccount.setText(getResources().getString(R.string.action_label_delete_ellipse));
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        cardView_deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deletionType == DELETE_ACCOUNT) {
                    deleteAccountCall();
                } else if (deletionType == DELETE_LOCK) {
                    deleteLockCall(macAddress, isCurrentlyConnected);
                }
            }
        });
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, SkylockBluetoothLEService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SkylockBluetoothLEService.LocalBinder) rawBinder).getService();
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    @Override
    protected void onResume() {
        registerReceiver(mBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        super.onResume();
    }

    private void deleteAccountCall() {
        rl_progressbar.setVisibility(View.VISIBLE);
        UserApiService userApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<SuccessResponse> deleteAccount = userApiService.DeleteUserAccount();

        deleteAccount.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> deleteAccount) {
                if (deleteAccount.code() == 200) {

                    if (mPrefUtil.getBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false)) {
                        if (profile != null) {
                            LoginManager.getInstance().logOut();
                            mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
                        }
                    }
                    putShippingMode();
                    deleteProfilePic();
                    final Intent intent = new Intent();
                    intent.setAction("USER_DELETE_ACCOUNT");
                    sendBroadcast(intent);

                    mPrefUtil.setBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, false);
                    mPrefUtil.clearAllPref();
                    try {
                        dbfunction.open();
                        dbfunction.deleteAccount();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if (dbfunction.isOpen())
                            dbfunction.close();
                    }
                    mPrefUtil.setBooleanPref(Myconstants.KEY_FIRST_TIME_WALK_THROUGH_STRING, true);
                    finish();
                } else {
                    stopLoading();
                }
            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {
                stopLoading();
                Toast.makeText(DeleteAccountActivity.this, "Deletion Failed", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void stopLoading() {
        rl_progressbar.setVisibility(View.GONE);
    }

    public void putShippingMode() {
        if (mService != null) {
            mService.putShippingMode();
        }
    }

    public void deleteProfilePic() {
        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("ellipes", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, "profile.jpg");
            mypath.deleteOnExit();
            if (mypath.exists())
                mypath.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        unbindService(mServiceConnection);
        mService.stopSelf();
    }

    public void deleteLockCall(final String macAddress, final boolean currentlyConnetedLock) {
        rl_progressbar.setVisibility(View.VISIBLE);
        final SendMacIdAsParameter sendMacIdAsParameter = new SendMacIdAsParameter();
        if (macAddress != null)
            sendMacIdAsParameter.setMac_id(macAddress);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);

        Call<SuccessResponse> delete = lockWebServiceApi.DeleteLock(sendMacIdAsParameter);

        delete.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                if (response.code() == 200) {
                    try {
                        if (currentlyConnetedLock) {
                            putShippingMode();
                            mPrefUtil.setStringPref(macAddress, "");
                        }
                        mPrefUtil.setIntPref(macAddress + SkylockConstant.PREF_LOCK_ID, 0);
                        mPrefUtil.setStringPref(macAddress + SkylockConstant.SKYLOCK_PUBLIC_KEYS, "");
                        mPrefUtil.setStringPref(macAddress + SkylockConstant.SKYLOCK_SIGNED_MESSAGES, "");
                        mPrefUtil.setBooleanPref(macAddress + SkylockConstant.SKYLOCK_PROXIMITY_UNLOCK_ENABLE, false);
                        mPrefUtil.setBooleanPref(macAddress + SkylockConstant.SKYLOCK_PROXIMITY_LOCK_ENABLE, false);

                        stopLoading();
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 403) {
                    rl_progressbar.setVisibility(View.GONE);
                    Toast.makeText(DeleteAccountActivity.this, "Not suffiecnt permission to delete the lock", Toast.LENGTH_LONG).show();
                } else if (response.code() == 429) {
                    rl_progressbar.setVisibility(View.GONE);
                    Toast.makeText(DeleteAccountActivity.this, "Try again later", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {
                rl_progressbar.setVisibility(View.GONE);

            }
        });

    }


}
