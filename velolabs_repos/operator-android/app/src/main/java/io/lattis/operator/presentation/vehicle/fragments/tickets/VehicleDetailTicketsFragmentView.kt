package io.lattis.operator.presentation.vehicle.fragments.tickets

import io.lattis.operator.presentation.base.BaseView

interface VehicleDetailTicketsFragmentView : BaseView {

    fun onTicketsSuccess()
    fun onTicketFailure()
}