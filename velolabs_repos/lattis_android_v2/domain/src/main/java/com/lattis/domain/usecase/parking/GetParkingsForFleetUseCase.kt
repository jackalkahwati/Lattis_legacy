package com.lattis.domain.usecase.parking

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.ParkingRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Parking
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetParkingsForFleetUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val parkingRepository: ParkingRepository
) : UseCase<List<Parking>>(threadExecutor, postExecutionThread) {
    private var fleetId = 0
    fun withFleetId(fleetId: Int): GetParkingsForFleetUseCase {
        this.fleetId = fleetId
        return this
    }

    override fun buildUseCaseObservable(): Observable<List<Parking>> {
        return parkingRepository.getParkingSpotsForFleet(fleetId)
    }

}