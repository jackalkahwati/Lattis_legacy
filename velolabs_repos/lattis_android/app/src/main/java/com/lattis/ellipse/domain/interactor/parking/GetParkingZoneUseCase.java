package com.lattis.ellipse.domain.interactor.parking;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.domain.repository.ParkingZoneRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by lattis on 24/05/17.
 */

public class GetParkingZoneUseCase extends UseCase {
    private int fleetId;
    private ParkingZoneRepository parkingZoneRepository;


    @Inject
    public GetParkingZoneUseCase(ThreadExecutor threadExecutor,
                                 PostExecutionThread postExecutionThread,
                                 ParkingZoneRepository parkingZoneRepository) {
        super(threadExecutor, postExecutionThread);
        this.parkingZoneRepository = parkingZoneRepository;
    }


    public GetParkingZoneUseCase withFleetID(int fleetId) {
        this.fleetId = fleetId;
        return this;
    }

    @Override
    protected Observable<List<ParkingZone>> buildUseCaseObservable() {
        return parkingZoneRepository.getParkingZone(fleetId);
    }

}
