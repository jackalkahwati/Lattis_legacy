package cc.skylock.skylock.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;

import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.Bean.ShareLockRequest;
import cc.skylock.skylock.Bean.ShareLockResponse;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.ui.fragment.SharingChildFragment_LockList;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharingActivity extends AppCompatActivity {
    TextView textView_header, textView_cv_label_send, textView_content, textView_time_expires, textView_share_user;
    Context mContext;
    Bundle mBundle;
    String shareUserName = null, mobileNumber = null;
    PrefUtil mPrefUtil;
    CardView cardView_sendShare;
    RelativeLayout loading_RelativeLayout;
    private String FAILURE_MESSAGE, FAILURE_HEADER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing);
        mContext = SharingActivity.this;
        mPrefUtil = new PrefUtil(mContext);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        textView_header = (TextView) findViewById(R.id.toolbar_title);
        textView_cv_label_send = (TextView) findViewById(R.id.tv_label_send_invitation);
        textView_content = (TextView) findViewById(R.id.tv_label_content);
        textView_time_expires = (TextView) findViewById(R.id.tv_time_expires);
        textView_share_user = (TextView) findViewById(R.id.tv_name);
        cardView_sendShare = (CardView) findViewById(R.id.cv_share_invitation);
        loading_RelativeLayout = (RelativeLayout) findViewById(R.id.rl_progressbar);
        loading_RelativeLayout.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final int colorprimary = ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null);
        changeStatusBarColor(colorprimary);
        FAILURE_MESSAGE = getResources().getString(R.string.sharing_failure_message);
        FAILURE_HEADER = getResources().getString(R.string.warning);
        textView_header.setTypeface(UtilHelper.getTypface(mContext));
        textView_cv_label_send.setTypeface(UtilHelper.getTypface(mContext));
        textView_content.setTypeface(UtilHelper.getTypface(mContext));
        textView_time_expires.setTypeface(UtilHelper.getTypface(mContext));
        textView_share_user.setTypeface(UtilHelper.getTypface(mContext));
        mBundle = getIntent().getExtras();
        if (mBundle != null) {
            shareUserName = mBundle.getString("SHARE_USER_NAME");
            mobileNumber = mBundle.getString("SHARE_USER_PHONE");
            textView_header.setText("SHARE WITH " + shareUserName.toUpperCase());
            final String one = "<font color='#3C5377'>" + shareUserName + "</font>";
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            final String two = "<font color='#9B9B9B'>" + "<br/>" + mobileNumber + "<br/>" + "</font>";
            textView_share_user.setText(Html.fromHtml(one + two));

        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        cardView_sendShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading_RelativeLayout.setVisibility(View.VISIBLE);
                shareLockCall(mobileNumber, shareUserName);
            }
        });
        loading_RelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void shareLockCall(String friendNumber, final String shareUserName) {
        if (friendNumber != null && SharingChildFragment_LockList.currentSharingLockID != null) {
            LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
            final ShareLockRequest.ContactEntity mContactEntity = new ShareLockRequest.ContactEntity();
            final ShareLockRequest mShareLockRequest = new ShareLockRequest();
            mShareLockRequest.setLock_id(SharingChildFragment_LockList.currentSharingLockID);
            mContactEntity.setPhone_number(friendNumber);
            mContactEntity.setCountry_code(GetCountryZipCode());
            mContactEntity.setFirst_name(shareUserName);
            mContactEntity.setLast_name("");
            mShareLockRequest.setContact(mContactEntity);
            Call<ShareLockResponse> shareLock = lockWebServiceApi.ShareLock(mShareLockRequest);
            shareLock.enqueue(new Callback<ShareLockResponse>() {
                @Override
                public void onResponse(Call<ShareLockResponse> call, Response<ShareLockResponse> response) {
                    if (response.code() == 200) {
                        mPrefUtil.setStringPref(SkylockConstant.PREF_KEY_LOCK_SHARED_TO + SharingChildFragment_LockList.currentSharingLockID, shareUserName);
                        getLockList();
                        UtilHelper.analyticTrackUserAction("Lock shared", "Share", "Sharing", null, "ANDROID");
                    } else if (response.code() == 500) {
                        loading_RelativeLayout.setVisibility(View.GONE);
                        UtilHelper.analyticTrackUserAction("Sharing Failed", "Share", "Sharing", "" + response.code(), "ANDROID");
                        CentralizedAlertDialog.showDialog(mContext, FAILURE_HEADER, FAILURE_MESSAGE, 0);
                    } else {
                        loading_RelativeLayout.setVisibility(View.GONE);
                        Toast.makeText(SharingActivity.this, "Try again later", Toast.LENGTH_SHORT).show();
                        UtilHelper.analyticTrackUserAction("Sharing Failed", "Share", "Sharing", "" + response.code(), "ANDROID");
                    }
                }

                @Override
                public void onFailure(Call<ShareLockResponse> call, Throwable t) {
                    t.printStackTrace();
                    UtilHelper.analyticTrackUserAction("Sharing Failed", "Sharing", "Share", t.getMessage(), "ANDROID");
                }
            });
        }
    }

    private void showSuccessAlert() {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Holo_Light_NoActionBar);
        dialog.setContentView(R.layout.alert_sharing_success);
        dialog.setCancelable(false);
        final TextView textView_label = (TextView) dialog.findViewById(R.id.tv_label);
        final TextView textView_label_one = (TextView) dialog.findViewById(R.id.tv_label1);
        final ImageView iv_Close = (ImageView) dialog.findViewById(R.id.iv_close);
        textView_label.setTypeface(UtilHelper.getTypface(this));
        textView_label_one.setTypeface(UtilHelper.getTypface(this));
        iv_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                SharingActivity.this.finish();
            }
        });
        dialog.show();
    }

    private void getLockList() {

        SkylockConstant.userToken = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<LockList> getLockList = lockWebServiceApi.GetLockData();
        getLockList.enqueue(new Callback<LockList>() {
            @Override
            public void onResponse(Call<LockList> call, Response<LockList> response) {
                if (response.code() == 200) {
                    loading_RelativeLayout.setVisibility(View.GONE);
                    LockList payloadEntity = response.body();
                    final Gson gson = new Gson();
                    String lockJson = gson.toJson(payloadEntity);
                    mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, lockJson);
                    showSuccessAlert();
                }
            }

            @Override
            public void onFailure(Call<LockList> call, Throwable t) {
                loading_RelativeLayout.setVisibility(View.GONE);
            }
        });
    }

    public String GetCountryZipCode() {
        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getNetworkCountryIso().toUpperCase();
        if (CountryID.equalsIgnoreCase("")) {
            CountryID = mContext.getResources().getConfiguration().locale.getCountry();
        }
        return CountryID;
    }

}
