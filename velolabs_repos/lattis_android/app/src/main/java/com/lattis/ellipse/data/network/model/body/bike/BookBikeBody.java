package com.lattis.ellipse.data.network.model.body.bike;

import com.google.gson.annotations.SerializedName;

public class BookBikeBody {

    @SerializedName("bike_id")
    private int bike_id;

    @SerializedName("by_scan")
    private Boolean by_scan=null;

    @SerializedName("latitude")
    private Double latitude=null;

    @SerializedName("longitude")
    private Double longitude=null;

    public BookBikeBody(int bike_id,boolean by_scan,double latitude,double longitude){
        this.bike_id=bike_id;

        if(by_scan) {
            this.by_scan = by_scan;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
