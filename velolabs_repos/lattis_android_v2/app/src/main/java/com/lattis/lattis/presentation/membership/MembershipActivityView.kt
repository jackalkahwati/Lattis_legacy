package com.lattis.lattis.presentation.membership

import com.lattis.lattis.presentation.base.BaseView

interface MembershipActivityView : BaseView{


    fun onMembershipsSuccess()
    fun onMembershipsFailure()

    fun onMembershipSubscribeFailure()

    fun onMembershipUnSubscribeSuccess()
    fun onMembershipUnSubscribeFailure()

    fun onCardListSuccess()

}