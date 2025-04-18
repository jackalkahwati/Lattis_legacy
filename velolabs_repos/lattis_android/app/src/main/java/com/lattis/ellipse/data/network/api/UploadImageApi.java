package com.lattis.ellipse.data.network.api;

import com.lattis.ellipse.data.network.model.response.uploadImage.UploadImageResponse;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by Velo Labs Android on 06-04-2017.
 */

public interface UploadImageApi {
    @Multipart
    @POST("misc/upload")
    Observable<UploadImageResponse> uploadImage(@Query("type") String upload_Type,
                                                @Part MultipartBody.Part  filePart);

}
