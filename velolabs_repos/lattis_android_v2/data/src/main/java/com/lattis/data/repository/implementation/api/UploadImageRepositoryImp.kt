package com.lattis.data.repository.implementation.api

import com.lattis.data.net.uploadimage.UploadImageApiClient
import com.lattis.domain.models.UploadImage
import com.lattis.domain.repository.UploadImageRepository
import io.reactivex.rxjava3.core.Observable
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*
import javax.inject.Inject

class UploadImageRepositoryImp @Inject
constructor(
    val uploadImageApiClient: UploadImageApiClient
) : UploadImageRepository {

    override fun uploadImage(type: String, filepath: String): Observable<UploadImage> {

        val file = File(filepath)
        val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData(
            type,
            "" + Date().time,
            requestFile
        )

        return uploadImageApiClient.api.uploadImage(type,body).map {
            UploadImage(it.uploadedUrl())
        }
    }
}