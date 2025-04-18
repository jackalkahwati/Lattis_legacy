package com.lattis.ellipse.presentation.ui.profile.changeMail;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;

import com.lattis.ellipse.data.network.model.response.AddPrivateNetworkResponse;
import com.lattis.ellipse.domain.interactor.user.AddPrivateNetworkUseCase;
import com.lattis.ellipse.domain.interactor.user.UpdateEmailUseCase;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;



/**
 * Created by lattis on 02/05/17.
 */

public class ChangeMailPresenter extends ActivityPresenter<ChangeMailView> {
    private String email;
    private UpdateEmailUseCase emailUseCase;
    public static String ARG_USER_ACCOUNT_TYPE = "ARG_USER_ACCOUNT_TYPE";
    public static String ARG_USER_FLEET_PRESENT = "ARG_USER_FLEET_PRESENT";
    public static String ARG_USER_ID = "USERID";
    private String ACCOUNT_TYPE = null;
    private String userID = null;
    public  static  String USER_ACCOUNT_TYPE_PRIVATE = "private_account";
    public static String USER_ACCOUNT_TYPE_MAIN = "main_account";
    private  boolean isPrivate = false;
    private boolean userFleetPresent = false;
    private AddPrivateNetworkUseCase addPrivateNetworkUseCase;



    @Inject
    ChangeMailPresenter(UpdateEmailUseCase emailUseCase,AddPrivateNetworkUseCase addPrivateNetworkUseCase) {
        this.emailUseCase = emailUseCase;
        this.addPrivateNetworkUseCase = addPrivateNetworkUseCase;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null) {

            ACCOUNT_TYPE = arguments.getString(ARG_USER_ACCOUNT_TYPE);
            this.userID = arguments.getString(ARG_USER_ID);

            if(ACCOUNT_TYPE!=null)
                view.onAccountType(ACCOUNT_TYPE);

            if(userID!=null)
                view.setUserId(userID);

            if (ACCOUNT_TYPE!=null && ACCOUNT_TYPE.equals(USER_ACCOUNT_TYPE_PRIVATE))
            {
                isPrivate = true;
            }
            else{
                isPrivate = false;
            }

            if(arguments.containsKey(ARG_USER_FLEET_PRESENT)){
                userFleetPresent = arguments.getBoolean(ARG_USER_FLEET_PRESENT);
            }


        }


    }

    public void updateEmail() {


        if (isPrivate)
        {
            subscriptions.add(addPrivateNetworkUseCase.withEmail(email).execute(new RxObserver<AddPrivateNetworkResponse>() {
                @Override
                public void onComplete() {

                }

                @Override
                public void onError(Throwable e) {
                    view.onCodeSentFail();
                }

                @Override
                public void onNext(AddPrivateNetworkResponse addPrivateNetworkResponse) {
                    if(addPrivateNetworkResponse!=null &&
                        addPrivateNetworkResponse.getAddPrivateNetworkDataResponse()!=null &&
                        addPrivateNetworkResponse.getAddPrivateNetworkDataResponse().getLattis_accounts()!=null){
                        if(addPrivateNetworkResponse.getAddPrivateNetworkDataResponse().getLattis_accounts().size()>0){
                            if(view!=null){
                                view.onCodeSentSuccess(email);
                            }
                        }else{
                            if(view!=null && userFleetPresent ){
                                view.onNoNewFleetWithCurrentFleetPresent();
                            }else if(view!=null && !userFleetPresent){
                                view.onNoNewFleetWithNoCurrentFleetPresent();
                            }
                        }
                    }else{
                        view.onCodeSentFail();
                    }


                }
            }));


        }else {
            subscriptions.add(emailUseCase.withEmail(email).execute(new RxObserver<Boolean>() {
                @Override
                public void onComplete() {

                }

                @Override
                public void onError(Throwable e) {
                    view.onCodeSentFail();
                }

                @Override
                public void onNext(Boolean status) {
                    view.onCodeSentSuccess(email);
                }
            }));
        }
    }

    @Override
    protected void updateViewState() {
        super.updateViewState();
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
