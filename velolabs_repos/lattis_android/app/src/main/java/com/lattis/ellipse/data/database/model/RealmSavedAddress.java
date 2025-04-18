package com.lattis.ellipse.data.database.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class RealmSavedAddress extends RealmObject {

    @Required
    @PrimaryKey
    private String placeId;
    private String name;
    private Double latitude;
    private Double longitude;


    public String getId() {
        return placeId;
    }
    public void setId(String id) {
        this.placeId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
