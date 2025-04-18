package com.lattis.ellipse.domain.interactor.ride;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.repository.RideRepository;

import javax.inject.Inject;

import io.reactivex.Observable;


public class StartRideUseCase extends UseCase<Ride>

    {

        private RideRepository rideRepository;

        private Bike bike;
        private Location location;
        private boolean first_lock_connect;


    @Inject
    public StartRideUseCase(ThreadExecutor threadExecutor,
                            PostExecutionThread postExecutionThread,
                            RideRepository rideRepository) {
        super(threadExecutor, postExecutionThread);
        this.rideRepository = rideRepository;
    }

    public StartRideUseCase withBike(Bike bike) {
        this.bike = bike;
        return this;
    }

    public StartRideUseCase withLocation(Location location) {
        this.location = location;
        return this;
    }

    public StartRideUseCase withFirstLockConnect(boolean first_lock_connect) {
            this.first_lock_connect = first_lock_connect;
            return this;
    }


    @Override
    protected Observable<Ride> buildUseCaseObservable() {
        return this.rideRepository.startRide(bike,location,first_lock_connect);
    }
}
