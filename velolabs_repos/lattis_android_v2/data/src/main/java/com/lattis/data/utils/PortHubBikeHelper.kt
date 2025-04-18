package com.lattis.data.utils

import com.lattis.data.entity.body.v2.BikeHubPortBody

object PortHubBikeHelper {

    fun getPortHubBikeQueryMap(bikeHubPortBody: BikeHubPortBody?):Map<String,Int>?{
        val queryMap = HashMap<String,Int>()
        return if(bikeHubPortBody!=null){
            if(bikeHubPortBody.bike_id!=null){
                queryMap.put("bike_id",bikeHubPortBody?.bike_id!!)
            }else if(bikeHubPortBody.hub_id!=null){
                queryMap.put("hub_id",bikeHubPortBody?.hub_id!!)
            }else if(bikeHubPortBody.port_id!=null){
                queryMap.put("port_id",bikeHubPortBody?.port_id!!)
            }
            queryMap
        }else null
    }
}