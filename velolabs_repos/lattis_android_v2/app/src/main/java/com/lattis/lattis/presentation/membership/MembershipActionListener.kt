package com.lattis.lattis.presentation.membership

import com.lattis.domain.models.Membership

interface MembershipActionListener {
    fun onMembershipClicked(position:Int,alreadyMember:Boolean)
}