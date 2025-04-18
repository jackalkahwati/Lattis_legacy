package com.lattis.data.entity.response.card

import com.google.gson.annotations.SerializedName

class SetUpIntentDataResponse {
    @SerializedName("id")
    val id: String? = null

    @SerializedName("object")
    val `object`: String? = null

    @SerializedName("application")
    val application: String? = null

    @SerializedName("cancellation_reason")
    val cancellation_reason: String? = null

    @SerializedName("client_secret")
    val client_secret: String? = null

    @SerializedName("created")
    val created: Long = 0

    @SerializedName("customer")
    val customer: String? = null

    @SerializedName("description")
    val description: String? = null

    @SerializedName("last_setup_error")
    val last_setup_error: String? = null

    @SerializedName("livemode")
    val isLivemode = false

    @SerializedName("next_action")
    val next_action: String? = null

    @SerializedName("on_behalf_of")
    val on_behalf_of: String? = null

    @SerializedName("payment_method")
    val payment_method: String? = null

    @SerializedName("status")
    val status: String? = null

    @SerializedName("usage")
    val usage: String? = null

    @SerializedName("payment_method_types")
    val payment_method_types: List<String>? = null

}