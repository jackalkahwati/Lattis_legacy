package com.lattis.ellipse.data.network.model.response.parking;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ssd3 on 4/5/17.
 */

public class FindParkingPayloadResponse {

    @SerializedName("parking_spots")
    List<FindParkingDataResponse> findParkingDataResponses;

    List<FindParkingDataResponse> findParkingDataResponses(){
        return findParkingDataResponses;
    }
}
