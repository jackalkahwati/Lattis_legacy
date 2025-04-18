package com.lattis.ellipse.domain.model;

import com.lattis.ellipse.data.network.model.response.bike.FindBikeDataResponse;

import java.util.List;

/**
 * Created by lattis on 24/08/17.
 */

public class SearchBike {
    List<Bike> nearestBikeList;
    List<Bike> availableBikeList;

    public List<Bike> getNearestBikeList() {
        return nearestBikeList;
    }

    public void setNearestBikeList(List<Bike> nearestBikeList) {
        this.nearestBikeList = nearestBikeList;
    }

    public List<Bike> getAvailableBikeList() {
        return availableBikeList;
    }

    public void setAvailableBikeList(List<Bike> availableBikeList) {
        this.availableBikeList = availableBikeList;
    }
}
