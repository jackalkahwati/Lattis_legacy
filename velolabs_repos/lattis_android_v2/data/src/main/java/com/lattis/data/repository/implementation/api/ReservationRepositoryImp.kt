package com.lattis.data.repository.implementation.api

import com.lattis.data.entity.body.reservation.CostEstimateBody
import com.lattis.data.entity.body.reservation.ReserveBody
import com.lattis.data.net.reservation.ReservationApiClient
import com.lattis.domain.models.*
import com.lattis.domain.repository.ReservationRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject


class ReservationRepositoryImp @Inject constructor(
    val reservationApiClient: ReservationApiClient
):ReservationRepository{

    override fun getAvailableVehicles(
        fleetId: Int,
        pickUpDate: String,
        returnDate: String
    ): Observable<List<Bike>> {
        return reservationApiClient.api.getAvailableVehicles(fleetId,pickUpDate,returnDate)
            .map {
                it.bikes
            }
    }

    override fun costEstimate(
        bikeId: Int,
        pickUpDate: String,
        returnDate: String,
        pricing_option_id:Int?
    ): Observable<CostEstimate> {
        return reservationApiClient.api.costEstimate(CostEstimateBody(pickUpDate,returnDate,bikeId,pricing_option_id))
            .map {
                it.costEstimate
            }
    }

    override fun reserve(
        bikeId: Int,
        pickUpDate: String,
        returnDate: String,
        pricing_option_id:Int?
    ): Observable<Reserve> {
        return reservationApiClient.api.reserve(ReserveBody(pickUpDate,returnDate,bikeId,pricing_option_id))
            .map {
                it.reserve
            }
    }

    override fun cancel(
        reservation_id:Int
    ):Observable<Boolean>{
        return reservationApiClient.api.cancel(reservation_id)
            .map {
                true
            }
    }

    override fun start_trip(
        reservation_id:Int
    ):Observable<StartReservation>{
        return reservationApiClient.api.startTrip(reservation_id)
            .map {
                it.startReservation
            }
    }

    override fun getReservations(
    ):Observable<List<Reservation>>{
        return reservationApiClient.api.getReservations()
            .map {
                it.reservations
            }
    }
}