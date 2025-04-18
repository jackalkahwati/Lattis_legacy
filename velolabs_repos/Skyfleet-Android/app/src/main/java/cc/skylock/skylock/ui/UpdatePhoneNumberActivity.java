package cc.skylock.skylock.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import cc.skylock.skylock.Bean.UpdateUserDetails;
import cc.skylock.skylock.Bean.UserRegistrationResponse;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.UserApiService;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePhoneNumberActivity extends AppCompatActivity {
    private TextView textView_header, textView_title, textView_description;
    private Toolbar toolbar;
    private Context mContext;
    private EditText editText_MobileNumber, editText_CountryCode;
    private UserRegistrationResponse lockList;
    private PrefUtil mPrefUtil;
    private String countrycode, mobilenumber;
    private RelativeLayout progress_RelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_mobile_number);
        mContext = this;
        mPrefUtil = new PrefUtil(UpdatePhoneNumberActivity.this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progress_RelativeLayout = (RelativeLayout) findViewById(R.id.progressBar_relativeLayout);
        textView_header = (TextView) findViewById(R.id.toolbar_title);
        textView_description = (TextView) findViewById(R.id.tv_label_decription);
        editText_MobileNumber = (EditText) findViewById(R.id.et_mobilenumber);
        editText_CountryCode = (EditText) findViewById(R.id.et_countrycode);
        progress_RelativeLayout.setVisibility(View.GONE);
        changeStatusBarColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        textView_header.setTypeface(UtilHelper.getTypface(mContext));
        textView_description.setTypeface(UtilHelper.getTypface(mContext));
        editText_MobileNumber.setTypeface(UtilHelper.getTypface(mContext));
        editText_CountryCode.setTypeface(UtilHelper.getTypface(mContext));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        editText_CountryCode.setText(GetCountryZipCodeAsNumber());
        editText_MobileNumber.requestFocus();
        editText_MobileNumber.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    validateAndProcessUserData();
                    return true;
                }
                return false;
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

    public String GetCountryZipCodeAsNumber() {

        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getNetworkCountryIso().toUpperCase();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.updatenumber, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            validateAndProcessUserData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void validateAndProcessUserData() {

        countrycode = editText_CountryCode.getText().toString().trim();
        mobilenumber = editText_MobileNumber.getText().toString().trim();
        if (countrycode.isEmpty() || mobilenumber.equals("")) {
            return;
        }
        getUserDetails();


    }


    private void getUserDetails() {
        progress_RelativeLayout.setVisibility(View.VISIBLE);
        final UpdateUserDetails mUpdateUserDetails = new UpdateUserDetails();
        final UpdateUserDetails.PropertiesEntity updateUserDetails = new UpdateUserDetails.PropertiesEntity();
        updateUserDetails.setCountry_code(GetCountryZipCode());
        updateUserDetails.setPhone_number(mobilenumber);
        updateUserDetails.setUser_id(mPrefUtil.getIntPref(SkylockConstant.PREF_USER_ID, 0));
        mUpdateUserDetails.setProperties(updateUserDetails);
        updateUserDetails(mUpdateUserDetails);
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

    private void updateUserDetails(UpdateUserDetails updateUserDetails) {


        UserApiService UserApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<UserRegistrationResponse> mUpdateAccount = UserApiService.UpdateUserDetails(updateUserDetails);
        mUpdateAccount.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> userRegistrationResponse) {
                progress_RelativeLayout.setVisibility(View.GONE);
                if (userRegistrationResponse.code() == 200) {
                    final Gson gson = new Gson();
                    final String userResponseBeenJson = gson.toJson(userRegistrationResponse.body());
                    mPrefUtil.setStringPref(SkylockConstant.PREF_USER_DETAILS, userResponseBeenJson);
                    SkylockConstant.userToken = userRegistrationResponse.body().getPayload().getRest_token();
                    finish();
                } else {
                    CentralizedAlertDialog.showDialog(mContext,
                            getResources().getString(R.string.sorry),
                            getResources().getString(R.string.mobile_exist), 1);
                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {
                progress_RelativeLayout.setVisibility(View.GONE);
            }
        });
    }
}


