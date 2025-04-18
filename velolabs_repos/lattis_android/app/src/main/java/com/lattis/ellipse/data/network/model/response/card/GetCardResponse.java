package com.lattis.ellipse.data.network.model.response.card;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;

import java.util.List;

/**
 * Created by ssd3 on 7/26/17.
 */

public class GetCardResponse extends AbstractApiResponse {

    @SerializedName("payload")
    List<GetCardDataResponse> getCardDataResponse;

    public List<GetCardDataResponse> getCardList() {
        return this.getCardDataResponse;
    }

}
