package com.lattis.domain.usecase.bike

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Rental
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class FindByQRCodeUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val bikeRepository: BikeRepository
) : UseCase<Rental>(threadExecutor, postExecutionThread){
    private var qr_code : String?=null
    fun withQRCode(qr_code: String): FindByQRCodeUseCase {
        this.qr_code = qr_code
        return this
    }
    override fun buildUseCaseObservable(): Observable<Rental> {
        return bikeRepository.findByQRCode(qr_code!!)
    }
}