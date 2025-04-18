package cc.skylock.skylock.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import cc.skylock.skylock.Bean.ForgotPasswordParameter;
import cc.skylock.skylock.Bean.PasswordHintParameter;
import cc.skylock.skylock.Bean.PasswordReset;
import cc.skylock.skylock.Bean.PasswordResetResponse;
import cc.skylock.skylock.Bean.ProfilePassword;
import cc.skylock.skylock.Bean.ResetPasswordBean;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Bean.UpdateUserDetails;
import cc.skylock.skylock.Bean.UserRegistrationParameter;
import cc.skylock.skylock.Bean.UserRegistrationResponse;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.UserApiService;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.ui.fragment.EnterSecretCodeFragment;
import cc.skylock.skylock.ui.fragment.PasswordResetFragment;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.SkylockConstant;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PasswordResetActivity extends AppCompatActivity {
    private Context mContext;
    private PrefUtil mPrefUtil;
    private Fragment enterSecretCodeFragment = null, passwordResetFragment = null;
    private Handler mHandler = new Handler();
    private RelativeLayout mprogress_RelativeLayout;
    private Bundle bundle = null;
    public static boolean isFromProfilePage = false;
    private String mobilenumber = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_password_reset);
        mContext = PasswordResetActivity.this;
        mPrefUtil = new PrefUtil(mContext);
        bundle = getIntent().getExtras();
        enterSecretCodeFragment = EnterSecretCodeFragment.newInstance();
        passwordResetFragment = PasswordResetFragment.newInstance();
        mprogress_RelativeLayout = (RelativeLayout) findViewById(R.id.rl_loadingprogress);
        mprogress_RelativeLayout.setVisibility(View.GONE);
        bundle = getIntent().getExtras();
        if (bundle != null) {
            final String callResetPassword = bundle.getString("RESET_PASSWORD");
            if (callResetPassword.equals("RESET_PASSWORD")) {
                isFromProfilePage = true;
                callPasswordResetFragment();
            } else if (callResetPassword.equals("FORGOT_PASSWORD")) {
                setFragment(enterSecretCodeFragment, "EnterSecretCodeFragment");
                mprogress_RelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
        }


        mprogress_RelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    public void requestCodeCall(String user_mobileNumber) {
        UserApiService userApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        final ForgotPasswordParameter mForgotPasswordParameter = new ForgotPasswordParameter();
        mForgotPasswordParameter.setUsers_id(user_mobileNumber);
        mForgotPasswordParameter.setCountry_code(GetCountryZipCode());
        mForgotPasswordParameter.setUser_type("ellipse");
        Call<UserRegistrationResponse> accountPasswordReset = userApiService.SendSecretCodeForPassword(mForgotPasswordParameter);
        accountPasswordReset.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus() == 200) {
                        mprogress_RelativeLayout.setVisibility(View.GONE);
                        mPrefUtil.setBooleanPref("KEY_SENT_CODE_RESET_PASSWORD", true);
                        setFragment(enterSecretCodeFragment, "EnterSecretCodeFragment");
                    } else if (response.body().getStatus() == 404) {
                        try {

                            final String warning = getResources().getString(R.string.warning);
                            CentralizedAlertDialog.showDialog(mContext, warning, "User Doesn't Exist", 0);
                        } catch (Exception E) {
                            E.printStackTrace();
                            finish();
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {

            }
        });


    }

    public String GetCountryZipCode() {
        String CountryID = "";
        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getNetworkCountryIso().toUpperCase();
        return CountryID;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public void sendSecrectCode(String securityHint) {
        PasswordHintParameter mPasswordHintParameter = new PasswordHintParameter();
        mPasswordHintParameter.setPassword_hint(securityHint);
        mprogress_RelativeLayout.setVisibility(View.VISIBLE);
        mobilenumber = mPrefUtil.getStringPref(Myconstants.KEY_USER_PHONE_NUMBER, "");
        UserApiService userApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<UserRegistrationResponse> accountPasswordReset = userApiService.ConfirmPassword(mPasswordHintParameter);
        accountPasswordReset.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> response) {
                mprogress_RelativeLayout.setVisibility(View.GONE);
                if (response.code() == 200) {
                    UserRegistrationResponse.PayloadEntity mPayloadEntity = response.body().getPayload();
                    SkylockConstant.userToken = mPayloadEntity.getRest_token();
                    mPrefUtil.setStringPref(SkylockConstant.PREF_USER_TOKEN, mPayloadEntity.getRest_token());
                    mPrefUtil.setIntPref(SkylockConstant.PREF_USER_ID, mPayloadEntity.getUser_id());
                    mPrefUtil.setBooleanPref("KEY_SENT_CODE_RESET_PASSWORD", false);
                    callPasswordResetFragment();
                } else {
                    Toast.makeText(PasswordResetActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {
                mprogress_RelativeLayout.setVisibility(View.GONE);
            }
        });


    }


    public void callPasswordResetFragment() {
        try {
            setFragment(passwordResetFragment, "PasswordResetFragment");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setFragment(final Fragment fragment, final String tag) {
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
                    fragmentTransaction.addToBackStack(null);

        /*Commit the transaction.*/
                    fragmentTransaction.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public void resetPasswordCall(UpdateUserDetails mUpdateUserDetails) {
        mprogress_RelativeLayout.setVisibility(View.VISIBLE);
        mobilenumber = mPrefUtil.getStringPref(Myconstants.KEY_USER_PHONE_NUMBER, "");
        UserApiService userApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<UserRegistrationResponse> accountPasswordReset = userApiService.UpdateUserDetails(mUpdateUserDetails);
        accountPasswordReset.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> response) {
                mprogress_RelativeLayout.setVisibility(View.GONE);
                if (response.code() == 200) {
                    mPrefUtil.setBooleanPref("KEY_SENT_CODE_RESET_PASSWORD", false);
                    PasswordResetActivity.this.finish();
                    startActivity(new Intent(PasswordResetActivity.this, LoginMenuActivity.class));
                } else {
                    try {
                        CentralizedAlertDialog.showDialog(mContext, getResources().getString(R.string.warning), "User Doesn't Exist", 0);
                    } catch (Exception E) {
                        E.printStackTrace();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {
                mprogress_RelativeLayout.setVisibility(View.GONE);
            }
        });


    }


    public void profilePasswordCall(UpdateUserDetails mUpdateUserDetails) {
        mprogress_RelativeLayout.setVisibility(View.VISIBLE);
        UserApiService userApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<UserRegistrationResponse> accountPasswordReset = userApiService.UpdateUserDetails(mUpdateUserDetails);
        accountPasswordReset.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus() == 201) {
                        mprogress_RelativeLayout.setVisibility(View.GONE);
                        mPrefUtil.setBooleanPref("KEY_SENT_CODE_RESET_PASSWORD", false);
                        PasswordResetActivity.this.finish();
                    } else if (response.body().getStatus() == 404) {
                        //  Toast.makeText(PasswordResetActivity.this, response.body().getPayload().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {
                mprogress_RelativeLayout.setVisibility(View.GONE);
            }
        });


    }

}
