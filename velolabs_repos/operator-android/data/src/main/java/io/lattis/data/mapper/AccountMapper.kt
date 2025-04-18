package io.lattis.data.mapper

import io.lattis.domain.models.Account
import javax.inject.Inject
import javax.inject.Named

class AccountMapper @Inject
constructor(@param:Named("AccountType") private val accountType: String) : AbstractDataMapper<android.accounts.Account, Account>() {

    override fun mapIn(androidAccount: android.accounts.Account?): Account {
        return Account(androidAccount?.name, androidAccount?.type)
    }

    override fun mapOut(account: Account?): android.accounts.Account? {
        return android.accounts.Account(account?.accountName, accountType)
    }
}
