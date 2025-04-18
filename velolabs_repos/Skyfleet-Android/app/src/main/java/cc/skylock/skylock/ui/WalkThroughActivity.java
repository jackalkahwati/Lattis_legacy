package cc.skylock.skylock.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import cc.skylock.skylock.R;
import cc.skylock.skylock.adapter.WalkThroughPagerAdapter;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.UtilHelper;

public class WalkThroughActivity extends AppCompatActivity {

    ViewPager viewPager;
    WalkThroughPagerAdapter myPagerAdapter;
    CardView cardView;
    TextView startButton;
    PrefUtil mPrefUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_through);
        mPrefUtil = new PrefUtil(WalkThroughActivity.this);
        mPrefUtil.setBooleanPref(Myconstants.KEY_FIRST_TIME_WALK_THROUGH_STRING, true);
        viewPager = (ViewPager) findViewById(R.id.myViewPager);
        cardView = (CardView) findViewById(R.id.cv_button);
        startButton = (TextView) findViewById(R.id.tv_start_button);
        startButton.setTypeface(UtilHelper.getTypface(this));
        startButton.setTextColor(Color.WHITE);


        myPagerAdapter = new WalkThroughPagerAdapter(WalkThroughActivity.this);
        viewPager.setAdapter(myPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalkThroughActivity.this.finish();
                Intent intent = new Intent(WalkThroughActivity.this, LoginMenuActivity.class);
                startActivity(intent);


            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        UtilHelper.analyticTrackUserAction("Welcome screen open","Custom","Welcome",null, "ANDROID");
    }


    @Override
    protected void onPause() {
        super.onPause();
    }
}
