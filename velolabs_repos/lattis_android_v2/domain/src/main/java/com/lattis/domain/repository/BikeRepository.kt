package com.lattis.domain.repository

import com.lattis.domain.models.*
import io.reactivex.rxjava3.core.Observable

interface BikeRepository {

    fun searchBike(northEast: Location?,southWest:Location?): Observable<Rentals>
    fun findByQRCode(qr_code:String):Observable<Rental>
    fun bookBike(
        bike: Bike,
        by_scan: Boolean,
        latitude: Double?,
        longitude: Double?,
        device_token:String,
        pricing_option_id:Int?
    ): Observable<Ride>

    fun bikeDetails(bike_id: Int, qr_code_id: Int, iot_qr_code:String?): Observable<Bike>

    fun updateBikeMetaData(
        bike_id: Int,
        bike_battery_level: Int?,
        lock_battery_level: Int?,
        firmware_version: String?,
        shackle_jam: Boolean
    ): Observable<Boolean>

    fun cancelBike(
        bike_id: Int,
        bike_damaged: Boolean,
        lockIssue: Boolean
    ): Observable<Boolean>

    fun lockUnlockIotBike(
        bike_id: Int,
        lock:Boolean,
        controller_key:List<String>?
    ):Observable<IoTBikeLockUnlockCommandStatus>

    fun getIotBikeStatus(
        bike_id: Int,
        controller_key:String?
    ):Observable<IoTBikeStatus>


    fun getLinkaIotBikeStatus(bike_id: Int,commandId:String): Observable<IoTBikeLockUnlockCommandStatus>
    fun searchBikeByName(bike_name:String?):Observable<List<Bike>>

    fun unlockSentinelBike(bike_id:Int):Observable<Boolean>
}