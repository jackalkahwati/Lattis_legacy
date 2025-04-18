package com.lattis.domain.usecase.parking

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.ParkingFeeForFleet
import com.lattis.domain.repository.ParkingRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Location
import com.lattis.domain.models.Ride
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetParkingFeeForFleetUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val parkingRepository: ParkingRepository
) : UseCase<ParkingFeeForFleet>(threadExecutor, postExecutionThread) {

    private var location: Location? = null
    private var fleetId = 0

    fun withFleetId(fleetId: Int): GetParkingFeeForFleetUseCase {
        this.fleetId = fleetId
        return this
    }

    fun withLocation(location: Location): GetParkingFeeForFleetUseCase {
        this.location = location
        return this
    }

    override fun buildUseCaseObservable(): Observable<ParkingFeeForFleet> {
        return this.parkingRepository.getParkingFeeForFleet(location!!,fleetId)
    }
}