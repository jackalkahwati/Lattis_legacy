package io.lattis.operator.presentation.popup

import io.lattis.operator.presentation.base.BaseView

interface PopUpActivityView : BaseView {
    fun setView(
        title: String?,
        subTitle1: String?,
        subTitle2: String?,
        actionBtnPositive1: String?,
        actionBtnPositive2: String?,
        actionBtnPositive3: String?,
        actionBtnNegative: String?
    )
}