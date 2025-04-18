package com.lattis.lattis.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.lattis.lattis.presentation.customview.CustomTextView
import com.lattis.lattis.presentation.utils.MapboxUtil
import io.lattis.lattis.R

object ResourceHelper {
    fun getBikeResource(bike_type: String?,bike_battery_level:String?): String {
        if (!TextUtils.isEmpty(bike_type)) {
            when (bike_type.toString().toUpperCase()) {
                "REGULAR" -> return MapboxUtil.regular
                "ELECTRIC" -> return MapboxUtil.e_bike
                "CART" -> return MapboxUtil.cart
                "KICK SCOOTER" -> {
                    try {
                        if (!TextUtils.isEmpty(bike_battery_level))
                            when (bike_battery_level!!.toInt()) {
                                in 1..26 -> return MapboxUtil.e_kick_scooter_25
                                in 25..51 -> return MapboxUtil.e_kick_scooter_50
                                in 50..76 -> return MapboxUtil.e_kick_scooter_75
                                else -> return MapboxUtil.e_kick_scooter_100
                            }
                    }catch(e:Exception){

                    }
                    return MapboxUtil.kick_scooter
                }
                "LOCKER" -> return MapboxUtil.locker
                "KAYAK" -> return MapboxUtil.kayak
                "HUB_DOCK" -> return MapboxUtil.hub_dock
                "PARKING_STATION" -> return MapboxUtil.parking_station
                "DOCKING_STATUS" -> return MapboxUtil.docking_station
                "MOPED" -> return MapboxUtil.moped
            }
        }
        return MapboxUtil.regular
    }


    fun getBikeResource(context: Context,bike_type: String?): Drawable {
        if (!TextUtils.isEmpty(bike_type)) {
            when (bike_type.toString().toUpperCase()) {
                "REGULAR" -> return ContextCompat.getDrawable(context,R.drawable.bike_regular)!!
                "ELECTRIC" -> return ContextCompat.getDrawable(context,R.drawable.bike_electric)!!
                "CART" -> return ContextCompat.getDrawable(context,R.drawable.bike_cart)!!
                "KICK SCOOTER" -> return ContextCompat.getDrawable(context,R.drawable.bike_kick_scooter)!!
                "LOCKER" -> return ContextCompat.getDrawable(context,R.drawable.bike_locker)!!
                "KAYAK" -> return ContextCompat.getDrawable(context,R.drawable.bike_kayak)!!
                "HUB_DOCK" -> return ContextCompat.getDrawable(context,R.drawable.bike_hub)!!
                "MOPED" -> return ContextCompat.getDrawable(context,R.drawable.moped)!!
            }
        }
        return ContextCompat.getDrawable(context,R.drawable.bike_regular)!!
    }

    fun getBikeType(bike_type: String?, context: Context): String {
        if (!TextUtils.isEmpty(bike_type)) {
            when (bike_type.toString().toUpperCase()) {
                "REGULAR" -> return context.getString(R.string.regular_bike)
                "ELECTRIC" -> return context.getString(R.string.electric_bike)
                "KICK SCOOTER" -> return context.getString(R.string.kick_scooter)
                "LOCKER" -> return context.getString(R.string.locker_bike)
                "CART" -> return context.getString(R.string.cart_bike)
                "KAYAK" -> return context.getString(R.string.kayak)
                "PARKING_STATION" -> return context.getString(R.string.parking)
                "DOCKING_STATUS" -> return context.getString(R.string.parking)
                "MOPED" -> return context.getString(R.string.scooter)
            }
        }
        return context.getString(R.string.regular_bike)
    }

    fun setBatteryImageAndText(batteryLevel: String?, bikeImageView:ImageView, customTextView: CustomTextView) {
        try {
            bikeImageView.visibility = View.GONE
            customTextView.visibility = View.GONE
            if (!TextUtils.isEmpty(batteryLevel)) {
                when (batteryLevel!!.toInt()) {
                    in 0..26 -> {
                        setBatteryUI(batteryLevel,bikeImageView,customTextView,R.drawable.ic_battery_25)
                    }
                    in 25..51 -> {
                        setBatteryUI(batteryLevel,bikeImageView,customTextView,R.drawable.ic_battery_50)
                    }
                    in 50..76 -> {
                        setBatteryUI(batteryLevel,bikeImageView,customTextView,R.drawable.ic_battery_75)
                    }
                    in 75..101 -> {
                        setBatteryUI(batteryLevel,bikeImageView,customTextView,R.drawable.ic_battery_100)
                    }
                }
            }
        }catch (e:Exception){

        }

    }

    fun setBatteryUI(batteryLevel: String?,bikeImageView:ImageView,customTextView: CustomTextView, drawableResource:Int){
        bikeImageView.visibility = View.VISIBLE
        customTextView.visibility = View.VISIBLE
        bikeImageView.setImageResource(drawableResource)
        customTextView.setText(batteryLevel + "%")
    }

    fun getResourcesByParkingType(parkingType: String?): String {
        if (!TextUtils.isEmpty(parkingType)) {
            when (parkingType.toString().toUpperCase()) {
                "GENERIC_PARKING" -> return MapboxUtil.generic_parking
                "PARKING_METER" -> return MapboxUtil.parking_meter
                "CHARGING_SPOT" -> return MapboxUtil.charging_spots
                "PARKING_RACKS", "BIKE_RACK" -> return MapboxUtil.parking_racks
                "SHEFFIELD_STAND", "LOCKER" -> return MapboxUtil.generic_parking
            }
        }
        return MapboxUtil.generic_parking
    }

    fun parkingStation(type:String?):Boolean{
        return !TextUtils.isEmpty(type) && "PARKING_STATION".equals(type,true)
    }
}