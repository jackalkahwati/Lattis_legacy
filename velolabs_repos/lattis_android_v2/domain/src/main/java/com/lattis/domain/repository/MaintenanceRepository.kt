package com.lattis.domain.repository

import io.reactivex.rxjava3.core.Observable

interface MaintenanceRepository {

    fun reportDamage(
        category: String?,
        riderNotes: String?,
        bikeId: Int, maintenanceImage: String?, trip_id: Int
    ): Observable<Boolean>

    fun reportTheft(bikeId: Int, trip_id: Int): Observable<Boolean>
}