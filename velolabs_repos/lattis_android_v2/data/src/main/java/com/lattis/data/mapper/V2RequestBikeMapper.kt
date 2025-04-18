package com.lattis.data.mapper

import com.lattis.data.entity.body.v2.BikeHubPortBody
import com.lattis.data.entity.response.user.UserResponse
import com.lattis.domain.models.Account
import com.lattis.domain.models.Bike
import com.lattis.domain.utils.Constants
import com.lattis.domain.utils.Constants.DOCKING_STATION
import com.lattis.domain.utils.Constants.PARKING_STATION
import com.lattis.domain.utils.Constants.hub
import com.lattis.domain.utils.Constants.port
import javax.inject.Inject

class V2RequestBikeMapper  @Inject
constructor() : AbstractDataMapper<Bike, BikeHubPortBody>() {
    override fun mapIn(bike: Bike?): BikeHubPortBody {
        return when(bike?.originalTypeOfObject){
                Constants.port -> {
                    return BikeHubPortBody(null,null,bike?.bike_id, port)
                }
                Constants.hub ->{
                    return BikeHubPortBody(null,bike?.bike_id,null, hub)
                }
                else ->{
                    return BikeHubPortBody(bike?.bike_id,null,null, Constants.bike)
                }
            }

    }

    override fun mapOut(out: BikeHubPortBody?): Bike? {
        return null
    }
}