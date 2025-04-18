package com.lattis.lattis.presentation.popup

import com.lattis.lattis.presentation.base.BaseView

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