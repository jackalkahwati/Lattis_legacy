package io.lattis.domain.repository

import io.lattis.domain.models.Fleet
import io.reactivex.Observable

interface FleetRepository {
    fun getFleets():Observable<List<Fleet>>
    fun getUserSavedFleet():Observable<Fleet>
    fun saveUserFleet(fleet: Fleet):Observable<Boolean>
}