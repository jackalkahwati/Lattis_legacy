package com.lattis.lattis.utils.localnotification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.lattis.data.utils.GeneralHelper.getPendingIntentFlags
import javax.inject.Inject

class LocalNotificationHelper @Inject constructor(
 private val context: Context
) {

    private var pendingIntent:PendingIntent?=null

    init {
        createNotificationChannel(context)
    }

    fun scheduleNotification(title: String,message: String,type:String,alarmDateTime: Long?)
    {
        cancelPreviousLocalNotification()

        if(alarmDateTime==null){
            return
        }
        val intent = Intent(context, LocalNotificationBroadcastReceiver::class.java)
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)
        intent.putExtra(notificationType,type)


        pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationID,
            intent,
            getPendingIntentFlags(false)
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmDateTime,
                pendingIntent
            )
        }else{
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                alarmDateTime,
                pendingIntent
            )
        }
    }



    private fun createNotificationChannel(context: Context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notif Channel"
            val desc = "A Description of the Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance)
            channel.description = desc
            val notificationManager =
                context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun cancelPreviousLocalNotification(){
        if(pendingIntent!=null){
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pendingIntent)
        }
    }

}