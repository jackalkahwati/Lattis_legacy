package com.lattis.ellipse.domain.interactor.updatetrip;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.presentation.ui.ride.service.util.ServiceAction;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 7/28/17.
 */

public class StopActiveTripUseCase extends UseCase<Boolean>

{
    private ServiceAction serviceAction;

    @Inject
    public StopActiveTripUseCase(ThreadExecutor threadExecutor,
                                 PostExecutionThread postExecutionThread,
                                 ServiceAction serviceAction) {
        super(threadExecutor, postExecutionThread);
        this.serviceAction = serviceAction;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return serviceAction.stopActiveTripService();
    }
}
