package com.lattis.ellipse.domain.interactor.history;

import com.lattis.ellipse.data.network.model.response.history.RideHistoryResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.RideRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 8/16/17.
 */

public class GetRideHistorUseCase extends UseCase<RideHistoryResponse>

{

    private RideRepository rideRepository;


    @Inject
    public GetRideHistorUseCase(ThreadExecutor threadExecutor,
                             PostExecutionThread postExecutionThread,
                             RideRepository rideRepository) {
        super(threadExecutor, postExecutionThread);
        this.rideRepository = rideRepository;
    }


    @Override
    protected Observable<RideHistoryResponse> buildUseCaseObservable() {
        return this.rideRepository.getRideHistory();
    }
}
