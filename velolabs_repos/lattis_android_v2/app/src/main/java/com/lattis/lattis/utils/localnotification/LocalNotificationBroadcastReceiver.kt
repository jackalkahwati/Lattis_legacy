package com.lattis.lattis.utils.localnotification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.lattis.lattis.utils.communication.AndroidBus
import io.lattis.lattis.R

const val notificationID = 138
const val channelID = "channel138"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"
const val notificationType = "notificationType"
const val RESERVATION_TIMER_OVER_NOTIFICATION_TYPE = "RESERVATION_TIMER_OVER_NOTIFICATION_TYPE"

class LocalNotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(intent.getStringExtra(titleExtra))
            .setContentText(intent.getStringExtra(messageExtra))
            .build()
        val  manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
        AndroidBus.stringPublishSubject.onNext(intent.getStringExtra(notificationType))
    }
}