package com.lattis.ellipse.domain.interactor.ride;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.RideRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/11/17.
 */

public class RideRatingUseCase extends UseCase<Boolean> {

    private RideRepository rideRepository;

    private int trip_id;
    private int rating;


    @Inject
    public RideRatingUseCase(ThreadExecutor threadExecutor,
                          PostExecutionThread postExecutionThread,
                          RideRepository rideRepository) {
        super(threadExecutor, postExecutionThread);
        this.rideRepository = rideRepository;
    }

    public RideRatingUseCase withTripId(int trip_id) {
        this.trip_id = trip_id;
        return this;
    }

    public RideRatingUseCase withRating(int rating) {
        this.rating = rating;
        return this;
    }


    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return this.rideRepository.rateRide(trip_id,rating);
    }
}

