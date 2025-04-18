package io.lattis.domain.models

class Account {

    var accountName: String? = null

    var accountType: String? = null

    var password: String? = null

    var accessToken: String? = null

    var refreshToken: String? = null

    var userId: String? = null


    constructor(accountName: String?, accountType: String?) {
        this.accountName = accountName
        this.accountType = accountType
    }

    constructor(accountType: String) {
        this.accountType = accountType
    }

    override fun toString(): String {
        return "Account{" +
                "accountName='" + accountName + '\''.toString() +
                ", accountType='" + accountType + '\''.toString() +
                ", password='" + password + '\''.toString() +
                ", accessToken='" + accessToken + '\''.toString() +
                ", refreshToken='" + refreshToken + '\''.toString() +
                '}'.toString()
    }
}
