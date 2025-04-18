package cc.skylock.skylock.ui;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
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

import cc.skylock.skylock.Bean.UpdateUserDetails;
import cc.skylock.skylock.Bean.UserRegistrationResponse;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.UserApiService;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PofilePasswordActivity extends AppCompatActivity {
    private TextView textView_content, textView_hideShow, textView_header;
    private EditText editText_Password;
    private String password = null;
    private PrefUtil mPrefUtil;
    private RelativeLayout mprogress_RelativeLayout;
    private UpdateUserDetails.PropertiesEntity mPropertiesEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pofile_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        textView_header = (TextView) findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        changeStatusBarColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null));
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mprogress_RelativeLayout = (RelativeLayout) findViewById(R.id.progressBar_relativeLayout);
        mprogress_RelativeLayout.setVisibility(View.GONE);
        editText_Password = (EditText) findViewById(R.id.et_password);
        textView_content = (TextView) findViewById(R.id.tv_content);
        textView_hideShow = (TextView) findViewById(R.id.tv_hideshow);
        textView_hideShow.setVisibility(View.GONE);
        textView_hideShow.setTypeface(UtilHelper.getTypface(this));
        textView_hideShow.setTag("SHOW");
        editText_Password.setError(null);
        mPropertiesEntity = new UpdateUserDetails.PropertiesEntity();
        mPrefUtil = new PrefUtil(this);
        editText_Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        textView_header.setTypeface(UtilHelper.getTypface(this));
        editText_Password.setTypeface(UtilHelper.getTypface(this));
        textView_content.setTypeface(UtilHelper.getTypface(this));
        textView_hideShow.setTypeface(UtilHelper.getTypface(this));
        mprogress_RelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
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
        editText_Password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    password = editText_Password.getText().toString().trim();
                    if (isPasswordValid(password)) {
                        textView_hideShow.setVisibility(View.VISIBLE);
                        validateAndProcessUserData();
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
        editText_Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Your validation code goes here
                if (s.length() != 0) {
                    textView_hideShow.setVisibility(View.VISIBLE);
                }
                if (s.length() >= 8) {
                    textView_hideShow.setTextColor(Color.WHITE);
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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

    private void validateAndProcessUserData() {
        if (!isPasswordValid(password)) {
            textView_hideShow.setVisibility(View.VISIBLE);
            editText_Password.setError(getString(R.string.error_invalid_password));
            return;
        }
        mPropertiesEntity.setUser_id(mPrefUtil.getIntPref(SkylockConstant.PREF_USER_ID, 0));
        mPropertiesEntity.setPassword(password);
        final UpdateUserDetails mUpdateUserDetails = new UpdateUserDetails();
        mUpdateUserDetails.setProperties(mPropertiesEntity);
        resetPasswordCall(mUpdateUserDetails);

    }

    public void resetPasswordCall(UpdateUserDetails mUpdateUserDetails) {
        mprogress_RelativeLayout.setVisibility(View.VISIBLE);
        UserApiService userApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<UserRegistrationResponse> accountPasswordReset = userApiService.UpdateUserDetails(mUpdateUserDetails);
        accountPasswordReset.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> response) {
                if (response.code() == 200) {
                    mprogress_RelativeLayout.setVisibility(View.GONE);
                    mPrefUtil.setBooleanPref("KEY_SENT_CODE_RESET_PASSWORD", false);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {
                mprogress_RelativeLayout.setVisibility(View.GONE);
            }

        });
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
            password = editText_Password.getText().toString().trim();
            validateAndProcessUserData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isPasswordValid(String password) {
        if (password != null) {
            return password.length() >= 8;
        }
        return false;
    }


}
