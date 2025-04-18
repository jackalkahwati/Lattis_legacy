package com.lattis.domain.models

class Account {

    var accountName: String? = null

    var accountType: String? = null

    var password: String? = null

    var accessToken: String? = null

    var refreshToken: String? = null

    var userId: String? = null

    var usersId: String? = null

    var userType: String? = null

    var isVerified: Boolean? = false

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
                ", userId='" + userId + '\''.toString() +
                ", usersId='" + usersId + '\''.toString() +
                ", userType='" + userType + '\''.toString() +
                ", verified=" + isVerified +
                '}'.toString()
    }
}
