package com.lattis.data.entity.body.dockhub

import com.google.gson.annotations.SerializedName

class UndockBody(hub_type: String) {
    @SerializedName("hub_type")
    private var hub_type: String? = null

    init {
        this.hub_type = hub_type
    }
}