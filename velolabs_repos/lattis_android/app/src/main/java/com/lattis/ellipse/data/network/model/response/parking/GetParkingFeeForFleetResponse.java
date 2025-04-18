package com.lattis.ellipse.data.network.model.response.parking;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;

/**
 * Created by ssd3 on 8/1/17.
 */

public class GetParkingFeeForFleetResponse extends AbstractApiResponse {

    @SerializedName("payload")
    GetParkingFeeForFleetDataResponse getParkingFeeForFleetDataResponse;

    public GetParkingFeeForFleetDataResponse GetParkingFeeForFleetData() {
        return getParkingFeeForFleetDataResponse;
    }

}
