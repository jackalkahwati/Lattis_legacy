package cc.skylock.skylock.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.method.PasswordTransformationMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import org.json.JSONObject;

import java.util.Locale;

import cc.skylock.skylock.Bean.CheckTermsCondition;
import cc.skylock.skylock.Bean.ForgotPasswordParameter;
import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Bean.UserRegistrationParameter;
import cc.skylock.skylock.Bean.UserRegistrationResponse;
import cc.skylock.skylock.Database.Dbfunction;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.LockWebServiceApi;
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


/**
 * Created by Velo Labs Android on 21-01-2016.
 */
public class PhoneLoginpageActvity extends AppCompatActivity implements View.OnClickListener {
    private SpannableString content;
    private EditText editText_Username, editText_password, editText_countrycode;
    private CardView cardView_loginbutton, cardView_Facebook_signup;
    private Handler handler = new Handler();
    final private static int SUCCESS_CODE = 1;
    final private static int FAILURE_CODE = 0;
    private Dbfunction dbfunction;
    private UserApiService userWebServiceApi;
    private static UserRegistrationParameter userRegistrationParameter = new UserRegistrationParameter();
    private String user_mobileNumber, password;
    boolean invalid = false;
    private PrefUtil mPrefUtil;
    private Context mContext;
    private LoginButton facebookLoginButton;
    private Profile profile;
    public CallbackManager mCallBackManager;
    private int verified = 0;
    private TextView textView_Header, textView_label_facebook, textView_label_login;
    private TextView textView_passwordReset;
    private String countryCode;
    private TextView or_label_textView;
    private RelativeLayout loading_RelativeLayout;
    private String userFirstName = null, userLastName = null, userEmail = null;
    private Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_phoneloginpage);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        mCallBackManager = CallbackManager.Factory.create();
        mContext = this;
        mPrefUtil = new PrefUtil(mContext);
        editText_Username = (EditText) findViewById(R.id.etPhoneNumber);
        editText_password = (EditText) findViewById(R.id.etPassword);
        editText_countrycode = (EditText) findViewById(R.id.et_countrycode);
        editText_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        cardView_loginbutton = (CardView) findViewById(R.id.cv_LoginPhone);
        loading_RelativeLayout = (RelativeLayout) findViewById(R.id.rl_loadingLayout);
        cardView_Facebook_signup = (CardView) findViewById(R.id.cv_fb_signup);
        facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        textView_label_facebook = (TextView) findViewById(R.id.textView_facebook_signup_user);
        textView_label_login = (TextView) findViewById(R.id.textView_login);
        textView_Header = (TextView) findViewById(R.id.tvLogin);
        or_label_textView = (TextView) findViewById(R.id.tv_label_or);
        textView_passwordReset = (TextView) findViewById(R.id.textView_resetPassword);
        textView_passwordReset.setVisibility(View.GONE);
        editText_countrycode.setSelection(editText_countrycode.getText().length());
        loading_RelativeLayout.setVisibility(View.GONE);
        content = new SpannableString("Forget Password");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        content = new SpannableString("Not an user already? Sign Up");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        cardView_loginbutton.setOnClickListener(this);
        cardView_Facebook_signup.setOnClickListener(this);
        facebookLoginButton.setOnClickListener(this);
        facebookLoginButton.setReadPermissions("email");
        dbfunction = new Dbfunction(mContext);
        gson = new Gson();
        textView_Header.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_facebook.setTypeface(UtilHelper.getTypface(mContext));
        editText_Username.setTypeface(UtilHelper.getTypface(mContext));
        editText_password.setTypeface(UtilHelper.getTypface(mContext));
        editText_countrycode.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_login.setTypeface(UtilHelper.getTypface(mContext));
        textView_passwordReset.setTypeface(UtilHelper.getTypface(mContext));
        or_label_textView.setTypeface(UtilHelper.getTypface(mContext));
        textView_passwordReset.setVisibility(View.GONE);
        editText_countrycode.setText(GetCountryZipCodeAsNumber());
        editText_Username.requestFocus();
        editText_Username.setFocusable(true);
        editText_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final String userId = editText_Username.getText().toString().trim();
                    final String password = editText_password.getText().toString().trim();
                    if (!userId.isEmpty() && !password.isEmpty()) {
                        handleUserLogin();
                    }
                }
                return false;
            }
        });
        loading_RelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int responseCode = msg.getData().getInt("responseCode");
                switch (responseCode) {
                    case SUCCESS_CODE: {

                        if (verified == 1) {
                            mPrefUtil.setBooleanPref(Myconstants.KEY_USER_VERIFIED, true);
                            if (!mPrefUtil.getBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, false)) {
                                CheckTermsAndCondition();
                            } else {
                                loading_RelativeLayout.setVisibility(View.GONE);
                                startActivity(new Intent(PhoneLoginpageActvity.this, HomePageActivity.class));
                                PhoneLoginpageActvity.this.finish();
                            }
                        } else {
                            sendVerficationCode();
                            loading_RelativeLayout.setVisibility(View.GONE);
                            startActivity(new Intent(PhoneLoginpageActvity.this, VerificationActivity.class));
                            PhoneLoginpageActvity.this.finish();
                        }
                        break;
                    }
                    case FAILURE_CODE: {
                        textView_passwordReset.setVisibility(View.VISIBLE);
                        loading_RelativeLayout.setVisibility(View.GONE);
                        CentralizedAlertDialog.showDialog(mContext, getResources().getString(R.string.login_fail), msg.getData().getString("message"), 0);
                        mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
                    }
                }

            }

        };
        editText_Username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    user_mobileNumber = editText_Username.getText().toString().trim();
                    mPrefUtil.setStringPref(SkylockConstant.PREF_USER_ID, user_mobileNumber);
                }
                return false;
            }
        });

        textView_passwordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetpasswordCall();
            }
        });
        facebookLoginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String status = NetworkUtil.getConnectivityStatusString(mContext);
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
                                Log.v("LoginActivity", object.toString() + ":" + response.getJSONObject().toString());
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
                                }
                                mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, true);
                                if (!object.isNull("email") && object.optString("email") != null) {
                                    email = object.optString("email");
                                    userEmail = email;
                                }
                                mPrefUtil.setStringPref(SkylockConstant.PREF_USER_EMAIL, email);
                                excuteLoginApiCall();

                            }
                        });
                mPrefUtil.setBooleanPref(Myconstants.KEY_USER_VERIFIED, true);
                Bundle parameters = new Bundle();
                parameters.putString("fields", "email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
            }

            @Override
            public void onError(FacebookException error) {
                mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
            }
        });
    }

    private void CheckTermsAndCondition() {
        final UserApiService mUserApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);

        Call<CheckTermsCondition> delete = mUserApiService.CheckTermsAndCondition();

        delete.enqueue(new Callback<CheckTermsCondition>() {
            @Override
            public void onResponse(Call<CheckTermsCondition> call, Response<CheckTermsCondition> response) {
                if (response.code() == 200) {
                    if (response.body().getPayload().isHas_accepted()) {
                        getLockLis();
                        mPrefUtil.setBooleanPref(SkylockConstant.PREF_KEY__ACCEPT_TERMS_AND_CONDITION, true);
                    } else {
                        finish();
                        startActivity(new Intent(PhoneLoginpageActvity.this, TermsAndConditionActivity.class));
                        mPrefUtil.setBooleanPref(SkylockConstant.PREF_KEY__ACCEPT_TERMS_AND_CONDITION, false);
                    }
                }
            }

            @Override
            public void onFailure(Call<CheckTermsCondition> call, Throwable t) {

            }

        });
    }

    private void forgetpasswordCall() {
        loading_RelativeLayout.setVisibility(View.VISIBLE);
        requestCodeCall();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.cv_LoginPhone: {
                handleUserLogin();
            }
            break;
            case R.id.cv_fb_signup: {
                if (NetworkUtil.isNetworkAvailable(mContext))
                    facebookLoginButton.performClick();
                else {
                    final String network_error_header = getResources().getString(R.string.network_error);
                    final String no_internet_alert_description = getResources().getString(R.string.no_internet_alert);
                    CentralizedAlertDialog.showDialog(mContext, network_error_header, no_internet_alert_description, 0);
                }
            }
            break;
        }
    }

    private void handleUserLogin() {
        final String status = NetworkUtil.getConnectivityStatusString(mContext);
        if (status != null) {
            final String network_error_header = getResources().getString(R.string.network_error);
            final String no_internet_alert_description = getResources().getString(R.string.no_internet_alert);
            CentralizedAlertDialog.showDialog(mContext, network_error_header, no_internet_alert_description, 0);
            return;
        }
        validateUserDetails();
        if (!invalid) {
            textView_passwordReset.setVisibility(View.GONE);
            excuteLoginApiCall();
            mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
        }

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */

    private void getLockLis() {
        loading_RelativeLayout.setVisibility(View.VISIBLE);
        SkylockConstant.userToken = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<LockList> getLockList = lockWebServiceApi.GetLockData();
        getLockList.enqueue(new Callback<LockList>() {
            @Override
            public void onResponse(Call<LockList> call, Response<LockList> response) {
                if (response.code() == 200) {
                    mPrefUtil.setStringPref("KEY_TEMP_CC_MN", "");
                    if (response.body().getStatus() == 200) {
                        mPrefUtil.setBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, true);
                        LockList payloadEntity = response.body();
                        String lockJson = gson.toJson(payloadEntity);
                        mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, lockJson);
                        Message msgObj = handler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putInt("responseCode", SUCCESS_CODE);
                        msgObj.setData(b);
                        handler.sendMessage(msgObj);

                    } else {
                        mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, "");
                        Message msgObj = handler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("message", String.valueOf(response.body().getError()));
                        b.putInt("responseCode", FAILURE_CODE);
                        msgObj.setData(b);
                        handler.sendMessage(msgObj);
                    }
                }
            }

            @Override
            public void onFailure(Call<LockList> call, Throwable t) {
                Log.e("There are some problem", t.toString());
            }
        });
    }

    private void validateUserDetails() {

        user_mobileNumber = editText_Username.getText().toString().trim();
        password = editText_password.getText().toString().trim();
        countryCode = editText_countrycode.getText().toString().trim();
        if (user_mobileNumber != null && !user_mobileNumber.equals(""))
            invalid = false;
        else
            invalid = true;
        if (password != null && !password.equals(""))
            invalid = false;
        else
            invalid = true;
        if (countryCode != null && !countryCode.equals(""))
            invalid = false;
        else
            invalid = true;
        countryCode = editText_countrycode.getText().toString().trim();
        mPrefUtil.setStringPref(Myconstants.KEY_USER_PHONE_NUMBER_COUNTRY_CODE, countryCode);
        mPrefUtil.setStringPref(Myconstants.KEY_USER_PHONE_NUMBER, user_mobileNumber);
        mPrefUtil.setStringPref("KEY_TEMP_CC_MN", countryCode + user_mobileNumber);
        userRegistrationParameter.setUsers_id(user_mobileNumber);
        userRegistrationParameter.setPassword(password);
        userRegistrationParameter.setUser_type("ellipse");
        userRegistrationParameter.setCountry_code(GetCountryZipCode());
        userRegistrationParameter.setPhone_number(user_mobileNumber);
        userRegistrationParameter.setIs_signing_up(false);
        userRegistrationParameter.setReg_id(mPrefUtil.getStringPref(SkylockConstant.PREF_GCM_NOTIFICATIONI_KEY, ""));
    }

    private void excuteLoginApiCall() {
        loading_RelativeLayout.setVisibility(View.VISIBLE);
        userWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<UserRegistrationResponse> userResponse = userWebServiceApi.UserCreation(userRegistrationParameter);
        userResponse.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> response) {
                if (response.code() == 200) {
                    SkylockConstant.userToken = response.body().getPayload().getRest_token();
                    mPrefUtil.setStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
                    final UserRegistrationResponse userRegistrationResponse = response.body();
                    final UserRegistrationResponse.PayloadEntity payloadEntity = userRegistrationResponse.getPayload();
                    if (userEmail != null)
                        payloadEntity.setEmail(userEmail);
                    if (userFirstName != null)
                        payloadEntity.setFirst_name(userFirstName);
                    if (userLastName != null)
                        payloadEntity.setLast_name(userLastName);
                    userRegistrationResponse.setPayload(payloadEntity);
                    final String userResponseBeenJson = gson.toJson(userRegistrationResponse);
                    mPrefUtil.setStringPref(SkylockConstant.PREF_USER_DETAILS, userResponseBeenJson);
                    mPrefUtil.setIntPref(SkylockConstant.PREF_USER_ID, response.body().getPayload().getUser_id());
                    final Message msgObj = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putInt("responseCode", SUCCESS_CODE);
                    msgObj.setData(b);
                    verified = response.body().getPayload().getVerified();
                    handler.sendMessage(msgObj);
                    if (userRegistrationParameter.getUser_type().equals("true")) {
                        UtilHelper.analyticTrackUserAction("Facebook Succeded", "Log In", "Log In", null, "ANDROID");
                    } else {
                        UtilHelper.analyticTrackUserAction("Phone Succeded", "Log In", "Log In", null, "ANDROID");
                    }
                } else {
                    loading_RelativeLayout.setVisibility(View.GONE);
                    Message msgObj = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("message", "You have entered the wrong password or User doesn't exist.  Please try again.");
                    b.putInt("responseCode", FAILURE_CODE);
                    msgObj.setData(b);
                    handler.sendMessage(msgObj);
                    if (userRegistrationParameter.getUser_type().equals("true")) {
                        UtilHelper.analyticTrackUserAction("Facebook Failed", "Log In", "Log In", "" + response.code(), "ANDROID");
                    } else {
                        UtilHelper.analyticTrackUserAction("Phone Failed", "Log In", "Log In", "" + response.code(), "ANDROID");
                        Log.i("error", response.errorBody().toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {
                Toast.makeText(PhoneLoginpageActvity.this, "The server not reachable.Please try again.", Toast.LENGTH_LONG).show();
                loading_RelativeLayout.setVisibility(View.GONE);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
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

    public String GetCountryZipCodeAsNumber() {

        String CountryID = "";
        String CountryZipCode = "";

        CountryID = this.GetCountryZipCode();

        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryZipCode;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(PhoneLoginpageActvity.this, LoginMenuActivity.class));
    }

    public void requestCodeCall() {
        final UserApiService userApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        final ForgotPasswordParameter mForgotPasswordParameter = new ForgotPasswordParameter();
        mForgotPasswordParameter.setUsers_id(user_mobileNumber);
        mForgotPasswordParameter.setCountry_code(GetCountryZipCode());
        mForgotPasswordParameter.setUser_type("ellipse");
        Call<UserRegistrationResponse> accountPasswordReset = userApiService.SendSecretCodeForPassword(mForgotPasswordParameter);
        accountPasswordReset.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> response) {
                if (response.code() == 200) {
                    final UserRegistrationResponse.PayloadEntity mPayloadEntity = response.body().getPayload();
                    mPrefUtil.setStringPref(SkylockConstant.PREF_USER_TOKEN, mPayloadEntity.getRest_token());
                    mPrefUtil.setIntPref(SkylockConstant.PREF_USER_ID, mPayloadEntity.getUser_id());
                    mPrefUtil.setStringPref(SkylockConstant.PREF_USER_ID, mPayloadEntity.getUsers_id());
                    SkylockConstant.userToken = mPayloadEntity.getRest_token();
                    loading_RelativeLayout.setVisibility(View.GONE);
                    mPrefUtil.setBooleanPref("KEY_SENT_CODE_RESET_PASSWORD", true);
                    startActivity(new Intent(PhoneLoginpageActvity.this, PasswordResetActivity.class).putExtra("RESET_PASSWORD", "FORGOT_PASSWORD"));
                    finish();

                } else if (response.code() == 404) {
                    try {
                        loading_RelativeLayout.setVisibility(View.GONE);
                        final String warning = getResources().getString(R.string.warning);
                        CentralizedAlertDialog.showDialog(mContext, warning, "User Dosent Exist", 0);
                    } catch (Exception E) {
                        E.printStackTrace();
                        finish();
                    }

                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {
                loading_RelativeLayout.setVisibility(View.GONE);

            }
        });


    }

    private void sendVerficationCode() {
        Call<SuccessResponse> sendVerificationcode = userWebServiceApi.RequestForSendVerificationCode();
        sendVerificationcode.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {

            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {

            }
        });
    }

}
