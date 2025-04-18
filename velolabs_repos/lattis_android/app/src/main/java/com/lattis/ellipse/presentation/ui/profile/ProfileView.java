package com.lattis.ellipse.presentation.ui.profile;

import androidx.annotation.StringRes;

import com.lattis.ellipse.domain.model.PrivateNetwork;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.ui.base.BaseView;

import java.util.List;

public interface ProfileView extends BaseView {

    void setLastName(String lastName);
    void setFirstName(String firstName);
    void setPhoneNumber(String phoneNumber);
    void setEmail(String email);
    void setImage(String imageUri);
    void setNoImage();
    void showEmailError(@StringRes int error);
    void hideEmailError();
    void onUserUpdated(User user);
    void onUserUpdateFailed();
    void setUserId(String userId);
    void setPrivateNetwork(List<PrivateNetwork> privateNetworks);
    void setNoPrivateNetwork();
    void checkForHintAndError(String type,String value);
}
