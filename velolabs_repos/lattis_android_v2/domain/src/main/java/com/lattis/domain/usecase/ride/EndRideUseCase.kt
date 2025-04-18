package com.lattis.domain.usecase.ride

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.RideSummary
import com.lattis.domain.repository.RideRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Location
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class EndRideUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val rideRepository: RideRepository
) : UseCase<RideSummary>(threadExecutor, postExecutionThread) {
    private var trip_id = 0
    private var location: Location? = null
    private var parkingId = 0
    private var imageURL: String? = null
    private var isReportDamage = false
    private var lock_battery: Int? = null
    private var bike_battery: Int? = null
    fun withTripId(trip_id: Int): EndRideUseCase {
        this.trip_id = trip_id
        return this
    }

    fun withLocation(location: Location?): EndRideUseCase {
        this.location = location
        return this
    }

    fun withParkingId(parkingId: Int): EndRideUseCase {
        this.parkingId = parkingId
        return this
    }

    fun withImageURL(imageURL: String?): EndRideUseCase {
        this.imageURL = imageURL
        return this
    }

    fun withLockBattery(lock_battery: Int?): EndRideUseCase {
        this.lock_battery = lock_battery
        return this
    }

    fun withBikeBattery(bike_battery: Int?): EndRideUseCase {
        this.bike_battery = bike_battery
        return this
    }

    override fun buildUseCaseObservable(): Observable<RideSummary> {
        return rideRepository.endRide(
            trip_id,
            location,
            parkingId,
            imageURL,
            isReportDamage,
            lock_battery,
            bike_battery
        )
    }

    fun withReportDamage(isReportDamage: Boolean): EndRideUseCase {
        this.isReportDamage = isReportDamage
        return this
    }

}