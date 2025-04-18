package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.data.network.model.response.history.RideHistoryResponse;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.data.network.model.response.ride.UpdateTripResponse;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Ride;

import io.reactivex.Observable;

public interface RideRepository {

    Observable<Ride> startRide(Bike bike, Location location, boolean first_lock_connect);
    Observable<Boolean> endRide(int trip_id, Location location, int parkingId, String imageURL,boolean isReportDamage, Integer lock_battery, Integer bike_battery);
    Observable<UpdateTripResponse> updateRide(int trip_id, double[][] steps);
    Observable<Ride> getRide();
    Observable<RideSummaryResponse> getRideSummary(int trip_id);
    Observable<Boolean> rateRide(int trip_id, int rating);
    Observable<Ride> saveRide(Ride ride);
    Observable<Boolean> deleteRide();
    Observable<RideHistoryResponse> getRideHistory();
}


