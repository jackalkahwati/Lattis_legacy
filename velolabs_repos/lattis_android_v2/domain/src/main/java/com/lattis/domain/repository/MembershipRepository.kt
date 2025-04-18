package com.lattis.domain.repository

import com.lattis.domain.models.Membership
import com.lattis.domain.models.Memberships
import com.lattis.domain.models.Subscription
import io.reactivex.rxjava3.core.Observable

interface MembershipRepository {
    fun getMemberships():Observable<MutableList<Membership>>
    fun getSubscriptions():Observable<List<Subscription>>
    fun getMembershipsAndSubscriptions():Observable<Memberships>
    fun subscribe(membership_id:Int):Observable<Boolean>
    fun unsubscribe(membership_id:Int):Observable<Boolean>
    fun getLocalSubscriptions(): Observable<List<Subscription>>
}