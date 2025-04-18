package com.lattis.domain.usecase.parking

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.ParkingRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.ParkingZone
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetParkingZoneUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val parkingRepository: ParkingRepository
) : UseCase<List<ParkingZone>>(threadExecutor, postExecutionThread) {
    private var fleetId = 0
    fun withFleetID(fleetId: Int): GetParkingZoneUseCase {
        this.fleetId = fleetId
        return this
    }

    override fun buildUseCaseObservable(): Observable<List<ParkingZone>> {
        return parkingRepository.getParkingZone(fleetId)
    }

}