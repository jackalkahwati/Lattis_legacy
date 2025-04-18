package com.lattis.data.entity.response.card

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse

class SetUpIntentResponse : AbstractApiResponse() {
    @SerializedName("payload")
    var setUpIntentDataResponse: SetUpIntentDataResponse? = null
}