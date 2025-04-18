package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/12/17.
 */

public class GetCurrentUserActiveBookingStatusResponse {

    @SerializedName("bike_id")
    private int bike_id;

    @SerializedName("booked_on")
    private long booked_on;

    @SerializedName("till")
    private long till;

    public int getBike_id() {
        return bike_id;
    }

    public long getBooked_on() {
        return booked_on;
    }

    public long getTill() {
        return till;
    }



}
