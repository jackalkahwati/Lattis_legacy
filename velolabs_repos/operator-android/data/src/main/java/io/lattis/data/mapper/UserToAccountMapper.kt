package io.lattis.data.mapper

import io.lattis.domain.models.Account
import io.lattis.domain.models.User

import javax.inject.Inject

class UserToAccountMapper @Inject
constructor() : AbstractDataMapper<User, Account>() {

    override fun mapIn(user: User?): Account {
        val account = Account(user?.operator?.id, null)
        account.accessToken = user?.restToken
        account.password = user?.restToken
        account.userId = user?.operator?.id
        return account
    }

    override fun mapOut(account: Account?): User? {
        return null;
    }
}
