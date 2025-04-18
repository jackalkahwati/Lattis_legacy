package com.lattis.domain.repository

import com.lattis.domain.models.RideSummary
import com.lattis.domain.models.UpdateTripData
import com.lattis.domain.models.Location
import com.lattis.domain.models.Ride
import com.lattis.domain.models.RideHistory
import io.reactivex.rxjava3.core.Observable

interface RideRepository {
    fun getRide(): Observable<Ride>
    fun saveRide(ride: Ride): Observable<Ride>
    fun deleteRide(): Observable<Boolean>
    fun getRideSummary(trip_id: Int): Observable<RideSummary>
    fun updateRide(trip_id: Int, steps: Array<DoubleArray>): Observable<UpdateTripData>
    fun startRide(ride: Ride, location: Location, first_lock_connect: Boolean,device_token:String): Observable<Ride>
    fun endRide(
        trip_id: Int,
        location: Location?,
        parkingId: Int,
        imageURL: String?,
        isReportDamage: Boolean,
        lock_battery: Int?,
        bike_battery: Int?
    ): Observable<RideSummary>


    fun rateRide(trip_id: Int, rating: Int): Observable<Boolean>

    fun getRideHistory(): Observable<RideHistory>
}