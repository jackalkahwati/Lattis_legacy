package com.lattis.ellipse.presentation.ui.home;

import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.domain.interactor.authentication.CheckAcceptTermsConditionsUseCase;
import com.lattis.ellipse.domain.interactor.user.GetCurrentUserStatusUseCase;
import com.lattis.ellipse.presentation.setting.BooleanPref;
import com.lattis.ellipse.presentation.setting.IntPref;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;
import javax.inject.Named;

import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_HAS_ACCEPTED_TERMS_CONDITIONS;
import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_QR_CODE_HELP_COUNT;
import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_SHOW_ON_BOARDING_FLOW;

class HomePresenter extends ActivityPresenter<HomeView> {

    private CheckAcceptTermsConditionsUseCase checkAcceptTermsConditionsUseCase;

    private BooleanPref hasAcceptedTermsConditionsPref;
    private BooleanPref showOnBoardingFlowPref;
    private final GetCurrentUserStatusUseCase getCurrentUserStatusUseCase;
    private IntPref QR_CODE_HELP_COUNT_PREF;


    @Inject
    public HomePresenter(CheckAcceptTermsConditionsUseCase checkAcceptTermsConditionsUseCase,
                         @Named(KEY_QR_CODE_HELP_COUNT) IntPref QR_CODE_HELP_COUNT_PREF,
                         @Named(KEY_HAS_ACCEPTED_TERMS_CONDITIONS) BooleanPref hasSeenTermsConditionsPref,
                         @Named(KEY_SHOW_ON_BOARDING_FLOW) BooleanPref showOnBoardingFlowPref,
                         GetCurrentUserStatusUseCase getCurrentUserStatusUseCase) {
        this.checkAcceptTermsConditionsUseCase = checkAcceptTermsConditionsUseCase;
        this.hasAcceptedTermsConditionsPref = hasSeenTermsConditionsPref;
        this.showOnBoardingFlowPref = showOnBoardingFlowPref;
        this.getCurrentUserStatusUseCase = getCurrentUserStatusUseCase;
        this.QR_CODE_HELP_COUNT_PREF = QR_CODE_HELP_COUNT_PREF;
    }

    public void getCurrentUserStatus() {
        subscriptions.add(getCurrentUserStatusUseCase
                .execute(new RxObserver<GetCurrentUserStatusResponse>(view) {
                    @Override
                    public void onNext(GetCurrentUserStatusResponse getCurrentUserStatusResponse) {
                        super.onNext(getCurrentUserStatusResponse);
                        view.onGetCurrentUserStatusSuccess(getCurrentUserStatusResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onGetCurrentUserStatusFailure();
                    }
                }));
    }

    @Override
    protected void updateViewState() {
//        if (hasAcceptedTermsConditionsPref.getValue() == Boolean.FALSE) {
//            subscriptions.add(checkAcceptTermsConditionsUseCase.execute(new RxSubscriber<Boolean>() {
//                @Override
//                public void onNext(Boolean accepted) {
//                    if (accepted) {
//                        hasAcceptedTermsConditionsPref.setValue(true);
//                    } else {
//                        view.showTermsAndConditions();
//                    }
//                }
//
//                @Override
//                public void onError(Throwable e) {
//                    super.onError(e);
//                }
//            }));
//        } else if (showOnBoardingFlowPref.getValue() == Boolean.TRUE) {
//            view.showOnBoardingFlow();
//        }

        QR_CODE_HELP_COUNT_PREF.setValue(QR_CODE_HELP_COUNT_PREF.getValue() + 1);
    }


    public void onTermsAndConditionAccepted() {
        hasAcceptedTermsConditionsPref.setValue(true);
        if (showOnBoardingFlowPref.getValue() == Boolean.TRUE) {
            view.showOnBoardingFlow();
        }
    }


}
