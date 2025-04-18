package io.lattis.domain.repository

import io.lattis.domain.models.Account
import io.reactivex.Observable

interface AccountRepository {

    fun getAccounts(): Observable<List<Account>>

    fun getAccount(): Observable<Account>

    fun getToken(): Observable<String>

    fun setToken(tokenType: String, tokenValue: String): Observable<Void>

    fun getToken(tokenType: String): Observable<String>

    fun invalidateToken(): Observable<Void>

    fun addAccount(): Observable<Cloneable>

    fun addAccountExplicitly(account: Account?): Observable<Account>

    fun signOut(): Observable<Boolean>

    fun invalidateToken(tokenType: String): Observable<Void>

    fun getAccounts(accountType: String): Observable<List<Account>>

    fun getAccount(accountType: String): Observable<Account>

    fun getToken(account: Account, tokenType: String): Observable<String>

    fun invalidateToken(accountType: String, token: String): Observable<Void>

}
