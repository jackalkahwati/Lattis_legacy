package com.lattis.ellipse.data.network.model.body.bike;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 4/13/17.
 */

public class BikeDetailBody {

    @SerializedName("bike_id")
    private Integer bike_id;

    @SerializedName("qr_code_id")
    private Integer qr_code_id=null;

    @SerializedName("trip_id")
    private Integer trip_id=null;



    public BikeDetailBody(int bike_id, int qr_code_id, int trip_id){

        if(qr_code_id>0){
            this.qr_code_id = qr_code_id;
        }

        if(bike_id>0){
            this.bike_id=bike_id;
        }

        if(trip_id>0){
            this.trip_id=trip_id;
        }

    }
}
