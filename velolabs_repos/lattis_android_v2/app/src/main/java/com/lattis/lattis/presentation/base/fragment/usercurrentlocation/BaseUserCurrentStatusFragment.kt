package com.lattis.lattis.presentation.base.fragment.usercurrentlocation

import com.lattis.domain.models.RideSummary
import com.lattis.domain.models.UserCurrentStatus
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Ride
import com.lattis.lattis.presentation.base.fragment.location.BaseLocationFragment
import com.lattis.lattis.uimodel.mapper.BikeHubMapper
import com.lattis.lattis.uimodel.mapper.BikePortMapper
import com.lattis.lattis.utils.ParkingHubHelper
import javax.inject.Inject

abstract class BaseUserCurrentStatusFragment<Presenter : BaseUserCurrentStatusPresenter<V>,V: BaseUserCurrentStatusView> :
    BaseLocationFragment<Presenter, V>(), BaseUserCurrentStatusView {

    abstract fun onUserCurrentStatusSuccess()
    abstract fun onUserCurrentStatusFailure()

    @Inject
    lateinit var bikePortMapper:BikePortMapper

    @Inject
    lateinit var bikeHubMapper:BikeHubMapper

    //// GET USER STATUS
    override fun onSaveRideSuccess(ride: Ride) {
        if (presenter.currentStatus == com.lattis.lattis.presentation.base.fragment.usercurrentlocation.BaseUserCurrentStatusPresenter.Companion.CurrentStatus.NO_BOOKING_NO_TRIP) {

        } else if (presenter.currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING) {

        } else if (presenter.currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_TRIP) {
            this.presenter.rideInUserCurrentStatus = ride
            if (this.presenter.rideInUserCurrentStatus!!.isFirst_lock_connect) {

            } else {
                presenter.currentStatus = BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING_WITH_TRIP_STARTED
            }
        }
        onUserCurrentStatusSuccess()
    }

    override fun doAllRequiredWhenNoBikeBookingAndNoTrip(userCurrentStatus: UserCurrentStatus){
        // No bike book and no trip
        presenter.rideInUserCurrentStatus = Ride()
        presenter.rideInUserCurrentStatus?.id=presenter.uuid
        presenter.rideInUserCurrentStatus?.bike_on_call_operator= userCurrentStatus.onCallOperator
        presenter.rideInUserCurrentStatus?.support_phone= userCurrentStatus.supportPhone
        presenter.saveRide(presenter.rideInUserCurrentStatus!!)
    }

    override fun doAllRequiredWhenNoTripButBikeBooking(userCurrentStatus: UserCurrentStatus){

        // no trip but active bike booking
        presenter.rideInUserCurrentStatus = Ride()
        presenter.rideInUserCurrentStatus?.id =presenter.uuid
        if(userCurrentStatus.activeBooking?.bike_id!=null){
            presenter.rideInUserCurrentStatus?.bikeId= (userCurrentStatus.activeBooking?.bike_id!!)
        }else if(userCurrentStatus.activeBooking?.port_id!=null){
            presenter.rideInUserCurrentStatus?.bikeId= (userCurrentStatus.activeBooking?.port_id!!)
        }else if(userCurrentStatus.activeBooking?.hub_id!=null){
            presenter.rideInUserCurrentStatus?.bikeId= (userCurrentStatus.activeBooking?.hub_id!!)
        }
        presenter.rideInUserCurrentStatus?.bike_booked_on= (userCurrentStatus.activeBooking?.booked_on!!)
        presenter.rideInUserCurrentStatus?.bike_expires_in= ((userCurrentStatus.activeBooking?.till!! - userCurrentStatus.activeBooking?.booked_on!!).toInt())
        presenter.rideInUserCurrentStatus?.bike_on_call_operator= userCurrentStatus.onCallOperator
        presenter.rideInUserCurrentStatus?.support_phone= userCurrentStatus.supportPhone
        presenter.rideInUserCurrentStatus?.bike_booking_id = userCurrentStatus.activeBooking?.booking_id
        presenter.rideInUserCurrentStatus?.bike_booking_id = userCurrentStatus.activeBooking?.booking_id
        if(userCurrentStatus.activeBooking?.port_id!=null){
            var port = ParkingHubHelper.getPortFromPortId(
                userCurrentStatus.dockHub,
                userCurrentStatus.activeBooking?.port_id
            )
            if(port!=null){
                val bike = bikePortMapper.mapOut(userCurrentStatus.dockHub,port)
                if(bike!=null){
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
            presenter.getBikeDetails(presenter.rideInUserCurrentStatus?.bikeId!!)
        }



    }

    override fun doAllRequiredTripPresent(userCurrentStatus: UserCurrentStatus) {
        //  active trip
        presenter.rideInUserCurrentStatus = Ride()
        presenter.rideInUserCurrentStatus?.bike_on_call_operator= userCurrentStatus.onCallOperator
        presenter.rideInUserCurrentStatus?.support_phone= userCurrentStatus.supportPhone
        presenter.getRideSummary(userCurrentStatus.tripId!!)
    }

    override fun onBikeDetailsSuccess(bike: Bike) {
        this.presenter.bike = bike
        if (presenter.currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING) {
            bike.booked_on= presenter.rideInUserCurrentStatus?.bike_booked_on!!
            bike.expires_in = (presenter.rideInUserCurrentStatus?.bike_expires_in!!)
            val ride: Ride = presenter.bikeModelMapper.mapIn(bike)
            ride.id =(presenter.uuid)
            ride.bikeId = (this.presenter.rideInUserCurrentStatus?.bikeId!!)
            ride.bike_booked_on= (this.presenter.rideInUserCurrentStatus?.bike_booked_on!!)
            ride.bike_expires_in = (this.presenter.rideInUserCurrentStatus?.bike_expires_in!!)
            ride.bike_on_call_operator=(this.presenter.rideInUserCurrentStatus?.bike_on_call_operator)
            ride.support_phone =(this.presenter.rideInUserCurrentStatus?.support_phone)
            presenter.saveRide(ride)
        } else if (presenter.currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_TRIP) {
            val ride: Ride = presenter.bikeModelMapper.mapIn(bike)
            ride.id =(presenter.uuid)
            ride.bikeId = (this.presenter.rideInUserCurrentStatus?.bikeId!!)
            ride.rideId = (this.presenter.rideInUserCurrentStatus?.rideId!!)
            ride.ride_booked_on= (this.presenter.rideInUserCurrentStatus?.ride_booked_on!!)
            ride.bike_on_call_operator=(this.presenter.rideInUserCurrentStatus?.bike_on_call_operator)
            ride.support_phone =(this.presenter.rideInUserCurrentStatus?.support_phone)
            ride.isFirst_lock_connect = (this.presenter.rideInUserCurrentStatus!!.isFirst_lock_connect)
            ride.do_not_track_trip= (this.presenter.rideInUserCurrentStatus?.do_not_track_trip)
            presenter.saveRide(ride)
        }
    }



    override fun onGetRideSummarySuccess(rideSummary: RideSummary) {
        presenter.rideInUserCurrentStatus?.id =(presenter.uuid)
        presenter.rideInUserCurrentStatus?.bikeId = if(rideSummary.bike_id!=null) rideSummary.bike_id!! else if(rideSummary.port_id!=null)rideSummary.port_id!! else if(rideSummary.hub_id!=null)rideSummary.hub_id!! else 0
        presenter.rideInUserCurrentStatus?.rideId = (rideSummary.trip_id)
        presenter.rideInUserCurrentStatus?.ride_booked_on = (rideSummary.date_created!!)
        presenter.rideInUserCurrentStatus?.do_not_track_trip=(rideSummary.do_not_track_trip)
        presenter.rideInUserCurrentStatus?.isFirst_lock_connect = (rideSummary.isFirst_lock_connect)
        if(rideSummary.port_id!=null){
            var port = ParkingHubHelper.getPortFromPortId(rideSummary.dockHub, rideSummary.port_id)
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
            presenter.getBikeDetails(presenter.rideInUserCurrentStatus?.bikeId!!)
        }
    }


    override fun onGetCurrentUserStatusFailure() {
        presenter.currentStatus === BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ERROR_FETCHING_STATUS
        onUserCurrentStatusFailure()
    }

    override fun onBikeDetailsFailure() {
        presenter.currentStatus === BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ERROR_FETCHING_STATUS
        onUserCurrentStatusFailure()
    }

    override fun onSaveRideFailure() {
        presenter.currentStatus === BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ERROR_FETCHING_STATUS
        onUserCurrentStatusFailure()
    }

    override fun onGetRideSummaryFailure() {
        presenter.currentStatus === BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ERROR_FETCHING_STATUS
        onUserCurrentStatusFailure()
    }

    override fun onNoInternet() {
        presenter.currentStatus === BaseUserCurrentStatusPresenter.Companion.CurrentStatus.NO_INTERNET
        onUserCurrentStatusFailure()
    }
}