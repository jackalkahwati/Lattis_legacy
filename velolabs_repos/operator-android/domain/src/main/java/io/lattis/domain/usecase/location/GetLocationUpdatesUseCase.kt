package io.lattis.domain.usecase.location

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Location
import io.lattis.domain.repository.LocationRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject




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
