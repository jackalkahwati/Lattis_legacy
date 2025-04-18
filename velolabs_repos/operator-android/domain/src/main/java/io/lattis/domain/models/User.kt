package io.lattis.domain.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class User(
        @SerializedName("token")
        var restToken:String?=null,
        @SerializedName("operator")
        var operator: Operator?=null
):Serializable {

    data class Operator(
            @SerializedName("id")
            var id: String? = null,
            @SerializedName("firstName")
            var firstName: String? = null,
            @SerializedName("lastName")
            var lastName: String? = null,
            @SerializedName("email")
            var email: String? = null,
            @SerializedName("phoneNumber")
            var phoneNumber: String? = null
    ):Serializable

}
