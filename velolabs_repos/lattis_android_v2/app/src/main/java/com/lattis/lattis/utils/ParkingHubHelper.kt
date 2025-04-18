package com.lattis.lattis.utils

import com.lattis.domain.models.DockHub

object ParkingHubHelper {

    fun getParkingHubFleetName(dockHub: DockHub):String?{
        return dockHub.fleet?.fleet_name
    }

    fun getParkingHubPortName(dockHub: DockHub,position:Int):String?{
        return if(dockHub.ports!=null && dockHub.ports?.size!!>position){
            dockHub.ports?.get(position)?.port_name
        }else{
            null
        }
    }


    fun getParkingHubPortType(dockHub: DockHub,position: Int):String?{
        return "Parking"
    }

    fun getParkingHubPortUrl(dockHub: DockHub,position: Int):String?{
        return dockHub.fleet?.logo
    }


    fun getPortFromPortId(dockHub: DockHub?,port_id:Int?):DockHub.Port?{
        if(dockHub!=null &&
            port_id!=null &&
            dockHub?.ports!=null &&
            dockHub?.ports?.size!!>0){
            for(port in dockHub.ports!!){
                if(port.port_id==port_id) return port
            }
        }

        return null
    }
}