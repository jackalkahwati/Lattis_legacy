package com.lattis.data.entity.body.user

import com.google.gson.annotations.SerializedName

class UserBody(@field:SerializedName("user_id")
               private val userId: String?, @field:SerializedName("first_name")
               private val firstName: String?, @field:SerializedName("last_name")
               private val lastName: String?, @field:SerializedName("email")
               private val email: String?) {

    override fun toString(): String {
        return "UserBody{" +
                "userId='" + userId + '\''.toString() +
                ", firstName='" + firstName + '\''.toString() +
                ", lastName='" + lastName + '\''.toString() +
                ", email='" + email + '\''.toString() +
                '}'.toString()
    }
}
