package com.lattis.ellipse.data.network.model.response.card;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;
import com.lattis.ellipse.data.network.model.response.bike.FindBikePayloadResponse;

public class SetUpIntentResponse extends AbstractApiResponse {
    @SerializedName("payload")
    SetUpIntentDataResponse setUpIntentDataResponse;

    public SetUpIntentDataResponse getSetUpIntentDataResponse() {
        return setUpIntentDataResponse;
    }

}
