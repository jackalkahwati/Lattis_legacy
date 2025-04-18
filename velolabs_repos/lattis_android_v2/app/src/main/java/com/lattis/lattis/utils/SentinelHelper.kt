package com.lattis.lattis.utils

import android.text.TextUtils
import com.lattis.data.database.model.RealmBikeController
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Ride

object SentinelHelper {

    fun isSentinel(ride:Ride?=null):Boolean{
        if(ride!=null && ride?.controllers!=null && ride?.controllers?.size!!>0) {
            for (controller in ride?.controllers!!) {
                if (!TextUtils.isEmpty(controller.device_type) && controller.device_type.equals(
                        "iot",
                        true
                    ) &&
                    !TextUtils.isEmpty(controller.vendor) && controller.vendor.equals(
                        "Sentinel",
                        true
                    )
                ) {
                    return true
                }
            }
        }
        return false
    }



}