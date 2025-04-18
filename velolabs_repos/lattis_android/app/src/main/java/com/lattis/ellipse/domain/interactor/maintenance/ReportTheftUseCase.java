package com.lattis.ellipse.domain.interactor.maintenance;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.MaintenanceRepository;

import javax.inject.Inject;

import io.reactivex.Observable;



public class ReportTheftUseCase extends UseCase<BasicResponse> {
    private int bikeId;
    private int tripId=-1;
    private MaintenanceRepository maintenanceRepository;
    @Inject
    protected ReportTheftUseCase(ThreadExecutor threadExecutor,
                                PostExecutionThread postExecutionThread,
                                MaintenanceRepository maintenanceRepository) {
        super(threadExecutor, postExecutionThread);
        this.maintenanceRepository = maintenanceRepository;
    }

    @Override
    protected Observable<BasicResponse> buildUseCaseObservable() {
        return maintenanceRepository.reportBikeTheft(bikeId,tripId);

    }

    public ReportTheftUseCase withBikeId(int bikeId) {
        this.bikeId = bikeId;
        return this;
    }

    public ReportTheftUseCase withTripId(int tripId) {
        this.tripId = tripId;
        return this;
    }

}
