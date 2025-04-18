package com.lattis.ellipse.domain.interactor.bike;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.SearchBike;
import com.lattis.ellipse.domain.repository.BikeRepository;

import javax.inject.Inject;

import io.reactivex.Observable;


public class FindBikesUseCase extends UseCase<SearchBike> {

    private BikeRepository bikeRepository;

    private double latitude;
    private double longitude;


    @Inject
    public FindBikesUseCase(ThreadExecutor threadExecutor,
                                  PostExecutionThread postExecutionThread,
                                  BikeRepository bikeRepository) {
        super(threadExecutor, postExecutionThread);
        this.bikeRepository = bikeRepository;
    }

    public FindBikesUseCase withLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }
    public FindBikesUseCase withLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    @Override
    protected Observable<SearchBike> buildUseCaseObservable() {
        return bikeRepository.findBikes(latitude,longitude);
    }

}

