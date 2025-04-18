package io.lattis.data.repository.datasources.api

import io.lattis.domain.models.Fleet
import io.reactivex.Observable
import retrofit2.http.GET

interface FleetApi {
    @GET("operator/fleets")
    fun getFleets(): Observable<List<Fleet>>
}