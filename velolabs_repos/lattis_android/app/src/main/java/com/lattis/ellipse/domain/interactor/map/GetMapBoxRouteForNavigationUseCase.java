package com.lattis.ellipse.domain.interactor.map;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.repository.LocationRepository;
import com.mapbox.api.directions.v5.models.DirectionsRoute;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 6/16/17.
 */

public class GetMapBoxRouteForNavigationUseCase extends UseCase<DirectionsRoute> {

    private LocationRepository locationRepository;

    private Location fromLocation;
    private Location toLocation;

    @Inject
    protected GetMapBoxRouteForNavigationUseCase(ThreadExecutor threadExecutor,
                                    PostExecutionThread postExecutionThread,
                                    LocationRepository locationRepository) {
        super(threadExecutor, postExecutionThread);
        this.locationRepository = locationRepository;
    }

    public GetMapBoxRouteForNavigationUseCase from(Location location) {
        fromLocation = location;
        return this;
    }

    public GetMapBoxRouteForNavigationUseCase to(Location location) {
        toLocation = location;
        return this;
    }

    @Override
    protected Observable<DirectionsRoute> buildUseCaseObservable() {
//        return locationRepository.getMapBoxRouteForNavigation(fromLocation, toLocation);
        return Observable.just(null);
    }

}
