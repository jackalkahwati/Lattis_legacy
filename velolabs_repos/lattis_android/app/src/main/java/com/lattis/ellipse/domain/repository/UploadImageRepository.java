package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.data.network.model.response.uploadImage.UploadImageResponse;

import io.reactivex.Observable;
import okhttp3.MultipartBody;


/**
 * Created by Velo Labs Android on 06-04-2017.
 */

public interface UploadImageRepository {
    Observable<UploadImageResponse> uploadImage(String type, MultipartBody.Part part);
}
