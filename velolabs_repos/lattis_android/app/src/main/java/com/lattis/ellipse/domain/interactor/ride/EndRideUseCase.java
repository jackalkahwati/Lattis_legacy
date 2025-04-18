package com.lattis.ellipse.domain.interactor.ride;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.repository.RideRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 3/23/17.
 */

public class EndRideUseCase extends UseCase<Boolean> {

    private RideRepository rideRepository;

    private int trip_id;
    private Location location;
    private int parkingId;
    private String imageURL;
    private boolean isReportDamage;
    private Integer lock_battery=null;
    private Integer bike_battery=null;

    @Inject
    public EndRideUseCase(ThreadExecutor threadExecutor,
                          PostExecutionThread postExecutionThread,
                          RideRepository rideRepository) {
        super(threadExecutor, postExecutionThread);
        this.rideRepository = rideRepository;
    }

    public EndRideUseCase withTripId(int trip_id) {
        this.trip_id = trip_id;
        return this;
    }

    public EndRideUseCase withLocation(Location location) {
        this.location = location;
        return this;
    }

    public EndRideUseCase withParkingId(int parkingId) {
        this.parkingId = parkingId;
        return this;
    }

    public EndRideUseCase withImageURL(String imageURL) {
        this.imageURL = imageURL;
        return this;
    }

    public EndRideUseCase withLockBattery(Integer lock_battery) {
        this.lock_battery = lock_battery;
        return this;
    }

    public EndRideUseCase withBikeBattery(Integer bike_battery) {
        this.bike_battery = bike_battery;
        return this;
    }


    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return this.rideRepository.endRide(trip_id, location, parkingId, imageURL,isReportDamage,lock_battery,bike_battery);
    }

    public EndRideUseCase withReportDamage(boolean isReportDamage) {
        this.isReportDamage = isReportDamage;
        return this;
    }
}
