package cc.skylock.skylock.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import cc.skylock.skylock.utils.ReceiverManager;
import io.fabric.sdk.android.Fabric;

import java.util.Objects;

import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.R;
import cc.skylock.skylock.gcm.RegistrationIntentService;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.SkylockConstant;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@SuppressWarnings("StringEquality")
public class SplashActivity extends Activity {
    private Intent mainIntent;
    private Context mContext;
    private PrefUtil mPrefUtil;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private GoogleApiAvailability apiAvailability;
    int resultCode;
    private Dialog dialog;
    ReceiverManager receiverManager;
    BroadcastReceiver connectivityBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Objects.equals(action, "android.net.conn.CONNECTIVITY_CHANGE")) {
                String status = NetworkUtil.getConnectivityStatusString(context);
                if (status != null) {
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Fabric.Builder(this).debuggable(true).kits(new Crashlytics()).build();
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mContext = this;
        mPrefUtil = new PrefUtil(mContext);
        receiverManager = ReceiverManager.init(mContext);
        SkylockConstant.userToken = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
        try {
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        handlePremisssion();
    }

    private void getUserLocksFromBackend() {
        if (mPrefUtil.getBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, false)) {
            if (NetworkUtil.isNetworkAvailable(mContext)) {
                getLockList();
            } else {
                resumingActvity();
            }
        } else {
            resumingActvity();
        }
    }

    private void resumingActvity() {

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPrefUtil.getBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, false)) {

                    if (mPrefUtil.getBooleanPref(Myconstants.KEY_USER_VERIFIED, false)) {
                        if (mPrefUtil.getBooleanPref(SkylockConstant.PREF_KEY__ACCEPT_TERMS_AND_CONDITION, false)) {
                            mainIntent = new Intent(SplashActivity.this, HomePageActivity.class);
                            startActivity(mainIntent);
                            SplashActivity.this.finish();
                        } else {
                            mainIntent = new Intent(SplashActivity.this, TermsAndConditionActivity.class);
                            startActivity(mainIntent);
                            SplashActivity.this.finish();

                        }
                    } else {
                        mainIntent = new Intent(SplashActivity.this, VerificationActivity.class);
                        startActivity(mainIntent);
                        SplashActivity.this.finish();
                    }
                } else {
                    if (mPrefUtil.getBooleanPref(Myconstants.KEY_FIRST_TIME_WALK_THROUGH_STRING, false)) {
                        if (mPrefUtil.getBooleanPref("KEY_SENT_CODE_RESET_PASSWORD", false)) {
                            mainIntent = new Intent(SplashActivity.this, PasswordResetActivity.class);
                            mainIntent.putExtra("RESET_PASSWORD", "FORGOT_PASSWORD");
                            startActivity(mainIntent);
                            SplashActivity.this.finish();

                        } else {
                            mainIntent = new Intent(SplashActivity.this, LoginMenuActivity.class);
                            startActivity(mainIntent);
                            SplashActivity.this.finish();
                        }
                    } else {
                        mainIntent = new Intent(SplashActivity.this, WalkThroughActivity.class);
                        startActivity(mainIntent);
                        SplashActivity.this.finish();

                    }
                }

            }
        }, 1000);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */

    private void getLockList() {

        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<LockList> getLockList = lockWebServiceApi.GetLockData();
        getLockList.enqueue(new Callback<LockList>() {
            @Override
            public void onResponse(Call<LockList> call, Response<LockList> response) {
                if (response.code() == 200) {
                    LockList payloadEntity = response.body();
                    Gson gson = new Gson();
                    String lockJson = gson.toJson(payloadEntity);
                    mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, lockJson);
                    resumingActvity();
                } else {
                    resumingActvity();
                }
            }

            @Override
            public void onFailure(Call<LockList> call, Throwable t) {
                resumingActvity();
            }
        });
    }

    private boolean checkPlayServices() {
        try {
            apiAvailability = GoogleApiAvailability.getInstance();
            resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (apiAvailability.isUserResolvableError(resultCode)) {
                    dialog = apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
                    dialog.show();
                } else {
                    finish();
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onResume() {
        receiverManager.registerReceiver(connectivityBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        super.onResume();

    }

    private void handlePremisssion() {
        if (Build.VERSION.SDK_INT >= 23) {
            getPermissionForBluetoothWrapper();
        } else {
            getUserLocksFromBackend();
        }
    }

    private void getPermissionForBluetoothWrapper() {
        int hasWriteContactsPermission = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasWriteContactsPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_LOCATION_PERMISSION);
                return;
            }
        }
        getUserLocksFromBackend();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocksFromBackend();
                } else {
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (dialog != null && dialog.isShowing())
                dialog.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (receiverManager.isReceiverRegistered(connectivityBroadcastReceiver)) {
            receiverManager.unregisterReceiver(connectivityBroadcastReceiver);
        }
        super.onDestroy();
    }
}
