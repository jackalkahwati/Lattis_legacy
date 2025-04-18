package com.lattis.ellipse.domain.model.map;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;

public class PlaceAutocomplete {

    private CharSequence placeId;
    private CharSequence address1;
    private CharSequence address2;
    private Double latitude;
    private Double longitude;
    private CarmenFeature carmenFeature;

    public CharSequence getPlaceId() {
        return placeId;
    }

    public void setPlaceId(CharSequence placeId) {
        this.placeId = placeId;
    }

    public CharSequence getAddress1() {
        return address1;
    }

    public void setAddress1(CharSequence address1) {
        this.address1 = address1;
    }

    public CharSequence getAddress2() {
        return address2;
    }

    public void setAddress2(CharSequence address2) {
        this.address2 = address2;
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

    public CarmenFeature getCarmenFeature() {
        return carmenFeature;
    }

    public void setCarmenFeature(CarmenFeature carmenFeature) {
        this.carmenFeature = carmenFeature;
    }


}
