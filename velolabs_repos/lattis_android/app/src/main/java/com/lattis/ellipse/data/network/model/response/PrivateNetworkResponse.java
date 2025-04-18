package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 5/8/17.
 */

public class PrivateNetworkResponse {

    @SerializedName("private_fleet_user_id")
    private int private_fleet_user_id;
    @SerializedName("user_id")
    private int user_id;
    @SerializedName("email")
    private String email;
    @SerializedName("fleet_id")
    private int fleet_id;
    @SerializedName("verified")
    private int verified;
    @SerializedName("fleet_name")
    private String fleet_name;
    @SerializedName("type")
    private String type;
    @SerializedName("logo")
    private String logo;

    public int getPrivate_fleet_user_id() {
        return private_fleet_user_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getEmail() {
        return email;
    }

    public int getFleet_id() {
        return fleet_id;
    }

    public int getVerified() {
        return verified;
    }

    public String getFleet_name() {
        return fleet_name;
    }

    public String getType() {
        return type;
    }

    public String getLogo() {
        return logo;
    }




}
