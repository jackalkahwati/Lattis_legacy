package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.IoTBikeLockUnlockCommandStatus

data class GetIoTBikeLockUnlockStatusResponse(
    @SerializedName("payload")
    var payload:Payload?
){
    data class Payload (
        @SerializedName("data")
        var ioTBikeLockUnlockCommandStatus: IoTBikeLockUnlockCommandStatus?
    )
}