package com.lattis.ellipse.data.network.model.response.parking;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 8/1/17.
 */

public class GetParkingFeeForFleetDataResponse {

    @SerializedName("outside")
    private boolean outside;
    @SerializedName("not_allowed")
    private boolean not_allowed;
    @SerializedName("fee")
    private float fee;
    @SerializedName("currency")
    private String currency;

    public String getCurrency() {
        return currency;
    }

    public boolean isOutside() {
        return outside;
    }

    public boolean isNot_allowed() {
        return not_allowed;
    }

    public float getFee() {
        return fee;
    }


}
