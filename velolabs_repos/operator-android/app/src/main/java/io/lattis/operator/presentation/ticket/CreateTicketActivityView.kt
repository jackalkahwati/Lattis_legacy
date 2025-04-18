package io.lattis.operator.presentation.ticket

import io.lattis.operator.presentation.base.BaseView

interface CreateTicketActivityView : BaseView {

    fun showVehicleInfo()

    fun onCreateTicketSuccess()
    fun onCreateTicketFailure()
    fun onCreateTicketValidationFailure()

    fun onVehiclesSearchSuccess()
    fun onVehiclesSearchFailure()
}