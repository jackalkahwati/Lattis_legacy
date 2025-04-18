package com.lattis.data.repository.implementation.api

import com.lattis.data.database.store.RideRealmDataStore
import com.lattis.data.entity.body.ride.*
import com.lattis.data.mapper.BikeModelMapper
import com.lattis.data.mapper.EndRideMapper
import com.lattis.data.mapper.RideSummaryMapper
import com.lattis.data.net.ride.RideApiClient
import com.lattis.domain.models.RideSummary
import com.lattis.domain.models.UpdateTripData
import com.lattis.domain.repository.RideRepository
import com.lattis.domain.models.Location
import com.lattis.domain.models.Ride
import com.lattis.domain.models.RideHistory
import io.reactivex.rxjava3.core.Observable
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class RideRepositoryImp @Inject
constructor(
    val rideRealmDataStore: RideRealmDataStore,
    @param:Named("UUID") private val uuid: String,
    val bikeModelMapper: BikeModelMapper,
    val rideApiClient: RideApiClient,
    val rideSummaryMapper: RideSummaryMapper,
    val endRideMapper: EndRideMapper
) : RideRepository {

    internal val doubleTime: Long
        get() {
            val dte = Date()
            return dte.time / 1000
        }

    override fun getRide(): Observable<Ride> {
        return rideRealmDataStore.getRide()
    }

    override fun saveRide(ride: Ride): Observable<Ride> {
        return rideRealmDataStore.createOrUpdateUser(ride)
    }

    override fun deleteRide(): Observable<Boolean> {
        return rideRealmDataStore.deleteRide()
    }

    override fun getRideSummary(trip_id: Int): Observable<RideSummary> {
        return this.rideApiClient.api.rideSummary(RideSummaryBody( trip_id)).
            map {
                rideSummaryMapper.mapIn(it)
            }
    }

    override fun updateRide(trip_id: Int, steps: Array<DoubleArray>): Observable<UpdateTripData> {
        return this.rideApiClient.api.updateRide(UpdateRideBody(trip_id, steps)).map {
            if(it.updateTripDataResponse!=null && it.updateTripDataResponse?.updateTripEndedResponse!=null){
                UpdateTripData(it.updateTripDataResponse?.duration,
                    it.updateTripDataResponse?.charge_for_duration,
                    it.updateTripDataResponse?.currency,
                    it.updateTripDataResponse?.updateTripEndedResponse?.date_endtrip,
                    it.updateTripDataResponse?.bike_battery_level
                )
            }else if(it.updateTripDataResponse!=null){
                UpdateTripData(it.updateTripDataResponse?.duration,
                    it.updateTripDataResponse?.charge_for_duration,
                    it.updateTripDataResponse?.currency,
                    null,
                    it.updateTripDataResponse?.bike_battery_level
                )
            }else{
                UpdateTripData(null,
                    null,
                    null,
                    null,
                    null
                )
            }
        }
    }


    override fun startRide(
        ride: Ride,
        location: Location,
        first_lock_connect: Boolean,
        device_token:String
    ): Observable<Ride> {
        return getRide().flatMap { oldRide: Ride ->
            ride.bike_on_call_operator = (oldRide.bike_on_call_operator)
            ride.support_phone= (oldRide.support_phone)
            this.rideApiClient.api.startRide(StartRideBody( ride.bikeId, location,device_token)).flatMap(
                { startRideResponse ->
                    ride.rideId =(startRideResponse.startRideDataResponse?.tripId!!)
                    ride.do_not_track_trip = (startRideResponse.startRideDataResponse?.do_not_track_trip)
                    if (first_lock_connect) {
                        ride.ride_booked_on = (getDoubleTime())
                    } else {
                        ride.ride_booked_on = (oldRide.ride_booked_on)
                    }
                    ride.isFirst_lock_connect = (true)
                    rideRealmDataStore.createOrUpdateUser(ride)
                }
            )
        }
    }

    override fun endRide(
        trip_id: Int,
        location: Location?,
        parkingId: Int,
        imageURL: String?,
        isReportDamage: Boolean,
        lock_battery: Int?,
        bike_battery: Int?
    ): Observable<RideSummary> {
        return rideApiClient.api.endRide(
            EndRideBody(
            trip_id,
            location,
            parkingId,
            imageURL,
            isReportDamage,
            lock_battery,
            bike_battery
            )
        ).map {
            endRideMapper.mapIn(it)
        }
    }

    override fun rateRide(trip_id: Int, rating: Int): Observable<Boolean> {
        return rideApiClient.api.rateRide(RideRatingBody(trip_id,rating))
            .map { true }
    }

    override fun getRideHistory(): Observable<RideHistory> {
        return rideApiClient.api.getRideHistory()
    }

    fun getDoubleTime(): Long {
        val dte = Date()
        return dte.time / 1000
    }
}