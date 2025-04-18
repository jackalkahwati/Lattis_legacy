package com.lattis.data.repository.datasources.api.uploadimage

import com.lattis.data.entity.response.uploadimage.UploadImageResponse
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UploadImageApi {
    @Multipart
    @POST("api/misc/upload")
    fun uploadImage(
        @Query("type") upload_Type: String,
        @Part filePart: MultipartBody.Part
    ): Observable<UploadImageResponse>
}