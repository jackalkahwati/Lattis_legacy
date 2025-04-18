package com.lattis.domain.repository

import com.lattis.domain.models.*
import io.reactivex.rxjava3.core.Observable

interface V2ApiRepository {
    fun bookings(bike: Bike,
                 by_scan:Boolean?,
                 latitude: Double?,
                 longitude: Double?,
                 device_token:String,
                 pricing_option_id:Int?
    ): Observable<Ride>
    fun cancelBooking(bookingId: Int,
                      bike_damaged: Boolean,
                      lockIssue: Boolean): Observable<Boolean>
    fun startTrip(ride: Ride,
                  location: Location,
                  first_lock_connect: Boolean,
                  device_token:String) : Observable<Ride>

    fun getIoTBikeStatus(bike:Bike?,ride: Ride?,controller_id:Int):Observable<IoTBikeStatus>
    fun lockUnlockIotBike(lock:Boolean,bike: Bike?, ride: Ride?,controller_id:Int): Observable<IoTBikeLockUnlockCommandStatus>
}