package io.lattis.data.repository.implementation.api

import io.lattis.data.database.fleet.UserSavedFleet
import io.lattis.data.net.fleet.FleetApiClient
import io.lattis.domain.models.Fleet
import io.lattis.domain.repository.FleetRepository
import io.reactivex.Observable
import javax.inject.Inject

class FleetRepositoryImp @Inject constructor(
    private val fleetApiClient: FleetApiClient,
    val userSavedFleet: UserSavedFleet
):FleetRepository{

    override fun getFleets(): Observable<List<Fleet>> {
        return fleetApiClient.api.getFleets()
    }

    override fun getUserSavedFleet(): Observable<Fleet> {
        return Observable.create<Fleet> {
            val userSavedFleet = userSavedFleet.getFleet()
            if(userSavedFleet!=null){
                it.onNext(userSavedFleet)
            }else{
                it.onError(Throwable("NO_SAVED_USER_FLEET"))
            }
        }
    }

    override fun saveUserFleet(fleet: Fleet): Observable<Boolean> {
        userSavedFleet.saveFleet(fleet)
        return  Observable.just(true)
    }

}