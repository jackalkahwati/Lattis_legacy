package com.lattis.data.entity.response.apps

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.domain.models.Help

class HelpResponse: AbstractApiResponse(){
    @SerializedName("payload")
    val help:Help?=null
}