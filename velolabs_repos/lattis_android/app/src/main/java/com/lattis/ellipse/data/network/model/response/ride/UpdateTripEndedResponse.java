package com.lattis.ellipse.data.network.model.response.ride;

import com.google.gson.annotations.SerializedName;

public class UpdateTripEndedResponse {


    @SerializedName("date_endtrip")
    private String date_endtrip;


    public String getDate_endtrip() {
        return date_endtrip;
    }
}
