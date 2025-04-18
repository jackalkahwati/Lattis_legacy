package com.lattis.data.entity.response.card

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse

class GetCardResponse : AbstractApiResponse() {

    @SerializedName("payload")
    var cardList: List<GetCardDataResponse>? = null
        internal set

}
