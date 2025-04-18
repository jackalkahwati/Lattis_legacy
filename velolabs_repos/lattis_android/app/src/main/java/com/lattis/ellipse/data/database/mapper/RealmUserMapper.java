package com.lattis.ellipse.data.database.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.database.base.AbstractRealmDataMapper;
import com.lattis.ellipse.data.database.model.RealmUser;
import com.lattis.ellipse.domain.model.User;

import javax.inject.Inject;

public class RealmUserMapper extends AbstractRealmDataMapper<User,RealmUser> {

    @Inject
    public RealmUserMapper() {}

    @NonNull
    @Override
    public RealmUser mapIn(@NonNull User user) {
        RealmUser realmUser = new RealmUser();
        realmUser.setId(user.getId());
        realmUser.setFirstName(user.getFirstName());
        realmUser.setLastName(user.getLastName());
        realmUser.setEmail(user.getEmail());
        realmUser.setPhoneNumber(user.getPhoneNumber());
        realmUser.setRegistrationId(user.getRegistrationId());
        realmUser.setUsingFacebook(user.isUsingFacebook());
        realmUser.setImageURI(user.getImageUri());
        return realmUser;
    }

    @NonNull
    @Override
    public User mapOut(@NonNull RealmUser realmUser) {
        User user = new User();
        user.setId(realmUser.getId());
        user.setFirstName(realmUser.getFirstName());
        user.setLastName(realmUser.getLastName());
        user.setEmail(realmUser.getEmail());
        user.setPhoneNumber(realmUser.getPhoneNumber());
        user.setRegistrationId(realmUser.getRegistrationId());
        user.setUsingFacebook(realmUser.isUsingFacebook());
        user.setImageUri(realmUser.getImageURI());

        return user;
    }
}
