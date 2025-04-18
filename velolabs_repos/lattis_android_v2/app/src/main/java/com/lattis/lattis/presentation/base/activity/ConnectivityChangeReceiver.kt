package com.lattis.lattis.presentation.base.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

class ConnectivityChangeReceiver(private val listener: OnConnectivityChangedListener) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (isNetworkAvailable(context)) {
            listener.onConnectivityChanged(true)
        } else {
            listener.onConnectivityChanged(false)
        }
    }

    interface OnConnectivityChangedListener {
        fun onConnectivityChanged(isConnected: Boolean)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        try {
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}