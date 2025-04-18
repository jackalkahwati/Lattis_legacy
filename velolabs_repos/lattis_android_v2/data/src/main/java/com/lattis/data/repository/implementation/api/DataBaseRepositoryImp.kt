package com.lattis.data.repository.implementation.api

import android.content.Context
import com.lattis.domain.repository.DataBaseRepository
import io.reactivex.rxjava3.core.Observable
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

class DataBaseRepositoryImp @Inject
constructor(private val context: Context, private val databaseName: String, private val schemaVersion: Int) :
    DataBaseRepository {

    private val configuration: RealmConfiguration
        get() = RealmConfiguration.Builder()
            .name(databaseName)
            .schemaVersion(schemaVersion.toLong())
            .deleteRealmIfMigrationNeeded()
            .build()

    override fun createDataBase(): Observable<Boolean> {
        return Observable.create { emitter ->
            Realm.init(context)
            Realm.setDefaultConfiguration(configuration)
            emitter.onNext(true)
        }
    }

    override fun deleteDataBase(): Observable<Boolean> {
        return Observable.create { emitter -> emitter.onNext(Realm.deleteRealm(configuration)) }
    }
}