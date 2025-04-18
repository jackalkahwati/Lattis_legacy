package com.lattis.domain.repository

import com.lattis.domain.models.*
import io.reactivex.rxjava3.core.Observable

interface ReservationRepository{

    fun getAvailableVehicles(fleetId:Int,pickUpDate:String,returnDate:String): Observable<List<Bike>>

    fun costEstimate(
        bikeId: Int,
        pickUpDate: String,
        returnDate: String,
        pricing_option_id:Int?
    ): Observable<CostEstimate>

    fun reserve(
        bikeId: Int,
        pickUpDate: String,
        returnDate: String,
        pricing_option_id:Int?
    ): Observable<Reserve>


    fun cancel(
        reservation_id:Int
    ):Observable<Boolean>

    fun start_trip(
        reservation_id:Int
    ):Observable<StartReservation>

    fun getReservations(
    ):Observable<List<Reservation>>
}