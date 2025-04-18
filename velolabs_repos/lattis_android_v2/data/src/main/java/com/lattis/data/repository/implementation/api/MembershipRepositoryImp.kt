package com.lattis.data.repository.implementation.api

import com.lattis.data.database.store.SubscriptionRealmDataStore
import com.lattis.data.net.membership.MembershipApiClient
import com.lattis.domain.models.Membership
import com.lattis.domain.models.Memberships
import com.lattis.domain.models.Subscription
import com.lattis.domain.repository.MembershipRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class MembershipRepositoryImp @Inject constructor(
    val membershipApiClient: MembershipApiClient,
    val subscriptionRealmDataStore: SubscriptionRealmDataStore
):MembershipRepository{
    override fun getMemberships(): Observable<MutableList<Membership>> {
        return membershipApiClient.api.getMemberships().map {
            it.memberships
        }
    }

    override fun getSubscriptions(): Observable<List<Subscription>> {
        return membershipApiClient.api.getSubscriptions().map {
            it.memberships
        }.flatMap{
            subscriptionRealmDataStore.saveSubscriptions(it)
        }
    }

    override fun getMembershipsAndSubscriptions(): Observable<Memberships> {
         return Observable.create { returnObservation ->
             Observable.zip(
                 getMemberships().subscribeOn(Schedulers.io())
                     .onErrorReturn { mutableListOf() },
                 getSubscriptions().subscribeOn(Schedulers.io())
                     .onErrorReturn { emptyList() },
                 BiFunction { memberships: MutableList<Membership>?,
                              subscriptions: List<Subscription>? ->
                     combineResult(memberships, subscriptions)
                 })
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe({ memberships ->
                     returnObservation.onNext(memberships)
                 }, {
                     returnObservation.onError(it)
                 })
         }
    }

    private fun combineResult(memberships:MutableList<Membership>?,subscriptions:List<Subscription>?):Memberships{
        if(subscriptions!=null && subscriptions.size>0 && memberships!=null && memberships.size>0){
            for (subscription in subscriptions){
                for(membership in memberships){
                    if(subscription.fleet_membership?.fleet_membership_id!!.equals(membership.fleet_membership_id)){
                        memberships.remove(membership)
                        break
                    }
                }
            }
        }
        return Memberships(memberships,subscriptions)
    }

    override fun subscribe(membership_id: Int): Observable<Boolean> {
        return membershipApiClient.api.subscribe(membership_id).map {
            true
        }
    }

    override fun unsubscribe(membership_id: Int): Observable<Boolean> {
        return membershipApiClient.api.unsubscribe(membership_id).map {
            true
        }
    }

    override fun getLocalSubscriptions(): Observable<List<Subscription>> {
        return subscriptionRealmDataStore.subscriptions
    }
}