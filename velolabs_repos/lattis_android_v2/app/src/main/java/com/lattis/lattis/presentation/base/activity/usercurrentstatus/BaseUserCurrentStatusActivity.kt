package com.lattis.lattis.presentation.base.activity.usercurrentstatus

import com.lattis.domain.models.RideSummary
import com.lattis.domain.models.UserCurrentStatus
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Ride
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import com.lattis.lattis.uimodel.mapper.BikeHubMapper
import com.lattis.lattis.uimodel.mapper.BikePortMapper
import com.lattis.lattis.utils.ParkingHubHelper.getPortFromPortId
import javax.inject.Inject

abstract class BaseUserCurrentStatusActivity<Presenter : BaseUserCurrentStatusPresenter<V>,V:BaseUserCurrentStatusView> :BaseActivity<Presenter,V>(),BaseUserCurrentStatusView{

    @Inject
    lateinit var bikePortMapper:BikePortMapper

    @Inject
    lateinit var bikeHubMapper: BikeHubMapper

    abstract fun onUserCurrentStatusSuccess()
    abstract fun onUserCurrentStatusFailure()

    //// GET USER STATUS
    override fun onSaveRideSuccess(ride: Ride) {
        if (presenter.currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.NO_BOOKING_NO_TRIP) {

        } else if (presenter.currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING) {

        } else if (presenter.currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_TRIP) {
            this.presenter.ride = ride
            if (presenter.activeTripStarted()) {

            } else {
                presenter.currentStatus = BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING_WITH_TRIP_STARTED
            }
        }
        onUserCurrentStatusSuccess()
    }

    override fun doAllRequiredWhenNoBikeBookingAndNoTrip(userCurrentStatus: UserCurrentStatus){
        // No bike book and no trip
        presenter.ride = Ride()
        presenter.ride?.id=presenter.uuid
        presenter.ride?.bike_on_call_operator= userCurrentStatus.onCallOperator
        presenter.ride?.support_phone= userCurrentStatus.supportPhone
        presenter.saveRide(presenter.ride!!)
    }

    override fun doAllRequiredWhenNoTripButBikeBooking(userCurrentStatus: UserCurrentStatus){

        // no trip but active bike booking
        presenter.ride = Ride()
        presenter.ride?.id =presenter.uuid
        if(userCurrentStatus.activeBooking?.bike_id!=null){
            presenter.ride?.bikeId= (userCurrentStatus.activeBooking?.bike_id!!)
        }else if(userCurrentStatus.activeBooking?.port_id!=null){
            presenter.ride?.bikeId= (userCurrentStatus.activeBooking?.port_id!!)
        }else if(userCurrentStatus.activeBooking?.hub_id!=null){
            presenter.ride?.bikeId= (userCurrentStatus.activeBooking?.hub_id!!)
        }
        presenter.ride?.bike_booked_on= (userCurrentStatus.activeBooking?.booked_on!!)
        presenter.ride?.bike_expires_in= ((userCurrentStatus.activeBooking?.till!! - userCurrentStatus.activeBooking?.booked_on!!).toInt())
        presenter.ride?.bike_on_call_operator= userCurrentStatus.onCallOperator
        presenter.ride?.support_phone= userCurrentStatus.supportPhone
        presenter.ride?.bike_booking_id = userCurrentStatus.activeBooking?.booking_id
        if(userCurrentStatus.activeBooking?.port_id!=null){
            var port = getPortFromPortId(userCurrentStatus.dockHub,userCurrentStatus.activeBooking?.port_id)
            if(port!=null){
                val bike = bikePortMapper.mapOut(userCurrentStatus.dockHub,port)
                if(bike!=null){
                    bike.bike_booking_id = userCurrentStatus.activeBooking?.booking_id
                    onBikeDetailsSuccess(bike)
                }else{
                    onBikeDetailsFailure()
                }
            }else{
                onBikeDetailsFailure()
            }
        }else if(userCurrentStatus.activeBooking?.hub_id!=null){
            val bike = bikeHubMapper.mapOut(userCurrentStatus.dockHub)
            if(bike!=null){
                bike.bike_booking_id = userCurrentStatus.activeBooking?.booking_id
                onBikeDetailsSuccess(bike)
            }else{
                onBikeDetailsFailure()
            }
        }else{
            presenter.getBikeDetails(presenter.ride?.bikeId!!)
        }
    }

    override fun doAllRequiredTripPresent(userCurrentStatus: UserCurrentStatus) {
        //  active trip
        presenter.ride = Ride()
        presenter.ride?.bike_on_call_operator= userCurrentStatus.onCallOperator
        presenter.ride?.support_phone= userCurrentStatus.supportPhone
        presenter.getRideSummary(userCurrentStatus.tripId!!)
    }

    override fun onBikeDetailsSuccess(bike: Bike) {
        this.presenter.bike = bike
        if (presenter.currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING) {
            bike.booked_on= presenter.ride?.bike_booked_on!!
            bike.expires_in = (presenter.ride?.bike_expires_in!!)
            val ride: Ride = presenter.bikeModelMapper.mapIn(bike)
            ride.id =(presenter.uuid)
            ride.bikeId = (this.presenter.ride?.bikeId!!)
            ride.bike_booked_on= (this.presenter.ride?.bike_booked_on!!)
            ride.bike_expires_in = (this.presenter.ride?.bike_expires_in!!)
            ride.bike_on_call_operator=(this.presenter.ride?.bike_on_call_operator)
            ride.support_phone =(this.presenter.ride?.support_phone)
            ride.bike_booking_id = this.presenter.ride?.bike_booking_id
            presenter.saveRide(ride)
        } else if (presenter.currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_TRIP) {
            val ride: Ride = presenter.bikeModelMapper.mapIn(bike)
            ride.id =(presenter.uuid)
            ride.bikeId = (this.presenter.ride?.bikeId!!)
            ride.rideId = (this.presenter.ride?.rideId!!)
            ride.ride_booked_on= (this.presenter.ride?.ride_booked_on!!)
            ride.bike_on_call_operator=(this.presenter.ride?.bike_on_call_operator)
            ride.support_phone =(this.presenter.ride?.support_phone)
            ride.isFirst_lock_connect = (this.presenter.ride!!.isFirst_lock_connect)
            ride.do_not_track_trip= (this.presenter.ride?.do_not_track_trip)
            presenter.saveRide(ride)
        }
    }



    override fun onGetRideSummarySuccess(rideSummary: RideSummary) {
        presenter.ride?.id =(presenter.uuid)
        presenter.ride?.bikeId = if(rideSummary.bike_id!=null) rideSummary.bike_id!! else if(rideSummary.port_id!=null)rideSummary.port_id!! else if(rideSummary.hub_id!=null)rideSummary.hub_id!! else 0
        presenter.ride?.rideId = (rideSummary.trip_id)
        presenter.ride?.ride_booked_on = (rideSummary.date_created!!)
        presenter.ride?.do_not_track_trip=(rideSummary.do_not_track_trip)
        presenter.ride?.isFirst_lock_connect = (rideSummary.isFirst_lock_connect)
        if(rideSummary.port_id!=null){
            var port = getPortFromPortId(rideSummary.dockHub,rideSummary.port_id)
            if(port!=null){
                val bike = bikePortMapper.mapOut(rideSummary.dockHub,port)
                if(bike!=null){
                    onBikeDetailsSuccess(bike)
                }else{
                    onBikeDetailsFailure()
                }
            }else{
                onBikeDetailsFailure()
            }
        }else{
            presenter.getBikeDetails(presenter.ride?.bikeId!!)
        }


    }

    override fun onGetCurrentUserStatusFailure() {
        presenter.currentStatus = BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ERROR_FETCHING_STATUS
        onUserCurrentStatusFailure()
    }

    override fun onBikeDetailsFailure() {
        presenter.currentStatus = BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ERROR_FETCHING_STATUS
        onUserCurrentStatusFailure()
    }

    override fun onSaveRideFailure() {
        presenter.currentStatus = BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ERROR_FETCHING_STATUS
        onUserCurrentStatusFailure()
    }

    override fun onGetRideSummaryFailure() {
        presenter.currentStatus = BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ERROR_FETCHING_STATUS
        onUserCurrentStatusFailure()
    }

    override fun onNoInternet() {
        presenter.currentStatus = BaseUserCurrentStatusPresenter.Companion.CurrentStatus.NO_INTERNET
        onUserCurrentStatusFailure()
    }
}