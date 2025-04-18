package com.lattis.data.entity.response.bike

import com.google.gson.annotations.SerializedName
import com.lattis.domain.models.IoTBikeLockUnlockCommandStatus

data class IoTBikeLockUnlockCommandStatusResponse(
    @SerializedName("payload")
    var ioTBikeLockUnlockCommandStatus: IoTBikeLockUnlockCommandStatus?
)