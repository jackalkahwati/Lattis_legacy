package io.lattis.data.entity.response.base

import com.google.gson.annotations.SerializedName

abstract class AbstractApiResponse {

    @SerializedName("status")
    val status: Int? = 0
    @SerializedName("error")
    val error: String? = null

    override fun toString(): String {
        return "AbstractApiResponse{" +
                "status=" + status +
                ", error='" + error + '\''.toString() +
                '}'.toString()
    }
}
