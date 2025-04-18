package com.lattis.ellipse.data.network.store;

import com.lattis.ellipse.data.network.api.RideApi;
import com.lattis.ellipse.data.network.model.body.ride.EndRideBody;
import com.lattis.ellipse.data.network.model.body.ride.RideRatingBody;
import com.lattis.ellipse.data.network.model.body.ride.RideSummaryBody;
import com.lattis.ellipse.data.network.model.body.ride.StartRideBody;
import com.lattis.ellipse.data.network.model.body.ride.UpdateRideBody;
import com.lattis.ellipse.data.network.model.response.history.RideHistoryResponse;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.data.network.model.response.ride.StartRideResponse;
import com.lattis.ellipse.data.network.model.response.ride.UpdateTripResponse;
import com.lattis.ellipse.domain.model.Location;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 3/23/17.
 */

public class RideNetworkDataStore {

    private RideApi rideApi;

    @Inject
    public RideNetworkDataStore(RideApi rideApi) {
        this.rideApi = rideApi;
    }

    public Observable<StartRideResponse> startRide(int bike_id, Location location){
        return this.rideApi.startRide(new StartRideBody(bike_id,location));
    }

    public Observable<Boolean> endRide(int trip_id, Location location, int parkingId, String imageURL,boolean isDamage, Integer lock_battery, Integer bike_battery){
        return this.rideApi.endRide(new EndRideBody(trip_id,location,parkingId,imageURL,isDamage,lock_battery,bike_battery)).flatMap( aVoid -> {
                return Observable.just(true);
        });
    }

    public Observable<UpdateTripResponse> updateRide(int trip_id, double[][] steps){
        return this.rideApi.updateRide(new UpdateRideBody(trip_id,steps));
    }

    public Observable<RideSummaryResponse> getRideSummary(int trip_id){
        return this.rideApi.getRideSummary(new RideSummaryBody(trip_id));
    }

    public Observable<Boolean> rateRide(int trip_id,int rating){
        return this.rideApi.rateRide(new RideRatingBody(trip_id,rating)).flatMap( aVoid -> {
            return Observable.just(true);
        });
    }

    public Observable<RideHistoryResponse> getRideHistory(){
        return this.rideApi.getRideHistory();
    }

}
