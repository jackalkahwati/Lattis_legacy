package com.lattis.domain.usecase.bike



import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Bike
import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable

class BikeDetailUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val bikeRepository: BikeRepository
) : UseCase<Bike>(threadExecutor, postExecutionThread) {
    private var bike_id: Int = 0
    private var qr_code_id = -1
    private var iot_qr_code:String? = null


    fun withBikeId(bike_id: Int): BikeDetailUseCase {
        this.bike_id = bike_id
        return this
    }

    fun withQRCodeId(qr_code_id: Int): BikeDetailUseCase {
        this.qr_code_id = qr_code_id
        return this
    }

    fun withIoTQRCodeId(iot_qr_code:String?): BikeDetailUseCase {
        this.iot_qr_code = iot_qr_code
        return this
    }

    override fun buildUseCaseObservable(): Observable<Bike> {
        return bikeRepository.bikeDetails(bike_id, qr_code_id,iot_qr_code)
    }

}
