package com.lattis.data.database.store

import android.graphics.Movie
import com.lattis.data.database.base.RealmObservable
import com.lattis.data.database.mapper.RealmSubscriptionMapper
import com.lattis.data.database.model.RealmSubscription
import com.lattis.domain.models.Subscription
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function
import io.realm.Realm
import io.realm.RealmResults
import java.util.*
import javax.inject.Inject


class SubscriptionRealmDataStore @Inject
constructor(private val realmSubscriptionMapper: RealmSubscriptionMapper) {

    val subscriptions: Observable<List<Subscription>>
        get() = RealmObservable.results<RealmSubscription>(
            Function { realm: Realm ->
                realm.where(RealmSubscription::class.java)
                    .findAll()
            })
            .map { realmSubscriptions ->

                if(realmSubscriptions!=null ){
                    realmSubscriptionMapper.mapOut(realmSubscriptions)
                }else{
                    emptyList()
                }

            }



    fun saveSubscriptions(subscriptions: List<Subscription>?): Observable<List<Subscription>> {
        return if (subscriptions == null || subscriptions.isEmpty()) {
            RealmObservable.deleteObject(Function { realm ->
                val subscriptions: RealmResults<RealmSubscription> = realm.where(RealmSubscription::class.java).findAll()
                if(subscriptions!=null)subscriptions.deleteAllFromRealm()
                true
            }).map {
                ArrayList<Subscription>()
            }
        } else RealmObservable.list<RealmSubscription>(
            Function { realm ->
                realm.copyToRealmOrUpdate(
                    realmSubscriptionMapper.mapIn(
                        subscriptions
                    )
                )
            })
            .map { subscriptions }
    }
}