package com.lattis.ellipse.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.lattis.ellipse.presentation.ui.authentication.intro.AuthenticationIntroActivity;

import javax.inject.Inject;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private Context context;
    private AccountManager accountManager;
    private Handler handler;

    @Inject
    public AccountAuthenticator(Context context) {
        super(context);
        this.context = context;
        this.accountManager = AccountManager.get(context);
        handler = new Handler();
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override @SuppressWarnings("MissingPermission")
    public Bundle addAccount(AccountAuthenticatorResponse response,
                             String accountType,
                             String authTokenType,
                             String[] requiredFeatures,
                             Bundle options) throws NetworkErrorException {

        Account[] accounts = accountManager.getAccountsByType(accountType);
        Bundle loginBundle = null;
        if (accounts.length == 0) {
            if (authTokenType!=null) {
                loginBundle = getLoginBundle(context,accountType,null,response,authTokenType,options);
            } else {
                //TODO
            }
        } else{
           //TODO
        }
        return loginBundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                                     Account account,
                                     Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
                               Account account,
                               String authTokenType,
                               Bundle options) throws NetworkErrorException {

        // If the caller requested an invalid token type, return error
       // if (!authTokenType.equals(UserData.DEFAULT_AUTH_TOKEN_TYPE)) {
            //Timber.d("Invalid authentication token type " + authTokenType);
            //return generateError(ERROR_CODE_AUTH_TOKEN, "Invalid authentication token type.");
       // }

        // Check if a valid authToken exists.
        String authToken = accountManager.peekAuthToken(account, authTokenType);

        // If a valid authToken exists, return it. Otherwise, try to refresh token using available refresh token.
        // If refresh token is not available, direct user to login screen.
        if (!TextUtils.isEmpty(authToken)) {
           // Timber.d("Valid authToken found, returning it.");
            return generateAuthTokenBundle(account.name, authToken);
        } else {
            //Timber.d("Unable to get refresh token, starting login activity.");
            return getLoginBundle(context, account.type, account.name, response, authTokenType, options);
        }
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        //return authTokenType.equals(UserData.DEFAULT_AUTH_TOKEN_TYPE) ? authTokenType : null;
        return "";
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
                                    Account account,
                                    String authTokenType,
                                    Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
                              Account account,
                              String[] features) throws NetworkErrorException {

        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    /**
     * Generate a bundle that contains account name, account type, and authentication token.
     * @param accountName AceAccount name
     * @param authToken Associated authentication token of account
     *
     * @return Bundle
     */
    private Bundle generateAuthTokenBundle(String accountName, String authToken) {
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, "com.ellipse");
        bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        return bundle;
    }

    /**
     * Generate an error bundle.
     *
     * @param errorCode Error code
     * @param errorMessage Error message
     * @return Bundle
     */
    private Bundle generateError(int errorCode, String errorMessage) {
        Bundle bundle = new Bundle();
        bundle.putInt(AccountManager.KEY_ERROR_CODE, errorCode);
        bundle.putString(AccountManager.KEY_ERROR_MESSAGE, errorMessage);
        return bundle;
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
    private Bundle getLoginBundle(Context context,
                                  String accountType,
                                  String accountName,
                                  AccountAuthenticatorResponse response,
                                  String authTokenType,
                                  Bundle options) {

        //TODO should be dynamic
        final Intent intent = new Intent(context, AuthenticationIntroActivity.class);

        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        // Only one auth token type used
        if (authTokenType != null) {
            intent.putExtra(AccountManager.KEY_AUTHENTICATOR_TYPES, authTokenType);
        }

        if (accountName != null) {
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName);
        }

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }
}
