package io.bikes.sandypedals.domain.interactor.updatetrip;

import io.bikes.sandypedals.domain.executor.PostExecutionThread;
import io.bikes.sandypedals.domain.executor.ThreadExecutor;
import io.bikes.sandypedals.domain.interactor.UseCase;
import io.bikes.sandypedals.presentation.ui.ride.service.util.ServiceAction;

import javax.inject.Inject;

import io.reactivex.Observable;

public class StopGetTripDetailsThreadIfApplicableUseCase extends UseCase<Boolean>

{
    private ServiceAction serviceAction;

    @Inject
    public StopGetTripDetailsThreadIfApplicableUseCase(ThreadExecutor threadExecutor,
                                                       PostExecutionThread postExecutionThread,
                                                       ServiceAction serviceAction) {
        super(threadExecutor, postExecutionThread);
        this.serviceAction = serviceAction;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return serviceAction.stopGetTripDetailsThreadIfApplicable();
    }
}

