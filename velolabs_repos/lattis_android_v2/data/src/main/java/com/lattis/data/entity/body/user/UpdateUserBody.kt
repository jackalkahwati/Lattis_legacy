package com.lattis.data.entity.body.user

import com.google.gson.annotations.SerializedName

class UpdateUserBody(@field:SerializedName("properties")
                     private val userBody: UserBody) {

    override fun toString(): String {
        return "UpdateUserBody{" +
                "userBody=" + userBody +
                '}'.toString()
    }
}
