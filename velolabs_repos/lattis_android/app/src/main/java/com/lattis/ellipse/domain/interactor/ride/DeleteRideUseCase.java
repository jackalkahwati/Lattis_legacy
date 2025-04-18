package com.lattis.ellipse.domain.interactor.ride;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.RideRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 7/19/17.
 */

public class DeleteRideUseCase extends UseCase<Boolean>

{

    private RideRepository rideRepository;


    @Inject
    public DeleteRideUseCase(ThreadExecutor threadExecutor,
                          PostExecutionThread postExecutionThread,
                          RideRepository rideRepository) {
        super(threadExecutor, postExecutionThread);
        this.rideRepository = rideRepository;
    }


    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return this.rideRepository.deleteRide();
    }
}
