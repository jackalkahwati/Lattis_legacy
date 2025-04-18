package io.lattis.data.repository.implementation.api

import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import io.lattis.data.database.fleet.UserSavedFleet
import io.lattis.data.mapper.AccountMapper
import io.lattis.domain.models.Account
import io.lattis.domain.repository.AccountRepository

import java.io.IOException

import javax.inject.Inject

import io.reactivex.Emitter
import io.reactivex.Observable
import javax.inject.Named

class AndroidAccountRepository @Inject
constructor(private val accountManager: AccountManager,
            private val accountMapper: AccountMapper,
            @param:Named("AccountType") val accountType: String,
            @param:Named("AuthenticationTokenType") val tokenType: String,
            private val userSavedFleet: UserSavedFleet
) : AccountRepository {

    override fun addAccountExplicitly(account: Account?): Observable<Account> {
        return Observable.create { emitter ->
            if (account != null) {

                Log.e("addAccountExplicitly ", " " + account.userId)
                Log.e("addAccountExplicitly ", " " + account.accountName)
                Log.e("addAccountExplicitly ", " " + account.refreshToken)


                val newAccount = this.accountMapper.mapOut(account)
                val accounts = accountManager.getAccountsByType(accountType)
                if (accounts.size == 0) {
                    newAccount?.let {
                        setResult(account, emitter, newAccount)
                    }
                } else { //We need to update the account instead
                    /*if(!accounts[0].name.equals(newAccount.name)){

                    }else setResult(account, subscriber, newAccount);*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        accountManager.removeAccountExplicitly(accounts[0])
                        newAccount?.let {
                            setResult(account, emitter, newAccount)
                        }
                    } else {
                        accountManager.removeAccount(accounts[0], { accountManagerFuture ->
                            newAccount?.let {
                                setResult(account, emitter, newAccount)
                            }
                        }, null)
                    }

                }
            } else
                emitter.onError(Throwable("cannot add a null account"))
        }
    }

    private fun setResult(account: Account, subscriber: Emitter<in Account>, newAccount: android.accounts.Account) {
        accountManager.addAccountExplicitly(newAccount, account.password, null)
        accountManager.setAuthToken(newAccount, tokenType, account.accessToken)
        accountManager.setUserData(newAccount, USER_DATA_USER_ID_KEY, account.userId.toString())
        subscriber.onNext(account)
    }

    override fun signOut(): Observable<Boolean> {
        return Observable.create { subscriber ->
            val accounts = accountManager.getAccountsByType(accountType)
            if (accounts.size > 0) {
                val tokenToInvalidate = accountManager.peekAuthToken(accounts[0], tokenType)
                accountManager.invalidateAuthToken(accountType, tokenToInvalidate)
                accountManager.setPassword(accounts[0], null)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    accountManager.removeAccountExplicitly(accounts[0])
                } else {
                    accountManager.removeAccount(accounts[0], null, null)
                }
                userSavedFleet.deleteFleet()
                subscriber.onNext(true)
            } else
                subscriber.onError(Throwable("account not found"))
        }
    }

    override fun getAccounts(): Observable<List<Account>> {
        return getAccounts(accountType)
    }

    override fun getAccounts(accountType: String): Observable<List<Account>> {
        return Observable.create { emitter ->
            val accounts = accountManager.getAccountsByType(accountType)
            if (accounts.size > 0) {
                //subscriber.onNext(accountMapper.mapOut(accounts[0]));
            } else
                emitter.onComplete()
        }
    }

    override fun getAccount(): Observable<Account> {
        return getAccount(accountType)
    }

    override fun invalidateToken(): Observable<Void> {
        return invalidateToken(tokenType)
    }

    override fun addAccount(): Observable<Cloneable> {
        return Observable.create { emitter ->
            accountManager.addAccount(accountType, tokenType, null, null, null, { accountManagerFuture ->
                var result: Bundle? = null
                try {
                    result = accountManagerFuture.result
                } catch (e: OperationCanceledException) {
                    e.printStackTrace()
                    emitter.onError(e)
                } catch (e: IOException) {
                    e.printStackTrace()
                    emitter.onError(e)
                } catch (e: AuthenticatorException) {
                    e.printStackTrace()
                    emitter.onError(e)
                }

                if (result != null)
                    if (result.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        val successful = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)

                    } else if (result.containsKey(AccountManager.KEY_INTENT)) {
                        // this case is not requested as soon as we put a context in the request,
                        // the AuthenticationActivity is automatically fired by android
                        val intent = result.getParcelable<Intent>(AccountManager.KEY_INTENT)
                        intent?.let {emitter.onNext(it)  }

                        //((Activity)context).startActivityForResult(intent, AuthenticationActivity.REQUEST_CODE_SIGN_IN);
                    }
            }, null)
        }
    }

    override fun getToken(): Observable<String> {
        return getToken(tokenType)
    }

    override fun setToken(tokenType: String, tokenValue: String): Observable<Void> {
        return Observable.create { emitter ->
            val accounts = accountManager.getAccountsByType(accountType)
            if (accounts.size > 0) {
                Log.w(TAG, "setAuthToken")
                Log.w(TAG, "Account " + accounts[0])
                Log.w(TAG, "tokenType $tokenType")
                Log.w(TAG, "tokenValue $tokenValue")
                accountManager.setAuthToken(accounts[0], tokenType, tokenValue)
                emitter.onComplete()
            } else {
                Log.w(TAG, "no account found")
                emitter.onComplete()
            }
        }
    }

    override fun getToken(tokenType: String): Observable<String> {
        return Observable.create { emitter ->
            val accounts = accountManager.getAccountsByType(accountType)
            if (accounts.size > 0) {
                emitter.onNext(accountManager.peekAuthToken(accounts[0], tokenType))
                emitter.onComplete()
            } else
                emitter.onComplete()
        }
    }

    override fun getAccount(accountType: String): Observable<Account> {
        return Observable.create { emitter ->
            val accounts = accountManager.getAccountsByType(accountType)
            var account: Account
            if (accounts.size > 0) {
                account = accountMapper.mapIn(accounts[0])
                account.accessToken = accountManager.peekAuthToken(accounts[0], tokenType)
                account.password = accountManager.getPassword(accounts[0])
                emitter.onNext(account)
                emitter.onComplete()
            }
        }
    }

    override fun getToken(account: Account, tokenType: String): Observable<String> {
        return Observable.create { emitter ->
            val accounts = accountManager.getAccountsByType(accountType)
            if (accounts.size > 0) {
                emitter.onNext(accountManager.peekAuthToken(
                        this.accountMapper.mapOut(account),
                        tokenType))
            } else
                emitter.onComplete()
        }
    }

    override fun invalidateToken(accountType: String, token: String): Observable<Void> {
        return Observable.create { emitter ->
            val accounts = accountManager.getAccountsByType(accountType)
            if (accounts.size > 0) {
                accountManager.invalidateAuthToken(accountType, token)
                Observable.empty<Void>()
            } else
                emitter.onError(Throwable("no account of this type"))
        }
    }

    override fun invalidateToken(tokenType: String): Observable<Void> {
        return Observable.create { emitter ->
            val accounts = accountManager.getAccountsByType(accountType)
            if (accounts.size > 0) {
                val token = accountManager.peekAuthToken(accounts[0], tokenType)
                if (token != null) {
                    accountManager.invalidateAuthToken(accountType, token)
                    Observable.empty<Void>()
                } else
                    emitter.onError(Throwable("no token of this type"))
            } else
                emitter.onError(Throwable("no account of this type"))
        }
    }


    companion object {
        private val TAG = AndroidAccountRepository::class.java.simpleName
        private const val HEADER_KEY_AUTHORIZATION = "Authorization"
        const val USER_DATA_USER_ID_KEY = "USER_DATA_USER_ID_KEY"
        const val USER_DATA_USERS_ID_KEY = "USER_DATA_USERS_ID_KEY"
        const val USER_DATA_USER_TYPE_KEY = "USER_DATA_USER_TYPE_KEY"
        const val USER_DATA_USER_VERIFIED = "USER_DATA_USER_VERIFIED"
    }


}
