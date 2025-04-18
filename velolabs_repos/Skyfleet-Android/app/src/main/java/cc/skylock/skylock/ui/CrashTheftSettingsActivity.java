package cc.skylock.skylock.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import cc.skylock.skylock.R;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;

import static java.lang.Math.round;

public class CrashTheftSettingsActivity extends AppCompatActivity {

    private TextView textView_content, textView_header, textView_setting_header, textView_cv_label, textView_label_low, textView_label_medium, textView_label_high;
    private Context mContext;
    private CardView mCardView_save_changes;
    private ProgressBar mProgressBar;
    private int screenWidth, theftSensitivityPercentage = 0;
    private PrefUtil mPrefUtil;

    private String macID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_theft_settings);
        mContext = this;
        final int colorprimary = ResourcesCompat.getColor(getResources(), R.color.colorPrimarylightdark, null);
        changeStatusBarColor(colorprimary);
        mPrefUtil = new PrefUtil(mContext);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        screenWidth = UtilHelper.getScreenWidthResolution(mContext);
        mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
        textView_content = (TextView) findViewById(R.id.tv_content);
        textView_header = (TextView) findViewById(R.id.toolbar_title);
        textView_setting_header = (TextView) findViewById(R.id.tv_header);
        textView_cv_label = (TextView) findViewById(R.id.tv_save_changes);
        textView_label_low = (TextView) findViewById(R.id.tv_low);
        textView_label_medium = (TextView) findViewById(R.id.tv_medium);
        textView_label_high = (TextView) findViewById(R.id.tv_high);
        mCardView_save_changes = (CardView) findViewById(R.id.cv_save_changes);
        textView_content.setTypeface(UtilHelper.getTypface(mContext));
        textView_header.setTypeface(UtilHelper.getTypface(mContext));
        textView_setting_header.setTypeface(UtilHelper.getTypface(mContext));
        textView_cv_label.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_low.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_medium.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_high.setTypeface(UtilHelper.getTypface(mContext));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            macID = bundle.getString("MAC_ID");
        }
        final String first = "<font color='#9B9B9B'>" + getString(R.string.action_settings_detection_content_first) + "</font>";
        final String next = "<font color='#57D8FF'>" + getString(R.string.action_settings_detection_content_next) + "</font>";
        textView_content.setText(Html.fromHtml(first + " " + next));
        final String theftLevel = mPrefUtil.getStringPref(SkylockConstant.THEFT_DETECTION_SENSITIVITY_LEVEL, "");
        if (!theftLevel.equals("") && theftLevel != null)
            theftSensitivityPercentage = Integer.parseInt(theftLevel);
        else
            theftSensitivityPercentage = 0;
        setProgressBySensitivity(theftSensitivityPercentage);
        mCardView_save_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTheftSensitivityLevel();
                finish();

            }
        });
        mProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Write your code to perform an action on down
                        break;
                    case MotionEvent.ACTION_MOVE:
                        theftSensitivityPercentage = round((float) x * 100 / screenWidth);
                        mPrefUtil.setIntPref(macID+SkylockConstant.PREF_LOCK_THEFT_SENSITIVITY,theftSensitivityPercentage/10);
                        setProgressBySensitivity(theftSensitivityPercentage);
                        // Write your code to perform an action on contineus touch move
                        break;
                    case MotionEvent.ACTION_UP:
                        // Write your code to perform an action on touch up
                        break;
                }
                return true;
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

    private void setProgressBySensitivity(int mTheftSensitivityPercentage) {
        if (mTheftSensitivityPercentage <= 30) {
            mProgressBar.setProgress(30);
        } else if (mTheftSensitivityPercentage > 30 && mTheftSensitivityPercentage <= 70) {
            mProgressBar.setProgress(55);
        } else if (mTheftSensitivityPercentage > 70) {
            mProgressBar.setProgress(100);
        }
    }

    private void changeTheftSensitivityLevel() {
        if (theftSensitivityPercentage <= 30) {
            mPrefUtil.setStringPref(SkylockConstant.THEFT_DETECTION_SENSITIVITY_LEVEL, "" + theftSensitivityPercentage);
        } else if (theftSensitivityPercentage > 30 && theftSensitivityPercentage <= 60) {
            mPrefUtil.setStringPref(SkylockConstant.THEFT_DETECTION_SENSITIVITY_LEVEL, "" + theftSensitivityPercentage);
        } else if (theftSensitivityPercentage > 60) {
            mPrefUtil.setStringPref(SkylockConstant.THEFT_DETECTION_SENSITIVITY_LEVEL, "" + theftSensitivityPercentage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
