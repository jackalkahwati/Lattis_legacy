package com.lattis.data.database.store


import com.lattis.data.database.base.RealmObservable
import com.lattis.data.database.mapper.RealmPrivateNetworkMapper
import com.lattis.data.database.model.RealmPrivateNetwork
import com.lattis.domain.models.PrivateNetwork
import java.util.ArrayList

import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function


class PrivateNetworkRealmDataStore @Inject
constructor(private val realmPrivateNetworkMapper: RealmPrivateNetworkMapper) {

    val privateNetworks: Observable<List<PrivateNetwork>>
        get() = RealmObservable.results<RealmPrivateNetwork>(
                Function{ realm ->
                    realm.where(RealmPrivateNetwork::class.java)
                            .findAll()
                })
                .map { realmPrivateNetworks -> realmPrivateNetworkMapper.mapOut(realmPrivateNetworks) }

    fun getPrivateNetwork(id: String): Observable<PrivateNetwork> {
        return RealmObservable.`object`<RealmPrivateNetwork>(
                Function{ realm ->
                    val realmPrivateNetwork = realm.where(RealmPrivateNetwork::class.java)
                            .equalTo(RealmPrivateNetwork.COLUMN_NAME_PRIVATE_FLEET_USER_ID, id)
                            .findFirst()

                    if (realmPrivateNetwork != null) realmPrivateNetwork else RealmPrivateNetwork()
                })
                .map { realmPrivateNetwork -> realmPrivateNetworkMapper.mapOut(realmPrivateNetwork) }
    }

    fun savePrivateNetworkList(privateNetworks: List<PrivateNetwork>?): Observable<List<PrivateNetwork>> {
        return if (privateNetworks == null) {
            Observable.just(ArrayList())
        } else RealmObservable.list<RealmPrivateNetwork>(
                Function{ realm -> realm.copyToRealmOrUpdate(realmPrivateNetworkMapper.mapIn(privateNetworks)) })
                .map { realmPrivateNetwork -> realmPrivateNetworkMapper.mapOut(realmPrivateNetwork) }
    }

    fun removePrivateNetwork(privateNetwork: PrivateNetwork): Observable<Boolean> {
        return RealmObservable.deleteObject(
                Function{ realm ->
                    val realmPrivateNetwork = realm.where(RealmPrivateNetwork::class.java)
                            .equalTo(RealmPrivateNetwork.COLUMN_NAME_PRIVATE_FLEET_USER_ID, privateNetwork.private_fleet_user_id)
                            .findFirst()
                    if (realmPrivateNetwork != null) {
                        realmPrivateNetwork!!.deleteFromRealm()
                        true
                    }
                    false
                })
    }

}
