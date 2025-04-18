package com.lattis.domain.usecase.location


import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.LocationSettingsResult
import com.lattis.domain.repository.LocationRepository
import com.lattis.domain.usecase.base.UseCase
import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver

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
