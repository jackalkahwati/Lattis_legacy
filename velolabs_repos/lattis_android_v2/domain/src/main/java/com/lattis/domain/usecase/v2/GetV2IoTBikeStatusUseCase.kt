package com.lattis.domain.usecase.v2

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Bike
import com.lattis.domain.models.IoTBikeStatus
import com.lattis.domain.models.Ride
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.repository.V2ApiRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetV2IoTBikeStatusUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val v2ApiRepository: V2ApiRepository
) : UseCase<IoTBikeStatus>(threadExecutor, postExecutionThread) {
    private var bike: Bike?=null
    private var ride: Ride?=null
    private var controller_id:Int?=null


    fun withBike(bike: Bike): GetV2IoTBikeStatusUseCase {
        this.bike = bike
        return this
    }

    fun withRide(ride: Ride?): GetV2IoTBikeStatusUseCase {
        this.ride = ride
        return this
    }

    fun withControllerId(controller_id:Int?):GetV2IoTBikeStatusUseCase {
        this.controller_id = controller_id
        return this
    }

    override fun buildUseCaseObservable(): Observable<IoTBikeStatus> {
        return v2ApiRepository.getIoTBikeStatus(bike,ride,controller_id!!)
    }

}