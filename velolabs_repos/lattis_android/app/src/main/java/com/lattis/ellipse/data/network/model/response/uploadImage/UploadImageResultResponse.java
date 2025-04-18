package com.lattis.ellipse.data.network.model.response.uploadImage;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Velo Labs Android on 06-04-2017.
 */

public class UploadImageResultResponse {
    @SerializedName("uploaded_url")
    String uploaded_url;

    public String getUploaded_url() {
        return uploaded_url;
    }



    @Override
    public String toString() {
        return "UploadImageResultResponse{" +
                "uploaded_url=" + uploaded_url +
                '}';
    }
}
