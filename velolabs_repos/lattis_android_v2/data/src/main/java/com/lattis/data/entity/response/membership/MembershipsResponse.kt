package com.lattis.data.entity.response.membership

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.data.entity.response.parking.FindParkingPayloadResponse
import com.lattis.domain.models.Membership

class MembershipsResponse : AbstractApiResponse(){
    @SerializedName("payload")
    var memberships:MutableList<Membership>?=null
}