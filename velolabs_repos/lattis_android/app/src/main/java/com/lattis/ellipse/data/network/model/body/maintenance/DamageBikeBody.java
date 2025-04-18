package com.lattis.ellipse.data.network.model.body.maintenance;

import com.google.gson.annotations.SerializedName;



public class DamageBikeBody {
    @SerializedName("category")
    private String category;
    @SerializedName("notes")
    private String rider_notes;
    @SerializedName("image")
    private String maintenance_image;
    @SerializedName("bike_id")
    private int bike_id;
    @SerializedName("trip_id")
    private Integer trip_id=null;

    public DamageBikeBody(String category, String rider_notes, int bike_id,String maintenance_image, int trip_id) {
        this.category = category;
        this.rider_notes = rider_notes;
        this.bike_id = bike_id;
        this.maintenance_image = maintenance_image;
        if(trip_id>0){
            this.trip_id=trip_id;
        }
    }
}
