package com.lattis.lattis.presentation.help

import com.lattis.lattis.presentation.base.BaseView


interface HelpActivityView : BaseView{

    fun showPhoneNumber(phoneNumber:String)
    fun showEmail(email:String)
    fun showFaq(faq: String)

    fun hideLoadingForAddPromotion()
}