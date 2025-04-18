package com.lattis.ellipse.domain.interactor.parking;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.domain.repository.ParkingRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;


public class GetParkingsForFleetUseCase extends UseCase<List<Parking>> {

    private ParkingRepository parkingRepository;

    private int fleetId;

    @Inject
    public GetParkingsForFleetUseCase(ThreadExecutor threadExecutor,
                                      PostExecutionThread postExecutionThread,
                                      ParkingRepository parkingRepository) {
        super(threadExecutor, postExecutionThread);
        this.parkingRepository = parkingRepository;
    }

    public GetParkingsForFleetUseCase withFleetId(int fleetId) {
        this.fleetId = fleetId;
        return this;
    }

    @Override
    protected Observable<List<Parking>> buildUseCaseObservable() {
        return parkingRepository.getParkingSpotsForFleet(fleetId);
    }

}

