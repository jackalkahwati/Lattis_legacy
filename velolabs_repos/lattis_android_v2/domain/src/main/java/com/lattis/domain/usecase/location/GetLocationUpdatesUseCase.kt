package com.lattis.domain.usecase.location

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.LocationRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Location
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import javax.inject.Inject


/**
 * Created by raverat on 2/23/17.
 */

class GetLocationUpdatesUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val locationRepository: LocationRepository
) : UseCase<Location>(threadExecutor, postExecutionThread) {


    var freshLoctionData =false

    fun withFreshLocationData(freshLoctionData:Boolean):GetLocationUpdatesUseCase{
        this.freshLoctionData=freshLoctionData
        return this
    }


    override fun buildUseCaseObservable(): Observable<Location> {
        return this.locationRepository.getLocationUpdates(freshLoctionData)
    }

    override fun execute(UseCaseSubscriber: DisposableObserver<Location>): Disposable {
        return this.buildUseCaseObservable()
                .subscribeWith(UseCaseSubscriber)
    }


}
