package com.lattis.domain.usecase.reservation

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.repository.ReservationRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Location
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetAvailableVehiclesUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val reservationRepository: ReservationRepository
) : UseCase<List<Bike>>(threadExecutor, postExecutionThread) {

    private var pickUpDate: String?=null
    private var returnDate: String?=null
    private var fleetId:Int? =null

    fun withPickUpDate(pickUpDate: String) :GetAvailableVehiclesUseCase{
        this.pickUpDate = pickUpDate
        return this
    }

    fun withReturnDate(returnDate: String) :GetAvailableVehiclesUseCase{
        this.returnDate = returnDate
        return this
    }

    fun withFleetId(fleetId: Int) :GetAvailableVehiclesUseCase{
        this.fleetId = fleetId
        return this
    }

    override fun buildUseCaseObservable(): Observable<List<Bike>> {
        return reservationRepository.getAvailableVehicles(fleetId!!,pickUpDate!!,returnDate!!)
    }

}