package com.lattis.domain.usecase.v2

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Ride
import com.lattis.domain.repository.V2ApiRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class BookingsUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val v2ApiRepository: V2ApiRepository
) : UseCase<Ride>(threadExecutor, postExecutionThread) {

    private lateinit var bike: Bike
    private var by_scan = false
    private var latitude:Double? = null
    private var longitude:Double? = null
    private var device_token:String?=null
    private var pricing_option_id:Int?=null
    private var promotion_id:Int?=null

    fun withBike(bike: Bike): BookingsUseCase {
        this.bike = bike
        return this
    }

    fun withScanStatus(by_scan: Boolean): BookingsUseCase {
        this.by_scan = by_scan
        return this
    }

    fun withLatitude(latitude: Double?): BookingsUseCase {
        this.latitude = latitude
        return this
    }

    fun withLongitude(longitude: Double?): BookingsUseCase {
        this.longitude = longitude
        return this
    }

    fun withDeviceToken(device_token:String): BookingsUseCase {
        this.device_token = device_token
        return this
    }

    fun withPricingOptionId(pricing_option_id:Int?): BookingsUseCase {
        this.pricing_option_id = pricing_option_id
        return this
    }

    fun withPromotionId(promotion_id:Int?): BookingsUseCase {
        this.promotion_id = promotion_id
        return this
    }

    override fun buildUseCaseObservable(): Observable<Ride> {
        return v2ApiRepository.bookings(bike, by_scan, latitude, longitude,device_token!!,pricing_option_id)
    }
}