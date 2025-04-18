package com.lattis.data.entity.response.uploadimage

import com.google.gson.annotations.SerializedName
import com.lattis.data.entity.response.base.AbstractApiResponse

class UploadImageResponse : AbstractApiResponse() {
    @SerializedName("payload")
    var resultResponse: UploadImageResultResponse? = null
    fun uploadedUrl(): String? {
        return resultResponse!!.uploaded_url
    }
}