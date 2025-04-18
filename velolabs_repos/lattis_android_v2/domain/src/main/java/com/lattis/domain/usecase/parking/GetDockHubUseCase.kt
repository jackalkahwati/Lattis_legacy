package com.lattis.domain.usecase.parking

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.DockHub
import com.lattis.domain.models.Location
import com.lattis.domain.repository.ParkingRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetDockHubUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val parkingRepository: ParkingRepository
) : UseCase<List<DockHub>>(threadExecutor, postExecutionThread) {

    private var location: Location? = null
    private var bikeId =0

    fun withBikeId(bikeId: Int): GetDockHubUseCase {
        this.bikeId = bikeId
        return this
    }

    fun withLocation(location: Location): GetDockHubUseCase {
        this.location = location
        return this
    }

    override fun buildUseCaseObservable(): Observable<List<DockHub>> {
        return this.parkingRepository.getDockHub(bikeId,location!!)
    }
}