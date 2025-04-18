package com.lattis.data.mapper

import android.text.TextUtils
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Promotion
import com.lattis.domain.models.Ride
import javax.inject.Inject

class BikeModelMapper @Inject constructor() : AbstractDataMapper<Bike, Ride>() {
    override fun mapIn(bike: Bike?): Ride {
        val ride = Ride()
        if (bike != null) {

            ride.bike_booking_id = bike.bike_booking_id
            ride.bike_id = bike.bike_id
            ride.port_id = bike.port_id
            ride.hub_id = bike.hub_id
            ride.device_type = bike.device_type
            ride.trip_id = bike.trip_id

            ride.bike_bike_name = bike.bike_name
            ride.bike_make = bike.make
            ride.bike_model = bike.model
            ride.bike_type = bike.type
            ride.bike_description = bike.description
            ride.bike_date_created = bike.date_created
            ride.bike_status = bike.status
            ride.bike_battery_level = bike.battery_level
            ride.bike_bike_battery_level = bike.bike_battery_level
            ride.bike_current_status = bike.current_status
            ride.bike_maintenance_status = bike.maintenance_status
            ride.bike_pic = bike.pic
            ride.bike_distance = bike.distance
            ride.bike_latitude = bike.latitude
            ride.bike_lock_id = bike.lock_id
            ride.bike_longitude = bike.longitude
            ride.bike_fleet_id = bike.fleet_id
            ride.bike_parking_spot_id = bike.parking_spot_id
            ride.bike_user_id = bike.user_id
            ride.bike_mac_id = bike.mac_id
            ride.bike_name = bike.bike_name
            ride.bike_bike_operator_id = bike.operator_id
            ride.bike_customer_id = bike.customer_id
            ride.bike_customer_name = bike.customer_name
            ride.bike_hub_id = bike.hub_id
            ride.bike_bike_fleet_key = bike.fleet_key
            ride.bike_fleet_logo = bike.fleet_logo
            ride.bike_fleet_name = bike.fleet_name
            ride.bike_tariff = bike.tariff
            ride.bike_isBikeBooked = bike.isBikeBooked
            ride.bike_price_for_membership = bike.price_for_membership
            ride.bike_price_type_value = bike.price_type_value
            ride.bike_price_type = bike.price_type
            ride.bike_ride_deposit = bike.ride_deposit
            ride.bike_price_for_ride_deposit = bike.price_for_ride_deposit
            ride.bike_price_for_ride_deposit_type = bike.price_for_ride_deposit_type
            ride.bike_excess_usage_fees = bike.excess_usage_fees
            ride.bike_excess_usage_type_value = bike.excess_usage_type_value
            ride.bike_excess_usage_type = bike.excess_usage_type
            ride.bike_excess_usage_type_after_value = bike.excess_usage_type_after_value
            ride.bike_excess_usage_type_after_type = bike.excess_usage_type_after_type
            ride.bike_terms_condition_url = bike.fleet_t_and_c
            ride.bike_fleet_type = bike.fleet_type
            ride.bike_skip_parking_image = bike.skip_parking_image
            ride.bike_max_trip_length = bike.max_trip_length
            ride.bike_usage_surcharge = bike.usage_surcharge
            ride.currency = bike.currency
            ride.price_for_bike_unlock = bike.price_for_bike_unlock
            ride.price_for_penalty_outside_parking = bike.price_for_penalty_outside_parking
            ride.bike_uuid = bike.bike_uuid
            ride.bike_originalTypeOfObject = bike.originalTypeOfObject
            if(bike.controllers!=null && bike.controllers?.size!!>0){
                var controllers : ArrayList<Ride.Controller> = ArrayList()
                for(bikeController in bike.controllers!!){
                    var controller = Ride.Controller()
                    controller.added_by_operator_id = bikeController.added_by_operator_id
                    controller.battery_level = bikeController.battery_level
                    controller.bike_id = bikeController.bike_id
                    controller.controller_id = bikeController.controller_id
                    controller.date_created = bikeController.date_created
                    controller.device_type = bikeController.device_type
                    controller.fleet_id = bikeController.fleet_id
                    controller.fw_version = bikeController.fw_version
                    controller.gps_log_time = bikeController.gps_log_time
                    controller.hw_version = bikeController.hw_version
                    controller.key = bikeController.key
                    controller.latitude = bikeController.latitude
                    controller.longitude = bikeController.longitude
                    controller.make = bikeController.make
                    controller.model  = bikeController.model
                    controller.qr_code = bikeController.qr_code
                    controller.status = bikeController.status
                    controller.vendor = bikeController.vendor
                    if(!TextUtils.isEmpty(controller.key) && !TextUtils.isEmpty(controller.vendor) && "AXA".equals(controller.vendor,true)){
                        ride.bike_mac_id = "AXA:"+controller.key
                    }else if(!TextUtils.isEmpty(controller.key) && !TextUtils.isEmpty(controller.vendor) &&
                        ("TAPKEY".equals(controller.vendor,true) || "TAP KEY".equals(controller.vendor,true))){
                        ride.bike_mac_id = controller.key
                    }else if(!TextUtils.isEmpty(controller.key) && !TextUtils.isEmpty(controller.vendor) && "sas".equals(controller.vendor,true)){
                        ride.bike_mac_id = controller.key
                    }
                    controllers.add(controller)
                }
                ride.controllers =controllers
            }
            ride.qr_code_id = bike.qr_code_id
            if(bike.pricing_options!=null && bike.pricing_options?.size!!>0){
                ride.pricing_options = ArrayList()
                for(pricing_option in bike.pricing_options!!){
                    var ridePricingOptions = Ride.Pricing_options()
                    ridePricingOptions.created_at = pricing_option?.created_at
                    ridePricingOptions.deactivated_at = pricing_option?.deactivated_at
                    ridePricingOptions.deactivation_reason = pricing_option?.deactivation_reason
                    ridePricingOptions.duration = pricing_option?.duration
                    ridePricingOptions.duration_unit = pricing_option?.duration_unit
                    ridePricingOptions.fleet_id = pricing_option?.fleet_id
                    ridePricingOptions.grace_period = pricing_option?.grace_period
                    ridePricingOptions.grace_period_unit = pricing_option?.grace_period_unit
                    ridePricingOptions.price = pricing_option?.price
                    ridePricingOptions.price_currency = pricing_option?.price_currency
                    ridePricingOptions.pricing_option_id = pricing_option?.pricing_option_id
                    ride.pricing_options?.add(ridePricingOptions)

                }
            }

            if(bike.promotions!=null && bike.promotions?.size!!>0){
                ride.promotions = ArrayList()
                for(promotion in bike.promotions!!){
                    val promotion = Promotion()
                    promotion.promotion_id = promotion?.promotion_id
                    promotion.created_at = promotion?.created_at
                    promotion.promotion_code = promotion?.promotion_code
                    promotion.deactivated_at = promotion?.deactivated_at
                    promotion.amount = promotion?.amount
                    promotion.fleet_id = promotion?.fleet_id
                    promotion.usage = promotion?.usage
                    if(promotion?.promotion_users!=null){
                        val PromotionUsers: Promotion.PromotionUsers = Promotion.PromotionUsers()
                        PromotionUsers.promotion_id = promotion?.promotion_users?.promotion_id
                        PromotionUsers.promotion_users_id = promotion?.promotion_users?.promotion_users_id
                        PromotionUsers.user_id = promotion?.promotion_users?.user_id
                        PromotionUsers.claimed_at = promotion?.promotion_users?.claimed_at
                        promotion?.promotion_users = PromotionUsers
                    }
                    ride?.promotions?.add(promotion)
                }
            }

        }
        return ride
    }

    override fun mapOut(ride: Ride?): Bike {
        val bike = Bike()
        ride?.let {

            bike.bike_booking_id = ride.bike_booking_id
            bike.bike_id = ride.bike_id?:0
            bike.port_id = ride.port_id
            bike.hub_id = ride.hub_id?:0
            bike.device_type = ride.device_type
            bike.trip_id = ride.trip_id

            bike.bike_name = ride.bike_bike_name
            bike.bike_name = ride.bike_make
            bike.model = ride.bike_model
            bike.type = ride.bike_type
            bike.description = ride.bike_description
            bike.date_created = ride.bike_date_created
            bike.status = ride.bike_status
            bike.battery_level = ride.bike_battery_level
            bike.bike_battery_level = ride.bike_bike_battery_level
            bike.current_status = ride.bike_current_status
            bike.maintenance_status = ride.bike_maintenance_status
            bike.pic = ride.bike_pic
            bike.distance = ride.bike_distance
            bike.latitude = ride.bike_latitude
            bike.lock_id = ride.bike_lock_id
            bike.longitude = ride.bike_longitude
            bike.fleet_id = ride.bike_fleet_id
            bike.parking_spot_id = ride.bike_parking_spot_id
            bike.user_id = ride.bike_user_id
            bike.mac_id = ride.bike_mac_id
            bike.name = ride.bike_name
            bike.operator_id = ride.bike_bike_operator_id
            bike.customer_id = ride.bike_customer_id
            bike.customer_name = ride.bike_customer_name
            bike.hub_id = ride.bike_hub_id
            bike.fleet_key = ride.bike_bike_fleet_key
            bike.fleet_logo = ride.bike_fleet_logo
            bike.fleet_name = ride.bike_fleet_name
            bike.tariff = ride.bike_tariff
            bike.isBikeBooked = ride.bike_isBikeBooked!!
            bike.price_for_membership = ride.bike_price_for_membership
            bike.price_type_value = ride.bike_price_type_value
            bike.price_type = ride.bike_price_type
            bike.ride_deposit = ride.bike_ride_deposit
            bike.price_for_ride_deposit = ride.bike_price_for_ride_deposit
            bike.price_for_ride_deposit_type = ride.bike_price_for_ride_deposit_type
            bike.excess_usage_fees = ride.bike_excess_usage_fees
            bike.excess_usage_type_value = ride.bike_excess_usage_type_value
            bike.excess_usage_type = ride.bike_excess_usage_type
            bike.excess_usage_type_after_value = ride.bike_excess_usage_type_after_value
            bike.excess_usage_type_after_type = ride.bike_excess_usage_type_after_type
            bike.fleet_t_and_c = ride.bike_terms_condition_url
            bike.fleet_type = ride.bike_fleet_type
            bike.skip_parking_image = (ride.bike_skip_parking_image)
            bike.max_trip_length = ride.bike_max_trip_length
            bike.usage_surcharge = ride.bike_usage_surcharge
            bike.currency = ride.currency
            bike.price_for_bike_unlock = ride.price_for_bike_unlock
            bike.price_for_penalty_outside_parking = ride.price_for_penalty_outside_parking
            bike.bike_uuid = ride.bike_uuid
            bike.originalTypeOfObject = ride.bike_originalTypeOfObject
            if(ride.controllers!=null && ride.controllers?.size!!>0){
                var controllers : ArrayList<Bike.Controller> = ArrayList()
                for(rideController in ride.controllers!!){
                    var controller = Bike.Controller()
                    controller.added_by_operator_id = rideController.added_by_operator_id
                    controller.battery_level = rideController.battery_level
                    controller.bike_id = rideController.bike_id
                    controller.controller_id = rideController.controller_id
                    controller.date_created = rideController.date_created
                    controller.device_type = rideController.device_type
                    controller.fleet_id = rideController.fleet_id
                    controller.fw_version = rideController.fw_version
                    controller.gps_log_time = rideController.gps_log_time
                    controller.hw_version = rideController.hw_version
                    controller.key = rideController.key
                    controller.latitude = rideController.latitude
                    controller.longitude = rideController.longitude
                    controller.make = rideController.make
                    controller.model  = rideController.model
                    controller.qr_code = rideController.qr_code
                    controller.status = rideController.status
                    controller.vendor = rideController.vendor
                    controllers.add(controller)
                }
                bike.controllers =controllers
            }
            bike.qr_code_id = ride.qr_code_id
            if(ride.pricing_options!=null && ride.pricing_options?.size!!>0){
                bike.pricing_options = ArrayList()
                for(pricing_option in ride.pricing_options!!){
                    var bikePricingOptions = Bike.Pricing_options()
                    bikePricingOptions.created_at = pricing_option?.created_at
                    bikePricingOptions.deactivated_at = pricing_option?.deactivated_at
                    bikePricingOptions.deactivation_reason = pricing_option?.deactivation_reason
                    bikePricingOptions.duration = pricing_option?.duration
                    bikePricingOptions.duration_unit = pricing_option?.duration_unit
                    bikePricingOptions.fleet_id = pricing_option?.fleet_id
                    bikePricingOptions.grace_period = pricing_option?.grace_period
                    bikePricingOptions.grace_period_unit = pricing_option?.grace_period_unit
                    bikePricingOptions.price = pricing_option?.price
                    bikePricingOptions.price_currency = pricing_option?.price_currency
                    bikePricingOptions.pricing_option_id = pricing_option?.pricing_option_id
                    bike.pricing_options?.add(bikePricingOptions)

                }
            }
            if(ride.promotions!=null && ride.promotions?.size!!>0){
                bike.promotions = ArrayList()
                for(promotion in ride.promotions!!){
                    val promotion = Promotion()
                    promotion.promotion_id = promotion?.promotion_id
                    promotion.created_at = promotion?.created_at
                    promotion.promotion_code = promotion?.promotion_code
                    promotion.deactivated_at = promotion?.deactivated_at
                    promotion.amount = promotion?.amount
                    promotion.fleet_id = promotion?.fleet_id
                    promotion.usage = promotion?.usage
                    if(promotion?.promotion_users!=null){
                        val PromotionUsers: Promotion.PromotionUsers = Promotion.PromotionUsers()
                        PromotionUsers.promotion_id = promotion?.promotion_users?.promotion_id
                        PromotionUsers.promotion_users_id = promotion?.promotion_users?.promotion_users_id
                        PromotionUsers.user_id = promotion?.promotion_users?.user_id
                        PromotionUsers.claimed_at = promotion?.promotion_users?.claimed_at
                        promotion?.promotion_users = PromotionUsers
                    }
                    bike?.promotions?.add(promotion)
                }
            }
        }
        return bike
    }
}