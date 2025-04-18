package com.lattis.data.database.mapper

import com.lattis.data.database.base.AbstractRealmDataMapper
import com.lattis.data.database.model.RealmSubscription
import com.lattis.domain.models.Subscription
import io.realm.RealmResults
import java.util.ArrayList
import javax.inject.Inject

class RealmSubscriptionMapper @Inject constructor(
    val realmMembershipMapper: RealmMembershipMapper
) : AbstractRealmDataMapper<Subscription, RealmSubscription>() {

    override fun mapIn(subscription: Subscription): RealmSubscription {
        val realmSubscription = RealmSubscription()
        realmSubscription.activation_date = subscription.activation_date
        realmSubscription.deactivation_date = subscription.deactivation_date
        if(subscription.fleet_membership!=null) {
            realmSubscription.fleet_membership =
                realmMembershipMapper.mapIn(subscription.fleet_membership!!)
        }
        realmSubscription.fleet_membership_id = subscription.fleet_membership_id
        realmSubscription.membership_subscription_id = subscription.membership_subscription_id
        realmSubscription.period_end = subscription.period_end
        realmSubscription.period_start = subscription.period_start
        realmSubscription.user_id  = subscription.user_id
        return realmSubscription

    }

    override fun mapOut(realmSubscription: RealmSubscription): Subscription {
        val subscription = Subscription()
        subscription.activation_date = realmSubscription.activation_date
        subscription.deactivation_date = realmSubscription.deactivation_date
        if(realmSubscription.fleet_membership!=null) {
            subscription.fleet_membership =
                realmMembershipMapper.mapOut(realmSubscription.fleet_membership!!)
        }
        subscription.fleet_membership_id = realmSubscription.fleet_membership_id
        subscription.membership_subscription_id = realmSubscription.membership_subscription_id
        subscription.period_end = realmSubscription.period_end
        subscription.period_start = realmSubscription.period_start
        subscription.user_id  = realmSubscription.user_id
        return subscription
    }


    override fun mapOut(realmSubscriptions: RealmResults<RealmSubscription>): List<Subscription> {
        val subscriptions = ArrayList<Subscription>()
        for (realmSubscription in realmSubscriptions) {
            subscriptions.add(mapOut(realmSubscription))
        }
        return subscriptions
    }
}