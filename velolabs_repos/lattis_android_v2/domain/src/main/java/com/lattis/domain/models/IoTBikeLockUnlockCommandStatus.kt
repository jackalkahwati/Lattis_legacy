package com.lattis.domain.models

data class IoTBikeLockUnlockCommandStatus (
    val command_id : String?,
    val status : Int?,
    val command : String?,
    val status_desc : String?,
    val date : String?,
    val mac_addr : String?
    )
