package com.lattis.domain.usecase.parking

import com.lattis.domain.models.GeoFence
import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.ParkingRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetGeoFenceUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val parkingRepository: ParkingRepository
) : UseCase<List<GeoFence>>(threadExecutor, postExecutionThread) {
    private var fleetId = 0
    fun withFleetID(fleetId: Int): GetGeoFenceUseCase {
        this.fleetId = fleetId
        return this
    }

    override fun buildUseCaseObservable(): Observable<List<GeoFence>> {
        return parkingRepository.getGeoFences(fleetId)
    }

}