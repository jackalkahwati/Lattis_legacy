package com.lattis.ellipse.data.network.model.body.ride;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 3/30/17.
 */

public class UpdateRideBody {

    @SerializedName("trip_id")
    private int trip_id;
    @SerializedName("steps")
    private double[][] steps;

    public UpdateRideBody(int trip_id,
                          double[][]steps) {
        this.trip_id = trip_id;
        this.steps=steps;
    }
}
