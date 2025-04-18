package com.lattis.ellipse.data.network.model.body.ride;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/11/17.
 */

public class RideSummaryBody {

    @SerializedName("trip_id")
    private int trip_id;

    public RideSummaryBody(int trip_id) {
        this.trip_id = trip_id;
    }
}
