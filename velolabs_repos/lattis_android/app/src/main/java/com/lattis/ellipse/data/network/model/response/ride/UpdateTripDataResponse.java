package com.lattis.ellipse.data.network.model.response.ride;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 7/28/17.
 */

public class UpdateTripDataResponse {

    @SerializedName("duration")
    private double duration;

    @SerializedName("charge_for_duration")
    private float charge_for_duration;

    @SerializedName("currency")
    private String currency;


    @SerializedName("ended_trip")
    UpdateTripEndedResponse updateTripEndedResponse;


    public double getDuration() {
        return duration;
    }

    public float getCharge_for_duration() {
        return charge_for_duration;
    }

    public String getCurrency() {
        return currency;
    }


    public UpdateTripEndedResponse getUpdateTripEndedResponse() {
        return updateTripEndedResponse;
    }




}
