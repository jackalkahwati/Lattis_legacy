package com.lattis.domain.usecase.reservation

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.CostEstimate
import com.lattis.domain.models.Reserve
import com.lattis.domain.repository.ReservationRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.usecase.bike.ReserveBikeUseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ReserveUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val reservationRepository: ReservationRepository
) : UseCase<Reserve>(threadExecutor, postExecutionThread) {

    private var pickUpDate: String?=null
    private var returnDate: String?=null
    private var bikeId:Int? =null
    private var pricing_option_id:Int?=null

    fun withPickUpDate(pickUpDate: String) :ReserveUseCase{
        this.pickUpDate = pickUpDate
        return this
    }

    fun withReturnDate(returnDate: String) :ReserveUseCase{
        this.returnDate = returnDate
        return this
    }

    fun withBikeId(bikeId: Int) :ReserveUseCase{
        this.bikeId = bikeId
        return this
    }

    fun withPricingOptionId(pricing_option_id:Int?): ReserveUseCase {
        this.pricing_option_id = pricing_option_id
        return this
    }

    override fun buildUseCaseObservable(): Observable<Reserve> {
        return reservationRepository.reserve(bikeId!!,pickUpDate!!,returnDate!!,pricing_option_id)
    }

}