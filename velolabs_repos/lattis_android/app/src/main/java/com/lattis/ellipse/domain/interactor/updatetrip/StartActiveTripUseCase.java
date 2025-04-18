package com.lattis.ellipse.domain.interactor.updatetrip;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.ride.service.util.ServiceAction;
import com.lattis.ellipse.presentation.ui.ride.service.util.UpdateTripData;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 7/28/17.
 */

public class StartActiveTripUseCase extends UseCase<UpdateTripData>

{
    private int trip_id = 0;
    private LockModel lockModel;
    private ServiceAction serviceAction;

    @Inject
    public StartActiveTripUseCase(ThreadExecutor threadExecutor,
                                  PostExecutionThread postExecutionThread,
                                  ServiceAction serviceAction) {
        super(threadExecutor, postExecutionThread);
        this.serviceAction = serviceAction;
    }

    public StartActiveTripUseCase withTripId(int trip_id) {
        this.trip_id = trip_id;
        return this;
    }

    @Override
    protected Observable<UpdateTripData> buildUseCaseObservable() {
        return serviceAction.startActiveTripService(trip_id);
    }
}
