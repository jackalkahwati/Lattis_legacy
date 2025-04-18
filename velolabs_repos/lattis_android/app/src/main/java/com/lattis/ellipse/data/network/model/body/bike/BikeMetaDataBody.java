package com.lattis.ellipse.data.network.model.body.bike;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 9/6/17.
 */

public class BikeMetaDataBody {

    @SerializedName("firmware_version")
    private String firmware_version=null;

    @SerializedName("shackle_jam")
    private Boolean shackle_jam=null;

    @SerializedName("bike_battery_level")
    private Integer bike_battery_level=null;

    @SerializedName("bike_id")
    private Integer bike_id=null;

    @SerializedName("lock_battery_level")
    private Integer lock_battery_level=null;

    public BikeMetaDataBody(int bike_id, int bike_battery_level, int lock_battery_level, String firmware_version, Boolean shackle_jam){

        this.firmware_version = firmware_version;
        this.shackle_jam = shackle_jam;
        if(bike_battery_level>0){
            this.bike_battery_level = bike_battery_level;
        }
        if(lock_battery_level>0){
            this.lock_battery_level=lock_battery_level;
        }
        this.bike_id=bike_id;

    }
}
