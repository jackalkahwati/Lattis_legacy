package com.lattis.ellipse.data.network.model.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.data.network.model.response.UserResponse;
import com.lattis.ellipse.domain.model.Account;

import javax.inject.Inject;

public class AccountMapper extends AbstractDataMapper<UserResponse,Account> {

    @Inject
    public AccountMapper() {}

    @NonNull
    @Override
    public Account mapIn(@NonNull UserResponse userResponse) {
        Account account = new Account(userResponse.getUserId(),null);
        account.setAccessToken(userResponse.getRestToken());
        account.setPassword(userResponse.getRefreshToken());
        account.setUserId(userResponse.getUserId());
        account.setUsersId(userResponse.getUsersId());
        account.setUserType(userResponse.getUserType());
        account.setVerified(userResponse.isVerified());
        return account;
    }

    @NonNull
    @Override
    public UserResponse mapOut(@NonNull Account account) {
        return null;
    }
}
