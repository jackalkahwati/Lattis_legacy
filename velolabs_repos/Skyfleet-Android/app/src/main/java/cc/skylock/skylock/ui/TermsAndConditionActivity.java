package cc.skylock.skylock.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;

import java.sql.SQLException;

import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Bean.TermsAndConditionResponse;
import cc.skylock.skylock.Database.Dbfunction;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.operation.UserApiService;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TermsAndConditionActivity extends Activity {
    private CardView mCardView_decline, mCardView_accept;
    private RelativeLayout mRelativeLayout_progress;
    private PrefUtil mPrefUtil;
    private TextView mTextView_description, textView_Header;
    private String description = null;
    private Profile profile;
    private Dbfunction dbfunction;
    private Context mContext;
    private LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_terms_and_condition);
        mPrefUtil = new PrefUtil(this);
        profile = Profile.getCurrentProfile();
        dbfunction = new Dbfunction(this);
        mContext = this;
        textView_Header = (TextView) findViewById(R.id.tv_header);
        linearLayout = (LinearLayout) findViewById(R.id.ll_buttons);
        mTextView_description = (TextView) findViewById(R.id.tv_description);
        mCardView_decline = (CardView) findViewById(R.id.cv_decline);
        mCardView_accept = (CardView) findViewById(R.id.cv_accept);
        mRelativeLayout_progress = (RelativeLayout) findViewById(R.id.progressBar_relativeLayout);
        mRelativeLayout_progress.setVisibility(View.VISIBLE);
        mTextView_description.setTypeface(UtilHelper.getTypface(this));
        textView_Header.setTypeface(UtilHelper.getTypface(this));
        mTextView_description.setMovementMethod(new ScrollingMovementMethod());
        if (NetworkUtil.isNetworkAvailable(this)) {
            getTermAndConditionsFromServer();
        } else {
            mRelativeLayout_progress.setVisibility(View.GONE);
            description = mPrefUtil.getStringPref(SkylockConstant.PREF_KEY__TERMS_AND_CONDITION, description);
            mTextView_description.setText(description);

        }

        mCardView_decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.setVisibility(View.INVISIBLE);
                mTextView_description.setVisibility(View.INVISIBLE);
                textView_Header.setVisibility(View.INVISIBLE);
                deleteAccountCall();
                showAlertDialogForDecline(mContext);
            }
        });
        mCardView_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRelativeLayout_progress.setVisibility(View.VISIBLE);
                acceptTermsAndCondition();
            }

        });
    }

    private void getTermAndConditionsFromServer() {
        UserApiService UserApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<TermsAndConditionResponse> mUpdateAccount = UserApiService.TermsAndCondition();

        mUpdateAccount.enqueue(new Callback<TermsAndConditionResponse>() {
            @Override
            public void onResponse(Call<TermsAndConditionResponse> call, Response<TermsAndConditionResponse> response) {
                mRelativeLayout_progress.setVisibility(View.GONE);
                if (response.code() == 200) {
                    description = response.body().getPayload().getTerms();
                    mTextView_description.setText(description);
                    mPrefUtil.setStringPref(SkylockConstant.PREF_KEY__TERMS_AND_CONDITION, description);
                }
            }

            @Override
            public void onFailure(Call<TermsAndConditionResponse> call, Throwable t) {
                mRelativeLayout_progress.setVisibility(View.GONE);
                description = mPrefUtil.getStringPref(SkylockConstant.PREF_KEY__TERMS_AND_CONDITION, description);
                mTextView_description.setText(description);

            }
        });

    }

    private void acceptTermsAndCondition() {
        final UserApiService mUserApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);

        Call<SuccessResponse> delete = mUserApiService.AcceptTermsAndCondition();

        delete.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                mRelativeLayout_progress.setVisibility(View.GONE);
                if (response.code() == 200) {

                    TermsAndConditionActivity.this.finish();
                    mPrefUtil.setBooleanPref(SkylockConstant.PREF_KEY__ACCEPT_TERMS_AND_CONDITION, true);
                    startActivity(new Intent(TermsAndConditionActivity.this, AddLockActivity.class)
                            .putExtra("ADD_LOCK", "HOME")
                    );
                }
            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {
                mRelativeLayout_progress.setVisibility(View.GONE);

            }
        });

    }

    private void deleteAccountCall() {
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
                }
            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {
            }
        });


    }

    private void showAlertDialogForDecline(Context mContext) {

        final Dialog dialog = new Dialog(mContext, android.R.style.Theme_Holo_Light_NoActionBar);
        dialog.setContentView(R.layout.alert_decline);
        dialog.setCancelable(false);
        final ImageView imageView_close = (ImageView) dialog.findViewById(R.id.iv_close);
        final TextView textView_label_content_one = (TextView) dialog.findViewById(R.id.tv_content1);
        textView_label_content_one.setTypeface(UtilHelper.getTypface(mContext));
        imageView_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TermsAndConditionActivity.this, LoginMenuActivity.class));
                dialog.cancel();
                finish();
            }
        });
        dialog.show();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(TermsAndConditionActivity.this, LoginMenuActivity.class));

    }
}
