package com.lattis.data.entity.response.membership

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.domain.models.Membership
import com.lattis.domain.models.Subscription

class SubscriptionResponse : AbstractApiResponse(){
    @SerializedName("payload")
    var memberships:List<Subscription>?=null
}