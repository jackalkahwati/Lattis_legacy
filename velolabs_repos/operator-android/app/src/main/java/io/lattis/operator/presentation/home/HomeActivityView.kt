package io.lattis.operator.presentation.home

import io.lattis.domain.models.Fleet
import io.lattis.operator.presentation.base.BaseView

interface HomeActivityView : BaseView {

    fun onFleetsSuccess()
    fun onFleetsFailure()

    fun onUserFleetSaveSuccess()
    fun onUserFleetSaveFailure()

    fun showFilteredList(fleets:ArrayList<Fleet>)
    fun showOriginalList()
}