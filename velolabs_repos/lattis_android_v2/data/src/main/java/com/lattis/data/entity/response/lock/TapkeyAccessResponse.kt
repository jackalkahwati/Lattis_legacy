package com.lattis.data.entity.response.lock

import com.google.gson.annotations.SerializedName

data class TapkeyAccessResponse(
    @SerializedName("payload")
    val tapkeyAccess: TapkeyAccess?=null
){
    data class TapkeyAccess(
        @SerializedName("token")
        val token:String?=null,
        @SerializedName("physical_lock_id")
        val physical_lock_id:String?=null
    )
}