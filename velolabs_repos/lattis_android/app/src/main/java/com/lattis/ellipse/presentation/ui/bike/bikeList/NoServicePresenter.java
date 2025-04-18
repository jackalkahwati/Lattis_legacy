package com.lattis.ellipse.presentation.ui.bike.bikeList;


import com.lattis.ellipse.domain.interactor.user.GetUserUseCase;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

public class NoServicePresenter extends ActivityPresenter<NoServiceView> {
    GetUserUseCase getUserUseCase;

    @Inject
    NoServicePresenter(GetUserUseCase getUserUseCase) {
        this.getUserUseCase = getUserUseCase;
    }

    @Override
    protected void updateViewState() {

    }

    public void getUserProfile() {
        subscriptions.add(getUserUseCase.execute(new RxObserver<User>(view, false) {
            @Override
            public void onNext(User currUser) {
                view.onGetUserSuccess(currUser);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                e.printStackTrace();
                view.onGetUserFail();
            }
        }));
    }

}
