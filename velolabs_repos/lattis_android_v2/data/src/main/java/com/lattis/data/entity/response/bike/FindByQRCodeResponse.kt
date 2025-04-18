package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.domain.models.Rental

class FindByQRCodeResponse : AbstractApiResponse() {
    @SerializedName("payload")
    var rental: Rental?=null
}