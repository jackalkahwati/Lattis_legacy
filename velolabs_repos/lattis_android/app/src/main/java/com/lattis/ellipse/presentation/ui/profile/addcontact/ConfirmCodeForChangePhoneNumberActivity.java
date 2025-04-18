package com.lattis.ellipse.presentation.ui.profile.addcontact;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;


public class ConfirmCodeForChangePhoneNumberActivity extends BaseBackArrowActivity<ConfirmCodeForChangePhoneNumberPresenter> implements ConfirmCodeForChangePhoneNumberView {


    @BindView(R.id.tv_verificationcontent)
    TextView tv_verificationcontent;



    @BindView(R.id.et_code)
    EditText et_confirm_code;


    @Inject
    ConfirmCodeForChangePhoneNumberPresenter presenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    protected void configureSubscriptions() {
        super.configureSubscriptions();
        subscriptions.add(RxTextView.textChangeEvents(et_confirm_code)
                .subscribe(textViewTextChangeEvent -> {
                    getPresenter().setConfirmCode(textViewTextChangeEvent.text().toString());
                }));
    }

    @OnClick(R.id.button_submit)
    public void submitCode(View v){
        getPresenter().updateCodePhoneNumber();
    }

    @OnClick(R.id.tv_resend)
    public void reSendCode(View v){
        getPresenter().reSendCode();
    }



    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }



    @NonNull
    @Override
    protected ConfirmCodeForChangePhoneNumberPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.add_phone_number));
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_validate_mobile_number;
    }


    @Override
    public void onCodePhoneNumberUpdated() {
        finish();
    }

    @Override
    public void updateVerificationInfoText(String phoneNumber){
        String text = getResources().getString(R.string.label_verification_code_text1) +" "+ phoneNumber+" "+getResources().getString(R.string.label_verification_code_text2);

        tv_verificationcontent.setText(text);
    }

    @Override
    public void onSendSuccessful() {

    }
    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}