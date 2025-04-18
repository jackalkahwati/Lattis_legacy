package com.lattis.lattis.infrastructure.di.module

import android.content.Context
import com.lattis.lattis.presentation.utils.FirebaseMessagingHelper
import com.lattis.lattis.utils.localnotification.LocalNotificationHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class HelperModule {

    @Provides
    @Singleton
    fun provideFirebaseMessaging(context: Context): FirebaseMessagingHelper {
        return FirebaseMessagingHelper(context)
    }

    @Provides
    @Singleton
    fun provideLocalNotificationHelper(context: Context): LocalNotificationHelper {
        return LocalNotificationHelper(context)
    }
}