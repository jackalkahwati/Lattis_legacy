package com.lattis.ellipse.data.network.api;

import com.lattis.ellipse.data.network.model.body.bike.BikeDetailBody;
import com.lattis.ellipse.data.network.model.body.bike.BikeMetaDataBody;
import com.lattis.ellipse.data.network.model.body.bike.BookBikeBody;
import com.lattis.ellipse.data.network.model.body.bike.CancelBikeBody;
import com.lattis.ellipse.data.network.model.body.bike.FindBikesBody;
import com.lattis.ellipse.data.network.model.response.bike.BikeDetailResponse;
import com.lattis.ellipse.data.network.model.response.bike.FindBikeResponse;
import com.lattis.ellipse.data.network.model.response.bike.ReserveBikeResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import io.reactivex.Observable;

public interface BikeApi {
    // @POST("bikes/find-bikes/")
    @POST("bikes/search-bikes/")
    Observable<FindBikeResponse> findBikes(@Body FindBikesBody findBikesBody);

    @POST("bikes/create-booking/")
    Observable<ReserveBikeResponse> bookBikes(@Body BookBikeBody bookBikeBody);

    @POST("bikes/cancel-booking/")
    Observable<Void> cancelBikes(@Body CancelBikeBody bookBikeBody);


    @POST("bikes/get-bike-details/")
    Observable<BikeDetailResponse> getBikeDetails(@Body BikeDetailBody bikeDetailBody);

    @POST("bikes/update-metadata-for-user/")
    Observable<Void> updateBikeMetaData(@Body BikeMetaDataBody bikeMetaDataBoday);
}
