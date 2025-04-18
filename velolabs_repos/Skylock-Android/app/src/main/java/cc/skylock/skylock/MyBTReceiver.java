package cc.skylock.skylock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by alexvijayraj on 12/15/15.
 */
public class MyBTReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals("cc.skylock.skylock.BROADCAST_RECEIVER")){
            Log.d("BTreceiver", "Bluetooth connect");
        }
    }
}