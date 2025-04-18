package io.lattis.operator.infrastructure

import dagger.android.support.DaggerApplication
import io.lattis.operator.infrastructure.di.component.DaggerAppComponent

class Operator : DaggerApplication() {


    val injector =   DaggerAppComponent.builder()
        .application(this)
        .build()

    override fun onCreate() {
        super.onCreate()
        injector.inject(this)
    }

    override fun applicationInjector() = injector
}