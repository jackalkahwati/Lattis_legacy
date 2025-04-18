package com.lattis.ellipse.data.platform;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.lattis.ellipse.data.platform.mapper.AccountMapper;
import com.lattis.ellipse.domain.model.Account;
import com.lattis.ellipse.domain.repository.AccountRepository;
import com.lattis.ellipse.presentation.dagger.module.AuthenticationModule;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Emitter;
import io.reactivex.Observable;

public class AndroidAccountRepository implements AccountRepository {

    private static final String TAG = AndroidAccountRepository.class.getSimpleName();

    private final AccountManager accountManager;
    private final AccountMapper accountMapper;
    private final String accountType;
    private final String tokenType;

    @Inject
    public AndroidAccountRepository(AccountManager accountManager,
                                    AccountMapper accountMapper,
                                    String accountType,
                                    String authTokenType) {
        this.accountManager = accountManager;
        this.accountMapper = accountMapper;
        this.accountType = accountType;
        this.tokenType = authTokenType;
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public Observable<Account> addAccountExplicitly(Account account) {
        return Observable.create(emitter -> {
            if(account!=null){

                Log.e("addAccountExplicitly "," "+account.getUserId());
                Log.e("addAccountExplicitly "," "+account.getAccountName());
                Log.e("addAccountExplicitly "," "+account.getRefreshToken());


                final android.accounts.Account newAccount = this.accountMapper.mapOut(account);
                android.accounts.Account[] accounts = accountManager.getAccountsByType(accountType);
                if (accounts.length == 0) {
                    setResult(account, emitter, newAccount);
                } else { //We need to update the account instead
                    /*if(!accounts[0].name.equals(newAccount.name)){

                    }else setResult(account, subscriber, newAccount);*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        accountManager.removeAccountExplicitly(accounts[0]);
                        setResult(account, emitter, newAccount);
                    }else {
                        accountManager.removeAccount(accounts[0],accountManagerFuture -> {
                            setResult(account, emitter, newAccount);
                        },null);
                    }

                }
            }else emitter.onError(new Throwable("cannot add a null account"));
        });
    }

    private void setResult(Account account, Emitter<? super Account> subscriber, android.accounts.Account newAccount) {
        accountManager.addAccountExplicitly(newAccount,account.getPassword(), null);
        accountManager.setAuthToken(newAccount,tokenType, account.getAccessToken());
        accountManager.setUserData(newAccount, AuthenticationModule.USER_DATA_USER_ID_KEY, String.valueOf(account.getUserId()));
        accountManager.setUserData(newAccount, AuthenticationModule.USER_DATA_USERS_ID_KEY, account.getUsersId());
        accountManager.setUserData(newAccount, AuthenticationModule.USER_DATA_USER_TYPE_KEY, account.getUserType());
        accountManager.setUserData(newAccount, AuthenticationModule.USER_DATA_USER_VERIFIED, Boolean.toString(account.isVerified()));
        subscriber.onNext(account);
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public Observable<Boolean> signOut() {
        return Observable.create(subscriber -> {
            android.accounts.Account[] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length > 0){
                String tokenToInvalidate = accountManager.peekAuthToken(accounts[0],tokenType);
                accountManager.invalidateAuthToken(accountType,tokenToInvalidate);
                accountManager.setPassword(accounts[0],null);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    accountManager.removeAccountExplicitly(accounts[0]);
                }else {
                    accountManager.removeAccount(accounts[0],null,null);
                }
                subscriber.onNext(true);
            }else subscriber.onError(new Throwable("account not found"));
        });
    }

    @Override
    public Observable<List<Account>> getAccounts() {
        return getAccounts(accountType);
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public Observable<List<Account>> getAccounts(String accountType) {
        return Observable.create(emitter -> {
            android.accounts.Account[] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length > 0){
               //subscriber.onNext(accountMapper.mapOut(accounts[0]));
            }else emitter.onComplete();
        });
    }

    @Override
    public Observable<Account> getAccount() {
        return getAccount(accountType);
    }

    @Override
    public Observable<Void> invalidateToken() {
        return invalidateToken(tokenType);
    }

    @Override
    public Observable<Cloneable> addAccount() {
        return Observable.create(emitter -> {
            accountManager.addAccount(accountType, tokenType, null,
                    null,null, accountManagerFuture -> {
                Bundle result = null;
                try {
                    result = accountManagerFuture.getResult();
                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
                if(result!=null)
                    if (result.containsKey(AccountManager.KEY_BOOLEAN_RESULT)){
                        boolean successful = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);

                    } else if(result.containsKey(AccountManager.KEY_INTENT)) {
                        // this case is not requested as soon as we put a context in the request,
                        // the AuthenticationActivity is automatically fired by android
                        Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
                        emitter.onNext(intent);
                        //((Activity)context).startActivityForResult(intent, AuthenticationActivity.REQUEST_CODE_SIGN_IN);
                    }
            },null);
        });
    }

    @Override
    public Observable<String> getToken() {
        return getToken(tokenType);
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public Observable<Void> setToken(String tokenType, String tokenValue) {
        return Observable.create(emitter -> {
            android.accounts.Account[] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length > 0){
                Log.w(TAG,"setAuthToken");
                Log.w(TAG,"Account "+accounts[0]);
                Log.w(TAG,"tokenType "+tokenType);
                Log.w(TAG,"tokenValue "+tokenValue);
                accountManager.setAuthToken(accounts[0],tokenType,tokenValue);
                emitter.onComplete();
            }else {
                Log.w(TAG,"no account found");
                emitter.onComplete();
            }
        });
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public Observable<String> getToken(String tokenType) {
        return Observable.create(emitter -> {
            android.accounts.Account[] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length > 0){
                emitter.onNext(accountManager.peekAuthToken(accounts[0],tokenType));
                emitter.onComplete();
            }else emitter.onComplete();
        });
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public Observable<Account> getAccount(String accountType) {
        return Observable.create(emitter -> {
            android.accounts.Account[] accounts = accountManager.getAccountsByType(accountType);
            Account account = null;
            if(accounts.length > 0){
                account = accountMapper.mapIn(accounts[0]);
                account.setAccessToken(accountManager.peekAuthToken(accounts[0],tokenType));
                account.setPassword(accountManager.getPassword(accounts[0]));

            }
            emitter.onNext(account);
            emitter.onComplete();
        });
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public Observable<String> getToken(Account account, String tokenType) {
        return Observable.create(emitter -> {
            android.accounts.Account[] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length > 0){
                emitter.onNext(accountManager.peekAuthToken(
                        this.accountMapper.mapOut(account),
                        tokenType));
            }else emitter.onComplete();
        });
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public Observable<Void> invalidateToken(String accountType, String token) {
        return Observable.create(emitter -> {
            android.accounts.Account[] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length > 0){
                accountManager.invalidateAuthToken(accountType,token);
                emitter.onNext(null);
            }else emitter.onError(new Throwable("no account of this type"));
        });
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public Observable<Void> invalidateToken(String tokenType) {
        return Observable.create(emitter -> {
            android.accounts.Account[] accounts = accountManager.getAccountsByType(accountType);
            if(accounts.length > 0){
                String token = accountManager.peekAuthToken(accounts[0],tokenType);
                if(token!=null){
                    accountManager.invalidateAuthToken(accountType,token);
                    emitter.onNext(null);
                }else emitter.onError(new Throwable("no token of this type"));
            }else emitter.onError(new Throwable("no account of this type"));
        });
    }


}
