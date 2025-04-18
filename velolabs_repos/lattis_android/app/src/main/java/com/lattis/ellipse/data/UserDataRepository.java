package com.lattis.ellipse.data;

import com.lattis.ellipse.data.database.MediaRealmDataStore;
import com.lattis.ellipse.data.database.PrivateNetworkRealmDataStore;
import com.lattis.ellipse.data.database.UserRealmDataStore;
import com.lattis.ellipse.data.network.model.response.AddPrivateNetworkResponse;
import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.data.network.store.UserNetworkDataStore;
import com.lattis.ellipse.domain.model.Media;
import com.lattis.ellipse.domain.model.PrivateNetwork;
import com.lattis.ellipse.domain.model.TermsAndConditions;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.domain.repository.UserRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Function;


public class UserDataRepository implements UserRepository {

    private UserNetworkDataStore userNetworkDataStore;
    private UserRealmDataStore userRealmDataStore;
    private PrivateNetworkRealmDataStore privateNetworkRealmDataStore;
    private String userId;
    private MediaRealmDataStore mediaRealmDataStore;

    @Inject
    public UserDataRepository(UserNetworkDataStore userNetworkDataStore,
                              UserRealmDataStore userRealmDataStore,
                              MediaRealmDataStore mediaRealmDataStore,
                              PrivateNetworkRealmDataStore privateNetworkRealmDataStore,
                              String userId) {
        this.userNetworkDataStore = userNetworkDataStore;
        this.userRealmDataStore = userRealmDataStore;
        this.mediaRealmDataStore = mediaRealmDataStore;
        this.privateNetworkRealmDataStore =privateNetworkRealmDataStore;
        this.userId = userId;
    }

    @Override
    public Observable<User> getUser() {
        return userNetworkDataStore.getUser()
                .flatMap(new Function<User, Observable<User>>() {
                    @Override
                    public Observable<User> apply(User user) {
                        String mediaId = String.format("m:u:%s:pp", user.getId());
                        return mediaRealmDataStore.getMedia(mediaId)
                                .flatMap(new Function<Media, Observable<User>>() {
                                    @Override
                                    public Observable<User> apply(Media media) {
                                        user.setImageUri(media.getUrl());
                                        return Observable.just(user);
                                    }
                                })
                                .onErrorResumeNext(throwable -> {
                                    return Observable.just(user);
                                });
                    }
                })
                .flatMap(this::saveUserLocally)
                .onErrorResumeNext(throwable -> {
                    return Observable.mergeDelayError(
                            userRealmDataStore.getUser(),
                            Observable.error(throwable));
                });

    }

    @Override
    public Observable<User> getLocalUser(){
        return userRealmDataStore.getUser();
    }

    @Override
    public Observable<User> saveUser(User user) {
        if (user.getImageUri() != null) {
            String mediaId = String.format("m:u:%s:pp", user.getId());
            Media media = new Media(mediaId, user.getImageUri());
            return mediaRealmDataStore.createOrUpdateUser(media)
                    .flatMap(new Function<Media, Observable<User>>() {
                        @Override
                        public Observable<User> apply(Media media) {
                            //savePrivateNetworks(user);
                            return userNetworkDataStore.saveUser(user)
                                    .flatMap(user1 -> saveUserLocally(user1));

                        }
                    });

        } else {
            return userNetworkDataStore.saveUser(user).flatMap(this::saveUserLocally);
        }
    }

    private Observable<List<PrivateNetwork>> savePrivateNetworks(User user){
        return privateNetworkRealmDataStore.savePrivateNetworkList(user.getPrivateNetworks());
    }

    @Override
    public Observable<User> saveUserLocally(User user) {
        return userRealmDataStore.createOrUpdateUser(user).flatMap(user1 -> {
            if(user.getPrivateNetworks()!=null)
                user1.setPrivateNetworks(user.getPrivateNetworks());
            return Observable.just(user1);
        });
    }



    @Override
    public Observable<Boolean> sendCodeToUpdatePhoneNumber(String countryCode, String phoneNumber) {
        return userNetworkDataStore.sendCodeToUpdatePhoneNumber(countryCode, phoneNumber);
    }

    @Override
    public Observable<Boolean> validateCodeForChangePhoneNumber(String code, String phoneNumber) {
        return userNetworkDataStore.validateCodeForChangePhoneNumber(code, phoneNumber);
    }


    @Override
    public Observable<Boolean> sendCodeToUpdateEmail(String email) {
        return userNetworkDataStore.sendCodeForUpdateEmail(email);
    }

    @Override
    public Observable<Boolean> changePassword(String password, String newPassword) {
        return userNetworkDataStore.changePassword(password, newPassword);
    }

    @Override
    public Observable<GetCurrentUserStatusResponse> getCurrentUserStatus() {
        return userNetworkDataStore.getCurrentUserStatus();
    }

    @Override
    public Observable<TermsAndConditions> getTermsAndConditions() {
        return userNetworkDataStore.getTermsAndConditions();
    }

    @Override
    public Observable<Boolean> acceptTermsAndCondition(boolean accepted) {
        return userNetworkDataStore.acceptTermsAndCondition(accepted);
    }

    @Override
    public Observable<Boolean> checkTermsAndConditionAccepted() {
        return userNetworkDataStore.checkTermsAndConditionAccepted();
    }

    @Override
    public Observable<BasicResponse> sendForgotPasswordCode(String email) {
        return userNetworkDataStore.sendForgotPasswordCode(email);
    }

    @Override
    public Observable<BasicResponse> confirmCodeForForgotPassword(String email, String code, String password) {
        return userNetworkDataStore.confirmCodeForForgotPassword(email, code, password);
    }

    @Override
    public Observable<Boolean> validateCodeForChangeEmail(String code, String email) {
        return userNetworkDataStore.validateCodeForChangeEmail(code, email);
    }

    @Override
    public Observable<Boolean> confirmVerificationCodeForPrivateNetwork(String userId, String account_type, String confirmationCode) {
        return userNetworkDataStore.confirmVerificationCodeForPrivateNetwork(userId,account_type,confirmationCode);
    }

    @Override
    public Observable<AddPrivateNetworkResponse> addPrivateNetworkEmail(String email) {
        return userNetworkDataStore.addPrivateNetworkEmail(email);
    }

    @Override
    public Observable<Boolean> deleteUserAccount() {
        return userNetworkDataStore.deleteUserAccount();
    }
}