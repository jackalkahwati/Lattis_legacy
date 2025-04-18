package com.lattis.lattis.uimodel.mapper

import android.text.TextUtils
import com.lattis.domain.models.Bike
import com.lattis.domain.models.DockHub
import com.lattis.domain.utils.Constants
import javax.inject.Inject

class BikeHubMapper  @Inject constructor()  {


    fun mapOut(dockHub: DockHub?): Bike? {

        return if(dockHub==null){
            null
        }else{
            getBike(dockHub!!)
        }
    }

    private fun getBike(dockHub: DockHub): Bike?{
        val bike = Bike()
        val fleet = dockHub?.fleet
        bike.type = dockHub?.type
        bike.bike_id = dockHub?.hub_id!!
        bike.bike_name = if(TextUtils.isEmpty(dockHub?.name)) dockHub?.hub_name else dockHub?.name

        bike.bike_battery_level = null
        bike.status = "active" //TODO
        bike.current_status = "parked"  //TODO
        bike.pic = dockHub?.image //TODO
        bike.longitude = dockHub?.longitude?:0.toDouble()
        bike.latitude = dockHub?.latitude?:0.toDouble()
        bike.fleet_id = fleet?.fleet_id?:0
        bike.mac_id = null  //TODO get it from controller
        bike.name = if(TextUtils.isEmpty(dockHub?.name)) dockHub?.hub_name else dockHub?.name
        bike.operator_id = fleet?.operator_id?:0
        bike.customer_id = fleet?.customer_id?:0
        bike.hub_id = dockHub?.hub_id?:0
        bike.fleet_key = fleet?.key
        bike.fleet_logo = fleet?.logo
        bike.fleet_name = fleet?.fleet_name


        bike.price_for_membership = fleet?.fleet_payment_settings?.price_for_membership
        bike.price_type_value = fleet?.fleet_payment_settings?.price_type_value
        bike.price_type = fleet?.fleet_payment_settings?.price_type
        bike.ride_deposit = fleet?.fleet_payment_settings?.ride_deposit
        bike.price_for_ride_deposit = fleet?.fleet_payment_settings?.price_for_ride_deposit_type
        bike.price_for_ride_deposit_type = fleet?.fleet_payment_settings?.price_for_ride_deposit_type
        bike.excess_usage_fees = fleet?.fleet_payment_settings?.excess_usage_fees
        bike.excess_usage_type_value = fleet?.fleet_payment_settings?.excess_usage_type_value
        bike.excess_usage_type = fleet?.fleet_payment_settings?.excess_usage_type
        bike.excess_usage_type_after_value = fleet?.fleet_payment_settings?.excess_usage_type_after_value
        bike.excess_usage_type_after_type = fleet?.fleet_payment_settings?.excess_usage_type_after_type
        bike.fleet_t_and_c = fleet?.t_and_c
        bike.skip_parking_image = if(
            fleet?.skip_parking_image!=null &&
            (fleet.skip_parking_image.equals("1") || fleet.skip_parking_image.equals("true",true)))
            true else false
        bike.max_trip_length = fleet?.max_trip_length?.toIntOrNull()?:0
        bike.usage_surcharge = fleet?.fleet_payment_settings?.usage_surcharge
        bike.currency = fleet?.fleet_payment_settings?.currency
        bike.require_phone_number = if(fleet?.require_phone_number!=null && (fleet?.require_phone_number.equals("1",true) || fleet?.require_phone_number.equals("true",true))) true else false
        bike.isDo_not_track_trip = if(fleet?.do_not_track_trip!=null && (fleet?.do_not_track_trip.equals("1",true) || fleet?.do_not_track_trip.equals("true",true))) true else false



        bike.fleet_type = fleet?.type
        bike.price_for_bike_unlock = fleet?.fleet_payment_settings?.price_for_bike_unlock
        bike.price_for_penalty_outside_parking = fleet?.fleet_payment_settings?.price_for_penalty_outside_parking
        bike.reservation_settings = null //TODO important
        bike.pricing_options = dockHub?.pricing_options
        bike.promotions = dockHub?.promotions

        if(dockHub?.equipment!=null){
            val controllers = ArrayList<Bike.Controller>()
            if(!TextUtils.isEmpty(dockHub?.equipment?.key) && !TextUtils.isEmpty(dockHub?.equipment?.vendor) && "AXA".equals(dockHub?.equipment?.vendor,true)){
                bike.mac_id = "AXA:"+dockHub?.equipment?.key
            }else if(!TextUtils.isEmpty(dockHub?.equipment?.key) && !TextUtils.isEmpty(dockHub?.equipment?.vendor) &&
                ("TAPKEY".equals(dockHub?.equipment?.vendor,true) || "TAP KEY".equals(dockHub?.equipment?.vendor,true))){
                bike.mac_id = dockHub?.equipment?.key
            }else if(!TextUtils.isEmpty(dockHub?.equipment?.key) && !TextUtils.isEmpty(dockHub?.equipment?.vendor) && "sas".equals(dockHub?.equipment?.vendor,true)){
                bike.mac_id = dockHub?.equipment?.key  //"50B0456022" //dockHub?.equipment?.key
            }
            controllers.add(dockHub.equipment!!)
            bike.controllers = controllers
        }
        bike.qr_code_id = dockHub?.qr_code

        val bikeGroup = Bike.Bike_Group()
        bikeGroup.make = dockHub?.make
        bikeGroup.model = dockHub?.model
        bikeGroup.type = dockHub?.type
        bikeGroup.customer_id = fleet?.customer_id
        bikeGroup.date_created = fleet?.date_created?.toString()
        bikeGroup.description = dockHub?.description
        bikeGroup.fleet_id = fleet?.fleet_id
        bikeGroup.operator_id = fleet?.operator_id
//        bikeGroup.pic // this is required to be added
        bike.bike_group = bikeGroup

        bike.payment_gateway = fleet?.fleet_payment_settings?.payment_gateway
        bike.enable_preauth = fleet?.fleet_payment_settings?.enable_preauth
        bike.preauth_amount = fleet?.fleet_payment_settings?.preauth_amount
        bike.bike_uuid = dockHub?.hub_uuid

        bike.hud = dockHub
        bike.port = null
        bike.originalTypeOfObject = Constants.hub
        return bike
    }
}