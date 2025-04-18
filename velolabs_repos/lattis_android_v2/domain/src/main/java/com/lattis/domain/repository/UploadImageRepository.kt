package com.lattis.domain.repository

import com.lattis.domain.models.UploadImage
import io.reactivex.rxjava3.core.Observable

interface UploadImageRepository {
    fun uploadImage(
        type: String,
        filepath :String
    ): Observable<UploadImage>
}