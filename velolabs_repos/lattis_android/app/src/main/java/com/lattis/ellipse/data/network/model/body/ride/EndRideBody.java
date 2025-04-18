package com.lattis.ellipse.data.network.model.body.ride;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.domain.model.Location;

public class EndRideBody {

    @SerializedName("trip_id")
    private int trip_id;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("parking_spot_id")
    private Integer parking_spot_id;

    @SerializedName("parking_image")
    private String parking_image;

    @SerializedName("bike_damaged")
    private boolean isBikeDamage;

    @SerializedName("bike_battery_level")
    private Integer bike_battery_level;

    @SerializedName("lock_battery_level")
    private Integer lock_battery_level;

    @SerializedName("accuracy")
    private double accuracy;

    public EndRideBody(int trip_id, Location location, int parkingId, String imageURL,boolean isBikeDamage, Integer lock_battery, Integer bike_battery) {
        this.trip_id = trip_id;
        this.latitude=location.getLatitude();
        this.longitude = location.getLongitude();
        this.isBikeDamage = isBikeDamage;

        if(parkingId!=-1)
            this.parking_spot_id = parkingId;
        else
            this.parking_spot_id = null;

        if(lock_battery!=null){
            lock_battery_level = lock_battery;
        }
        if(bike_battery!=null){
            bike_battery_level = bike_battery;
        }

        if(location.hasAccuracy()) {
            this.accuracy = location.getAccuracy();
        }else{
            this.accuracy = 0.0;
        }

        this.parking_image = imageURL;
    }
}
