package com.lattis.data.repository.implementation.api

import com.lattis.data.database.store.RideRealmDataStore
import com.lattis.data.entity.body.bike.*
import com.lattis.data.mapper.BikeModelMapper
import com.lattis.data.mapper.BikeResponseMapper
import com.lattis.data.mapper.FindQRCodeMapper
import com.lattis.data.net.bike.BikeApiClient
import com.lattis.domain.models.*
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.repository.RideRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Named

class BikeRepositoryImp @Inject constructor(
    val bikeApiClient: BikeApiClient,
    val bikeModelMapper: BikeModelMapper,
    val rideRepository: RideRepository,
    val rideRealmDataStore: RideRealmDataStore,
    val bikeResponseMapper: BikeResponseMapper,
    @param:Named("UUID")val uuid:String,
    val findQRCodeMapper: FindQRCodeMapper
    ):BikeRepository {
    override fun searchBike(northEast: Location?, southWest: Location?):Observable<Rentals> {
        return bikeApiClient.api.searchBike(northEast.toString(),southWest.toString())
            .map {
                it.rentals
            }
    }

    override fun findByQRCode(qr_code:String):Observable<Rental> {
        return bikeApiClient.api.findByQRCode(qr_code)
            .map {
                findQRCodeMapper.mapIn(it.rental)
                it.rental
            }
    }

    override fun bookBike(
        bike: Bike,
        by_scan: Boolean,
        latitude: Double?,
        longitude: Double?,
        device_token:String,
        pricing_option_id:Int?
    ): Observable<Ride> {
        val ride = bikeModelMapper.mapIn(bike)
        return rideRepository.getRide().flatMap { oldRide ->
            ride.bike_on_call_operator = oldRide.bike_on_call_operator
            ride.support_phone = oldRide.support_phone
            bikeApiClient.api.bookBike(BookBikeBody(bike.bike_id, by_scan, latitude, longitude,device_token,pricing_option_id)).flatMap { reserveBikeResponse ->
                ride.id = uuid
                ride.bikeId = bike.bike_id
                ride.bike_booked_on = reserveBikeResponse.reserveBikeResponse!!.booked_on
                ride.bike_expires_in = reserveBikeResponse.reserveBikeResponse!!.expires_in

                if (reserveBikeResponse.reserveBikeResponse!!.on_call_operator == null) {
                } else if (reserveBikeResponse.reserveBikeResponse!!.on_call_operator!!.equals("null", ignoreCase = true) || reserveBikeResponse.reserveBikeResponse!!.on_call_operator!!.equals("undefined", ignoreCase = true)) {
                } else {
                    ride.bike_on_call_operator = reserveBikeResponse.reserveBikeResponse!!.on_call_operator
                }
                rideRealmDataStore.createOrUpdateUser(ride)
            }
        }
    }


    override fun bikeDetails(bike_id: Int, qr_code_id: Int,iot_qr_code:String?): Observable<Bike> {
        return bikeApiClient.api.bikeDetails(BikeDetailBody(bike_id, qr_code_id,iot_qr_code,-1))
            .map { bikeDetailResponse -> bikeResponseMapper.mapIn(bikeDetailResponse.bikeDetailResponse!!) }
    }

    override fun updateBikeMetaData(
        bike_id: Int,
        bike_battery_level: Int?,
        lock_battery_level: Int?,
        firmware_version: String?,
        shackle_jam: Boolean
    ): Observable<Boolean> {
            return bikeApiClient.api.updateBikeMetaData(BikeMetaDataBody(bike_id, bike_battery_level, lock_battery_level, firmware_version, shackle_jam))
                .flatMap{
                    Observable.just(true);
                };
    }

    override fun cancelBike(
        bike_id: Int,
        bike_damaged: Boolean,
        lockIssue: Boolean
    ): Observable<Boolean>{
        return bikeApiClient.api.cancelBikes(CancelBikeBody(bike_id,bike_damaged,lockIssue))
            .flatMap{
                Observable.just(true);
            };
    }


    override fun lockUnlockIotBike(bike_id: Int, lock: Boolean,controller_key:List<String>?): Observable<IoTBikeLockUnlockCommandStatus> {
        return if(lock){
            bikeApiClient.api.lockIotBike(bike_id, ControllerKeyBody(controller_key))
        }else{
            bikeApiClient.api.unLockIotBike(bike_id, ControllerKeyBody(controller_key))
        }.map {
            if(it.ioTBikeLockUnlockCommandStatus==null){
                it.ioTBikeLockUnlockCommandStatus = IoTBikeLockUnlockCommandStatus(null,null,null,null,null,null)
            }
            it.ioTBikeLockUnlockCommandStatus
        }
    }

    override fun getIotBikeStatus(bike_id: Int,controller_key:String?): Observable<IoTBikeStatus> {
        return bikeApiClient.api.getIotBikeStatus(bike_id,controller_key)
            .map {
                it.iotBikeBikeStatus
            }
    }

    override fun getLinkaIotBikeStatus(bike_id: Int,commandId:String): Observable<IoTBikeLockUnlockCommandStatus> {
        return bikeApiClient.api.getLinkaIotBikeStatus(bike_id,commandId)
            .map {
                it.payload?.ioTBikeLockUnlockCommandStatus!!
            }
    }

    override fun searchBikeByName(bike_name:String?):Observable<List<Bike>>{
        return bikeApiClient.api.searchBikeByName(bike_name)
            .map {
                it.payload?.bikeList
            }
    }

    override fun unlockSentinelBike(bike_id: Int): Observable<Boolean> {
        return bikeApiClient.api.unlockSentinel(bike_id).map { true }
    }
}