package com.lattis.ellipse.presentation.ui.payment;


import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.domain.interactor.card.GetCardUseCase;
import com.lattis.ellipse.domain.interactor.card.UpdateCardUseCase;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class PaymentInfoPresenter extends ActivityPresenter<PaymentInfoView> {
    private GetCardUseCase getCardUseCase;
    private UpdateCardUseCase updateCardUseCase;

    @Inject
    PaymentInfoPresenter(GetCardUseCase getCardUseCase, UpdateCardUseCase updateCardUseCase) {
        this.getCardUseCase = getCardUseCase;
        this.updateCardUseCase = updateCardUseCase;
    }

    public void getCards() {

        subscriptions.add(getCardUseCase
                .execute(new RxObserver<List<Card>>() {
                    @Override
                    public void onNext(List<Card> cards) {
                        view.onGetCardSuccess(cards);
                        view.hideProgressLoading();

                    }


                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onGetCardFailure();
                        view.hideProgressLoading();
                    }
                }));
    }

    @DebugLog
    public void updateCard(int id) {
        subscriptions.add(updateCardUseCase
                .setCardId(id)
                .execute(new RxObserver<BasicResponse>() {
                    @Override
                    public void onNext(BasicResponse o) {
                        super.onNext(o);
                        getCards();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onUpdateCardFailure();
                    }
                }));
    }

    @Override
    protected void updateViewState() {
        super.updateViewState();
    }
}
