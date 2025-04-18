package com.lattis.ellipse.data.platform.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.domain.model.Account;
import com.lattis.ellipse.presentation.dagger.qualifier.AccountType;

import javax.inject.Inject;

public class AccountMapper extends AbstractDataMapper<android.accounts.Account, Account>{

    private String accountType;

    @Inject
    public AccountMapper(@AccountType String accountType) {
        this.accountType = accountType;
    }

    @NonNull
    @Override
    public Account mapIn(@NonNull android.accounts.Account androidAccount) {
        return new Account(androidAccount.name, androidAccount.type);
    }

    @NonNull
    @Override
    public android.accounts.Account mapOut(@NonNull Account account) {
        return new android.accounts.Account(account.getAccountName(),accountType);
    }
}
