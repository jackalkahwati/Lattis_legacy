package cc.skylock.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class ExitApplicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit_application);
        Log.i("Ondestroy", "killed");
        try {
            if (HomeActivity.activity != null)
                HomeActivity.activity.finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            //     System.exit(0);
            this.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
