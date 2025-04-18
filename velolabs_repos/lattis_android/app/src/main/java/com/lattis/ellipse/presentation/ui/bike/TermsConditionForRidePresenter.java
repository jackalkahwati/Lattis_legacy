package com.lattis.ellipse.presentation.ui.bike;

import com.lattis.ellipse.domain.interactor.authentication.GetTermsConditionsUseCase;
import com.lattis.ellipse.domain.model.TermsAndConditions;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;


public class TermsConditionForRidePresenter extends ActivityPresenter<TermsConditionForRideView>{
    private GetTermsConditionsUseCase getTermsConditionsUseCase;


    @Inject
    TermsConditionForRidePresenter (GetTermsConditionsUseCase  getTermsConditionsUseCase)
    {
        this.getTermsConditionsUseCase = getTermsConditionsUseCase;
            }


    @Override
    protected void updateViewState() {
        super.updateViewState();
        subscriptions.add(getTermsConditionsUseCase.execute(new RxObserver<TermsAndConditions>() {
            @Override
            public void onNext(TermsAndConditions termsAndConditions) {
                view.onTermsAndConditionsLoaded(termsAndConditions);
            }
            @Override
            public void onError(Throwable e) {
                view.onTermsAndConditionsFailedLoading();
            }
        }));

    }
}
