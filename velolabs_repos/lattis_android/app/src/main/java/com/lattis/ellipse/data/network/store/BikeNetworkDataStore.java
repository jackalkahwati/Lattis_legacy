package com.lattis.ellipse.data.network.store;


import com.lattis.ellipse.data.network.api.BikeApi;
import com.lattis.ellipse.data.network.model.body.bike.BikeDetailBody;
import com.lattis.ellipse.data.network.model.body.bike.BikeMetaDataBody;
import com.lattis.ellipse.data.network.model.body.bike.BookBikeBody;
import com.lattis.ellipse.data.network.model.body.bike.CancelBikeBody;
import com.lattis.ellipse.data.network.model.body.bike.FindBikesBody;
import com.lattis.ellipse.data.network.model.mapper.BikeListResponseMapper;
import com.lattis.ellipse.data.network.model.mapper.BikeResponseMapper;
import com.lattis.ellipse.data.network.model.response.bike.ReserveBikeResponse;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.SearchBike;

import javax.inject.Inject;

import io.reactivex.Observable;

public class BikeNetworkDataStore {

    private BikeApi bikeApi;
    private BikeResponseMapper bikeResponseMapper;
    private BikeListResponseMapper bikeListResponseMapper;


    @Inject
    public BikeNetworkDataStore(BikeApi bikeApi,
                                BikeResponseMapper bikeResponseMapper,
                                BikeListResponseMapper bikeListResponseMapper) {
        this.bikeApi = bikeApi;
        this.bikeResponseMapper = bikeResponseMapper;
        this.bikeListResponseMapper = bikeListResponseMapper;
    }

    public Observable<SearchBike> findBikes(double latitude, double longitude) {
        return this.bikeApi.findBikes(new FindBikesBody(latitude, longitude)).map(findBikeResponse -> {
            return bikeListResponseMapper.mapIn(findBikeResponse.getFindBikePayloadResponse());
        });
    }

    public Observable<ReserveBikeResponse> bookBike(int bike_id,boolean by_scan,double latitude,double longitude) {
        return this.bikeApi.bookBikes(new BookBikeBody(bike_id,by_scan, latitude, longitude));
    }


    public Observable<Boolean> cancelBike(int bike_id, boolean bike_damaged,boolean lockIssue){
        return this.bikeApi.cancelBikes( new CancelBikeBody(bike_id,bike_damaged,lockIssue))
                .flatMap(aVoid -> {
                    return Observable.just(true);
                });
    }

    public Observable<Bike> bikeDetails(int bike_id, int qr_code_id){
        return this.bikeApi.getBikeDetails( new BikeDetailBody(bike_id, qr_code_id,-1)).map(bikeDetailResponse -> {
            return bikeResponseMapper.mapIn(bikeDetailResponse.getBikeDetailResponse());
        });
    }

    public Observable<Void> updateBikeMetaData(int bike_id,int bike_battery_level, int lock_battery_level, String firmware_version, Boolean shackle_jam){
        return this.bikeApi.updateBikeMetaData(new BikeMetaDataBody(bike_id, bike_battery_level, lock_battery_level, firmware_version, shackle_jam));
    }



}
