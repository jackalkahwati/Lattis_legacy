package com.lattis.lattis.infrastructure

import com.jakewharton.threetenabp.AndroidThreeTen
import com.lattis.domain.repository.DataBaseRepository
import com.lattis.lattis.infrastructure.di.component.DaggerAppComponent
import com.lattis.lattis.presentation.utils.FirebaseMessagingHelper
import com.lattis.lattis.presentation.utils.FirebaseUtil

import com.mapbox.mapboxsdk.Mapbox
import dagger.android.support.DaggerApplication
import io.lattis.lattis.R
import javax.inject.Inject

class Lattis : DaggerApplication() {

    @Inject
    lateinit var databaseRepository: DataBaseRepository

    @Inject
    lateinit var firebaseMessagingHelper: FirebaseMessagingHelper




    val injector =   DaggerAppComponent.builder()
        .application(this)
        .build()

    override fun onCreate() {
        super.onCreate()
        injector.inject(this)
        setupRealmDatabase()
        FirebaseUtil.instance?.instantiateSDK(this)
        Mapbox.getInstance(this, getString(R.string.map_box_access_token))
        AndroidThreeTen.init(this);
        firebaseMessagingHelper.initFCM()
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.OPEN_APPLICATION, FirebaseUtil.OPEN_APPLICATION)
    }

    override fun applicationInjector() = injector

    private fun setupRealmDatabase() {

        databaseRepository.createDataBase()
            .subscribe({
            },{
            })
    }
}