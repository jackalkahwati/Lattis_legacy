package com.lattis.ellipse.data.network.model.body.parking;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lattis on 24/05/17.
 */

public class GetParkingZoneBody {
    @SerializedName("fleet_id")
    private int fleetId;

    public GetParkingZoneBody(int fleetId) {
        this.fleetId = fleetId;
    }

}
