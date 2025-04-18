package com.lattis.data.entity.response.parking

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse
import com.lattis.domain.models.DockHub

data class GetDockHubResponse(
    @SerializedName("payload")
    var hubs:List<DockHub>?
) : AbstractApiResponse(){
}