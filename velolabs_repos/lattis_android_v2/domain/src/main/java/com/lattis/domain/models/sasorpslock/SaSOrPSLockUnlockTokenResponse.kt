package com.lattis.domain.models.sasorpslock

import com.google.gson.annotations.SerializedName

data class SaSOrPSLockUnlockTokenResponse(
    @SerializedName("payload")
    val saSOrPSLockUnlockToken:SaSOrPSLockUnlockToken?=null
) {
    data class SaSOrPSLockUnlockToken(
        @SerializedName("token")
        val token:String?=null
    )
}