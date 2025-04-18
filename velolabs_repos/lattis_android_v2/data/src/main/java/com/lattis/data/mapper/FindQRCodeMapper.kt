package com.lattis.data.mapper

import android.text.TextUtils
import com.lattis.domain.models.Rental
import javax.inject.Inject

class FindQRCodeMapper @Inject
constructor() : AbstractDataMapper<Rental, Rental>() {

    override fun mapIn(rental: Rental?): Rental {
        if(rental!=null &&
            rental.bike!=null &&
            rental?.bike?.controllers!=null &&
            rental?.bike?.controllers?.size!!>0){
            for (controller in rental?.bike?.controllers!!){
                if(!TextUtils.isEmpty(controller.key) && !TextUtils.isEmpty(controller.vendor) && "AXA".equals(controller.vendor,true)){
                    rental?.bike?.mac_id = "AXA:"+controller.key
                }else if(!TextUtils.isEmpty(controller.key) && !TextUtils.isEmpty(controller.vendor) &&
                    ("TAPKEY".equals(controller.vendor,true) || "TAP KEY".equals(controller.vendor,true))){
                    rental?.bike?.mac_id = controller.key
                }else if(!TextUtils.isEmpty(controller.key) && !TextUtils.isEmpty(controller.vendor) && "sas".equals(controller.vendor,true)){
                    rental?.bike?.mac_id = controller.key
                }
            }
        }
        return if(rental!=null) rental!! else Rental()
    }

    override fun mapOut(out: Rental?): Rental? {
        return null
    }
}