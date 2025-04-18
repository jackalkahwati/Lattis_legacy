package com.lattis.data.entity.response.promotion

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.Promotion

class PromotionsResponse {
    @SerializedName("payload")
    var promotions:MutableList<Promotion>?=null
}