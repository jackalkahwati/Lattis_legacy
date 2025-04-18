package com.lattis.domain.models

data class Memberships(
    val memberships:List<Membership>?=null,
    val subscriptions:List<Subscription>?=null
)