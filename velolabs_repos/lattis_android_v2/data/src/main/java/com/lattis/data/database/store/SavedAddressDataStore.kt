package com.lattis.data.database.store

import com.lattis.data.database.base.RealmObservable.deleteObject
import com.lattis.data.database.base.RealmObservable.list
import com.lattis.data.database.base.RealmObservable.results
import com.lattis.data.database.mapper.RealmSavedAddressMapper
import com.lattis.data.database.model.RealmSavedAddress
import com.lattis.domain.models.SavedAddress
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.RealmResults
import java.util.*
import javax.inject.Inject

class SavedAddressDataStore @Inject constructor(
    private val realmConfiguration: RealmConfiguration,
    private val savedAddressMapper: RealmSavedAddressMapper
) {
    val savedAddress: Observable<List<SavedAddress>>
        get() = results(
            Function{ realm: Realm ->
                realm.where(
                    RealmSavedAddress::class.java
                ).findAll()
            }
        )
            .map { realmSavedAddresses->
                if (realmSavedAddresses != null) {
                    savedAddressMapper.mapOut(realmSavedAddresses)
                } else {
                    emptyList()
                }
            }

    fun saveSavedAddresses(savedAddresses: List<SavedAddress>): Observable<Boolean> {
        return list(
            Function{ realm: Realm ->
                realm.copyToRealmOrUpdate<RealmSavedAddress>(
                    savedAddressMapper.mapIn(savedAddresses)
                )
            }
        )
            .flatMap { realmSavedAddresses: List<RealmObject?>? ->
                Observable.just(true)
            }
    }

    fun deleteSavedAddress(): Observable<Boolean> {
        return deleteObject(
            Function { realm: Realm ->
                val realmSavedAddress = realm.where(
                    RealmSavedAddress::class.java
                )
                    .findFirst()
                if (realmSavedAddress != null) {
                    realmSavedAddress.deleteFromRealm()
                    true
                }
                false
            }
        )
    }

}