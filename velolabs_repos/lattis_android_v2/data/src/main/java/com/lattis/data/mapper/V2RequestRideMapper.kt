package com.lattis.data.mapper

import com.lattis.data.entity.body.v2.BikeHubPortBody
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Ride
import com.lattis.domain.utils.Constants
import javax.inject.Inject

class V2RequestRideMapper  @Inject
constructor() : AbstractDataMapper<Ride, BikeHubPortBody>() {
    override fun mapIn(ride: Ride?): BikeHubPortBody {
        return when(ride?.bike_originalTypeOfObject){
            Constants.port -> {
                return BikeHubPortBody(null,null,ride?.bike_id, Constants.port)
            }
            Constants.hub ->{
                return BikeHubPortBody(null,ride?.bike_id,null, Constants.hub)
            }
            else ->{
                return BikeHubPortBody(ride?.bike_id,null,null, Constants.bike)
            }
        }

    }

    override fun mapOut(out: BikeHubPortBody?): Ride? {
        return null
    }
}