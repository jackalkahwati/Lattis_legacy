package cc.skylock.skylock.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import cc.skylock.skylock.R;
import cc.skylock.skylock.adapter.AlertSettingsMenuApater;
import cc.skylock.skylock.utils.UtilHelper;

public class AlertNotificationActivity extends AppCompatActivity {
    TextView textView_header;
    Toolbar toolbar;
    Context mContext;
    RecyclerView alertsRecyclerView;
    String mTitle[] = {"Push notifications", "Theft detection", "Crash detection", "Auto proximity lock",
            "Auto proximity unlock", "Low battery"};

    String alert_Description[] = {"", "When a theft has been detected", "When a crash has been detected", "When my Ellipse is automatically locked",
            "When my Ellipse is automatically locked", "When my Ellipse battery reaches 20%"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_notification);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mContext = this;
        textView_header = (TextView) findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        textView_header.setTypeface(UtilHelper.getTypface(mContext));
        alertsRecyclerView = (RecyclerView) findViewById(R.id.recylerView_alertSettings);
        alertsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alertsRecyclerView.setAdapter(new AlertSettingsMenuApater(mContext, mTitle, alert_Description));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
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
}
