package com.lattis.data.repository.datasources.api.membership

import com.lattis.data.entity.response.BasicResponse
import com.lattis.data.entity.response.membership.MembershipsResponse
import com.lattis.data.entity.response.membership.SubscriptionResponse
import com.lattis.domain.models.Membership
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface MembershipApi {

    @GET("api/memberships")
    fun getMemberships(): Observable<MembershipsResponse>

    @GET("api/subscriptions")
    fun getSubscriptions():Observable<SubscriptionResponse>

    @POST("api/memberships/{membership_id}/subscribe")
    fun subscribe(@Path("membership_id") membership_id:Int):Observable<BasicResponse>

    @PATCH("api/memberships/{membership_id}/unsubscribe")
    fun unsubscribe(@Path("membership_id") membership_id:Int):Observable<BasicResponse>


}