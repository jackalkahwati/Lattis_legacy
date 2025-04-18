package com.lattis.data.repository.implementation.api

import com.lattis.data.database.store.RideRealmDataStore
import com.lattis.data.entity.body.bike.CancelBikeBody
import com.lattis.data.entity.body.bike.ControllerKeyBody
import com.lattis.data.entity.body.v2.BookingsBody
import com.lattis.data.entity.body.v2.StartTripBody
import com.lattis.data.mapper.BikeModelMapper
import com.lattis.data.mapper.V2RequestBikeMapper
import com.lattis.data.mapper.V2RequestRideMapper
import com.lattis.data.net.v2.V2ApiClient
import com.lattis.data.utils.GeneralHelper.getTodayAsDoubleTime
import com.lattis.data.utils.PortHubBikeHelper.getPortHubBikeQueryMap
import com.lattis.domain.models.*
import com.lattis.domain.repository.RideRepository
import com.lattis.domain.repository.V2ApiRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Named

class V2ApiRepositoryImp @Inject constructor(
    val v2ApiClient: V2ApiClient,
    val v2RequestBikeMapper: V2RequestBikeMapper,
    val v2RequestRideMapper:V2RequestRideMapper,
    val bikeModelMapper: BikeModelMapper,
    val rideRepository: RideRepository,
    val rideRealmDataStore: RideRealmDataStore,
    @param:Named("UUID")val uuid:String
) : V2ApiRepository{


    override fun bookings(bike: Bike,
                          by_scan:Boolean?,
                          latitude: Double?,
                          longitude: Double?,
                          device_token:String,
                          pricing_option_id:Int?
    ) : Observable<Ride>  {
        val hubPortBody = v2RequestBikeMapper.mapIn(bike)

        val ride = bikeModelMapper.mapIn(bike)
        return rideRepository.getRide().flatMap { oldRide ->
            ride.bike_on_call_operator = oldRide.bike_on_call_operator
            ride.support_phone = oldRide.support_phone
            v2ApiClient.api.booking(hubPortBody.endUrl, BookingsBody(by_scan,latitude,longitude,device_token,pricing_option_id,hubPortBody.bike_id,hubPortBody.hub_id,hubPortBody.port_id)).flatMap { reserveBikeResponse ->
                ride.id = uuid
                if(reserveBikeResponse.reserveBikeResponse!!.bike_id!=null) {
                    ride.bikeId = reserveBikeResponse.reserveBikeResponse!!.bike_id!!
                    ride.bike_id = reserveBikeResponse.reserveBikeResponse!!.bike_id!!
                }else if(reserveBikeResponse.reserveBikeResponse!!.port_id!=null){
                    ride.bikeId = reserveBikeResponse.reserveBikeResponse!!.port_id!!
                    ride.bike_id = reserveBikeResponse.reserveBikeResponse!!.port_id!!
                }else if (reserveBikeResponse.reserveBikeResponse!!.hub_id!=null){
                    ride.bikeId = reserveBikeResponse.reserveBikeResponse!!.hub_id!!
                    ride.bike_id = reserveBikeResponse.reserveBikeResponse!!.hub_id!!
                }
                ride.bike_booked_on = reserveBikeResponse.reserveBikeResponse!!.booked_on
                ride.bike_expires_in = reserveBikeResponse.reserveBikeResponse!!.expires_in
                ride.bike_booking_id = reserveBikeResponse.reserveBikeResponse!!.booking_id
                ride.device_type = reserveBikeResponse.reserveBikeResponse!!.device_type
                ride.trip_id = reserveBikeResponse.reserveBikeResponse!!.trip_id

                if (reserveBikeResponse.reserveBikeResponse!!.on_call_operator == null) {
                } else if (reserveBikeResponse.reserveBikeResponse!!.on_call_operator!!.equals("null", ignoreCase = true) || reserveBikeResponse.reserveBikeResponse!!.on_call_operator!!.equals("undefined", ignoreCase = true)) {
                } else {
                    ride.bike_on_call_operator = reserveBikeResponse.reserveBikeResponse!!.on_call_operator
                }
                rideRealmDataStore.createOrUpdateUser(ride)
            }
        }
    }

    override fun cancelBooking(bookingId: Int,
                               bike_damaged: Boolean,
                               lockIssue: Boolean): Observable<Boolean> {
        return v2ApiClient.api.cancelBooking(bookingId, CancelBikeBody(null,bike_damaged,lockIssue)).map { true }
    }

    override fun startTrip(ride: Ride,
                           location: Location,
                           first_lock_connect: Boolean,
                           device_token:String) : Observable<Ride> {
        return rideRealmDataStore.getRide().flatMap { oldRide: Ride ->
            val hubPortBody = v2RequestRideMapper.mapIn(ride)
            ride.bike_on_call_operator = (oldRide.bike_on_call_operator)
            ride.support_phone= (oldRide.support_phone)
            this.v2ApiClient.api.startTrip(StartTripBody(location.latitude,location.longitude,device_token,hubPortBody.bike_id,hubPortBody.hub_id,hubPortBody.port_id)).flatMap(
                { startRideResponse ->
                    ride.rideId =(startRideResponse.startRideDataResponse?.tripId!!)
                    ride.do_not_track_trip = (startRideResponse.startRideDataResponse?.do_not_track_trip)
                    if (first_lock_connect) {
                        ride.ride_booked_on = (getTodayAsDoubleTime())
                    } else {
                        ride.ride_booked_on = (oldRide.ride_booked_on)
                    }
                    ride.isFirst_lock_connect = (true)
                    rideRealmDataStore.createOrUpdateUser(ride)
                }
            )
        }
    }

    override fun getIoTBikeStatus(bike: Bike?, ride: Ride?,controller_id:Int): Observable<IoTBikeStatus> {
        val hubPortBody = if(ride!=null) v2RequestRideMapper.mapIn(ride) else if (bike!=null) v2RequestBikeMapper.mapIn(bike) else null
        return v2ApiClient.api.getIoTBikeStatus(controller_id,getPortHubBikeQueryMap(hubPortBody))
            .map {
                it.iotBikeBikeStatus
            }
    }

    override fun lockUnlockIotBike(lock:Boolean,bike: Bike?, ride: Ride?,controller_id:Int): Observable<IoTBikeLockUnlockCommandStatus> {
        val hubPortBody = if(ride!=null) v2RequestRideMapper.mapIn(ride) else if (bike!=null) v2RequestBikeMapper.mapIn(bike) else null
        return if(lock){
            v2ApiClient.api.lockIotBike(controller_id, hubPortBody!!)
        }else{
            v2ApiClient.api.unLockIotBike(controller_id, hubPortBody!!)
        }.map {
            if(it.ioTBikeLockUnlockCommandStatus==null){
                it.ioTBikeLockUnlockCommandStatus = IoTBikeLockUnlockCommandStatus(null,null,null,null,null,null)
            }
            it.ioTBikeLockUnlockCommandStatus
        }
    }
}