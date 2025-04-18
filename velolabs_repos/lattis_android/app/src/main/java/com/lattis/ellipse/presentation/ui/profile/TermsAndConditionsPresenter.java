package com.lattis.ellipse.presentation.ui.profile;

import com.lattis.ellipse.domain.interactor.authentication.AcceptTermsConditionsUseCase;
import com.lattis.ellipse.domain.interactor.authentication.DeleteAccountUseCase;
import com.lattis.ellipse.presentation.setting.BooleanPref;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;
import javax.inject.Named;

import static com.lattis.ellipse.presentation.dagger.module.SettingModule.PREF_KEY__ACCEPT_TERMS_AND_CONDITION;

public class TermsAndConditionsPresenter extends ActivityPresenter<TermsAndConditionsView> {

    private AcceptTermsConditionsUseCase acceptTermsConditionsUseCase;
    private DeleteAccountUseCase deleteAccountUseCase;
    private BooleanPref tcPref;

    @Inject
    public TermsAndConditionsPresenter(AcceptTermsConditionsUseCase acceptTermsConditionsUseCase,
                                       DeleteAccountUseCase deleteAccountUseCase,
                                       @Named(PREF_KEY__ACCEPT_TERMS_AND_CONDITION) BooleanPref tcPref) {
        this.acceptTermsConditionsUseCase = acceptTermsConditionsUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.tcPref = tcPref;
    }

    @Override
    protected void updateViewState() {

    }

    public void acceptTermsAndConditions() {
        subscriptions.add(acceptTermsConditionsUseCase.setHasAccepted(true).execute(new RxObserver<Boolean>(view, false) {
            @Override
            public void onNext(Boolean aVoid) {
                tcPref.setValue(true);
                view.onTermsAndConditionsAccepted();
                super.onNext(aVoid);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
            }
        }));
    }

    public void declineTermsAndConditions() {
        subscriptions.add(deleteAccountUseCase.execute(new RxObserver<Boolean>(view, false) {
            @Override
            public void onNext(Boolean aVoid) {
                view.onTermsAndConditionsDeclined();
                super.onNext(aVoid);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
            }
        }));
    }

}
