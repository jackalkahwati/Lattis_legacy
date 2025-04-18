package com.lattis.data.entity.body.card

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class UpdateCardExpirationBody(
    @field:SerializedName("card_id") private val cardId: String,
    @field:SerializedName("exp_month") private val exp_month: Int,
    @field:SerializedName("exp_year") private val exp_year: Int
    ) {


}