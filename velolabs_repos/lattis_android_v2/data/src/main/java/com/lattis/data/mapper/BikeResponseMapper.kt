package com.lattis.data.mapper

import android.text.TextUtils
import com.lattis.data.entity.response.bike.FindBikeDataResponse
import com.lattis.domain.models.Bike

import javax.inject.Inject

class BikeResponseMapper @Inject
constructor() : AbstractDataMapper<FindBikeDataResponse, Bike>() {

    override fun mapIn(findBikeDataResponse: FindBikeDataResponse?): Bike {
        val bike = Bike()
        findBikeDataResponse?.let {
            bike.bike_id = findBikeDataResponse?.bike_id
            bike.bike_name = findBikeDataResponse?.bike_name
            bike.make = findBikeDataResponse?.make
            bike.model = findBikeDataResponse?.model
            bike.type = findBikeDataResponse?.type
            bike.description = findBikeDataResponse?.description
            bike.date_created = findBikeDataResponse?.date_created
            bike.status = findBikeDataResponse?.status
            bike.battery_level = findBikeDataResponse?.battery_level
            bike.bike_battery_level = findBikeDataResponse?.bike_battery_level
            bike.current_status = findBikeDataResponse?.current_status
            bike.maintenance_status = findBikeDataResponse?.maintenance_status
            bike.lock_id = findBikeDataResponse?.lock_id
            bike.fleet_id = findBikeDataResponse?.fleet_id
            bike.user_id = findBikeDataResponse?.user_id
            bike.mac_id = findBikeDataResponse?.mac_id
            bike.name = findBikeDataResponse?.name
            bike.operator_id = findBikeDataResponse?.operator_id
            bike.customer_id = findBikeDataResponse?.customer_id
            bike.customer_name = findBikeDataResponse?.customer_name
            bike.hub_id = findBikeDataResponse?.hub_id
            bike.latitude = findBikeDataResponse?.latitude
            bike.longitude = findBikeDataResponse?.longitude
            bike.fleet_id = findBikeDataResponse?.fleet_id
            bike.fleet_name = findBikeDataResponse?.fleet_name
            bike.fleet_logo = findBikeDataResponse?.fleet_logo
            bike.pic = findBikeDataResponse?.pic
            bike.fleet_key = findBikeDataResponse?.fleet_key
            bike.tariff = findBikeDataResponse?.tariff
            bike.fleet_bikes = findBikeDataResponse?.fleet_bikes
            bike.fleet_parking_spots = findBikeDataResponse?.fleet_parking_spots
            bike.price_for_membership = findBikeDataResponse?.price_for_membership
            bike.distance = findBikeDataResponse?.distance
            bike.price_for_membership = findBikeDataResponse?.price_for_membership
            bike.price_type_value = findBikeDataResponse?.price_type_value
            bike.price_type = findBikeDataResponse?.price_type
            bike.ride_deposit = findBikeDataResponse?.ride_deposit
            bike.price_for_ride_deposit = findBikeDataResponse?.price_for_ride_deposit
            bike.price_for_ride_deposit_type = findBikeDataResponse?.price_for_ride_deposit_type
            bike.excess_usage_fees = findBikeDataResponse?.excess_usage_fees
            bike.excess_usage_type_value = findBikeDataResponse?.excess_usage_type_value
            bike.excess_usage_type = findBikeDataResponse?.excess_usage_type
            bike.excess_usage_type_after_value = findBikeDataResponse?.excess_usage_type_after_value
            bike.excess_usage_type_after_type = findBikeDataResponse?.excess_usage_type_after_type
            bike.fleet_t_and_c = findBikeDataResponse?.terms_condition_url
            bike.fleet_type = findBikeDataResponse?.fleet_type
            bike.skip_parking_image = findBikeDataResponse?.skip_parking_image
            bike.max_trip_length = findBikeDataResponse?.max_trip_length
            bike.usage_surcharge = findBikeDataResponse?.usage_surcharge
            bike.currency = findBikeDataResponse?.currency
            bike.require_phone_number = findBikeDataResponse?.isRequire_phone_number
            bike.isDo_not_track_trip = findBikeDataResponse?.isDo_not_track_trip
            bike.price_for_bike_unlock = findBikeDataResponse?.price_for_bike_unlock
            bike.price_for_penalty_outside_parking = findBikeDataResponse?.price_for_penalty_outside_parking
            bike.bike_uuid = findBikeDataResponse?.bike_uuid
            bike.controllers = findBikeDataResponse.controllers
            if(bike.controllers!=null && bike.controllers?.size!!>0){
                for (controller in bike.controllers!!){
                    if(!TextUtils.isEmpty(controller.key) && !TextUtils.isEmpty(controller.vendor) && "AXA".equals(controller.vendor,true)){
                        bike.mac_id = "AXA:"+controller.key
                    }else if(!TextUtils.isEmpty(controller.key) && !TextUtils.isEmpty(controller.vendor) &&
                        ("TAPKEY".equals(controller.vendor,true) || "TAP KEY".equals(controller.vendor,true))){
                        bike.mac_id = controller.key
                    }else if(!TextUtils.isEmpty(controller.key) && !TextUtils.isEmpty(controller.vendor) && "sas".equals(controller.vendor,true)){
                        bike.mac_id = controller.key
                    }
                }
            }
            bike.qr_code_id = findBikeDataResponse?.qr_code_id.toString()?:null
            bike.payment_gateway = findBikeDataResponse?.payment_gateway
            bike.enable_preauth = findBikeDataResponse.enable_preauth
            bike.preauth_amount = findBikeDataResponse.preauth_amount
            bike.pricing_options = findBikeDataResponse.pricing_options
            bike.promotions = findBikeDataResponse.promotions
            bike.reservation = findBikeDataResponse.reservation
        }

        return bike
    }

    override fun mapOut(bike: Bike?): FindBikeDataResponse? {
        return null
    }
}
