package com.lattis.lattis.presentation.popup.edit

import com.lattis.lattis.presentation.base.BaseView

interface PopUpEditActivityView : BaseView{
    fun setView(
        title: String?,
        subTitle1: String?,
        icon:Int?,
        submitBtnTitle: String?,
        editTextValue:String?,
        showSecondEditText:Boolean,
        editTextHint: String?,
        editTextKeyboard :Int?,
        editText2Hint: String?
    )
}