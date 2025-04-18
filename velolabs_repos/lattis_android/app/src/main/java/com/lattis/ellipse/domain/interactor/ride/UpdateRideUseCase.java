package com.lattis.ellipse.domain.interactor.ride;

import com.lattis.ellipse.data.network.model.response.ride.UpdateTripResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.RideRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 3/30/17.
 */

public class UpdateRideUseCase extends UseCase<UpdateTripResponse> {

    private RideRepository rideRepository;

    private int trip_id;
    private double[][] steps;


    @Inject
    public UpdateRideUseCase(ThreadExecutor threadExecutor,
                             PostExecutionThread postExecutionThread,
                             RideRepository rideRepository) {
        super(threadExecutor, postExecutionThread);
        this.rideRepository = rideRepository;
    }

    public UpdateRideUseCase withTripId(int trip_id) {
        this.trip_id = trip_id;
        return this;
    }

    public UpdateRideUseCase withSteps(double[][] steps) {
        this.steps = steps;
        return this;
    }


    @Override
    protected Observable<UpdateTripResponse> buildUseCaseObservable() {
        return this.rideRepository.updateRide(trip_id, steps);
    }
}

