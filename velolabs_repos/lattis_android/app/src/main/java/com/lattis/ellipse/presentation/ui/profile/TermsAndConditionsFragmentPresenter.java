package com.lattis.ellipse.presentation.ui.profile;

import com.lattis.ellipse.domain.interactor.authentication.GetTermsConditionsUseCase;
import com.lattis.ellipse.domain.model.TermsAndConditions;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.fragment.FragmentPresenter;

import javax.inject.Inject;

/**
 * Created by raverat on 2/20/17.
 */

public class TermsAndConditionsFragmentPresenter extends FragmentPresenter<TermsAndConditionsFragmentView> {

    private GetTermsConditionsUseCase getTermsConditionsUseCase;

    @Inject
    public TermsAndConditionsFragmentPresenter(GetTermsConditionsUseCase getTermsConditionsUseCase) {
        this.getTermsConditionsUseCase = getTermsConditionsUseCase;
    }

    @Override
    protected void updateViewState() {
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
