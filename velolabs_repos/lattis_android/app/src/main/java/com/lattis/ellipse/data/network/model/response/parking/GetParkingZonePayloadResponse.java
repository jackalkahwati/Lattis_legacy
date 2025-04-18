package com.lattis.ellipse.data.network.model.response.parking;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by lattis on 24/05/17.
 */

public class GetParkingZonePayloadResponse {
    @SerializedName("parking_zones")
    List<GetParkingZoneDataResponse> getParkingZoneDataResponse;

    public List<GetParkingZoneDataResponse> getParkingZoneDataResponses() {
        return getParkingZoneDataResponse;
    }
}
