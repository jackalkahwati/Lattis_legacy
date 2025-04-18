package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/12/17.
 */

public class GetCurrentUserStatusTripResponse {

    @SerializedName("trip_id")
    private int trip_id;

    public int getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(int trip_id) {
        this.trip_id = trip_id;
    }


}
