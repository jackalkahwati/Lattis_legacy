package io.lattis.operator.authentication

import android.accounts.*
import android.accounts.AccountManager.KEY_BOOLEAN_RESULT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import io.lattis.operator.presentation.authentication.SignInActivity

import javax.inject.Inject

class AccountAuthenticator @Inject
constructor(private val context: Context) : AbstractAccountAuthenticator(context) {
    private val accountManager: AccountManager
    private val handler: Handler

    init {
        this.accountManager = AccountManager.get(context)
        handler = Handler()
    }

    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle? {
        return null
    }

    @SuppressWarnings("MissingPermission")
    @Throws(NetworkErrorException::class)
    override fun addAccount(response: AccountAuthenticatorResponse,
                            accountType: String,
                            authTokenType: String,
                            requiredFeatures: Array<String>?,
                            options: Bundle): Bundle {

        val accounts = accountManager.getAccountsByType(accountType)
        var loginBundle: Bundle
//        if (accounts.size == 0) {
//            if (authTokenType != null) {
                loginBundle = getLoginBundle(context, accountType, null, response, authTokenType, options)
//            } else {
//                //TODO
//            }
//        } else {
//            //TODO
//        }
        return loginBundle
    }

    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(response: AccountAuthenticatorResponse,
                                    account: Account,
                                    options: Bundle): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(response: AccountAuthenticatorResponse,
                              account: Account,
                              authTokenType: String,
                              options: Bundle): Bundle {

        // If the caller requested an invalid token type, return error
        // if (!authTokenType.equals(UserData.DEFAULT_AUTH_TOKEN_TYPE)) {
        //Timber.d("Invalid authentication token type " + authTokenType);
        //return generateError(ERROR_CODE_AUTH_TOKEN, "Invalid authentication token type.");
        // }

        // Check if a valid authToken exists.
        val authToken = accountManager.peekAuthToken(account, authTokenType)

        // If a valid authToken exists, return it. Otherwise, try to refresh token using available refresh token.
        // If refresh token is not available, direct user to login screen.
        return if (!TextUtils.isEmpty(authToken)) {
            // Timber.d("Valid authToken found, returning it.");
            generateAuthTokenBundle(account.name, authToken)
        } else {
            //Timber.d("Unable to get refresh token, starting login activity.");
            getLoginBundle(context, account.type, account.name, response, authTokenType, options)
        }
    }

    override fun getAuthTokenLabel(authTokenType: String): String {
        //return authTokenType.equals(UserData.DEFAULT_AUTH_TOKEN_TYPE) ? authTokenType : null;
        return ""
    }

    @Throws(NetworkErrorException::class)
    override fun updateCredentials(response: AccountAuthenticatorResponse,
                                   account: Account,
                                   authTokenType: String,
                                   options: Bundle): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun hasFeatures(response: AccountAuthenticatorResponse,
                             account: Account,
                             features: Array<String>): Bundle {

        val result = Bundle()
        result.putBoolean(KEY_BOOLEAN_RESULT, false)
        return result
    }

    /**
     * Generate a bundle that contains account name, account type, and authentication token.
     * @param accountName AceAccount name
     * @param authToken Associated authentication token of account
     *
     * @return Bundle
     */
    private fun generateAuthTokenBundle(accountName: String, authToken: String): Bundle {
        val bundle = Bundle()
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName)
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, "io.lattis.operator")
        bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken)
        return bundle
    }

    /**
     * Generate an error bundle.
     *
     * @param errorCode Error code
     * @param errorMessage Error message
     * @return Bundle
     */
    private fun generateError(errorCode: Int, errorMessage: String): Bundle {
        val bundle = Bundle()
        bundle.putInt(AccountManager.KEY_ERROR_CODE, errorCode)
        bundle.putString(AccountManager.KEY_ERROR_MESSAGE, errorMessage)
        return bundle
    }

    /**
     * Initialize a login activity bundle to obtain user credentials.
     *
     * @param context Context
     * @param accountType the type of account to add, will never be null
     * @param accountName User identification
     * @param response to send the result back to the AccountManager, will never be null
     * @param authTokenType the type of auth token to retrieve after adding the account, may be null
     * @param options a Bundle of authenticator-specific options, may be null
     * @return a Bundle result or null if the result is to be returned via the response. The result
     * will contain either:
     */
    private fun getLoginBundle(context: Context,
                               accountType: String,
                               accountName: String?,
                               response: AccountAuthenticatorResponse,
                               authTokenType: String?,
                               options: Bundle): Bundle {

        val intent = Intent(context, SignInActivity::class.java)

        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)

        // Only one auth token type used
        if (authTokenType != null) {
            intent.putExtra(AccountManager.KEY_AUTHENTICATOR_TYPES, authTokenType)
        }

        if (accountName != null) {
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName)
        }

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    private fun runOnUiThread(runnable: Runnable) {
        handler.post(runnable)
    }
}
