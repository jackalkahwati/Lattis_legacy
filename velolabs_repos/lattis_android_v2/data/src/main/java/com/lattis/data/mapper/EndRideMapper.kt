package com.lattis.data.mapper

import com.lattis.data.entity.response.ride.EndRideResponse
import com.lattis.domain.models.RideSummary
import javax.inject.Inject

class EndRideMapper @Inject
constructor() : AbstractDataMapper<EndRideResponse, RideSummary>() {


    override fun mapIn(endRideResponse: EndRideResponse?): RideSummary {
        var rideSummary = RideSummary()
        rideSummary.trip_id = endRideResponse?.rideSummaryDataResponse?.trip_id?:0
        rideSummary.steps = endRideResponse?.rideSummaryDataResponse?.steps
        rideSummary.start_address = endRideResponse?.rideSummaryDataResponse?.start_address
        rideSummary.date_created = endRideResponse?.rideSummaryDataResponse?.date_created
        rideSummary. date_endtrip = endRideResponse?.rideSummaryDataResponse?.date_endtrip
        rideSummary.parking_image = endRideResponse?.rideSummaryDataResponse?.parking_image
        rideSummary.rating = endRideResponse?.rideSummaryDataResponse?.rating?:0.toFloat()
        rideSummary.user_id= endRideResponse?.rideSummaryDataResponse?.user_id?:0
        rideSummary.operator_id= endRideResponse?.rideSummaryDataResponse?.operator_id?:0
        rideSummary.customer_id= endRideResponse?.rideSummaryDataResponse?.customer_id?:0
        rideSummary.lock_id= endRideResponse?.rideSummaryDataResponse?.lock_id?:0
        rideSummary.bike_id= endRideResponse?.rideSummaryDataResponse?.bike_id?:0
        rideSummary.fleet_id= endRideResponse?.rideSummaryDataResponse?.fleet_id?:0
        rideSummary. transaction_id = endRideResponse?.rideSummaryDataResponse?.transaction_id
        rideSummary. duration = endRideResponse?.rideSummaryDataResponse?.duration
        rideSummary. charge_for_duration = endRideResponse?.rideSummaryDataResponse?.charge_for_duration
        rideSummary. currency = endRideResponse?.rideSummaryDataResponse?.currency
        rideSummary. penalty_fees = endRideResponse?.rideSummaryDataResponse?.penalty_fees
        rideSummary. deposit = endRideResponse?.rideSummaryDataResponse?.deposit
        rideSummary. total = endRideResponse?.rideSummaryDataResponse?.total
        rideSummary. over_usage_fees = endRideResponse?.rideSummaryDataResponse?.over_usage_fees
        rideSummary. user_profile_id = endRideResponse?.rideSummaryDataResponse?.user_profile_id
        rideSummary. card_id = endRideResponse?.rideSummaryDataResponse?.card_id
        rideSummary. price_for_membership = endRideResponse?.rideSummaryDataResponse?.price_for_membership
        rideSummary. price_type_value = endRideResponse?.rideSummaryDataResponse?.price_type_value
        rideSummary. price_type = endRideResponse?.rideSummaryDataResponse?.price_type
        rideSummary. ride_deposit = endRideResponse?.rideSummaryDataResponse?.ride_deposit
        rideSummary. price_for_ride_deposit_type = endRideResponse?.rideSummaryDataResponse?.price_for_ride_deposit_type
        rideSummary. excess_usage_fees = endRideResponse?.rideSummaryDataResponse?.excess_usage_fees
        rideSummary. excess_usage_type_value = endRideResponse?.rideSummaryDataResponse?.excess_usage_type_value
        rideSummary. excess_usage_type = endRideResponse?.rideSummaryDataResponse?.excess_usage_type
        rideSummary. excess_usage_type_after_value = endRideResponse?.rideSummaryDataResponse?.excess_usage_type_after_value
        rideSummary. excess_usage_type_after_type = endRideResponse?.rideSummaryDataResponse?.excess_usage_type_after_type
        rideSummary. isFirst_lock_connect = endRideResponse?.rideSummaryDataResponse?.isFirst_lock_connect?:false
        rideSummary. do_not_track_trip = endRideResponse?.rideSummaryDataResponse?.do_not_track_trip
        rideSummary.price_for_bike_unlock = endRideResponse?.rideSummaryDataResponse?.price_for_bike_unlock
        rideSummary.membership_discount = endRideResponse?.rideSummaryDataResponse?.membership_discount
        rideSummary.promo_code_discount = endRideResponse?.rideSummaryDataResponse?.promo_code_discount
        rideSummary.promotionId = endRideResponse?.rideSummaryDataResponse?.promotion_id
        rideSummary.taxes = endRideResponse?.rideSummaryDataResponse?.taxes
        return rideSummary
    }

    override fun mapOut(out: RideSummary?): EndRideResponse? {
        return EndRideResponse()
    }

}