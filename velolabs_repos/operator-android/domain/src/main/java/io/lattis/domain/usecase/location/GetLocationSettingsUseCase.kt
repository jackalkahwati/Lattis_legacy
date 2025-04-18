package io.lattis.domain.usecase.location


import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.LocationSettingsResult
import io.lattis.domain.repository.LocationRepository
import io.lattis.domain.usecase.base.UseCase
import javax.inject.Inject

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver

class GetLocationSettingsUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val locationRepository: LocationRepository
) : UseCase<LocationSettingsResult>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<LocationSettingsResult> {
        return this.locationRepository.getLocationSettings()
    }

    override fun execute(UseCaseSubscriber: DisposableObserver<LocationSettingsResult>): Disposable {
        return this.buildUseCaseObservable()
                .subscribeWith(UseCaseSubscriber)
    }

}
