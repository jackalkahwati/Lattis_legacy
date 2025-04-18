package com.lattis.domain.repository

import com.lattis.domain.models.UpdateTripData
import com.lattis.domain.models.Lock
import io.reactivex.rxjava3.core.Observable

interface ActiveTripRepository {
    fun stopActiveTripService(): Observable<Boolean>
    fun startActiveTripService(trip_id: Int,title:String): Observable<UpdateTripData>
    fun startLocationTracking(lock: Lock): Observable<Boolean>
    fun stopLocationTracking(): Observable<Boolean>
    fun stopGetTripDetailsThreadIfApplicable(): Observable<Boolean>
    fun pauseUpdateTrip(active:Boolean): Observable<Boolean>
}