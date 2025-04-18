package com.lattis.ellipse.domain.interactor.ride;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.repository.RideRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/13/17.
 */

public class SaveRideUseCase extends UseCase<Ride>

{

    private RideRepository rideRepository;
    private Ride ride;

    @Inject
    public SaveRideUseCase(ThreadExecutor threadExecutor,
                           PostExecutionThread postExecutionThread,
                           RideRepository rideRepository) {
        super(threadExecutor, postExecutionThread);
        this.rideRepository = rideRepository;
    }

    public SaveRideUseCase withRide(Ride ride) {
        this.ride = ride;
        return this;
    }


    @Override
    protected Observable<Ride> buildUseCaseObservable() {
        return this.rideRepository.saveRide(ride);
    }
}
