package io.lattis.operator.presentation.vehicle.fragments.ticket

import io.lattis.operator.presentation.base.BaseView

interface VehicleDetailTicketFragmentView : BaseView {

    fun startShowingTicketInformation()
    fun showAssignee()
    fun onTicketResolved()
}