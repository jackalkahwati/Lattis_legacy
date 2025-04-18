package io.lattis.operator.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import io.lattis.operator.R

class MarkerImageUtil {
    
    companion object{
        var ebike_image: BitmapDescriptor?=null
        var e_kick_scooter_image_25: BitmapDescriptor?=null
        var e_kick_scooter_image_50: BitmapDescriptor?=null
        var e_kick_scooter_image_75: BitmapDescriptor?=null
        var e_kick_scooter_image_100: BitmapDescriptor?=null
        var kick_scooter_image: BitmapDescriptor?=null
        var regular_image: BitmapDescriptor?=null
        var locker_image: BitmapDescriptor?=null
        var cart_image: BitmapDescriptor?=null
        var location_image: BitmapDescriptor?=null
        

        fun getBikeResource(bike_type: String?,bike_battery_level:String?): BitmapDescriptor {
            if (!TextUtils.isEmpty(bike_type)) {
                when (bike_type.toString().toUpperCase()) {
                    "REGULAR" -> {
                        if(regular_image==null){
                            regular_image=BitmapDescriptorFactory.fromResource(R.drawable.regular_bike)
                        }
                        return regular_image!!
                    }
                    "ELECTRIC" -> {

                        if(ebike_image==null){
                            ebike_image=BitmapDescriptorFactory.fromResource( R.drawable.e_bike)
                        }
                        return ebike_image!!
                    }
                    "CART" -> {
                        if(cart_image==null){
                            cart_image=BitmapDescriptorFactory.fromResource( R.drawable.cart)
                        }
                        return cart_image!!
                    }
                    "KICK SCOOTER" -> {
                        try {
                            if (!TextUtils.isEmpty(bike_battery_level)) {
                                when (bike_battery_level!!.toInt()) {
                                    in 1..26 -> {
                                        if(e_kick_scooter_image_25==null){
                                            e_kick_scooter_image_25=BitmapDescriptorFactory.fromResource( R.drawable.e_bike_25)
                                        }
                                        return e_kick_scooter_image_25!!
                                    }
                                    in 25..51 -> {
                                        if(e_kick_scooter_image_50==null){
                                            e_kick_scooter_image_50=BitmapDescriptorFactory.fromResource( R.drawable.e_bike_50)
                                        }
                                        return e_kick_scooter_image_50!!
                                    }
                                    in 50..76 -> {
                                        if(e_kick_scooter_image_75==null){
                                            e_kick_scooter_image_75=BitmapDescriptorFactory.fromResource( R.drawable.e_bike_75)
                                        }
                                        return e_kick_scooter_image_75!!
                                    }
                                    else -> {
                                        if(e_kick_scooter_image_100==null){
                                            e_kick_scooter_image_100=BitmapDescriptorFactory.fromResource( R.drawable.e_bike_100)
                                        }
                                        return e_kick_scooter_image_100!!
                                    }
                                }
                            }
                        }catch(e:Exception){

                        }

                        if(kick_scooter_image==null){
                            kick_scooter_image=BitmapDescriptorFactory.fromResource( R.drawable.kick_scooter)
                        }
                        return kick_scooter_image!!
                        
                    }
                    "LOCKER" -> {
                        if(locker_image==null){
                            locker_image=BitmapDescriptorFactory.fromResource( R.drawable.locker)
                        }
                        return locker_image!!
                    }
                }
            }

            if(regular_image==null){
                regular_image=BitmapDescriptorFactory.fromResource( R.drawable.regular_bike)
            }
            return regular_image!!
        }
        
        
        fun getLocationMarker():BitmapDescriptor{
            if(location_image==null){
                location_image= BitmapDescriptorFactory.fromResource(R.drawable.current_location_icon)
            }
            return location_image!!
        }
    }
}