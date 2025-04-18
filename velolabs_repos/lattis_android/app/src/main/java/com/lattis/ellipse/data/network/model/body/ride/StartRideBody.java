package com.lattis.ellipse.data.network.model.body.ride;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.domain.model.Location;

/**
 * Created by ssd3 on 3/23/17.
 */

public class StartRideBody {



        @SerializedName("bike_id")
        private int bike_id;
        @SerializedName("latitude")
        private double latitude;
    @SerializedName("longitude")
    private double longitude;

        public StartRideBody(int bike_id, Location location) {
            this.bike_id = bike_id;
            this.latitude = location.getLatitude();
            this.longitude =location.getLongitude();
        }

}
