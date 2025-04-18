package com.lattis.ellipse.domain.interactor.maintenance;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.MaintenanceRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by Velo Labs Android on 06-04-2017.
 */

public class DamageBikeUseCase extends UseCase<BasicResponse> {

    private MaintenanceRepository maintenanceRepository;
    private int bikeId;
    private int tripId;
    private String maintenanceImage,riderNotes,category;

    @Inject
    protected DamageBikeUseCase(ThreadExecutor threadExecutor,
                                PostExecutionThread postExecutionThread,
                                MaintenanceRepository maintenanceRepository) {
        super(threadExecutor, postExecutionThread);
        this.maintenanceRepository = maintenanceRepository;
    }

    @Override
    protected Observable<BasicResponse> buildUseCaseObservable() {
        return maintenanceRepository.damageBikes(category,riderNotes,
                bikeId,maintenanceImage,tripId);
    }

    public DamageBikeUseCase withTripId(int tripId) {
        this.tripId = tripId;
        return this;
    }

    public DamageBikeUseCase withBikeId(int bikeId) {
        this.bikeId = bikeId;
        return this;
    }

    public DamageBikeUseCase  withCategory(String category) {
        this.category = category;
        return this;
    }

    public DamageBikeUseCase withMaintenanceImage(String maintenance_image) {
        this.maintenanceImage = maintenance_image;
        return this;
    }

    public DamageBikeUseCase withRiderNotes(String rider_notes) {
        this.riderNotes = rider_notes;
        return this;
    }
}
