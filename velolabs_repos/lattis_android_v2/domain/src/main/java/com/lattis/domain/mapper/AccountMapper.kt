package com.lattis.domain.mapper


import com.lattis.domain.mapper.base.AbstractDataMapper
import com.lattis.domain.models.Account
import com.lattis.domain.models.User
import javax.inject.Inject

class AccountMapper @Inject
constructor() : AbstractDataMapper<User, Account>() {

    override fun mapIn(user: User?): Account {
        val account = Account(user?.id, null)
        account.accessToken = user?.restToken
        account.password = user?.refreshToken
        account.userId = user?.id
        account.usersId = user?.usersId
        account.userType = user?.userType
        account.isVerified = user?.isVerified
        return account
    }

    override fun mapOut(account: Account?): User? {
        return null;
    }
}
