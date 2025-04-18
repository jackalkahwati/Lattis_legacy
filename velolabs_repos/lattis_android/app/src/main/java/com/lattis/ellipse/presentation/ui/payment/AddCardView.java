package com.lattis.ellipse.presentation.ui.payment;

import androidx.annotation.NonNull;

import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.presentation.ui.base.BaseView;


public interface AddCardView extends BaseView {
    void onCardAddSuccess();
    void onCardAddFailure();
    void onCardInvalid();
    void onCardAlreadyExists();
    void setCardDetails(Card card);
    void onDeleteCardSuccess();
    void onDeleteCardFailure();
    void showError();
    void confirmSetupIntent(@NonNull String paymentMethodId);
}
