package com.lattis.ellipse.domain.interactor.location;

import com.google.android.libraries.places.api.model.Place;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.LocationRepository;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class GetPlaceBufferUseCase extends UseCase<Place> {

    private LocationRepository locationRepository;
    private String placeId;

    @Inject
    protected GetPlaceBufferUseCase(ThreadExecutor threadExecutor,
                               PostExecutionThread postExecutionThread,
                               LocationRepository locationRepository) {
        super(threadExecutor, postExecutionThread);
        this.locationRepository = locationRepository;
    }

    public GetPlaceBufferUseCase withPlaceId(String placeId){
        this.placeId = placeId;
        return this;
    }

    @Override
    protected Observable<Place> buildUseCaseObservable() {
        return this.locationRepository.getPlaceBuffer(placeId);
    }

    @Override
    public Disposable execute(DisposableObserver<Place> UseCaseSubscriber) {
        return this.buildUseCaseObservable()
                .subscribeWith(UseCaseSubscriber);
    }


}

