package com.lattis.data.entity.response.card

import com.google.gson.annotations.SerializedName

/**
 * Created by ssd3 on 7/26/17.
 */

class GetCardDataResponse {

    @SerializedName("id")
    val id: Int = 0

    @SerializedName("user_id")
    val user_id: Int = 0

    @SerializedName("stripe_net_profile_id")
    val stripe_net_profile_id: String? = null

    @SerializedName("stripe_net_payment_id")
    val stripe_net_payment_id: String? = null

    @SerializedName("is_primary")
    val is_primary: Boolean = false

    @SerializedName("type_card")
    val type_card: String? = null

    @SerializedName("cc_no")
    val cc_no: String? = null

    @SerializedName("exp_month")
    val exp_month: Int = 0

    @SerializedName("exp_year")
    val exp_year: Int = 0

    @SerializedName("fingerprint")
    val fingerprint: String? = null

    @SerializedName("cc_type")
    val cc_type: String? = null

    @SerializedName("created_date")
    val created_date: Int = 0

    @SerializedName("last_updated")
    val last_updated: String? = null

    @SerializedName("card_id")
    val card_id: String? = null
}
