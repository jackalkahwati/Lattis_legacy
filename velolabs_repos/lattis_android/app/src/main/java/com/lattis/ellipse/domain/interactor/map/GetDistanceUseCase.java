package com.lattis.ellipse.domain.interactor.map;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.repository.LocationRepository;
import com.lattis.ellipse.presentation.dagger.qualifier.BikeNumberForSearch;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 8/21/17.
 */

public class GetDistanceUseCase extends UseCase<List<Bike>> {

    private LocationRepository locationRepository;

    private Location fromLocation;
    private List<Bike> bikes;
    private Integer bikeNumberForSearch;

    @Inject
    protected GetDistanceUseCase(ThreadExecutor threadExecutor,
                                  PostExecutionThread postExecutionThread,
                                  LocationRepository locationRepository,
                                 @BikeNumberForSearch Integer bikeNumberForSearch) {
        super(threadExecutor, postExecutionThread);
        this.locationRepository = locationRepository;
        this.bikeNumberForSearch = bikeNumberForSearch;
    }

    public GetDistanceUseCase from(Location location) {
        fromLocation = location;
        return this;
    }

    public GetDistanceUseCase forBikes(List<Bike> bikes) {
        this.bikes = bikes;
        return this;
    }

    @Override
    protected Observable<List<Bike>> buildUseCaseObservable() {
        return locationRepository.getDistanceForBikes(fromLocation,bikes).flatMap( bikes -> returnBikeNumber(bikes));
    }

    private Observable<List<Bike>> returnBikeNumber(List<Bike> bikes){
        if(bikes ==null || bikes.size() ==0){
            return Observable.just(bikes);
        }else if(bikeNumberForSearch >= bikes.size()){
            return Observable.just(bikes);
        }else{
            return Observable.just(bikes.subList(0,bikeNumberForSearch));
        }
    }

}
