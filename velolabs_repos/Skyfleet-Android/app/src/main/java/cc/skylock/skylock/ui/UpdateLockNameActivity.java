package cc.skylock.skylock.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.widget.Toast;
import cc.skylock.skylock.Bean.UpdateLockNameParameter;
import cc.skylock.skylock.Bean.UpdateLockNameResponse;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UpdateLockNameActivity extends AppCompatActivity {
    private RelativeLayout progress_RelativeLayout;
    private EditText editText_LockName;
    private PrefUtil mPrefUtil;
    private String lockMacId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_lock_name);
        mPrefUtil = new PrefUtil(this);
        final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        editText_LockName = (EditText) findViewById(R.id.et_lockname);
        ((TextView) findViewById(R.id.tv_label_title)).setTypeface(UtilHelper.getTypface(this));
        progress_RelativeLayout = ((RelativeLayout) findViewById(R.id.progressBar_relativeLayout));
        editText_LockName.setTypeface(UtilHelper.getTypface(this));
        changeStatusBarColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null));
        editText_LockName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    validateLockName();
                    return true;
                }
                return false;
            }
        });
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            lockMacId = bundle.getString("MAC_ID");
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
        MenuItem settingsMenuItem = menu.findItem(R.id.action_done);
        settingsMenuItem.setTitle(this.getString(R.string.save));
        return true;

    }

    private void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            validateLockName();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void validateLockName() {
        final String lockName = editText_LockName.getText().toString().trim();
        if (lockName != null && !lockName.isEmpty()) {
            if (NetworkUtil.isNetworkAvailable(this)) {
                updateLockName(lockName);
            } else {
                CentralizedAlertDialog.showDialog(this,
                        getResources().getString(R.string.network_error),
                        getResources().getString(R.string.no_internet_alert), 0);
            }
        } else {
            CentralizedAlertDialog.showDialog(this,
                    getResources().getString(R.string.warning),
                    getResources().getString(R.string.enter_lock_name_message), 0);
        }
    }

    private void updateLockName(final String lockName) {
        progress_RelativeLayout.setVisibility(View.VISIBLE);
        final UpdateLockNameParameter.PropertiesEntity mPropertiesEntity = new UpdateLockNameParameter.PropertiesEntity();
        mPropertiesEntity.setLock_id(mPrefUtil.getIntPref(SkylockConstant.PREF_LOCK_ID + lockMacId, 0));
        mPropertiesEntity.setName(lockName);
        final UpdateLockNameParameter mUpdateLockNameParameter = new UpdateLockNameParameter();
        mUpdateLockNameParameter.setProperties(mPropertiesEntity);
        final LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<UpdateLockNameResponse> getLockList = lockWebServiceApi.AddLockName(mUpdateLockNameParameter);
        getLockList.enqueue(new Callback<UpdateLockNameResponse>() {
            @Override
            public void onResponse(Call<UpdateLockNameResponse> call, Response<UpdateLockNameResponse> response) {
                progress_RelativeLayout.setVisibility(View.GONE);
                if (response.code() == 200) {
                    mPrefUtil.setStringPref(lockMacId, lockName);
                    UpdateLockNameActivity.this.finish();
                } else {
                    Toast.makeText(UpdateLockNameActivity.this, "Try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateLockNameResponse> call, Throwable t) {
                Log.e("There are some problem", t.toString());
                progress_RelativeLayout.setVisibility(View.GONE);
            }
        });

    }
}
