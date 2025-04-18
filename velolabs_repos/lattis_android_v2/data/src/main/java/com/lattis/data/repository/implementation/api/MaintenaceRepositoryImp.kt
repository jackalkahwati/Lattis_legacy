package com.lattis.data.repository.implementation.api

import com.lattis.data.entity.body.bike.BikeDetailBody
import com.lattis.data.entity.body.maintenance.DamageBikeBody
import com.lattis.data.net.maintenance.MaintenanceApiClient
import com.lattis.domain.repository.MaintenanceRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class MaintenaceRepositoryImp @Inject constructor(
    val maintenanceApiClient: MaintenanceApiClient
) : MaintenanceRepository{
    override fun reportDamage(
        category: String?,
        riderNotes: String?,
        bikeId: Int,
        maintenanceImage: String?,
        trip_id: Int
    ): Observable<Boolean> {
        return maintenanceApiClient.api.damageBikes(DamageBikeBody(category,riderNotes,bikeId,maintenanceImage,trip_id)).map {
            true
        }
    }

    override fun reportTheft(bikeId: Int, trip_id: Int): Observable<Boolean> {
        return maintenanceApiClient.api.reportBikeTheft(BikeDetailBody(bikeId,0,null,trip_id)).map {
            true
        }
    }
}