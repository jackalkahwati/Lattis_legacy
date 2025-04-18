package com.lattis.ellipse.domain.interactor.map;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.LocationRepository;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.light.Position;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;



public class GetMapBoxRouteMatcherUseCase extends UseCase<List<LatLng>> {

    private LocationRepository locationRepository;

    Position[] coordinates;
    @Inject
    protected GetMapBoxRouteMatcherUseCase(ThreadExecutor threadExecutor,
                                           PostExecutionThread postExecutionThread,
                                           LocationRepository locationRepository) {
        super(threadExecutor, postExecutionThread);
        this.locationRepository = locationRepository;
    }



    @Override
    protected Observable<List<LatLng>> buildUseCaseObservable() {
//        return locationRepository.getMapBoxRouteMatcher(coordinates);
        return Observable.just(null);
    }

    public  GetMapBoxRouteMatcherUseCase add(Position[] coordinates) {
        this.coordinates = coordinates;
        return this;
    }
}
