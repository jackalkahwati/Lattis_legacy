package com.lattis.lattis.presentation.history

import com.lattis.lattis.presentation.base.BaseView


interface RideHistoryActivityView : BaseView{

    fun onRideHistorySuccess()
    fun onNoRideHistory()
    fun onRideHistoryFailure()
}