package cc.skylock.skylock.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.UiUtils.RippleBackground;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by admin on 10/09/16.
 */
public class TheftAlert extends Activity {
    int colorapptheme = 0;
    final int theftId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_theft);
        colorapptheme = ResourcesCompat.getColor(getResources(), R.color.app_background, null);
        final TextView textView_label_cancel = (TextView) findViewById(R.id.tv_ok_got_it);
        final TextView textView_label_Locate = (TextView) findViewById(R.id.textView_label_locatemybike);
        final TextView textView_label_description1 = (TextView) findViewById(R.id.tv_description_1);
        final TextView textView_label_description2 = (TextView) findViewById(R.id.tv_description_2);
        final RippleBackground mRippleBackground = (RippleBackground) findViewById(R.id.content);
        final CardView cv_ok = (CardView) findViewById(R.id.cv_ok_got_it);
        final CardView cv_locate = (CardView) findViewById(R.id.cv_locatemybike);
        mRippleBackground.startRippleAnimation();
        textView_label_description1.setTypeface(UtilHelper.getTypface(this));
        textView_label_description2.setTypeface(UtilHelper.getTypface(this));
        textView_label_cancel.setTypeface(UtilHelper.getTypface(this));
        textView_label_Locate.setTypeface(UtilHelper.getTypface(this));
        changeStatusBarColor(colorapptheme);
        cv_locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNotification(TheftAlert.this, SkylockConstant.NOTIFICATION_ID + theftId);

                finish();
                final Intent intent = new Intent(TheftAlert.this, HomePageActivity.class);
                intent.putExtra("typeOfNotification", 1);
                startActivity(intent);

            }
        });
        cv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNotification(TheftAlert.this, SkylockConstant.NOTIFICATION_ID + theftId);

                finish();

            }
        });
    }

    private static void cancelNotification(Context ctx, int notifyId) {
        try {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
            nMgr.cancel(notifyId);
        } catch (Exception e) {

        }
    }


    private void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
