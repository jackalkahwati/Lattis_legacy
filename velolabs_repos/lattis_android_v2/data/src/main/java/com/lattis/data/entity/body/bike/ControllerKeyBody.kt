package com.lattis.data.entity.body.bike

import com.google.gson.annotations.SerializedName

class ControllerKeyBody (controller_key:List<String>?=null){

    @SerializedName("controller_key")
    private var controller_key:List<String>?=null

    init {
        this.controller_key = controller_key
    }
}