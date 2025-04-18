package cc.skylock.skylock.ui.Acivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import cc.skylock.skylock.ObjectRepo;
import cc.skylock.skylock.R;
import cc.skylock.skylock.adapter.SwipeViewAdapter;
import cc.skylock.skylock.cardswipe.CardStackView;
import cc.skylock.skylock.util.SkylockConstand;

/**
 * Created by AlexVijayRaj on 8/12/2015.
 */
public class AddLockNew extends FragmentActivity {
    int stepToDisplay=1;
    BroadcastReceiver addLockBackOperationLisner = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             stepToDisplay = intent.getExtras().getInt("step");
            mCardStack.setAdapter(new SwipeViewAdapter(mContext,mCardStack,stepToDisplay));
        }
    };
    CardStackView mCardStack;
    Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lock_swiper_layout);
        mContext = this;
        registerReceiver(addLockBackOperationLisner, new IntentFilter(SkylockConstand.addLockBackOption));
        mCardStack = (CardStackView) findViewById(R.id.mCardStackAddLock);
        mCardStack.setAdapter(new SwipeViewAdapter(mContext,mCardStack,stepToDisplay));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(addLockBackOperationLisner);
        super.onDestroy();
    }
}
