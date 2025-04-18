package com.lattis.lattis.presentation.damage

import com.lattis.lattis.presentation.base.BaseView
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusView


interface ReportDamageActivityView : BaseUserCurrentStatusView {

    fun activateSubmitBtn(state:Boolean)
    fun onUploadImageSuccess()
    fun onUploadImageFailure()
    fun onReportDamageFailure()

    fun showViewWith(ride:Boolean)

}