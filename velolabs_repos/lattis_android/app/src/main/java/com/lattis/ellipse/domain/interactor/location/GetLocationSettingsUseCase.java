package com.lattis.ellipse.domain.interactor.location;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.LocationSettingsResult;
import com.lattis.ellipse.domain.repository.LocationRepository;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class GetLocationSettingsUseCase extends UseCase<LocationSettingsResult> {

    private LocationRepository locationRepository;

    @Inject
    protected GetLocationSettingsUseCase(ThreadExecutor threadExecutor,
                                        PostExecutionThread postExecutionThread,
                                        LocationRepository locationRepository) {
        super(threadExecutor, postExecutionThread);
        this.locationRepository = locationRepository;
    }

    @Override
    protected Observable<LocationSettingsResult> buildUseCaseObservable() {
        return this.locationRepository.getLocationSettings();
    }

    @Override
    public Disposable execute(DisposableObserver<LocationSettingsResult> UseCaseSubscriber) {
        return this.buildUseCaseObservable()
                .subscribeWith(UseCaseSubscriber);
    }

}
