package com.lattis.domain.models

data class ParkingFeeForFleet(
    val outside: Boolean?,
    val not_allowed:Boolean? ,
    val fee: Float? ,
    val currency: String?
)