package com.lattis.lattis.presentation.ride

import com.lattis.lattis.presentation.base.BaseView

interface RideSummaryView : BaseView {

    fun onRideRatingSuccess()
    fun onRideRatingFailure()
}