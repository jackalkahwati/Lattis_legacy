package com.lattis.lattis.infrastructure.di.module

import com.lattis.data.net.activetrip.ActiveTripService
import com.lattis.lattis.presentation.pushnotification.AppFirebaseMessagingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule{
    @ContributesAndroidInjector
    abstract fun activeTripServiceInjector(): ActiveTripService

    @ContributesAndroidInjector
    abstract fun contributeAppFirebaseMessagingService(): AppFirebaseMessagingService
}