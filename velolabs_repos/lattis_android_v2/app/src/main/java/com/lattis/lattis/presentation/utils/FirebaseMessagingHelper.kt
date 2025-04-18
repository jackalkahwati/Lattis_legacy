package com.lattis.lattis.presentation.utils

import android.content.Context
import android.text.TextUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.lattis.domain.models.FirebasePushNotification
import com.lattis.lattis.utils.communication.AndroidBus

class FirebaseMessagingHelper constructor(
    context: Context
) {
    var token:String?=null

    fun initFCM(){
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(object : OnCompleteListener<String> {
                override fun onComplete(task: Task<String>) {
                    if (task.isSuccessful()) {
                        token = task.getResult();
                    }
                }
            })
    }

    fun getFirebaseToken():String{
       return if(!TextUtils.isEmpty(token)) token!! else ""
    }

    fun broadcastNotification(firebasePushNotification: FirebasePushNotification){
        AndroidBus.firebasePushNotificationPublishSubject.onNext(firebasePushNotification)
    }
}