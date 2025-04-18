package com.lattis.ellipse.domain.interactor.location;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.map.PlaceAutocomplete;
import com.lattis.ellipse.domain.repository.LocationRepository;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class GetPlacesUseCase extends UseCase<ArrayList<PlaceAutocomplete>> {

    private LocationRepository locationRepository;
    private String constraint;

    @Inject
    protected GetPlacesUseCase(ThreadExecutor threadExecutor,
                                        PostExecutionThread postExecutionThread,
                                        LocationRepository locationRepository) {
        super(threadExecutor, postExecutionThread);
        this.locationRepository = locationRepository;
    }

    public GetPlacesUseCase withConstraint(String constraint){
        this.constraint = constraint;
        return this;
    }

    @Override
    protected Observable<ArrayList<PlaceAutocomplete>> buildUseCaseObservable() {
        return this.locationRepository.getPlaces(constraint);
    }

    @Override
    public Disposable execute(DisposableObserver<ArrayList<PlaceAutocomplete>> UseCaseSubscriber) {
        return this.buildUseCaseObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(UseCaseSubscriber);
    }


}
