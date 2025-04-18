package com.lattis.domain.usecase.bike

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Ride
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ReserveBikeUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val bikeRepository: BikeRepository
) : UseCase<Ride>(threadExecutor, postExecutionThread) {

    private lateinit var bike: Bike
    private var by_scan = false
    private var latitude:Double? = null
    private var longitude:Double? = null
    private var device_token:String?=null
    private var pricing_option_id:Int?=null

    fun withBike(bike: Bike): ReserveBikeUseCase {
        this.bike = bike
        return this
    }

    fun withScanStatus(by_scan: Boolean): ReserveBikeUseCase {
        this.by_scan = by_scan
        return this
    }

    fun withLatitude(latitude: Double?): ReserveBikeUseCase {
        this.latitude = latitude
        return this
    }

    fun withLongitude(longitude: Double?): ReserveBikeUseCase {
        this.longitude = longitude
        return this
    }

    fun withDeviceToken(device_token:String): ReserveBikeUseCase {
        this.device_token = device_token
        return this
    }

    fun withPricingOptionId(pricing_option_id:Int?): ReserveBikeUseCase {
        this.pricing_option_id = pricing_option_id
        return this
    }

    override fun buildUseCaseObservable(): Observable<Ride> {
        return bikeRepository.bookBike(bike, by_scan, latitude, longitude,device_token!!,pricing_option_id)
    }
}