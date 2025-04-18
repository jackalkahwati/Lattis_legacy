package io.lattis.operator.presentation.qrcodescan

import io.lattis.operator.model.ChangeStatus
import io.lattis.operator.presentation.base.BaseView

interface ScanQRCodeActivityView :BaseView {

    fun hideProgressbar()
    fun showProgressbar()

    fun onQRCodeVehicleSuccess()
    fun onQRCodeVehicleFailure()

    fun restartScanner()

    fun onChangeBulkStatusSuccess()
    fun onChangeBuldStatusFailure()
}