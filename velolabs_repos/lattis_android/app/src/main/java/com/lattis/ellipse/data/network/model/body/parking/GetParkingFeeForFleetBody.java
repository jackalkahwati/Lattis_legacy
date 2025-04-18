package com.lattis.ellipse.data.network.model.body.parking;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.domain.model.Location;

/**
 * Created by ssd3 on 8/1/17.
 */

public class GetParkingFeeForFleetBody {

    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("accuracy")
    private double accuracy;
    @SerializedName("fleet_id")
    private int fleet_id;

    public GetParkingFeeForFleetBody(Location location,
                           int fleet_id) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        if(location.hasAccuracy()) {
            this.accuracy = location.getAccuracy();
        }else{
            this.accuracy = 0.0;
        }
        this.fleet_id = fleet_id;
    }

}
