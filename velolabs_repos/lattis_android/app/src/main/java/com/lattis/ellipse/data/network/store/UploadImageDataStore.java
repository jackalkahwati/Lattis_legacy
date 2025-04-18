package com.lattis.ellipse.data.network.store;

import com.lattis.ellipse.data.network.api.UploadImageApi;
import com.lattis.ellipse.data.network.model.response.uploadImage.UploadImageResponse;

import javax.inject.Inject;

import io.reactivex.Observable;
import okhttp3.MultipartBody;

/**
 * Created by Velo Labs Android on 06-04-2017.
 */

public class UploadImageDataStore {
    private UploadImageApi uploadImageApi;
    @Inject
    UploadImageDataStore (UploadImageApi uploadImageApi)
    {
        this.uploadImageApi = uploadImageApi;
    }
    public Observable<UploadImageResponse> uploadImage(String type, MultipartBody.Part multipart){
        return this.uploadImageApi.uploadImage(type,multipart);
    }

}
