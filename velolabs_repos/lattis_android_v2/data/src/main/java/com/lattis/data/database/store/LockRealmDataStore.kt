package com.lattis.data.database.store

import com.lattis.data.database.base.RealmObservable
import com.lattis.data.database.base.RealmObservable.`object`
import com.lattis.data.database.base.RealmObservable.deleteObject
import com.lattis.data.database.mapper.RealmLockMapper
import com.lattis.data.database.model.RealmLock
import com.lattis.domain.models.Lock
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import javax.inject.Inject

class LockRealmDataStore @Inject internal constructor(
    private val realmConfiguration: RealmConfiguration,
    private val lockMapper: RealmLockMapper
) {
    fun createOrUpdateLock(lock: Lock): Observable<Lock> {
        return RealmObservable. `object`(
            Function<Realm, RealmLock> { realm ->
                realm.copyToRealmOrUpdate(
                    lockMapper.mapIn(
                        lock!!
                    )
                )
            })
            .map( {realmlock->
                     lockMapper.mapOut(realmlock!!)
                }
            )
    }

    fun getLock(): Observable<Lock>
    {
        return RealmObservable.`object`<RealmLock>(
            Function { realm->
                val realmlock = realm.where(RealmLock::class.java).findFirst()
                if (realmlock != null) realmlock else RealmLock()
            }
        ).map { realmLock -> lockMapper.mapOut(realmLock) }
    }

    fun deleteLock(): Observable<Boolean> {
        return deleteObject(
            Function { realm: Realm ->
                val realmLock = realm.where(RealmLock::class.java)
                    .findFirst()
                if (realmLock != null) {
                    realmLock.deleteFromRealm()
                    true
                }
                false
            }
        )
    }

}