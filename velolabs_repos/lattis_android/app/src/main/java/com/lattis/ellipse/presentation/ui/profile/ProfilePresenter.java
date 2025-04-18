package com.lattis.ellipse.presentation.ui.profile;

import android.text.TextUtils;

import com.lattis.ellipse.domain.interactor.error.UpdateUserValidationError;
import com.lattis.ellipse.domain.interactor.profile.UpdateUserUseCase;
import com.lattis.ellipse.domain.interactor.user.GetUserUseCase;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.List;

import javax.inject.Inject;

import io.lattis.ellipse.R;



public class ProfilePresenter extends ActivityPresenter<ProfileView> {
    private String lastName;
    private String firstName;
    private String phoneNumber;
    private String email;
    private String imageUri;
    private User user;
    private UpdateUserUseCase updateUserUseCase;
    private GetUserUseCase getUserUseCase;


    @Inject
    ProfilePresenter(UpdateUserUseCase updateUserUseCase,GetUserUseCase getUserUseCase)
    {
        this.getUserUseCase = getUserUseCase;
        this.updateUserUseCase = updateUserUseCase;

    }



    @Override
    protected void updateViewState() {

    }

    public void getUserProfile(){
        subscriptions.add(getUserUseCase.execute(new RxObserver<User>(view, false) {
            @Override
            public void onNext(User currUser) {
                if(currUser!=null){
                    user  = currUser;
                    view.setFirstName(user.getFirstName());
                    view.setLastName(user.getLastName());
                    view.setPhoneNumber(user.getPhoneNumber());
                    view.setEmail(user.getEmail());
                    view.setUserId(user.getId()
                    );
                    if(user.getImageUri()!=null && !user.getImageUri().equals("")){
                        view.setImage((user.getImageUri()));
                    }else{
                        view.setNoImage();
                    }


                    if(user.getPrivateNetworks()!=null && user.getPrivateNetworks().size()>0){
                        view.setPrivateNetwork(user.getPrivateNetworks());
                    }else{
                        view.setNoPrivateNetwork();
                    }

                }else{
                    user = new User();
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                view.setFirstName("");
                e.printStackTrace();
            }
        }));
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        view.checkForHintAndError("lastname",lastName);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        view.checkForHintAndError("firstname",firstName);

    }

    public boolean readyToSave(){
        return !TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName);
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void updateUser() {
        if(user==null){
            user = new User();
        }
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setImageUri(imageUri);

        subscriptions.add(updateUserUseCase
                .withUser(user)
                .execute(new RxObserver<User>(view, false) {
                    @Override
                    public void onNext(User user) {
                        view.onUserUpdated(user);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e instanceof UpdateUserValidationError) {
                            onValidationError((UpdateUserValidationError) e);
                        } else {
                            view.onUserUpdateFailed();
                        }
                    }
                }));
    }

    public void onValidationError(UpdateUserValidationError e) {
        List<UpdateUserValidationError.Status> status = e.getStatus();
        if (status.contains(UpdateUserValidationError.Status.INVALID_EMAIL)) {
            view.showEmailError(R.string.error_invalid_email);
        } else {
            view.hideEmailError();
        }
    }

}
