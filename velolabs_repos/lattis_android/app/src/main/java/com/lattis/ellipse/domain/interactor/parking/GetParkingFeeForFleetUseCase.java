package com.lattis.ellipse.domain.interactor.parking;

import com.lattis.ellipse.data.network.model.response.parking.GetParkingFeeForFleetResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.repository.ParkingRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 8/1/17.
 */

public class GetParkingFeeForFleetUseCase extends UseCase<GetParkingFeeForFleetResponse> {

    private ParkingRepository parkingRepository;
    private Location location;
    private int fleetId;

    @Inject
    public GetParkingFeeForFleetUseCase(ThreadExecutor threadExecutor,
                                      PostExecutionThread postExecutionThread,
                                      ParkingRepository parkingRepository) {
        super(threadExecutor, postExecutionThread);
        this.parkingRepository = parkingRepository;
    }

    public GetParkingFeeForFleetUseCase withFleetId(int fleetId) {
        this.fleetId = fleetId;
        return this;
    }

    public GetParkingFeeForFleetUseCase withLocation(Location location) {
        this.location = location;
        return this;
    }

    @Override
    protected Observable<GetParkingFeeForFleetResponse> buildUseCaseObservable() {
        return parkingRepository.getParkingFeeForFleet(location,fleetId);
    }
}
