package com.lattis.lattis.presentation.utils

import android.text.TextUtils
import com.lattis.domain.models.Ride

object RideUtil {

    fun provideControllerKeys(ride:Ride?):List<String>?{
        if(ride?.controllers!=null && ride?.controllers?.size!!>0){
            var controllerKey = ArrayList<String>()
            for(controller in ride?.controllers!!){
                if(controller.key!=null)
                    controllerKey.add(controller.key!!)
            }
            return controllerKey
        }
        return null
    }


    fun provideIoTControllerKey(ride:Ride?):String?{
        if(ride?.controllers!=null && ride?.controllers?.size!!>0){
            for(controller in ride?.controllers!!){
                if(!TextUtils.isEmpty(controller.device_type) && controller.device_type.equals("iot",true)){
                    return controller.key
                }
            }
        }
        return null
    }


    fun provideIoTControllerId(ride:Ride?):Int?{
        if(ride?.controllers!=null && ride?.controllers?.size!!>0){
            for(controller in ride?.controllers!!){
                if(!TextUtils.isEmpty(controller.device_type) && controller.device_type.equals("iot",true)){
                    return controller.controller_id
                }
            }
        }
        return null
    }
}