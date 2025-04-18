package com.lattis.ellipse.domain.interactor.ride;

import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.RideRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/11/17.
 */

public class RideSummaryUseCase extends UseCase<RideSummaryResponse>

{

    private RideRepository rideRepository;
    private int trip_id;


    @Inject
    public RideSummaryUseCase(ThreadExecutor threadExecutor,
                          PostExecutionThread postExecutionThread,
                          RideRepository rideRepository) {
        super(threadExecutor, postExecutionThread);
        this.rideRepository = rideRepository;
    }
    public RideSummaryUseCase withTripId(int trip_id) {
        this.trip_id = trip_id;
        return this;
    }

    @Override
    protected Observable<RideSummaryResponse> buildUseCaseObservable() {
        return this.rideRepository.getRideSummary(trip_id);
    }
}

