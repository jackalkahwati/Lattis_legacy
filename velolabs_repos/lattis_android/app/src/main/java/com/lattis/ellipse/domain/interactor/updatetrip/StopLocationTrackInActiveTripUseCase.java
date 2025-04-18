package com.lattis.ellipse.domain.interactor.updatetrip;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.presentation.model.LockModel;
import com.lattis.ellipse.presentation.ui.ride.service.util.ServiceAction;
import com.lattis.ellipse.presentation.ui.ride.service.util.UpdateTripData;

import javax.inject.Inject;

import io.reactivex.Observable;

public class StopLocationTrackInActiveTripUseCase extends UseCase<Boolean> {
    private int trip_id = 0;
    private LockModel lockModel;
    private ServiceAction serviceAction;

    @Inject
    public StopLocationTrackInActiveTripUseCase(ThreadExecutor threadExecutor,
                                                PostExecutionThread postExecutionThread,
                                                ServiceAction serviceAction) {
        super(threadExecutor, postExecutionThread);
        this.serviceAction = serviceAction;
    }

    public StopLocationTrackInActiveTripUseCase withTripId(int trip_id) {
        this.trip_id = trip_id;
        return this;
    }


    public StopLocationTrackInActiveTripUseCase withLockModel(LockModel lockModel) {
        this.lockModel = lockModel;
        return this;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return serviceAction.stopLocationTracking();
    }
}
