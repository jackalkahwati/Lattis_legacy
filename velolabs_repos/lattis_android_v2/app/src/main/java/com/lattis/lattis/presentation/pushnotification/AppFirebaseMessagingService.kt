package com.lattis.lattis.presentation.pushnotification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lattis.domain.models.FirebasePushNotification
import com.lattis.domain.models.FirebasePushNotification.Companion.docked
import com.lattis.domain.models.FirebasePushNotification.Companion.docking
import com.lattis.lattis.presentation.utils.FirebaseMessagingHelper
import dagger.android.AndroidInjection
import javax.inject.Inject

class AppFirebaseMessagingService :FirebaseMessagingService (){

    @Inject
    lateinit var firebaseMessagingHelper: FirebaseMessagingHelper


    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        firebaseMessagingHelper.token = newToken
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.e(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.e(TAG, "Message data payload: ${remoteMessage.data}")

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
//                scheduleJob()
            } else {
                // Handle message within 10 seconds
//                handleNow()
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.e(TAG, "Message Notification Body: ${it.body}")
            Log.e(TAG, "Message Notification clickAction: ${it.clickAction}")

            if(it.clickAction!=null){
                firebaseMessagingHelper.broadcastNotification(
                    FirebasePushNotification(it.title,
                        it.body,
                        it.titleLocalizationKey,
                        it.bodyLocalizationKey,
                        it.clickAction!!)
                )
            }

        }
    }

    companion object {
        private const val TAG = "AppFirebaseMsgService"
    }
}