package com.lattis.ellipse.data.network.model.response.ride;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 7/28/17.
 */

public class UpdateTripResponse {

    @SerializedName("payload")
    UpdateTripDataResponse updateTripDataResponse;

    public UpdateTripDataResponse getUpdateTripDataResponse(){
        return updateTripDataResponse;
    }



}
