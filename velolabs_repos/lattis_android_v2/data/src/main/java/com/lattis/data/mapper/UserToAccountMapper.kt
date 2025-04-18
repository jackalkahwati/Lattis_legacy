package com.lattis.data.mapper

import com.lattis.data.entity.response.user.UserResponse
import com.lattis.domain.models.Account

import javax.inject.Inject

class UserToAccountMapper @Inject
constructor() : AbstractDataMapper<UserResponse, Account>() {

    override fun mapIn(userResponse: UserResponse?): Account {
        val account = Account(userResponse?.userId, null)
        account.accessToken = userResponse?.restToken
        account.password = userResponse?.refreshToken
        account.userId = userResponse?.userId
        account.usersId = userResponse?.usersId
        account.userType = userResponse?.userType
        account.isVerified = userResponse?.isVerified
        return account
    }

    override fun mapOut(account: Account?): UserResponse? {
        return null;
    }
}
