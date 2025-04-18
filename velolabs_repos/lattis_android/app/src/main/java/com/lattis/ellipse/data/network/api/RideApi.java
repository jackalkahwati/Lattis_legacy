package com.lattis.ellipse.data.network.api;


import com.lattis.ellipse.data.network.model.body.ride.EndRideBody;
import com.lattis.ellipse.data.network.model.body.ride.RideRatingBody;
import com.lattis.ellipse.data.network.model.body.ride.RideSummaryBody;
import com.lattis.ellipse.data.network.model.body.ride.StartRideBody;
import com.lattis.ellipse.data.network.model.body.ride.UpdateRideBody;
import com.lattis.ellipse.data.network.model.response.history.RideHistoryResponse;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.data.network.model.response.ride.StartRideResponse;
import com.lattis.ellipse.data.network.model.response.ride.UpdateTripResponse;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import io.reactivex.Observable;

public interface RideApi {

    @POST("trips/start-trip/")
    Observable<StartRideResponse> startRide(@Body StartRideBody startRideBody);

    @POST("trips/end-trip/")
    Observable<Void> endRide(@Body EndRideBody endRideBody);

    @POST("trips/update-trip/")
    Observable<UpdateTripResponse> updateRide(@Body UpdateRideBody updateRideBody);


    @POST("trips/get-trip-details/")
    Observable<RideSummaryResponse> getRideSummary(@Body RideSummaryBody rideSummaryBody);

    @POST("trips/update-rating/")
    Observable<Void> rateRide(@Body RideRatingBody rideRatingBody);

    @GET("trips/get-trips/")
    Observable<RideHistoryResponse> getRideHistory();

}
