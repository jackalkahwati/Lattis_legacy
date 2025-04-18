package com.lattis.data.mapper

import com.lattis.data.entity.response.ride.RideSummaryResponse
import com.lattis.domain.models.RideSummary
import javax.inject.Inject

class RideSummaryMapper @Inject
constructor() : AbstractDataMapper<RideSummaryResponse, RideSummary>() {

    override fun mapIn(rideSummaryResponse: RideSummaryResponse?): RideSummary {
        var rideSummary = RideSummary()
        rideSummary.trip_id = rideSummaryResponse?.rideSummaryResponse?.trip_id?:0
        rideSummary.steps = rideSummaryResponse?.rideSummaryResponse?.steps
        rideSummary.start_address = rideSummaryResponse?.rideSummaryResponse?.start_address
        rideSummary.date_created = rideSummaryResponse?.rideSummaryResponse?.date_created
        rideSummary. date_endtrip = rideSummaryResponse?.rideSummaryResponse?.date_endtrip
        rideSummary.parking_image = rideSummaryResponse?.rideSummaryResponse?.parking_image
        rideSummary.rating = rideSummaryResponse?.rideSummaryResponse?.rating?:0.toFloat()
        rideSummary.user_id= rideSummaryResponse?.rideSummaryResponse?.user_id?:0
        rideSummary.operator_id= rideSummaryResponse?.rideSummaryResponse?.operator_id?:0
        rideSummary.customer_id= rideSummaryResponse?.rideSummaryResponse?.customer_id?:0
        rideSummary.lock_id= rideSummaryResponse?.rideSummaryResponse?.lock_id?:0
        rideSummary.bike_id= rideSummaryResponse?.rideSummaryResponse?.bike_id
        rideSummary.port_id= rideSummaryResponse?.rideSummaryResponse?.port_id
        rideSummary.hub_id= rideSummaryResponse?.rideSummaryResponse?.hub_id
        rideSummary.fleet_id= rideSummaryResponse?.rideSummaryResponse?.fleet_id?:0
        rideSummary. transaction_id = rideSummaryResponse?.rideSummaryResponse?.transaction_id
        rideSummary. duration = rideSummaryResponse?.rideSummaryResponse?.duration
        rideSummary. charge_for_duration = rideSummaryResponse?.rideSummaryResponse?.charge_for_duration
        rideSummary. currency = rideSummaryResponse?.rideSummaryResponse?.currency
        rideSummary. penalty_fees = rideSummaryResponse?.rideSummaryResponse?.penalty_fees
        rideSummary. deposit = rideSummaryResponse?.rideSummaryResponse?.deposit
        rideSummary. total = rideSummaryResponse?.rideSummaryResponse?.total
        rideSummary. over_usage_fees = rideSummaryResponse?.rideSummaryResponse?.over_usage_fees
        rideSummary. user_profile_id = rideSummaryResponse?.rideSummaryResponse?.user_profile_id
        rideSummary. card_id = rideSummaryResponse?.rideSummaryResponse?.card_id
        rideSummary. price_for_membership = rideSummaryResponse?.rideSummaryResponse?.price_for_membership
        rideSummary. price_type_value = rideSummaryResponse?.rideSummaryResponse?.price_type_value
        rideSummary. price_type = rideSummaryResponse?.rideSummaryResponse?.price_type
        rideSummary. ride_deposit = rideSummaryResponse?.rideSummaryResponse?.ride_deposit
        rideSummary. price_for_ride_deposit_type = rideSummaryResponse?.rideSummaryResponse?.price_for_ride_deposit_type
        rideSummary. excess_usage_fees = rideSummaryResponse?.rideSummaryResponse?.excess_usage_fees
        rideSummary. excess_usage_type_value = rideSummaryResponse?.rideSummaryResponse?.excess_usage_type_value
        rideSummary. excess_usage_type = rideSummaryResponse?.rideSummaryResponse?.excess_usage_type
        rideSummary. excess_usage_type_after_value = rideSummaryResponse?.rideSummaryResponse?.excess_usage_type_after_value
        rideSummary. excess_usage_type_after_type = rideSummaryResponse?.rideSummaryResponse?.excess_usage_type_after_type
        rideSummary. isFirst_lock_connect = rideSummaryResponse?.rideSummaryResponse?.isFirst_lock_connect?:false
        rideSummary. do_not_track_trip = rideSummaryResponse?.rideSummaryResponse?.do_not_track_trip
        rideSummary.price_for_bike_unlock = rideSummaryResponse?.rideSummaryResponse?.price_for_bike_unlock
        rideSummary.dockHub = rideSummaryResponse?.rideSummaryPayloadResponse?.dockHub
        rideSummary.taxes = rideSummaryResponse?.rideSummaryResponse?.taxes
        return rideSummary
    }

    override fun mapOut(out: RideSummary?): RideSummaryResponse? {
        return RideSummaryResponse()
    }
}