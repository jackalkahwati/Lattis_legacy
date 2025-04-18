package com.lattis.lattis.presentation.reservation

import android.os.Bundle
import android.os.CountDownTimer
import com.lattis.domain.mapper.ride_bike.BikeModelMapper
import com.lattis.domain.models.*
import com.lattis.domain.usecase.bike.BikeDetailUseCase
import com.lattis.domain.usecase.card.GetCardUseCase
import com.lattis.domain.usecase.reservation.CancelReservationUseCase
import com.lattis.domain.usecase.reservation.GetReservationsUseCase
import com.lattis.domain.usecase.reservation.StartReservationTripUseCase
import com.lattis.domain.usecase.ride.SaveRideUseCase
import com.lattis.domain.usecase.user.GetUserFleetsUseCase
import com.lattis.domain.usecase.user.GetUserUseCase
import com.lattis.lattis.presentation.base.activity.location.BaseLocationActivityPresenter
import com.lattis.lattis.presentation.reservation.edit.ReservationEditActivity
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.IsRidePaid
import com.lattis.lattis.utils.UtilsHelper
import com.lattis.lattis.utils.UtilsHelper.getDurationBreakdown
import com.lattis.lattis.utils.UtilsHelper.getDurationFromNow
import com.lattis.lattis.utils.localnotification.LocalNotificationHelper
import com.lattis.lattis.utils.localnotification.RESERVATION_TIMER_OVER_NOTIFICATION_TYPE
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReservationListOrCreateActivityPresenter @Inject constructor(
    val getReservationsUseCase: GetReservationsUseCase,
    val getCardUseCase: GetCardUseCase,
    val startReservationTripUseCase: StartReservationTripUseCase,
    val cancelReservationUseCase: CancelReservationUseCase,
    val bikeModelMapper: BikeModelMapper,
    val saveRideUseCase: SaveRideUseCase,
    val bikeDetailUseCase: BikeDetailUseCase,
    val getUserUseCase: GetUserUseCase,
    val getUserFleetsUseCase: GetUserFleetsUseCase,
    val localNotificationHelper: LocalNotificationHelper
) : BaseLocationActivityPresenter<ReservationListOrCreateActivityView>(){

    var reservations: List<Reservation>?=null
    var selectedReservation:Reservation?=null
    var alreadyActiveBooking:Boolean=true
    var availableTimer: CountDownTimer? = null
    var cards:List<Card>?=null
    var startReservation:StartReservation?=null
    var ride:Ride?=null
    var fleets:List<Bike.Fleet>?=null


    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null) {
            alreadyActiveBooking = if(arguments.containsKey(ReservationEditActivity.ALREADY_ACTIVE_BOOKING)) arguments.getBoolean(
                ReservationEditActivity.ALREADY_ACTIVE_BOOKING
            ) else true
        }
        fetchCardList()
//        getUserProfile()
        getUserFleet()
    }

    fun getReservations(){
        subscriptions.add(getReservationsUseCase
            .execute(object : RxObserver<List<Reservation>>(view, false) {
                override fun onNext(newReservations: List<Reservation>) {
                    super.onNext(newReservations)
                    reservations = newReservations
                    view?.onReservationsAvailable()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onReservationNotAvailable()
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

    fun startReservationAvailableTimer(){

        stopeReservationAvailableTimer()

        var timeRemaining =
            UtilsHelper.dateFromUTC(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(selectedReservation?.reservation_start)
            )!!.time - Date().time

        availableTimer = object : CountDownTimer((timeRemaining).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                view?.onAvailableTimerValue(getDurationBreakdown(millisUntilFinished/1000))
            }

            override fun onFinish() {
                view?.onAvailableTimerFinish()
            }
        }.start()
    }

    fun stopeReservationAvailableTimer(){
        if (availableTimer != null) {
            availableTimer?.cancel()
            availableTimer=null
        }
    }

    fun isBikeReservationTimeStarted():Boolean{
        val reservationStartTime =
            UtilsHelper.dateFromUTC(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(selectedReservation?.reservation_start))
        return !reservationStartTime!!.after(Date())
    }


    fun cancelReservation(){
        subscriptions.add(cancelReservationUseCase
            .withReservation(selectedReservation?.reservation_id!!)
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

    fun getBikeDetails() {

        if(alreadyActiveBooking){
            view?.onAlreadyOnRideFailure()
            return
        }

        subscriptions.add(
            bikeDetailUseCase
                .withBikeId(selectedReservation?.bike_id!!)
                .withQRCodeId(-1)
                .execute(object : RxObserver<Bike>(view) {
                    override fun onNext(newBike: Bike) {
                        super.onNext(newBike)
                        ride = bikeModelMapper.mapIn(newBike)
                        if (IsRidePaid.isRidePaidForFleet(newBike.fleet_type) && (cards == null || cards?.size == 0)) {
                            view?.onCardMissingFailure()
                        }else if(IsRidePaid.isRidePaidForFleet(newBike.fleet_type) && getPrimaryCard()==null){
                            view?.onNoPrimaryCardFailure()
                        }else{
                            startTrip()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onReservationCStartTripFailure()
                    }
                })
        )
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

    fun startTrip(){
        subscriptions.add(startReservationTripUseCase
            .withReservation(selectedReservation?.reservation_id!!)
            .execute(object : RxObserver<StartReservation>(view, false) {
                override fun onNext(newStartReservation: StartReservation) {
                    super.onNext(newStartReservation)
                    startReservation= newStartReservation
                    ride?.rideId = startReservation?.trip_id!!
                    ride?.do_not_track_trip = startReservation?.do_not_track_trip
                    ride?.ride_booked_on = Date().time/1000
                    ride?.isFirst_lock_connect = true
                    view?.startReservationTimerOverLocalNotification()
                    saveRide()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onReservationCStartTripFailure()
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

    fun getUserFleet() {
        subscriptions.add(getUserFleetsUseCase.execute(object : RxObserver<List<Bike.Fleet>>(view, false) {
            override fun onNext(newFleets:List<Bike.Fleet>) {
                fleets = newFleets
            }

            override fun onError(e: Throwable) {
                super.onError(e)
            }
        }))
    }

    fun getUserProfile() {
        subscriptions.add(getUserUseCase.execute(object : RxObserver<User>(view, false) {
            override fun onNext(currUser: User) {
                if (currUser != null) {

                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
            }
        }))
    }

    fun getUserFleets():List<Bike.Fleet>? {
        return fleets
    }

    //// localNotificationHelper :start

    fun createLocalNotification(title:String,message:String){
        localNotificationHelper.scheduleNotification(title,message,
            RESERVATION_TIMER_OVER_NOTIFICATION_TYPE,
            UtilsHelper.getAlarmTimeMinusMinutes(UtilsHelper.dateFromUTC(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(selectedReservation?.reservation_end)), 30)
        )
    }

    //// locatNotificationHelper :end

}