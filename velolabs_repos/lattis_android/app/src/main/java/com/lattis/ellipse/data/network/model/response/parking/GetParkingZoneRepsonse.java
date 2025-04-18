package com.lattis.ellipse.data.network.model.response.parking;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;

import java.util.List;

/**
 * Created by lattis on 24/05/17.
 */

public class GetParkingZoneRepsonse extends AbstractApiResponse {

    @SerializedName("payload")
    GetParkingZonePayloadResponse getParkingZonePayloadResponse;

    public List<GetParkingZoneDataResponse> getParkingZoneResponse() {
        return getParkingZonePayloadResponse.getParkingZoneDataResponses();
    }

    @Override
    public String toString() {
        return "GetParkingZoneRepsonse{" +
                "GetParkingZoneRepsonse=" + getParkingZonePayloadResponse +
                '}';
    }

}
