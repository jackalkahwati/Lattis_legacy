package com.lattis.ellipse.data;

import android.util.Log;

import com.lattis.ellipse.data.database.RideRealmDataStore;
import com.lattis.ellipse.data.network.store.BikeNetworkDataStore;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.model.SearchBike;
import com.lattis.ellipse.domain.repository.BikeRepository;
import com.lattis.ellipse.domain.repository.RideRepository;
import com.lattis.ellipse.presentation.dagger.qualifier.FleetId;
import com.lattis.ellipse.presentation.model.mapper.BikeModelMapper;

import javax.inject.Inject;

import io.reactivex.Observable;

public class BikeDataRepository implements BikeRepository{

    private BikeNetworkDataStore bikeNetworkDataStore;
    private RideRealmDataStore rideRealmDataStore;
    private RideRepository rideRepository;
    private String fleetId;
    private BikeModelMapper bikeModelMapper;

    @Inject
    public BikeDataRepository(BikeNetworkDataStore bikeNetworkDataStore,
                              RideRealmDataStore rideRealmDataStore,
                              @FleetId String fleetId,
                              BikeModelMapper bikeModelMapper,
                              RideRepository rideRepository) {
        this.bikeNetworkDataStore = bikeNetworkDataStore;
        this.rideRealmDataStore = rideRealmDataStore;
        this.bikeModelMapper = bikeModelMapper;
        this.rideRepository = rideRepository;
        this.fleetId = fleetId;
    }


    @Override
    public Observable<SearchBike> findBikes(double latitude, double longitude) {
        return bikeNetworkDataStore.findBikes(latitude,longitude);
    }

    @Override
    public Observable<Ride> bookBike(Bike bike,boolean by_scan,double latitude,double longitude) {
        Ride ride = bikeModelMapper.mapIn(bike);
        return rideRepository.getRide().flatMap(oldRide -> {
            ride.setBike_on_call_operator(oldRide.getBike_on_call_operator());
            ride.setSupport_phone(oldRide.getSupport_phone());
            return bikeNetworkDataStore.bookBike(bike.getBike_id(), by_scan, latitude, longitude).flatMap(
                    reserveBikeResponse -> {
                        ride.setId(fleetId);
                        ride.setBikeId(bike.getBike_id());
                        ride.setBike_booked_on(reserveBikeResponse.getReserveBikeResponse().getBooked_on());
                        ride.setBike_expires_in(reserveBikeResponse.getReserveBikeResponse().getExpires_in());

                        if (reserveBikeResponse.getReserveBikeResponse().getOn_call_operator() == null) {
                            Log.e("BikeDataRepository", "######bookBike::NULL");
                        } else if (reserveBikeResponse.getReserveBikeResponse().getOn_call_operator().equalsIgnoreCase("null") || reserveBikeResponse.getReserveBikeResponse().getOn_call_operator().equalsIgnoreCase("undefined")) {
                            Log.e("BikeDataRepository", "######bookBike::'NULL'");
                        } else {
                            ride.setBike_on_call_operator(reserveBikeResponse.getReserveBikeResponse().getOn_call_operator());
                            Log.e("BikeDataRepository", "######bookBike::NOT 'NULL'");
                        }
                        return rideRealmDataStore.createOrUpdateUser(ride);
                    }
            );
        });
    }

    @Override
    public Observable<Boolean> cancelBike(int bike_id,boolean bike_damaged,boolean lockIssue) {
        return bikeNetworkDataStore.cancelBike(bike_id, bike_damaged,lockIssue);
    }


    @Override
    public Observable<Bike> bikeDetails(int bike_id, int qr_code_id) {
        return bikeNetworkDataStore.bikeDetails(bike_id, qr_code_id);
    }

    @Override
    public Observable<Boolean> updateBikeMetaData(int bike_Id, int bike_battery_level, int lock_battery_level, String firmware_version, Boolean shackle_jam) {
        return bikeNetworkDataStore.updateBikeMetaData(bike_Id,bike_battery_level, lock_battery_level, firmware_version, shackle_jam)
                .flatMap(aVoid -> {
                    return Observable.just(true);
                });
    }



}
