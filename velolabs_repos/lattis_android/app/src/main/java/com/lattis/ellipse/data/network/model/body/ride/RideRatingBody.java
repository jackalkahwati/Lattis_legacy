package com.lattis.ellipse.data.network.model.body.ride;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/11/17.
 */

public class RideRatingBody {
    @SerializedName("trip_id")
    private int trip_id;

    @SerializedName("rating")
    private int rating;

    public RideRatingBody(int trip_id, int rating) {
        this.trip_id = trip_id;
        this.rating =rating;
    }

}
