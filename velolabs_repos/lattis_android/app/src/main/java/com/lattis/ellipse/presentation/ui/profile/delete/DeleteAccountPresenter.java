package com.lattis.ellipse.presentation.ui.profile.delete;

import com.lattis.ellipse.domain.interactor.authentication.DeleteAccountUseCase;
import com.lattis.ellipse.domain.interactor.authentication.LogOutUseCase;
import com.lattis.ellipse.presentation.setting.BooleanPref;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;
import javax.inject.Named;

import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_HAS_ACCEPTED_TERMS_CONDITIONS;
import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_SHOW_ON_BOARDING_FLOW;

public class DeleteAccountPresenter extends ActivityPresenter<DeleteAccountView> {

    private DeleteAccountUseCase deleteAccountUseCase;
    private LogOutUseCase logOutUseCase;
    private BooleanPref hasAcceptedTermsConditionsPref;
    private BooleanPref showOnBoardingFlowPref;

    @Inject
    DeleteAccountPresenter(DeleteAccountUseCase deleteAccountUseCase,
                           LogOutUseCase logOutUseCase,
                           @Named(KEY_HAS_ACCEPTED_TERMS_CONDITIONS) BooleanPref hasAcceptedTermsConditionsPref,
                           @Named(KEY_SHOW_ON_BOARDING_FLOW) BooleanPref showOnBoardingFlowPref) {
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.logOutUseCase = logOutUseCase;
        this.hasAcceptedTermsConditionsPref = hasAcceptedTermsConditionsPref;
        this.showOnBoardingFlowPref = showOnBoardingFlowPref;
    }

    @Override
    protected void updateViewState() {}

    void deleteAccount() {
        subscriptions.add(deleteAccountUseCase.execute(new RxObserver<Boolean>(view, false) {
            @Override
            public void onNext(Boolean aVoid) {
                super.onNext(aVoid);

                view.onAccountDeleted();
            }
            @Override
            public void onError(Throwable e) {
                super.onError(e);

                view.onAccountDeletionFailed();
            }
        }));
    }


    void logOut() {
        subscriptions.add(logOutUseCase.execute(new RxObserver<Boolean>(view, false) {
            @Override
            public void onNext(Boolean success) {
                hasAcceptedTermsConditionsPref.remove();
                showOnBoardingFlowPref.remove();
                view.onLogOutSuccessful();
            }
            @Override
            public void onError(Throwable e) {
                super.onError(e);
            }
        }));
    }
}
