package com.lattis.domain.usecase.reservation

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.CostEstimate
import com.lattis.domain.repository.ReservationRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetCostEstimateUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val reservationRepository: ReservationRepository
) : UseCase<CostEstimate>(threadExecutor, postExecutionThread) {

    private var pickUpDate: String?=null
    private var returnDate: String?=null
    private var bikeId:Int? =null
    private var pricing_option_id:Int?=null

    fun withPickUpDate(pickUpDate: String) :GetCostEstimateUseCase{
        this.pickUpDate = pickUpDate
        return this
    }

    fun withReturnDate(returnDate: String) :GetCostEstimateUseCase{
        this.returnDate = returnDate
        return this
    }

    fun withBikeId(bikeId: Int) :GetCostEstimateUseCase{
        this.bikeId = bikeId
        return this
    }

    fun withPricingOptionId(pricing_option_id:Int?): GetCostEstimateUseCase {
        this.pricing_option_id = pricing_option_id
        return this
    }

    override fun buildUseCaseObservable(): Observable<CostEstimate> {
        return reservationRepository.costEstimate(bikeId!!,pickUpDate!!,returnDate!!,pricing_option_id)
    }

}