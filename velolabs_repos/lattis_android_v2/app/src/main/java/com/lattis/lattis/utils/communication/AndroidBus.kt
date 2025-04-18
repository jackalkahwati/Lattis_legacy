package com.lattis.lattis.utils.communication

import com.lattis.domain.models.FirebasePushNotification
import io.reactivex.rxjava3.subjects.PublishSubject

class AndroidBus {
    companion object {
        val stringPublishSubject = PublishSubject.create<String>()
        val firebasePushNotificationPublishSubject = PublishSubject.create<FirebasePushNotification>()
    }
}