package com.lattis.domain.usecase.location

import com.google.android.libraries.places.api.model.Place
import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.LocationRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import javax.inject.Inject


class GetPlaceBufferUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val locationRepository: LocationRepository
) : UseCase<Place>(threadExecutor, postExecutionThread) {
    private lateinit var placeId: String

    fun withPlaceId(placeId: String): GetPlaceBufferUseCase {
        this.placeId = placeId
        return this
    }

    override fun buildUseCaseObservable(): Observable<Place> {
        return this.locationRepository.getPlaceBuffer(placeId)
    }

    override fun execute(UseCaseSubscriber: DisposableObserver<Place>): Disposable {
        return this.buildUseCaseObservable()
                .subscribeWith(UseCaseSubscriber)
    }


}

