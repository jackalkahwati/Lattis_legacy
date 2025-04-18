package com.lattis.lattis.presentation.parking

import com.lattis.domain.models.ParkingZone
import com.lattis.lattis.presentation.base.BaseView

interface ParkingActivityView :BaseView{

    fun onFindingParkingSuccess()
    fun onFindParkingFailure()
    fun onFindingZoneSuccess(parkingZone: List<ParkingZone>)
    fun onDockHubsSuccess()

}