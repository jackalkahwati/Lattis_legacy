package com.lattis.domain.usecase.location

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.map.PlaceAutocomplete
import com.lattis.domain.repository.LocationRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


class GetPlacesUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val locationRepository: LocationRepository
) : UseCase<ArrayList<PlaceAutocomplete>>(threadExecutor, postExecutionThread) {
    private lateinit var constraint: String

    fun withConstraint(constraint: String): GetPlacesUseCase {
        this.constraint = constraint
        return this
    }

    override fun buildUseCaseObservable(): Observable<ArrayList<PlaceAutocomplete>> {
        return this.locationRepository.getPlaces(constraint)
    }

    override fun execute(UseCaseSubscriber: DisposableObserver<ArrayList<PlaceAutocomplete>>): Disposable {
        return this.buildUseCaseObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(UseCaseSubscriber)
    }


}
