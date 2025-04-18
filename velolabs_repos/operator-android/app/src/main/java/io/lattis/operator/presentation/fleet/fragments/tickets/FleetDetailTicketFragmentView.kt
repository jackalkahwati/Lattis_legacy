package io.lattis.operator.presentation.fleet.fragments.tickets

import io.lattis.operator.presentation.base.BaseView

interface FleetDetailTicketFragmentView :BaseView{

    fun onTicketsSuccess()
    fun onTicketFailure()
}