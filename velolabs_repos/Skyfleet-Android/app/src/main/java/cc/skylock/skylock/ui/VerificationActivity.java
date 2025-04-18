package cc.skylock.skylock.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Objects;

import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Bean.UserRegistrationResponse;
import cc.skylock.skylock.Bean.UserVerificationParameter;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.UserApiService;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerificationActivity extends AppCompatActivity {
    TextView textView_verificationText, textView_resend, textView_signup;
    CardView cardView_signup, cardView_enterCode;
    PrefUtil mPrefUtil;
    String number = null, countrycode = null, mobileNumber = null;
    EditText editText_enterCode;
    UserVerificationParameter userVerificationParameter;
    UserApiService userWebServiceApi;
    Handler handler;
    final private static int SUCCESS_CODE = 1;
    final private static int FAILURE_CODE = 0;
    int verified = 0;
    private Context mContext;
    private RelativeLayout loading_Relativelayout;
    private String FAILURE_VERIFICATION_MESSAGE = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_verification);
        mContext = VerificationActivity.this;
        userVerificationParameter = new UserVerificationParameter();
        mPrefUtil = new PrefUtil(VerificationActivity.this);
        mPrefUtil.setBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, true);
        textView_verificationText = (TextView) findViewById(R.id.tv_verificationcontent);
        textView_resend = (TextView) findViewById(R.id.tv_resend);
        loading_Relativelayout = (RelativeLayout) findViewById(R.id.rl_loadingLayout);
        cardView_signup = (CardView) findViewById(R.id.cv_Signup);
        editText_enterCode = (EditText) findViewById(R.id.et_code);
        cardView_enterCode = (CardView) findViewById(R.id.cv_code);
        textView_signup = (TextView) findViewById(R.id.textView_signup);
        number = mPrefUtil.getStringPref(Myconstants.KEY_USER_PHONE_NUMBER, "");
        countrycode = mPrefUtil.getStringPref(Myconstants.KEY_USER_PHONE_NUMBER_COUNTRY_CODE, "");
        FAILURE_VERIFICATION_MESSAGE = getResources().getString(R.string.action_failure_verfication_messgae);
        if (!Objects.equals(countrycode, "") && !Objects.equals(number, "")) {
            if (validateUsing_libphonenumber(countrycode, number))
                textView_verificationText.setText("A verification code was sent via SMS to " + mobileNumber);

        } else {
            mobileNumber = mPrefUtil.getStringPref("KEY_TEMP_CC_MN", "");
            if (mobileNumber != null)
                textView_verificationText.setText("A verification code was sent via SMS to " + mobileNumber);
        }
        loading_Relativelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        textView_verificationText.setTypeface(UtilHelper.getTypface(mContext));
        textView_resend.setTypeface(UtilHelper.getTypface(mContext));
        editText_enterCode.setTypeface(UtilHelper.getTypface(mContext));
        textView_signup.setTypeface(UtilHelper.getTypface(mContext));
        textView_resend.setClickable(true);
        cardView_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleVerifationCall();
            }
        });
        textView_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView_resend.setClickable(false);
                textView_resend.setTextColor(Color.parseColor("#7a7a7a"));
                reSendVerificationCode();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView_resend.setTextColor(Color.WHITE);
                        textView_resend.setClickable(true);
                    }
                }, 120 * 1000);
            }
        });
        editText_enterCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return handleVerifationCall();
                }
                return false;
            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int responseCode = msg.getData().getInt("responseCode");
                final String verification = msg.getData().getString("SENT_CODE");
                switch (responseCode) {
                    case SUCCESS_CODE: {
                        loading_Relativelayout.setVisibility(View.GONE);
                        mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
                        if (verified == 1) {
                            mPrefUtil.setBooleanPref(Myconstants.KEY_USER_VERIFIED, true);
                            startActivity(new Intent(VerificationActivity.this, TermsAndConditionActivity.class));
                            VerificationActivity.this.finish();
                        }
                        break;
                    }
                    case FAILURE_CODE: {
                        loading_Relativelayout.setVisibility(View.GONE);
                        CentralizedAlertDialog.showDialog(mContext, "Code Verification Error",getResources().getString(R.string.verificationcode_error), 1);
                        break;
                    }
                }

            }

        };


    }

    private boolean handleVerifationCall() {
        final String verficationCode = editText_enterCode.getText().toString().trim();
        if (verficationCode != null && verficationCode.length() == 6) {
            userVerificationParameter.setconfirmation_code(verficationCode);
            excuteUserVerificationApiCall();
        } else {
            Toast.makeText(VerificationActivity.this, "Please enter valid code", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private boolean validateUsing_libphonenumber(String countryCode, String phNumber) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            phoneNumber = phoneNumberUtil.parse(phNumber, isoCode);
        } catch (NumberParseException e) {
            System.err.println(e);
        }

        boolean isValid = phoneNumberUtil.isValidNumber(phoneNumber);
        if (isValid) {
            mobileNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            return true;
        }
        return false;
    }

    private void reSendVerificationCode() {

        userWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<SuccessResponse> userResponse = userWebServiceApi.RequestForSendVerificationCode();
        userResponse.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                if (response.code() == 200) {

                    Message msgObj = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putInt("responseCode", SUCCESS_CODE);
                    b.putString("SENT_CODE", "VERIFICATION");
                    msgObj.setData(b);
                    handler.sendMessage(msgObj);
                } else if (response.code() == 400) {
                    Message msgObj = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("message", "Registration code is invalid");
                    b.putInt("responseCode", FAILURE_CODE);
                    msgObj.setData(b);
                    handler.sendMessage(msgObj);
                }
            }


            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {
                Message msgObj = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "Try again later");
                b.putInt("responseCode", FAILURE_CODE);
                msgObj.setData(b);
                handler.sendMessage(msgObj);
            }
        });

    }

    private void excuteUserVerificationApiCall() {
        loading_Relativelayout.setVisibility(View.VISIBLE);
        userWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<UserRegistrationResponse> verifyUser = userWebServiceApi.RequestForVerifyUser(userVerificationParameter);
        verifyUser.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus() == 201 || response.body().getStatus() == 200) {

                        SkylockConstant.userToken = response.body().getPayload().getRest_token();
                        mPrefUtil.setStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
                        Message msgObj = handler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putInt("responseCode", SUCCESS_CODE);
                        msgObj.setData(b);
                        verified = response.body().getPayload().getVerified();
                        handler.sendMessage(msgObj);
                    }
                } else {
                    Message msgObj = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("message", FAILURE_VERIFICATION_MESSAGE);
                    b.putInt("responseCode", FAILURE_CODE);
                    msgObj.setData(b);
                    handler.sendMessage(msgObj);
                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {
                loading_Relativelayout.setVisibility(View.VISIBLE);
                Message msgObj = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "Try again later");
                b.putInt("responseCode", FAILURE_CODE);
                msgObj.setData(b);
                handler.sendMessage(msgObj);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        mPrefUtil.setBooleanPref(Myconstants.KEY_USER_VERIFIED, false);
        mPrefUtil.setBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, false);
        startActivity(new Intent(VerificationActivity.this, SignUpPageActivity.class));
    }
}
