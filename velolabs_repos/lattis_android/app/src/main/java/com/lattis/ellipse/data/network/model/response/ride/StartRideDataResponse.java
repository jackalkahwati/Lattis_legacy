package com.lattis.ellipse.data.network.model.response.ride;

import com.google.gson.annotations.SerializedName;

public class StartRideDataResponse {

    @SerializedName("trip_id")
    private int tripId;

    @SerializedName("do_not_track_trip")
    private Boolean do_not_track_trip;

    public int getTripId() {
        return tripId;
    }

    public Boolean getDo_not_track_trip() {
        return do_not_track_trip;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }


}
