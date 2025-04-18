package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

public class CheckTermsConditionResponse extends AbstractApiResponse{

    @SerializedName("payload")
    public CheckTermsConditionResultResponse resultResponse;

    public boolean hasAccepted() {
        return resultResponse.hasAccepted();
    }

}
