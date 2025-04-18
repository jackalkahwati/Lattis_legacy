package com.lattis.ellipse.domain.interactor.map;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.repository.LocationRepository;
import com.mapbox.mapboxsdk.geometry.LatLng;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 3/29/17.
 */

public class GetMapBoxRouteUseCase extends UseCase<LatLng[]> {

    private LocationRepository locationRepository;

    private Location fromLocation;
    private Location toLocation;
    private String directionProfile;

    @Inject
    protected GetMapBoxRouteUseCase(ThreadExecutor threadExecutor,
                                    PostExecutionThread postExecutionThread,
                                    LocationRepository locationRepository) {
        super(threadExecutor, postExecutionThread);
        this.locationRepository = locationRepository;
    }

    public GetMapBoxRouteUseCase from(Location location) {
        fromLocation = location;
        return this;
    }

    public GetMapBoxRouteUseCase to(Location location) {
        toLocation = location;
        return this;
    }

    @Override
    protected Observable<LatLng[]> buildUseCaseObservable() {
//        return locationRepository.getMapBoxRouteBtw(fromLocation, toLocation, directionProfile);
        return Observable.just(null);
    }

    public GetMapBoxRouteUseCase setDirectionProfile(String directionProfile) {
        this.directionProfile = directionProfile;
        return this;

    }
}
