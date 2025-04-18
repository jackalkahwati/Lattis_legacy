package com.lattis.ellipse.domain.interactor.parking;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.domain.repository.ParkingRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;


public class FindParkingsUseCase extends UseCase<List<Parking>> {

    private ParkingRepository parkingRepository;

    private double latitude;
    private double longitude;
    private int fleetId;
    private float rangeInMiles;

    @Inject
    public FindParkingsUseCase(ThreadExecutor threadExecutor,
                               PostExecutionThread postExecutionThread,
                               ParkingRepository parkingRepository) {
        super(threadExecutor, postExecutionThread);
        this.parkingRepository = parkingRepository;
    }

    public FindParkingsUseCase withLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }
    public FindParkingsUseCase withLongitude(double longitude) {

        this.longitude = longitude;
        return this;
    }

    public FindParkingsUseCase withRangeInMiles(float rangeInMiles) {
        this.rangeInMiles = rangeInMiles;
        return this;
    }

    @Override
    protected Observable<List<Parking>> buildUseCaseObservable() {
        return parkingRepository.findParkings(latitude,longitude,rangeInMiles);
    }

}

