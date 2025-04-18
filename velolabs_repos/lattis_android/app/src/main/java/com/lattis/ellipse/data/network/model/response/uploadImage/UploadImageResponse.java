package com.lattis.ellipse.data.network.model.response.uploadImage;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;

import java.util.Arrays;

/**
 * Created by Velo Labs Android on 06-04-2017.
 */

public class UploadImageResponse extends AbstractApiResponse {
    @SerializedName("payload")
    public UploadImageResultResponse resultResponse;

    public String uploadedUrl() {
        return resultResponse.getUploaded_url();
    }
}
