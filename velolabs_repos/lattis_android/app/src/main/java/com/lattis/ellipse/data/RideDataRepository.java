package com.lattis.ellipse.data;

import com.lattis.ellipse.data.database.RideRealmDataStore;
import com.lattis.ellipse.data.network.model.response.history.RideHistoryResponse;
import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.data.network.model.response.ride.UpdateTripResponse;
import com.lattis.ellipse.data.network.store.RideNetworkDataStore;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.repository.RideRepository;
import com.lattis.ellipse.presentation.dagger.qualifier.FleetId;
import com.lattis.ellipse.presentation.model.mapper.BikeModelMapper;

import java.util.Date;

import javax.inject.Inject;

import io.reactivex.Observable;



public class RideDataRepository implements RideRepository {

    private RideNetworkDataStore rideNetworkDataStore;
    private RideRealmDataStore rideRealmDataStore;
    private String fleetId;
    private BikeModelMapper bikeModelMapper;


    @Inject
    public RideDataRepository(RideNetworkDataStore rideNetworkDataStore,
                              RideRealmDataStore rideRealmDataStore,
                              @FleetId String fleetId,
                              BikeModelMapper bikeModelMapper) {
        this.rideNetworkDataStore = rideNetworkDataStore;
        this.rideRealmDataStore = rideRealmDataStore;
        this.fleetId = fleetId;
        this.bikeModelMapper = bikeModelMapper;
    }

    @Override
    public Observable<Ride> startRide(Bike bike, Location location, boolean first_lock_connect) {
        Ride ride = bikeModelMapper.mapIn(bike);
        return getRide().flatMap(oldRide -> {
            ride.setBike_on_call_operator(oldRide.getBike_on_call_operator());
            ride.setSupport_phone(oldRide.getSupport_phone());

            return this.rideNetworkDataStore.startRide(bike.getBike_id(),location).flatMap(
                    startRideResponse ->  {
                        ride.setId(fleetId);
                        ride.setRideId(startRideResponse.getStartRideDataResponse().getTripId());
                        ride.setBikeId(bike.getBike_id());
                        ride.setDo_not_track_trip(bike.isDo_not_track_trip());
                        if(first_lock_connect){
                            ride.setRide_booked_on(getDoubleTime());
                        }else{
                            ride.setRide_booked_on(oldRide.getRide_booked_on());
                        }
                        ride.setFirst_lock_connect(true);

                        return rideRealmDataStore.createOrUpdateUser(ride);
                    }
            );
        });
    }

    @Override
    public Observable<Boolean> endRide(int trip_id, Location location, int parkingId, String imageURL, boolean isReportDamage, Integer lock_battery, Integer bike_battery) {
        return this.rideNetworkDataStore.endRide(trip_id,location,parkingId,imageURL,isReportDamage, lock_battery,bike_battery);
    }


    @Override
    public Observable<UpdateTripResponse> updateRide(int trip_id, double[][] steps) {
        return this.rideNetworkDataStore.updateRide(trip_id,steps);
    }

    @Override
    public Observable<Ride> getRide() {
        return rideRealmDataStore.getRide();
    }

    @Override
    public Observable<RideSummaryResponse> getRideSummary(int trip_id) {
        return this.rideNetworkDataStore.getRideSummary(trip_id);
    }

    @Override
    public Observable<Boolean> rateRide(int trip_id, int rating) {
        return this.rideNetworkDataStore.rateRide(trip_id,rating);
    }

    @Override
    public Observable<Ride> saveRide(Ride ride) {
        return rideRealmDataStore.createOrUpdateUser(ride);
    }


    @Override
    public Observable<Boolean> deleteRide() {
        return rideRealmDataStore.deleteRide();
    }


    @Override
    public Observable<RideHistoryResponse> getRideHistory() {
        return rideNetworkDataStore.getRideHistory();
    }

    long getDoubleTime() {
        Date dte = new Date();
        return (long)dte.getTime()/1000;
    }
}
