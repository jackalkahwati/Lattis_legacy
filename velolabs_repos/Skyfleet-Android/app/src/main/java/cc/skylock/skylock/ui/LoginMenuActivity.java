package cc.skylock.skylock.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;

import cc.skylock.skylock.Bean.UserRegistrationParameter;
import cc.skylock.skylock.Bean.UserRegistrationResponse;
import cc.skylock.skylock.Database.Dbfunction;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.UserApiService;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginMenuActivity extends AppCompatActivity implements View.OnClickListener {
    private CardView cardView_existingUser, cardView_User_signup, cardView_Facebook_signup;
    private Intent nextActivityIntent;
    private LoginButton facebookLoginButton;
    private Profile profile;
    private Handler handler = new Handler();
    final private static int SUCCESS_CODE = 1;
    final private static int FAILURE_CODE = 0;
    public CallbackManager mCallBackManager;
    private Context mContext;
    private PrefUtil mPrefUtil;
    private Dbfunction dbfunction;
    private static UserRegistrationParameter userRegistrationParameter;
    private UserApiService userWebServiceApi;
    private RelativeLayout relativeLayout_existing_userlogin;
    private String noNetworkConnection, facebookLoginFailed;
    private String userFirstName = null, userLastName = null, userEmail = null;
    private TextView textView_SingUpUser, textView_ExisitingUser, textView_facebookUser;
    private BroadcastReceiver connectivityBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Objects.equals(action, "android.net.conn.CONNECTIVITY_CHANGE")) {
                String status = NetworkUtil.getConnectivityStatusString(context);
                if (status != null) {
                    CentralizedAlertDialog.showDialog(mContext,
                            getResources().getString(R.string.network_error),
                            getResources().getString(R.string.no_internet_alert), 0);
                }

            }
        }
    };
    private RelativeLayout loading_Relativelayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mPrefUtil = new PrefUtil(mContext);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this);
        mCallBackManager = CallbackManager.Factory.create();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login_menu);
        noNetworkConnection = getResources().getString(R.string.toast_message_no_network);
        facebookLoginFailed = getResources().getString(R.string.toast_message_facebook_login_failed);
        loading_Relativelayout = (RelativeLayout) findViewById(R.id.rl_loadingLayout);
        cardView_existingUser = (CardView) findViewById(R.id.cv_exisiting_user_login);
        cardView_User_signup = (CardView) findViewById(R.id.cv_Phonenumber_signup);
        cardView_Facebook_signup = (CardView) findViewById(R.id.cv_fb_signup);
        facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        relativeLayout_existing_userlogin = (RelativeLayout) findViewById(R.id.rl_exisiting_user_login);
        textView_SingUpUser = (TextView) findViewById(R.id.textView_signup_user);
        textView_ExisitingUser = (TextView) findViewById(R.id.textView_existing_user);
        textView_facebookUser = (TextView) findViewById(R.id.textView_facebook_signup_user);
        textView_ExisitingUser.setTypeface(UtilHelper.getTypface(mContext));
        textView_facebookUser.setTypeface(UtilHelper.getTypface(mContext));
        textView_SingUpUser.setTypeface(UtilHelper.getTypface(mContext));
        facebookLoginButton.setReadPermissions("email");
        relativeLayout_existing_userlogin.setOnClickListener(this);
        cardView_User_signup.setOnClickListener(this);
        cardView_Facebook_signup.setOnClickListener(this);
        facebookLoginButton.setOnClickListener(this);
        dbfunction = new Dbfunction(mContext);
        loading_Relativelayout.setVisibility(View.GONE);
        userRegistrationParameter = new UserRegistrationParameter();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int responseCode = msg.getData().getInt("responseCode");
                switch (responseCode) {
                    case SUCCESS_CODE: {
                        loading_Relativelayout.setVisibility(View.GONE);
                        addUserData();
                        mPrefUtil.setBooleanPref(Myconstants.KEY_USER_VERIFIED, true);
                        if (!mPrefUtil.getBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, false)) {
                            startActivity(new Intent(LoginMenuActivity.this, TermsAndConditionActivity.class));
                            LoginMenuActivity.this.finish();
                        } else {
                            startActivity(new Intent(LoginMenuActivity.this, HomePageActivity.class));
                            LoginMenuActivity.this.finish();
                        }
                        break;
                    }
                    case FAILURE_CODE: {
                        loading_Relativelayout.setVisibility(View.GONE);
                        mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
                        CentralizedAlertDialog.showDialog(mContext, getResources().getString(R.string.network_error), msg.getData().getString("message"), 0);
                        mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
                    }
                }

            }

            private void addUserData() {
                try {
                    dbfunction.open();
                    dbfunction.insertUserDetails(userRegistrationParameter);
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (dbfunction.isOpen())
                        dbfunction.close();
                }

            }
        };

        facebookLoginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final String status = NetworkUtil.getConnectivityStatusString(mContext);
                if (status != null) {
                    CentralizedAlertDialog.showDialog(mContext,
                            getResources().getString(R.string.network_error),
                            getResources().getString(R.string.no_internet_alert), 0);
                    return;
                }
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                String email = "";
                                profile = Profile.getCurrentProfile();
                                if (profile != null) {
                                    if (profile.getFirstName() != null) {
                                        userFirstName = profile.getFirstName();
                                    }
                                    if (profile.getLastName() != null) {
                                        userLastName = profile.getLastName();
                                    }

                                    userRegistrationParameter.setUsers_id(profile.getId());
                                    userRegistrationParameter.setPassword(profile.getId());
                                    userRegistrationParameter.setUser_type("facebook");
                                    userRegistrationParameter.setReg_id(mPrefUtil.getStringPref(SkylockConstant.PREF_GCM_NOTIFICATIONI_KEY, ""));
                                    userRegistrationParameter.setCountry_code(GetCountryZipCode());
                                    userRegistrationParameter.setIs_signing_up(true);
                                }
                                mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, true);
                                if (!object.isNull("email") && object.optString("email") != null) {
                                    email = object.optString("email");
                                    userEmail = email;
                                    mPrefUtil.setStringPref(SkylockConstant.PREF_USER_EMAIL, email);
                                }

                                excuteLoginApiCall();
                            }
                        });
                final Bundle parameters = new Bundle();
                parameters.putString("fields", "email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                CentralizedAlertDialog.showDialog(mContext,
                        getResources().getString(R.string.warning), facebookLoginFailed, 0);
                mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);

            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                CentralizedAlertDialog.showDialog(mContext, getResources().getString(R.string.warning), facebookLoginFailed, 0);
                mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);


            }
        });

    }

    private void excuteLoginApiCall() {
        loading_Relativelayout.setVisibility(View.VISIBLE);
        userWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<UserRegistrationResponse> userResponse = userWebServiceApi.UserCreation(userRegistrationParameter);
        userResponse.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> response) {
                if (response.code() == 200) {

                    SkylockConstant.userToken = response.body().getPayload().getRest_token();
                    mPrefUtil.setStringPref(SkylockConstant.PREF_USER_TOKEN, response.body().getPayload().getRest_token());
                    mPrefUtil.setIntPref(SkylockConstant.PREF_USER_ID, response.body().getPayload().getUser_id());
                    final UserRegistrationResponse userRegistrationResponse = response.body();
                    final UserRegistrationResponse.PayloadEntity payloadEntity = userRegistrationResponse.getPayload();
                    if (userEmail != null)
                        payloadEntity.setEmail(userEmail);
                    if (userFirstName != null)
                        payloadEntity.setFirst_name(userFirstName);
                    if (userLastName != null)
                        payloadEntity.setLast_name(userLastName);
                    userRegistrationResponse.setPayload(payloadEntity);
                    final Gson gson = new Gson();
                    final String userResponseBeenJson = gson.toJson(userRegistrationResponse);
                    mPrefUtil.setStringPref(SkylockConstant.PREF_USER_DETAILS, userResponseBeenJson);
                    final Message msgObj = handler.obtainMessage();
                    final Bundle b = new Bundle();
                    b.putInt("responseCode", SUCCESS_CODE);
                    msgObj.setData(b);
                    handler.sendMessage(msgObj);
                    if (userRegistrationParameter.getUser_type().equals("true")) {
                        UtilHelper.analyticTrackUserAction("Facebook Succeded", "Log In", "Log In", null, "ANDROID");
                    } else {
                        UtilHelper.analyticTrackUserAction("Phone Succeded", "Log In", "Log In", null, "ANDROID");
                    }
                } else {
                    Message msgObj = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("message", response.body().getError().toString());
                    b.putInt("responseCode", FAILURE_CODE);
                    msgObj.setData(b);
                    handler.sendMessage(msgObj);
                    if (userRegistrationParameter.getUser_type().equals("true")) {
                        UtilHelper.analyticTrackUserAction("Facebook Failed", "Log In", "Log In", "" + response.code(), "ANDROID");
                    } else {
                        UtilHelper.analyticTrackUserAction("Phone Failed", "Log In", "Log In", "" + response.code(), "ANDROID");
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {
                final Message msgObj = handler.obtainMessage();
                final Bundle b = new Bundle();
                b.putString("message", "Try again later");
                b.putInt("responseCode", FAILURE_CODE);
                msgObj.setData(b);
                handler.sendMessage(msgObj);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(connectivityBroadcastReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_exisiting_user_login: {
                nextActivityIntent = new Intent(LoginMenuActivity.this, PhoneLoginpageActvity.class);
                startActivity(nextActivityIntent);
                finish();
            }
            break;
            case R.id.cv_Phonenumber_signup: {
                nextActivityIntent = new Intent(LoginMenuActivity.this, SignUpPageActivity.class);
                startActivity(nextActivityIntent);
                finish();
            }
            break;
            case R.id.cv_fb_signup: {
                if (NetworkUtil.isNetworkAvailable(mContext))
                    facebookLoginButton.performClick();
                else {
                    CentralizedAlertDialog.showDialog(mContext,
                            getResources().getString(R.string.warning),
                            getResources().getString(R.string.no_internet_alert), 0);

                }
            }
            break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(connectivityBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }


    public String GetCountryZipCode() {
        String isoCountryCode = "";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        isoCountryCode = manager.getNetworkCountryIso().toUpperCase();

        if (isoCountryCode == null || isoCountryCode.length() == 0) {
            isoCountryCode = Locale.getDefault().getCountry();
        }

        return isoCountryCode;
    }


}
