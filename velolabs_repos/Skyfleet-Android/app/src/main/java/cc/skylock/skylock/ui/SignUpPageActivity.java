package cc.skylock.skylock.ui;

import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cc.skylock.skylock.Bean.SuccessResponse;
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

import static android.Manifest.permission.READ_CONTACTS;

/**
 * Created by Velo Labs Android on 21-01-2016.
 */
public class SignUpPageActivity extends AppCompatActivity implements View.OnClickListener, LoaderCallbacks<Cursor> {

    private EditText editText_Phonenumber, editText_Password, editText_countryCode;
    private TextView textView_SignIn, textView_hideShow, textView_verfiy, textView_header;
    private SpannableString content;
    private CardView cardView_loginPhonenumber;
    private static String password = null, countrycode = null, mobilenumber = null, email = null;
    private static UserRegistrationParameter userRegistrationParameter = new UserRegistrationParameter();
    private JSONObject values = new JSONObject();
    private Handler handler = new Handler();
    final private static int SUCCESS_CODE = 1;
    final private static int FAILURE_CODE = 0;
    private Dbfunction dbfunction;
    private UserApiService userWebServiceApi;
    private PrefUtil mPrefUtil;
    private Context mContext;
    private static final int REQUEST_READ_CONTACTS = 0;
    private AutoCompleteTextView mEmailView;
    private RelativeLayout loading_Relativelayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signup);
        mContext = SignUpPageActivity.this;
        FacebookSdk.sdkInitialize(mContext);
        dbfunction = new Dbfunction(mContext);
        mPrefUtil = new PrefUtil(mContext);
        loading_Relativelayout = (RelativeLayout) findViewById(R.id.rl_loadingLayout);
        editText_countryCode = (EditText) findViewById(R.id.et_countrycode);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.atv_emailaddress);
        editText_Phonenumber = (EditText) findViewById(R.id.et_mobilenumber);
        editText_Password = (EditText) findViewById(R.id.et_password);
        textView_SignIn = (TextView) findViewById(R.id.tvSignIn);
        textView_hideShow = (TextView) findViewById(R.id.tv_hideshow);
        textView_verfiy = (TextView) findViewById(R.id.textView_sendverify);
        textView_hideShow.setVisibility(View.GONE);
        cardView_loginPhonenumber = (CardView) findViewById(R.id.cv_Signup);
        textView_header = (TextView) findViewById(R.id.tvCreateAccount);
        loading_Relativelayout.setVisibility(View.GONE);
        editText_Phonenumber.setTypeface(UtilHelper.getTypface(mContext));
        editText_countryCode.setTypeface(UtilHelper.getTypface(mContext));
        editText_Password.setTypeface(UtilHelper.getTypface(mContext));
        textView_SignIn.setTypeface(UtilHelper.getTypface(mContext));
        textView_hideShow.setTypeface(UtilHelper.getTypface(mContext));
        mEmailView.setTypeface(UtilHelper.getTypface(mContext));
        textView_verfiy.setTypeface(UtilHelper.getTypface(mContext));
        textView_header.setTypeface(UtilHelper.getTypface(mContext));
        editText_countryCode.setText(GetCountryZipCodeAsNumber());
        mEmailView.setError(null);
        editText_Password.setError(null);
        textView_hideShow.setTag("SHOW");
        editText_Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        textView_SignIn.setOnClickListener(this);
        cardView_loginPhonenumber.setOnClickListener(this);
        populateAutoComplete();
        loading_Relativelayout.setOnClickListener(new View.OnClickListener() {
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
                        loading_Relativelayout.setVisibility(View.GONE);
                        addUserData();
                        mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
                        if (!mPrefUtil.getBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, false)) {
                            startActivity(new Intent(SignUpPageActivity.this, VerificationActivity.class));
                            SignUpPageActivity.this.finish();
                        } else {
                            mPrefUtil.setBooleanPref(Myconstants.KEY_USER_VERIFIED, true);
                            startActivity(new Intent(SignUpPageActivity.this, HomePageActivity.class));
                            SignUpPageActivity.this.finish();
                        }
                        break;
                    }
                    case FAILURE_CODE: {
                        loading_Relativelayout.setVisibility(View.GONE);
                        mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
                        if (SignUpPageActivity.this != null) {
                            CentralizedAlertDialog.showDialog(mContext,
                                    mContext.getString(R.string.login_failed_alert_title),
                                    mContext.getString(R.string.login_failed_alert), 0);
                        }
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


        textView_hideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String SHOW = getResources().getString(R.string.show);
                final String HIDE = getResources().getString(R.string.hide);

                if (textView_hideShow.getTag().equals(SHOW)) {
                    textView_hideShow.setTag(HIDE);
                    textView_hideShow.setText(HIDE);
                    editText_Password.setTransformationMethod(null);
                } else {
                    textView_hideShow.setTag(SHOW);
                    textView_hideShow.setText(SHOW);
                    editText_Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    email = mEmailView.getText().toString().trim();
                    if (isEmailValid(email)) {
                        return false;
                    } else {
                        mEmailView.setError(getString(R.string.error_invalid_email));
                        return true;
                    }


                }
                return false;
            }
        });
        editText_Password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    password = editText_Password.getText().toString().trim();
                    if (isPasswordValid(password)) {
                        textView_hideShow.setVisibility(View.VISIBLE);
                        return false;
                    } else {
                        textView_hideShow.setText("Must be 8 - 20 characters");
                        textView_hideShow.setTextColor(Color.parseColor("#F599AE"));
                        return true;
                    }


                }
                return false;
            }
        });
        editText_Phonenumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mobilenumber = editText_Phonenumber.getText().toString().trim();
                    countrycode = editText_countryCode.getText().toString().trim();
                    try {
                        if (isValidPhoneNumber(mobilenumber)) {
                            if (!countrycode.isEmpty() && !mobilenumber.isEmpty()) {
                                boolean status = validateUsing_libphonenumber(countrycode, mobilenumber);
                                if (status) {
                                    validateAndProcessUserData();
                                    return false;
                                } else {
                                    editText_Phonenumber.setError(getString(R.string.error_invalid_mobilenumber));
                                    return true;
                                }
                            } else {
                                editText_Phonenumber.setError(getString(R.string.error_invalid_mobilenumber));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;

            }

        });
        editText_Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Your validation code goes here
                if (s.length() != 0) {
                    textView_hideShow.setVisibility(View.VISIBLE);
                }
                if (s.length() >= 8) {
                    textView_hideShow.setTextColor(Color.parseColor("#A0C8E0"));
                    textView_hideShow.setText(textView_hideShow.getTag().toString());
                    textView_hideShow.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
    }

    private boolean validateUsing_libphonenumber(String countryCode, String phNumber) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            if (!Objects.equals(isoCode, "ZZ"))
                phoneNumber = phoneNumberUtil.parse(phNumber, isoCode);
            else
                return false;
        } catch (NumberParseException e) {
            System.err.println(e);
        }

        boolean isValid = phoneNumberUtil.isValidNumber(phoneNumber);
        if (isValid) {
            String internationalFormat = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            return true;
        } else {
            Toast.makeText(this, "Phone Number is Invalid " + phoneNumber, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    private boolean isEmailValid(String email) {

        if (!TextUtils.isEmpty(email)) {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
        return false;


    }

    private boolean isPasswordValid(String password) {

        return password.length() >= 8;
    }

    private void validateAndProcessUserData() {

        email = mEmailView.getText().toString().trim();
        password = editText_Password.getText().toString().trim();
        countrycode = editText_countryCode.getText().toString().trim();
        mobilenumber = editText_Phonenumber.getText().toString().trim();
        countrycode = editText_countryCode.getText().toString().trim();
        if (countrycode.isEmpty() || mobilenumber.equals("")) {
            editText_Phonenumber.setError(getString(R.string.error_invalid_mobilenumber));
            return;
        }

        if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            return;
        } else if (!isPasswordValid(password)) {
            textView_hideShow.setVisibility(View.GONE);
            editText_Password.setError(getString(R.string.error_invalid_password));
            return;
        } else if (!validateUsing_libphonenumber(countrycode, mobilenumber)) {
            editText_Phonenumber.setError(getString(R.string.error_invalid_mobilenumber));
            return;
        } else {
            mPrefUtil.setStringPref(Myconstants.KEY_USER_PHONE_NUMBER_COUNTRY_CODE, countrycode);
            mPrefUtil.setStringPref(Myconstants.KEY_USER_PHONE_NUMBER, mobilenumber);
            userRegistrationParameter.setUsers_id(mobilenumber);
            userRegistrationParameter.setPhone_number(mobilenumber);
            userRegistrationParameter.setReg_id(mPrefUtil.getStringPref(SkylockConstant.PREF_GCM_NOTIFICATIONI_KEY, ""));
            userRegistrationParameter.setPassword(password);
            userRegistrationParameter.setUser_type("ellipse");
            userRegistrationParameter.setCountry_code(GetCountryZipCode());
            userRegistrationParameter.setIs_signing_up(true);
            mPrefUtil.setStringPref(SkylockConstant.PREF_USER_EMAIL, email);
            try {
                values.put("first_name", "");
                values.put("user_id", mobilenumber);
                values.put("last_name", "");
                values.put("password", password);
                values.put("fb_flag", "0");
                values.put("reg_id", mPrefUtil.getStringPref(SkylockConstant.PREF_GCM_NOTIFICATIONI_KEY, ""));

            } catch (Exception e) {
                e.printStackTrace();

            }
            excuteLoginApiCall();

        }


    }


    private void excuteLoginApiCall() {
        loading_Relativelayout.setVisibility(View.VISIBLE);
        userWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<UserRegistrationResponse> userResponse = userWebServiceApi.UserCreation(userRegistrationParameter);
        userResponse.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus() == 201 || response.body().getStatus() == 200) {

                        SkylockConstant.userToken = response.body().getPayload().getRest_token();
                        mPrefUtil.setStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
                        mPrefUtil.setIntPref(SkylockConstant.PREF_USER_ID, response.body().getPayload().getUser_id());
                        final UserRegistrationResponse userRegistrationResponse = response.body();
                        final int verified = userRegistrationResponse.getPayload().getVerified();
                        if (verified == 1)
                            mPrefUtil.setBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, true);
                        else
                            sendVerficationCode();
                        final Gson gson = new Gson();
                        final String userResponseBeenJson = gson.toJson(userRegistrationResponse);
                        mPrefUtil.setStringPref(SkylockConstant.PREF_USER_DETAILS, userResponseBeenJson);
                        Message msgObj = handler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putInt("responseCode", SUCCESS_CODE);
                        msgObj.setData(b);
                        handler.sendMessage(msgObj);
                        if (userRegistrationParameter.getUser_type().equals("true")) {
                            UtilHelper.analyticTrackUserAction("Facebook Succeded", "Sign Up", "Sign Up", null, "ANDROID");
                        } else {
                            UtilHelper.analyticTrackUserAction("Phone Succeded", "Sign Up", "Sign Up", null, "ANDROID");
                        }
                    } else {
                        Message msgObj = handler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("message", response.body().getError().toString());
                        b.putInt("responseCode", FAILURE_CODE);
                        msgObj.setData(b);
                        handler.sendMessage(msgObj);
                        if (userRegistrationParameter.getUser_type().equals("true")) {
                            UtilHelper.analyticTrackUserAction("Facebook Failed", "Sign Up", "Sign Up", "" + response.code(), "ANDROID");
                        } else {
                            UtilHelper.analyticTrackUserAction("Phone Failed", "Sign Up", "Sign Up", "" + response.code(), "ANDROID");
                        }
                    }
                } else {
                    Message msgObj = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("message", "Try again later");
                    b.putInt("responseCode", FAILURE_CODE);
                    msgObj.setData(b);
                    handler.sendMessage(msgObj);
                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {
                Message msgObj = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("message", "Try again later");
                b.putInt("responseCode", FAILURE_CODE);
                msgObj.setData(b);
                handler.sendMessage(msgObj);
                if (userRegistrationParameter.getUser_type().equals("true")) {
                    UtilHelper.analyticTrackUserAction("Facebook Failed", "Sign Up", "Sign Up", "Try again later", "ANDROID");
                } else {
                    UtilHelper.analyticTrackUserAction("Phone Failed", "Sign Up", "Sign Up", "Try again later", "ANDROID");
                }
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


    private boolean isValidPhoneNumber(CharSequence phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            return Patterns.PHONE.matcher(phoneNumber).matches();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cv_Signup: {
                String status = NetworkUtil.getConnectivityStatusString(mContext);
                if (status != null) {
                    CentralizedAlertDialog.showDialog(mContext,
                            getResources().getString(R.string.network_error),
                            getResources().getString(R.string.no_internet_alert), 0);
                    return;
                }
                validateAndProcessUserData();

            }
            break;
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(SignUpPageActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(SignUpPageActivity.this, LoginMenuActivity.class));
    }

    public String GetCountryZipCodeAsNumber() {
        String CountryID = "";
        String CountryZipCode = "";

        //getNetworkCountryIso
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

}
