package com.lattis.data.database.mapper

import com.lattis.data.database.base.AbstractRealmDataMapper
import com.lattis.data.database.model.RealmLock
import com.lattis.data.database.model.RealmMembership
import com.lattis.domain.models.Lock
import com.lattis.domain.models.Membership
import javax.inject.Inject
import javax.inject.Named

class RealmMembershipMapper @Inject constructor(
    ) : AbstractRealmDataMapper<Membership, RealmMembership>() {

    override fun mapIn(membership: Membership): RealmMembership {
        val realmMembership = RealmMembership()
        realmMembership.created_at = membership.created_at
        realmMembership.deactivation_date = membership.deactivation_date
        realmMembership.deactivation_reason = membership.deactivation_reason
        realmMembership.fleet_id = membership.fleet_id
        realmMembership.fleet_membership_id = membership.fleet_membership_id
        realmMembership.membership_incentive = membership.membership_incentive
        realmMembership.membership_price = membership.membership_price
        realmMembership.membership_price_currency = membership.membership_price_currency
        realmMembership.payment_frequency = membership.payment_frequency
        return realmMembership

    }

    override fun mapOut(realmMembership: RealmMembership): Membership {
        val realMembership = Membership()
        realMembership.created_at = realmMembership.created_at
        realMembership.deactivation_date = realmMembership.deactivation_date
        realMembership.deactivation_reason = realmMembership.deactivation_reason
        realMembership.fleet_id = realmMembership.fleet_id
        realMembership.fleet_membership_id = realmMembership.fleet_membership_id
        realMembership.membership_incentive = realmMembership.membership_incentive
        realMembership.membership_price = realmMembership.membership_price
        realMembership.membership_price_currency = realmMembership.membership_price_currency
        realMembership.payment_frequency = realmMembership.payment_frequency
        return realMembership
    }

}