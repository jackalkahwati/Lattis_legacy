package com.lattis.domain.usecase.ride

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.RideRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Location
import com.lattis.domain.models.Ride
import com.lattis.domain.usecase.bike.ReserveBikeUseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class StartRideUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val rideRepository: RideRepository
) : UseCase<Ride>(threadExecutor, postExecutionThread) {
    private var ride: Ride? = null
    private var location: Location? = null
    private var first_lock_connect = false
    private var device_token:String?=null
    fun withRide(ride: Ride?): StartRideUseCase {
        this.ride = ride
        return this
    }

    fun withLocation(location: Location?): StartRideUseCase {
        this.location = location
        return this
    }

    fun withFirstLockConnect(first_lock_connect: Boolean): StartRideUseCase {
        this.first_lock_connect = first_lock_connect
        return this
    }

    fun withDeviceToken(device_token:String): StartRideUseCase {
        this.device_token = device_token
        return this
    }

    override fun buildUseCaseObservable(): Observable<Ride> {
        return rideRepository.startRide(ride!!, location!!, first_lock_connect,device_token!!)
    }

}