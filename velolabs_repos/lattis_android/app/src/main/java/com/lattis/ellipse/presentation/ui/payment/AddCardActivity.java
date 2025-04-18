package com.lattis.ellipse.presentation.ui.payment;


import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomButton;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.SetupIntentResult;
import com.stripe.android.model.Card;
import com.stripe.android.model.ConfirmSetupIntentParams;
import com.stripe.android.model.SetupIntent;
import com.stripe.android.view.CardMultilineWidget;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

import static com.lattis.ellipse.presentation.ui.payment.PaymentInfoActivity.PAYMENT_CARD_MODIFIED;

public class AddCardActivity extends BaseBackArrowActivity<AddCardPresenter> implements AddCardView {

    private final String TAG = AddCardActivity.class.getName();

    @Inject
    AddCardPresenter addCardPresenter;
    @BindView(R.id.card_multiline_widget)
    CardMultilineWidget mCardMultilineWidget;

    @BindView(R.id.btn_save)
    CustomButton save_Button;
    @BindView(R.id.tv_delete_card)
    CustomTextView deleteCardView;
    private com.lattis.ellipse.domain.model.Card card;

    @BindView((R.id.rl_loading_operation))
    View add_card_loading_operation_view;

    @BindView(R.id.label_operation_name)
    CustomTextView loading_operation_name;

    private int REQUEST_FAILURE_CARD_OPERATION = 4859;
    private int REQUEST_CARD_DELETE_OPERATION = 4860;
    private int REQUEST_GENERAL_ERROR = 4861;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @OnClick(R.id.tv_delete_card)
    void deleteCardDetails() {
        showCardOperationPopUp(getString(R.string.card_delete_confirmation_title),getString(R.string.card_delete_confirmation_subtitle),getString(R.string.card_delete_confirmation_actionbtn),REQUEST_CARD_DELETE_OPERATION);
    }

    @OnClick(R.id.btn_save)
    void saveCardDetails() {
        Card card = mCardMultilineWidget.getCard();
        if(card!=null){
            if (card.validateCard()) {
                showOperationLoading(getString(R.string.add_card_loading_name));
                    getPresenter().createPaymentMethod(card);
            }else{
                onCardInvalid();
            }
        }else{
            onCardInvalid();
        }
    }

    @NonNull
    @Override
    protected AddCardPresenter getPresenter() {
        return addCardPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_add_card;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCardMultilineWidget.setCardHint("");
        getPresenter().getUserProfile();
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setToolbarHeader(getString(R.string.add_card));
    }

    @Override
    public void onCardAddSuccess() {
        hideOperationLoading();
        Log.e(TAG, "onCardAddSuccess");
        Intent intent = new Intent();
        intent.putExtra(PAYMENT_CARD_MODIFIED, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onCardAddFailure() {
        hideOperationLoading();
        Log.e(TAG, "onCardAddFailure");
        showCardOperationPopUp(getString(R.string.card_operation_error_title),getString(R.string.card_adding_error_subtitle),getString(R.string.card_operation_error_action_btn),REQUEST_FAILURE_CARD_OPERATION);
    }


    @Override
    public void onCardInvalid(){
        hideOperationLoading();
        showCardOperationPopUp(getString(R.string.card_operation_error_title),getString(R.string.card_details_error_subtitle),getString(R.string.card_operation_error_action_btn),REQUEST_FAILURE_CARD_OPERATION);
    }
    @Override
    public void onCardAlreadyExists() {
        hideOperationLoading();
        showCardOperationPopUp(getString(R.string.card_already_exist_title),getString(R.string.card_already_exist_subtitle),getString(R.string.card_operation_error_action_btn),REQUEST_FAILURE_CARD_OPERATION);
    }

    @Override
    public void setCardDetails(com.lattis.ellipse.domain.model.Card card) {
        this.card = card;
        mCardMultilineWidget.setCardNumber(card.getCc_no());
        mCardMultilineWidget.setExpiryDate(card.getExp_month(), card.getExp_year());
        mCardMultilineWidget.setCvcCode("");
        mCardMultilineWidget.setEnabled(false);
        save_Button.setVisibility(View.GONE);
        deleteCardView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDeleteCardSuccess() {
        hideOperationLoading();
        Intent intent = new Intent();
        intent.putExtra(PAYMENT_CARD_MODIFIED, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDeleteCardFailure() {
        hideOperationLoading();
        showCardOperationPopUp(getString(R.string.card_operation_error_title),getString(R.string.card_deleting_error_subtitle),getString(R.string.card_operation_error_action_btn),REQUEST_FAILURE_CARD_OPERATION);
    }


    void deleteCard(){
        showOperationLoading(getString(R.string.delete_card_loading_name));
        getPresenter().deleteCard(card.getId());
    }

    private void showCardOperationPopUp(String title, String subTitle, String actionBtn, int requestCode){
        PopUpActivity.launchForResult(this,requestCode, title,subTitle, null,actionBtn);
    }


    private void showOperationLoading(String operationName) {
        add_card_loading_operation_view.setVisibility(View.VISIBLE);
        loading_operation_name.setText(operationName);
    }

    private void hideOperationLoading() {
        add_card_loading_operation_view.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CARD_DELETE_OPERATION && resultCode == RESULT_OK){
            deleteCard();
        }else if(requestCode == REQUEST_GENERAL_ERROR){
                finish();
        }else{
            getPresenter().getStripe().onSetupResult(requestCode, data,
                    new ApiResultCallback<SetupIntentResult>() {
                        @Override
                        public void onSuccess(@NonNull SetupIntentResult result) {
                            // If confirmation and authentication succeeded,
                            // the SetupIntent will have user actions resolved;
                            // otherwise, handle the failure as appropriate
                            // (e.g. the customer may need to choose a new payment
                            // method)
                            final SetupIntent setupIntent = result.getIntent();
                            final SetupIntent.Status status =
                                    setupIntent.getStatus();
                            if (status == SetupIntent.Status.Succeeded) {
                                // show success UI
                                getPresenter().addCard(setupIntent);
                            }else{
                                hideOperationLoading();
                            }
                        }

                        @Override
                        public void onError(@NonNull Exception e) {
                            // handle error
                            hideOperationLoading();
                            showError();
                        }



                    });
        }

    }

    @Override
    public void confirmSetupIntent(@NonNull String paymentMethodId) {
        getPresenter().getStripe().confirmSetupIntent(
                this,
                ConfirmSetupIntentParams.create(paymentMethodId, getPresenter().getClient_secret())
        );
    }



    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }


    @Override
    public void showError(){
        PopUpActivity.launchForResult(this,REQUEST_GENERAL_ERROR ,getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle),null,getString(R.string.ok));
    }

}
