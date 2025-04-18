package com.lattis.ellipse.presentation.ui.payment;

import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.presentation.ui.base.BaseView;

import java.util.List;


public interface PaymentInfoView extends BaseView {
    void onGetCardSuccess(List<Card> cards);
    void onGetCardFailure();
    void onUpdateCardFailure();
    void showLoading(String message);
    void hideProgressLoading();
    void showCardListView(List<Card> cards);
    void showNoCardView();

}
