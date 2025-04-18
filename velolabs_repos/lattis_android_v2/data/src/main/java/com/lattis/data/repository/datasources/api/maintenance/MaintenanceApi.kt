package com.lattis.data.repository.datasources.api.maintenance

import com.lattis.data.entity.body.bike.BikeDetailBody
import com.lattis.data.entity.body.maintenance.DamageBikeBody
import com.lattis.data.entity.response.BasicResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface MaintenanceApi {
    @POST("api/maintenance/create-damage-report/")
    fun damageBikes(@Body damageBikeBody: DamageBikeBody): Observable<BasicResponse>

    @PUT("api/maintenance/report-bike-theft/")
    fun reportBikeTheft(@Body BikeDetailBody: BikeDetailBody): Observable<BasicResponse>
}