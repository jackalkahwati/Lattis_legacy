package com.lattis.ellipse.presentation.ui.payment;


import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.LinearLayout;

import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

public class PaymentInfoActivity extends BaseBackArrowActivity<PaymentInfoPresenter> implements PaymentInfoView,
        UserCardListListener {


    public static final String PAYMENT_CARD_MODIFIED = "PAYMENT_CARD_MODIFIED";

    private final int REQUEST_CODE_ADD_CARD_ACTIVITY = 7940;
    private final int REQUEST_CARD_UPDATE_FAILURE = 3409;

    @BindView(R.id.rv_card_list)
    RecyclerView rv_UserCardLit;
    @BindView(R.id.ll_no_card_view)
    LinearLayout noCardView;
    @BindView((R.id.rl_loading_operation))
    View payment_info_loading_operation_view;
    @BindView(R.id.label_operation_name)
    CustomTextView loading_operation_name;
    List<Card> cardList;
    boolean isPaymentCardModified=false;

    @OnClick(R.id.tv_add_card)
    public void addCreditCard() {
        startActivityForResult(new Intent(PaymentInfoActivity.this, AddCardActivity.class), REQUEST_CODE_ADD_CARD_ACTIVITY);
    }

    @Inject
    PaymentInfoPresenter paymentInfoPresenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected PaymentInfoPresenter getPresenter() {
        return paymentInfoPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_payment_info;
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.payment));
        showLoading(getString(R.string.loading));
        getPresenter().getCards();
    }


    @Override
    public void onGetCardSuccess(List<Card> cards) {
        if (cards == null || cards.size() == 0) {
            showNoCardView();
        } else {
            this.cardList = cards;
            showCardListView(cards);
        }
    }


    @Override
    public void onGetCardFailure() {
        rv_UserCardLit.setVisibility(View.GONE);
        noCardView.setVisibility(View.VISIBLE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ADD_CARD_ACTIVITY && resultCode == RESULT_OK) {
            if(data!=null) {
                if (data.hasExtra(PAYMENT_CARD_MODIFIED)) {
                    if (data.getExtras().getBoolean(PAYMENT_CARD_MODIFIED)) {
                        isPaymentCardModified=true;
                    }
                }
            }
            getPresenter().getCards();
        } else if (requestCode == REQUEST_CARD_UPDATE_FAILURE) {
            isPaymentCardModified=false;
            getPresenter().getCards();
        }
    }

    @Override
    public void showLoading(String message) {
        payment_info_loading_operation_view.setVisibility(View.VISIBLE);
        loading_operation_name.setText(message);
    }

    @Override
    public void hideProgressLoading() {
        payment_info_loading_operation_view.setVisibility(View.GONE);

    }

    @Override
    public void showCardListView(List<Card> cards) {
        rv_UserCardLit.setVisibility(View.VISIBLE);
        noCardView.setVisibility(View.GONE);
        rv_UserCardLit.setLayoutManager(new LinearLayoutManager(this));
        rv_UserCardLit.setAdapter(new UserCardListAdapter(this, cards, true, this));
    }

    @Override
    public void showNoCardView() {
        rv_UserCardLit.setVisibility(View.GONE);
        noCardView.setVisibility(View.VISIBLE);

    }


    @Override
    public void onclickCheckBox(int position) {
        showLoading(getString(R.string.card_primary_loading_name));
        getPresenter().updateCard(cardList.get(position).getId());
    }

    @Override
    public void onUpdateCardFailure() {
        PopUpActivity.launchForResult(this, REQUEST_CARD_UPDATE_FAILURE, getString(R.string.card_operation_error_title),
                getString(R.string.card_update_error_subtitle),
                null,
                getString(R.string.card_operation_error_action_btn));
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }


}
