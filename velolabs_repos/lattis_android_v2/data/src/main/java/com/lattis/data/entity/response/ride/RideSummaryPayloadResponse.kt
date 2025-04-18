package com.lattis.data.entity.response.ride

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.DockHub

/**
 * Created by ssd3 on 4/11/17.
 */

class RideSummaryPayloadResponse {

    @SerializedName("trip")
    internal var rideSummaryDataResponse: RideSummaryDataResponse? = null

    @SerializedName("hub")
    var dockHub:DockHub?=null
}
