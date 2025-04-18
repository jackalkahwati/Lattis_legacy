package com.lattis.data.database.mapper


import com.lattis.data.database.base.AbstractRealmDataMapper
import com.lattis.data.database.model.*
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Promotion
import com.lattis.domain.models.Ride
import io.realm.RealmList

import javax.inject.Inject

/**
 * Created by ssd3 on 4/4/17.
 */

class RealmRideMapper @Inject
constructor() : AbstractRealmDataMapper<Ride, RealmRide>() {

    override fun mapIn(ride: Ride): RealmRide {

        val realmRide = RealmRide()
        realmRide.id = ride.id
        realmRide.bikeId = ride.bikeId
        realmRide.bike_booked_on = ride.bike_booked_on
        realmRide.bike_expires_in = ride.bike_expires_in
        realmRide.bike_booking_id = ride.bike_booking_id
        realmRide.bike_id = ride.bike_id
        realmRide.port_id = ride.port_id
        realmRide.hub_id = ride.hub_id
        realmRide.device_type = ride.device_type
        realmRide.trip_id = ride.trip_id


        realmRide.rideId = ride.rideId
        realmRide.ride_booked_on = ride.ride_booked_on

        realmRide.bike_bike_name = ride.bike_bike_name
        realmRide.bike_make = ride.bike_make
        realmRide.bike_model = ride.bike_model
        realmRide.bike_type = ride.bike_type
        realmRide.bike_description = ride.bike_description
        realmRide.bike_date_created = ride.bike_date_created
        realmRide.bike_status = ride.bike_status
        realmRide.bike_battery_level = ride.bike_battery_level
        realmRide.bike_bike_battery_level = ride.bike_bike_battery_level
        realmRide.bike_current_status = ride.bike_current_status
        realmRide.bike_maintenance_status = ride.bike_maintenance_status
        realmRide.bike_pic = ride.bike_pic
        realmRide.bike_distance = ride.bike_distance
        realmRide.bike_latitude = ride.bike_latitude
        realmRide.bike_lock_id = ride.bike_lock_id
        realmRide.bike_longitude = ride.bike_longitude
        realmRide.bike_fleet_id = ride.bike_fleet_id
        realmRide.bike_parking_spot_id = ride.bike_parking_spot_id
        realmRide.bike_user_id = ride.bike_user_id
        realmRide.bike_mac_id = ride.bike_mac_id
        realmRide.bike_name = ride.bike_name
        realmRide.bike_bike_operator_id = ride.bike_bike_operator_id
        realmRide.bike_customer_id = ride.bike_customer_id
        realmRide.bike_customer_name = ride.bike_customer_name
        realmRide.bike_hub_id = ride.bike_hub_id
        realmRide.bike_bike_operator_email = ride.bike_bike_fleet_key
        realmRide.bike_fleet_logo = ride.bike_fleet_logo
        realmRide.bike_fleet_name = ride.bike_fleet_name
        realmRide.bike_tariff = ride.bike_tariff
        realmRide.bike_isBikeBooked = ride.isBike_isBikeBooked()

        realmRide.bike_on_call_operator = ride.bike_on_call_operator
        realmRide.support_phone = ride.support_phone


        realmRide.bike_price_for_membership = ride.bike_price_for_membership
        realmRide.bike_price_type_value = ride.bike_price_type_value
        realmRide.bike_price_type = ride.bike_price_type
        realmRide.bike_ride_deposit = ride.bike_ride_deposit
        realmRide.bike_price_for_ride_deposit = ride.bike_price_for_ride_deposit
        realmRide.bike_price_for_ride_deposit_type = ride.bike_price_for_ride_deposit_type
        realmRide.bike_excess_usage_fees = ride.bike_excess_usage_fees
        realmRide.bike_excess_usage_type_value = ride.bike_excess_usage_type_value
        realmRide.bike_excess_usage_type = ride.bike_excess_usage_type
        realmRide.bike_excess_usage_type_after_value = ride.bike_excess_usage_type_after_value
        realmRide.bike_excess_usage_type_after_type = ride.bike_excess_usage_type_after_type

        realmRide.bike_terms_condition_url = ride.bike_terms_condition_url
        realmRide.bike_fleet_type = ride.bike_fleet_type
        realmRide.bike_skip_parking_image = ride.bike_skip_parking_image
        realmRide.bike_max_trip_length = ride.bike_max_trip_length

        realmRide.isFirst_lock_connect = ride.isFirst_lock_connect
        realmRide.currency = ride.currency

        realmRide.do_not_track_trip = ride.do_not_track_trip
        realmRide.price_for_bike_unlock = ride.price_for_bike_unlock
        realmRide.price_for_penalty_outside_parking = ride.price_for_penalty_outside_parking
        realmRide.payment_gateway = ride.payment_gateway


        if(ride.controllers!=null && ride.controllers?.size!!>0){
            var controllers : RealmList<RealmBikeController> = RealmList()
            for(rideController in ride.controllers!!){
                var controller = RealmBikeController()
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
            realmRide.controllers =controllers
        }

        realmRide.qr_code_id = ride.qr_code_id
        realmRide.dock_hub_bike_docked = ride.dock_hub_bike_docked
        realmRide.bike_uuid = ride.bike_uuid
        realmRide.bike_originalTypeOfObject = ride.bike_originalTypeOfObject

        if(ride.pricing_options!=null && ride.pricing_options?.size!!>0){
            realmRide.pricing_options = RealmList()
            for(pricing_option in ride.pricing_options!!){
                var realmRidePricingOptions = RealmPricingOption()
                realmRidePricingOptions.created_at = pricing_option?.created_at
                realmRidePricingOptions.deactivated_at = pricing_option?.deactivated_at
                realmRidePricingOptions.deactivation_reason = pricing_option?.deactivation_reason
                realmRidePricingOptions.duration = pricing_option?.duration
                realmRidePricingOptions.duration_unit = pricing_option?.duration_unit
                realmRidePricingOptions.fleet_id = pricing_option?.fleet_id
                realmRidePricingOptions.grace_period = pricing_option?.grace_period
                realmRidePricingOptions.grace_period_unit = pricing_option?.grace_period_unit
                realmRidePricingOptions.price = pricing_option?.price
                realmRidePricingOptions.price_currency = pricing_option?.price_currency
                realmRidePricingOptions.pricing_option_id = pricing_option?.pricing_option_id
                realmRide.pricing_options?.add(realmRidePricingOptions)
            }
        }

        if(ride.promotions!=null && ride.promotions?.size!!>0){
            realmRide.promotions = RealmList()
            for(promotion in ride.promotions!!){
                val realmPromotion = RealmPromotion()
                realmPromotion.promotion_id = promotion?.promotion_id
                realmPromotion.created_at = promotion?.created_at
                realmPromotion.promotion_code = promotion?.promotion_code
                realmPromotion.deactivated_at = promotion?.deactivated_at
                realmPromotion.amount = promotion?.amount
                realmPromotion.fleet_id = promotion?.fleet_id
                realmPromotion.usage = promotion?.usage
                if(promotion?.promotion_users!=null){
                    val realmPromotionUsers: RealmPromotionUsers = RealmPromotionUsers()
                    realmPromotionUsers.promotion_id = promotion?.promotion_users?.promotion_id
                    realmPromotionUsers.promotion_users_id = promotion?.promotion_users?.promotion_users_id
                    realmPromotionUsers.user_id = promotion?.promotion_users?.user_id
                    realmPromotionUsers.claimed_at = promotion?.promotion_users?.claimed_at
                    realmPromotion?.promotion_users = realmPromotionUsers
                }
                realmRide?.promotions?.add(realmPromotion)
            }
        }

        return realmRide
    }

    override fun mapOut(realmRide: RealmRide): Ride {
        val ride = Ride()
        ride.id = realmRide.id
        ride.bikeId = realmRide.bikeId
        ride.bike_booked_on = realmRide.bike_booked_on
        ride.bike_expires_in = realmRide.bike_expires_in
        ride.bike_booking_id = realmRide.bike_booking_id
        ride.bike_id = realmRide.bike_id
        ride.port_id = realmRide.port_id
        ride.hub_id = realmRide.hub_id
        ride.device_type = realmRide.device_type
        ride.trip_id = realmRide.trip_id



        ride.rideId = realmRide.rideId
        ride.ride_booked_on = realmRide.ride_booked_on


        ride.bike_bike_name = realmRide.bike_bike_name
        ride.bike_make = realmRide.bike_make
        ride.bike_model = realmRide.bike_model
        ride.bike_type = realmRide.bike_type
        ride.bike_description = realmRide.bike_description
        ride.bike_date_created = realmRide.bike_date_created
        ride.bike_status = realmRide.bike_status
        ride.bike_battery_level = realmRide.bike_battery_level
        ride.bike_bike_battery_level = realmRide.bike_bike_battery_level
        ride.bike_current_status = realmRide.bike_current_status
        ride.bike_maintenance_status = realmRide.bike_maintenance_status
        ride.bike_pic = realmRide.bike_pic
        ride.bike_distance = realmRide.bike_distance
        ride.bike_latitude = realmRide.bike_latitude
        ride.bike_lock_id = realmRide.bike_lock_id
        ride.bike_longitude = realmRide.bike_longitude
        ride.bike_fleet_id = realmRide.bike_fleet_id
        ride.bike_parking_spot_id = realmRide.bike_parking_spot_id
        ride.bike_user_id = realmRide.bike_user_id
        ride.bike_mac_id = realmRide.bike_mac_id
        ride.bike_name = realmRide.bike_name
        ride.bike_bike_operator_id = realmRide.bike_bike_operator_id
        ride.bike_customer_id = realmRide.bike_customer_id
        ride.bike_customer_name = realmRide.bike_customer_name
        ride.bike_hub_id = realmRide.bike_hub_id
        ride.bike_bike_fleet_key = realmRide.bike_bike_operator_email
        ride.bike_fleet_logo = realmRide.bike_fleet_logo
        ride.bike_fleet_name = realmRide.bike_fleet_name
        ride.bike_tariff = realmRide.bike_tariff
        ride.bike_isBikeBooked = realmRide.isBike_isBikeBooked()
        ride.bike_on_call_operator = realmRide.bike_on_call_operator
        ride.support_phone = realmRide.support_phone
        ride.currency = realmRide.currency


        ride.bike_price_for_membership = realmRide.bike_price_for_membership
        ride.bike_price_type_value = realmRide.bike_price_type_value
        ride.bike_price_type = realmRide.bike_price_type
        ride.bike_ride_deposit = realmRide.bike_ride_deposit
        ride.bike_price_for_ride_deposit = realmRide.bike_price_for_ride_deposit
        ride.bike_price_for_ride_deposit_type = realmRide.bike_price_for_ride_deposit_type
        ride.bike_excess_usage_fees = realmRide.bike_excess_usage_fees
        ride.bike_excess_usage_type_value = realmRide.bike_excess_usage_type_value
        ride.bike_excess_usage_type = realmRide.bike_excess_usage_type
        ride.bike_excess_usage_type_after_value = realmRide.bike_excess_usage_type_after_value
        ride.bike_excess_usage_type_after_type = realmRide.bike_excess_usage_type_after_type

        ride.bike_terms_condition_url = realmRide.bike_terms_condition_url
        ride.bike_fleet_type = realmRide.bike_fleet_type

        ride.bike_skip_parking_image = realmRide.bike_skip_parking_image
        ride.bike_max_trip_length = realmRide.bike_max_trip_length
        ride.isFirst_lock_connect = realmRide.isFirst_lock_connect

        ride.do_not_track_trip = realmRide.do_not_track_trip
        ride.price_for_bike_unlock = realmRide.price_for_bike_unlock
        ride.price_for_penalty_outside_parking = realmRide.price_for_penalty_outside_parking
        ride.payment_gateway = realmRide.payment_gateway
        ride.bike_uuid = realmRide.bike_uuid
        ride.bike_originalTypeOfObject = realmRide.bike_originalTypeOfObject

        if(realmRide.controllers!=null && realmRide.controllers?.size!!>0){
            var controllers : ArrayList<Ride.Controller> = ArrayList()
            for(realmRideController in realmRide.controllers!!){
                var controller = Ride.Controller()
                controller.added_by_operator_id = realmRideController.added_by_operator_id
                controller.battery_level = realmRideController.battery_level
                controller.bike_id = realmRideController.bike_id
                controller.controller_id = realmRideController.controller_id
                controller.date_created = realmRideController.date_created
                controller.device_type = realmRideController.device_type
                controller.fleet_id = realmRideController.fleet_id
                controller.fw_version = realmRideController.fw_version
                controller.gps_log_time = realmRideController.gps_log_time
                controller.hw_version = realmRideController.hw_version
                controller.key = realmRideController.key
                controller.latitude = realmRideController.latitude
                controller.longitude = realmRideController.longitude
                controller.make = realmRideController.make
                controller.model  = realmRideController.model
                controller.qr_code = realmRideController.qr_code
                controller.status = realmRideController.status
                controller.vendor = realmRideController.vendor
                controllers.add(controller)
            }
            ride.controllers =controllers
        }
        ride.qr_code_id = realmRide.qr_code_id
        ride.dock_hub_bike_docked = realmRide.dock_hub_bike_docked
        if(realmRide.pricing_options!=null && realmRide.pricing_options?.size!!>0){
            ride.pricing_options = ArrayList()
            for(pricing_option in realmRide.pricing_options!!){
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


        if(realmRide.promotions!=null && realmRide.promotions?.size!!>0){
            ride.promotions = ArrayList()
            for(promotion in realmRide.promotions!!){
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

        return ride
    }
}
