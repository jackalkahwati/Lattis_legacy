package com.lattis.lattis.presentation.reservation.edit

import android.os.Bundle
import com.lattis.domain.mapper.ride_bike.BikeModelMapper
import com.lattis.domain.models.Reservation
import com.lattis.domain.models.StartReservation
import com.lattis.domain.usecase.bike.BikeDetailUseCase
import com.lattis.domain.usecase.card.GetCardUseCase
import com.lattis.domain.usecase.reservation.CancelReservationUseCase
import com.lattis.domain.usecase.reservation.StartReservationTripUseCase
import com.lattis.domain.usecase.ride.SaveRideUseCase
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Card
import com.lattis.domain.models.Ride
import com.lattis.lattis.presentation.base.activity.location.BaseLocationActivityPresenter
import com.lattis.lattis.presentation.reservation.edit.ReservationEditActivity.Companion.ALREADY_ACTIVE_BOOKING
import com.lattis.lattis.presentation.reservation.edit.ReservationEditActivity.Companion.RESERVATIONINCURRENTSTATUS
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.utils.UtilsHelper.dateFromUTC
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReservationEditActivityPresenter @Inject constructor(
    val startReservationTripUseCase: StartReservationTripUseCase,
    val cancelReservationUseCase: CancelReservationUseCase,
    val bikeDetailUseCase: BikeDetailUseCase,
    val getCardUseCase: GetCardUseCase,
    val bikeModelMapper: BikeModelMapper,
    val saveRideUseCase: SaveRideUseCase
) : BaseLocationActivityPresenter<ReservationEditActivityView>(){

    var reservation:Reservation?=null
    var bike:Bike?=null
    var ride:Ride?=null
    var cards:List<Card>?=null
    var alreadyActiveBooking:Boolean=true
    var startReservation:StartReservation?=null


    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null) {
            reservation = if(arguments.containsKey(RESERVATIONINCURRENTSTATUS)) arguments.getSerializable(RESERVATIONINCURRENTSTATUS) as Reservation else null
            alreadyActiveBooking = if(arguments.containsKey(ALREADY_ACTIVE_BOOKING)) arguments.getBoolean(ALREADY_ACTIVE_BOOKING) else true
        }
        isBikeReservationTimeStarted()
        getBikeDetails()
        fetchCardList()
    }

    fun isBikeReservationTimeStarted(){
        val reservationStartTime = dateFromUTC(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reservation?.reservation_start))
        view?.showTripStart(!reservationStartTime!!.after(Date()) && !alreadyActiveBooking)
    }

    fun cancelReservation(){
        subscriptions.add(cancelReservationUseCase
            .withReservation(reservation?.reservation_id!!)
            .execute(object : RxObserver<Boolean>(view, false) {
                override fun onNext(status: Boolean) {
                    super.onNext(status)
                    view?.onReservationCancelSuccess()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onReservationCancelFailure()
                }
            })
        )
    }

    fun startTrip(){
        subscriptions.add(startReservationTripUseCase
            .withReservation(reservation?.reservation_id!!)
            .execute(object : RxObserver<StartReservation>(view, false) {
                override fun onNext(newStartReservation: StartReservation) {
                    super.onNext(newStartReservation)
                    startReservation= newStartReservation
                    ride?.rideId = startReservation?.trip_id!!
                    ride?.do_not_track_trip = startReservation?.do_not_track_trip
                    ride?.ride_booked_on = Date().time/1000
                    ride?.isFirst_lock_connect = true
                    saveRide()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onReservationCStartTripFailure()
                }
            })
        )
    }


    fun getBikeDetails() {
        subscriptions.add(
            bikeDetailUseCase
                .withBikeId(reservation?.bike_id!!)
                .withQRCodeId(-1)
                .execute(object : RxObserver<Bike>(view) {
                    override fun onNext(newBike: Bike) {
                        super.onNext(newBike)
                        bike = newBike
                        ride = bikeModelMapper.mapIn(bike)
                        view?.onReservationInformationSuccess(bike!!)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onReservationInformationFailure()
                    }
                })
        )
    }

    fun saveRide() {
        subscriptions.add(
            saveRideUseCase
                .withRide(ride!!)
                .execute(object : RxObserver<Ride>(view) {
                    override fun onNext(ride: Ride) {
                        super.onNext(ride)
                        view?.onReservationStartTripSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onReservationStartTripSuccess()
                    }
                })
        )
    }

    fun fetchCardList(){
        subscriptions.add(getCardUseCase
            .execute(object : RxObserver<List<Card>>() {
                override fun onNext(newCards: List<Card>) {
                    cards = newCards
                    if(cards==null || cards?.size==0){
                        view?.handleNoCard()
                        return
                    }

                    for(card in cards!!){
                        if(card.is_primary){
                            view?.handleCard(card)
                            return
                        }
                    }
                    view?.handleNoCard()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    cards=null
                    view?.handleNoCard()
                }
            }))
    }

    fun getPrimaryCard():Card?{
        if(cards==null || cards?.size==0)
            return null

        for(card in cards!!){
            if(card.is_primary){
                return card
            }
        }

        return null

    }

}