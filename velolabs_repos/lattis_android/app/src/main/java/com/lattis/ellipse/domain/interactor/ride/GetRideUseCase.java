package com.lattis.ellipse.domain.interactor.ride;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.repository.RideRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/4/17.
 */

public class GetRideUseCase extends UseCase<Ride>

{

    private RideRepository rideRepository;


    @Inject
    public GetRideUseCase(ThreadExecutor threadExecutor,
                            PostExecutionThread postExecutionThread,
                            RideRepository rideRepository) {
        super(threadExecutor, postExecutionThread);
        this.rideRepository = rideRepository;
    }


    @Override
    protected Observable<Ride> buildUseCaseObservable() {
        return this.rideRepository.getRide();
    }
}
