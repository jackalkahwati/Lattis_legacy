package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.domain.model.Account;

import java.util.List;

import io.reactivex.Observable;

public interface AccountRepository {

    Observable<List<Account>> getAccounts();

    Observable<Account> getAccount();

    Observable<String> getToken();

    Observable<Void> setToken(String tokenType, String tokenValue);

    Observable<String> getToken(String tokenType);

    Observable<Void> invalidateToken();

    Observable<Cloneable> addAccount();

    Observable<Account> addAccountExplicitly(Account account);

    Observable<Boolean> signOut();

    Observable<Void> invalidateToken(String tokenType);

    Observable<List<Account>> getAccounts(String accountType);

    Observable<Account> getAccount(String accountType);

    Observable<String> getToken(Account account, String tokenType);

    Observable<Void> invalidateToken(String accountType, String token);

}
