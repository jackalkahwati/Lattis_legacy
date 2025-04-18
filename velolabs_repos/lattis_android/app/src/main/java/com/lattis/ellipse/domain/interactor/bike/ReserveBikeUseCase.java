package com.lattis.ellipse.domain.interactor.bike;


import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.repository.BikeRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ReserveBikeUseCase extends UseCase<Ride>{

    private BikeRepository bikeRepository;
    private Bike bike;
    private boolean by_scan;
    private double latitude;
    private double longitude;

    @Inject
    public ReserveBikeUseCase(ThreadExecutor threadExecutor,
                              PostExecutionThread postExecutionThread,
                              BikeRepository bikeRepository) {
        super(threadExecutor, postExecutionThread);
        this.bikeRepository = bikeRepository;
    }


    public ReserveBikeUseCase withBike(Bike bike) {
        this.bike = bike;
        return this;
    }

    public ReserveBikeUseCase withScanStatus(boolean by_scan) {
        this.by_scan = by_scan;
        return this;
    }

    public ReserveBikeUseCase withLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public ReserveBikeUseCase withLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    @Override
    protected Observable<Ride> buildUseCaseObservable() {
        return bikeRepository.bookBike(bike, by_scan, latitude, longitude);
    }
}
