package com.lattis.ellipse.domain.interactor.location;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.repository.LocationRepository;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by raverat on 2/23/17.
 */

public class GetLocationUpdatesUseCase extends UseCase<Location> {

    private LocationRepository locationRepository;

    @Inject
    protected GetLocationUpdatesUseCase(ThreadExecutor threadExecutor,
                                        PostExecutionThread postExecutionThread,
                                        LocationRepository locationRepository) {
        super(threadExecutor, postExecutionThread);
        this.locationRepository = locationRepository;
    }

    @Override
    protected Observable<Location> buildUseCaseObservable() {
        return this.locationRepository.getLocationUpdates();
    }

    @Override
    public Disposable execute(DisposableObserver<Location> UseCaseSubscriber) {
        return this.buildUseCaseObservable()
                .subscribeWith(UseCaseSubscriber);
    }


}
