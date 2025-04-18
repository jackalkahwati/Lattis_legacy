package com.lattis.ellipse.presentation.ui.base.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by ssd3 on 10/6/17.
 */

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private OnConnectivityChangedListener listener;

    public ConnectivityChangeReceiver(OnConnectivityChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(isNetworkAvailable(context)) {
            listener.onConnectivityChanged(true);
        }else{
            listener.onConnectivityChanged(false);
        }

    }

    public interface OnConnectivityChangedListener {
        void onConnectivityChanged(boolean isConnected);
    }



    private boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            return isConnected;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

}
