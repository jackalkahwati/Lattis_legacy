package com.lattis.data.entity.body.card

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

class AddCardBody(
    @field:SerializedName("cc_no") private val cc_no: String,
    @field:SerializedName("exp_month") private val exp_month: Int,
    @field:SerializedName("exp_year") private val exp_year: Int,
    @field:SerializedName("cvc") private val cvc: String,
    intent: JSONObject
) {

    @SerializedName("intent")
    private val intent: JsonObject

    init {
        this.intent =
            JsonParser().parse(intent.toString()) as JsonObject
    }
}