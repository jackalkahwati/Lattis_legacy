package com.lattis.lattis.presentation.base.fragment.usercurrentlocation

import com.lattis.domain.mapper.ride_bike.BikeModelMapper
import com.lattis.domain.models.RideSummary
import com.lattis.domain.models.UserCurrentStatus
import com.lattis.domain.usecase.bike.BikeDetailUseCase
import com.lattis.domain.usecase.ride.RideSummaryUseCase
import com.lattis.domain.usecase.ride.SaveRideUseCase
import com.lattis.domain.usecase.user.GetUserCurrentStatusUseCase
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Ride
import com.lattis.lattis.presentation.base.fragment.location.BaseLocationFragmentPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Named

open abstract class BaseUserCurrentStatusPresenter<View: BaseUserCurrentStatusView>: BaseLocationFragmentPresenter<View>() {


    companion object {
        enum class CurrentStatus{
            ERROR_FETCHING_STATUS,
            NO_BOOKING_NO_TRIP,
            ACTIVE_BOOKING,
            ACTIVE_BOOKING_WITH_TRIP_STARTED,
            ACTIVE_TRIP,
            NO_INTERNET,
            INVALID
        }
        val CURRENT_STATUS = "CURRENT_STATUS"
    }

    var currentStatus= BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ERROR_FETCHING_STATUS
    var userCurrentStatus: UserCurrentStatus?=null
    var rideInUserCurrentStatus: Ride?=null
    var bike: Bike?=null

    @Inject
    lateinit var getUserCurrentStatusUseCase: GetUserCurrentStatusUseCase

    @Inject
    lateinit var saveRideUseCase: SaveRideUseCase

    @Inject
    lateinit var bikeDetailUseCase: BikeDetailUseCase

    @Inject
    lateinit var rideSummaryUseCase: RideSummaryUseCase

    @Inject
    @field:Named("UUID")
    lateinit var uuid: String

    @Inject
    lateinit var bikeModelMapper: BikeModelMapper


    fun userCurrentStatus() {
        subscriptions.add(
            getUserCurrentStatusUseCase
                .execute(object : RxObserver<UserCurrentStatus>(view) {
                    override fun onNext(newUserCurrentStatus: UserCurrentStatus) {
                        super.onNext(newUserCurrentStatus)
                        if (newUserCurrentStatus != null){
                            userCurrentStatus = newUserCurrentStatus
                            setScreen(userCurrentStatus!!)
                        }else{
                            view?.onGetCurrentUserStatusFailure()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (e is SocketTimeoutException || e is UnknownHostException) {
                            view?.onNoInternet()
                        }else{
                            view?.onGetCurrentUserStatusFailure()
                        }
                    }
                })
        )
    }

    fun setScreen(userCurrentStatus: UserCurrentStatus) {
        if(userCurrentStatus.tripId==null && userCurrentStatus.activeBooking==null){
            // No bike booking and no active trip
            currentStatus = BaseUserCurrentStatusPresenter.Companion.CurrentStatus.NO_BOOKING_NO_TRIP
            view?.doAllRequiredWhenNoBikeBookingAndNoTrip(userCurrentStatus)

        }else if(userCurrentStatus.tripId==null && userCurrentStatus.activeBooking!=null){
            //bike booking and no trip
            currentStatus = BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING
            view?.doAllRequiredWhenNoTripButBikeBooking(userCurrentStatus)

        }else if(userCurrentStatus.tripId!=null && userCurrentStatus.activeBooking==null){
            // in ride
            currentStatus = BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_TRIP
            view?.doAllRequiredTripPresent(userCurrentStatus)
        }else{
            // invalid state
            view?.onGetCurrentUserStatusFailure()
        }
    }



    fun saveRide(ride: Ride) {
        subscriptions.add(
            saveRideUseCase
                .withRide(ride)
                .execute(object : RxObserver<Ride>(view) {
                    override fun onNext(ride: Ride) {
                        super.onNext(ride)
                        if (view != null) view.onSaveRideSuccess(ride)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        if (view != null) view.onSaveRideFailure()
                    }
                })
        )
    }

    fun getBikeDetails(bike_id: Int) {
        subscriptions.add(
            bikeDetailUseCase
                .withBikeId(bike_id)
                .withQRCodeId(-1)
                .execute(object : RxObserver<Bike>(view) {
                    override fun onNext(bike: Bike) {
                        super.onNext(bike)
                        if (view != null) view.onBikeDetailsSuccess(bike)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        if (view != null) view.onBikeDetailsFailure()
                    }
                })
        )
    }

    fun getRideSummary(trip_id: Int) {
        subscriptions.add(
            rideSummaryUseCase
                .withTripId(trip_id)
                .execute(object : RxObserver<RideSummary>(view) {
                    override fun onNext(rideSummaryResponse: RideSummary) {
                        super.onNext(rideSummaryResponse)
                        if (view != null) view.onGetRideSummarySuccess(rideSummaryResponse)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        if (view != null) view.onGetRideSummaryFailure()
                    }
                })
        )
    }

    fun getRideSummaryForEndingRide(trip_id: Int) {
        subscriptions.add(
            rideSummaryUseCase
                .withTripId(trip_id)
                .execute(object : RxObserver<RideSummary>(view) {
                    override fun onNext(rideSummaryResponse: RideSummary) {
                        super.onNext(rideSummaryResponse)
                        if (view != null) view.onGetRideSummarySuccessForEndingRide(rideSummaryResponse)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        if (view != null) view.onGetRideSummaryFailureForEndingRide()
                    }
                })
        )
    }
}