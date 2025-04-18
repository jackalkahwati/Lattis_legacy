package com.lattis.ellipse.data.database.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ssd3 on 5/8/17.
 */

public class RealmPrivateNetwork extends RealmObject {


    public static final String COLUMN_NAME_PRIVATE_FLEET_USER_ID = "private_fleet_user_id";

    @PrimaryKey
    private int private_fleet_user_id;
    private int userId;
    private String email;
    private int fleet_id;
    private int verified;
    private String fleet_name;
    private String type;
    private String logo;

    public int getPrivate_fleet_user_id() {
        return private_fleet_user_id;
    }

    public void setPrivate_fleet_user_id(int private_fleet_user_id) {
        this.private_fleet_user_id = private_fleet_user_id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getFleet_id() {
        return fleet_id;
    }

    public void setFleet_id(int fleet_id) {
        this.fleet_id = fleet_id;
    }

    public int getVerified() {
        return verified;
    }

    public void setVerified(int verified) {
        this.verified = verified;
    }

    public String getFleet_name() {
        return fleet_name;
    }

    public void setFleet_name(String fleet_name) {
        this.fleet_name = fleet_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }


}
