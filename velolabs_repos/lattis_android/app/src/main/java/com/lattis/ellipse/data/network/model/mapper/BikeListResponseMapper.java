package com.lattis.ellipse.data.network.model.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.data.network.model.response.bike.FindBikePayloadResponse;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.SearchBike;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by lattis on 24/08/17.
 */

public class BikeListResponseMapper extends AbstractDataMapper<FindBikePayloadResponse, SearchBike> {

    private BikeResponseMapper bikeResponseMapper;
    List<Bike> availableBikeList = new ArrayList<>();
    List<Bike> nearestBikeList = new ArrayList<>();


    @Inject
    BikeListResponseMapper(BikeResponseMapper bikeResponseMapper) {
        this.bikeResponseMapper = bikeResponseMapper;
    }

    @NonNull
    @Override
    public SearchBike mapIn(@NonNull FindBikePayloadResponse findBikePayloadResponse) {
        SearchBike searchBike = new SearchBike();
        availableBikeList = (bikeResponseMapper.mapIn(findBikePayloadResponse.getFindAvailableBikeDataResponse()));
        nearestBikeList = (bikeResponseMapper.mapIn(findBikePayloadResponse.getFindNearestBikeDataResponse()));
        searchBike.setAvailableBikeList(availableBikeList);
        searchBike.setNearestBikeList(nearestBikeList);
        return searchBike;
    }

    @NonNull
    @Override
    public FindBikePayloadResponse mapOut(@NonNull SearchBike searchBike) {
        return null;
    }

}
