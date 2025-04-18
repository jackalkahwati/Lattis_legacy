package com.lattis.data.entity.response.uploadimage

import com.google.gson.annotations.SerializedName

class UploadImageResultResponse {

    @SerializedName("uploaded_url")
    var uploaded_url: String? = null

    override fun toString(): String {
        return "UploadImageResultResponse{" +
                "uploaded_url=" + uploaded_url +
                '}'
    }
}