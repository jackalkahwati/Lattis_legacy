package com.lattis.ellipse.presentation.ui.profile.changeMail;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;


public class ConfirmCodeForChangeEmailActivity extends BaseBackArrowActivity<ConfirmCodeForChangeEmailPresenter> implements ConfirmCodeForChangeEmailView {


    private final int REQUEST_CODE_CONFIRM_CODE_FAILURE = 921;

    @BindView(R.id.tv_verificationcontent)
    TextView tv_verificationcontent;



    @BindView(R.id.et_code)
    EditText et_confirm_code;


    @Inject
    ConfirmCodeForChangeEmailPresenter presenter;

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
        getPresenter().updateCodeEmail();
    }




    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }



    @NonNull
    @Override
    protected ConfirmCodeForChangeEmailPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected void configureViews() {
        super.configureViews();

    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_validate_email;
    }


    @Override
    public void onCodeEmailUpdated() {
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onCodeEmailUpdateFail() {
        PopUpActivity.launchForResult(this, REQUEST_CODE_CONFIRM_CODE_FAILURE, getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle), "", getString(R.string.ok));
    }

    @Override
    public void isPrivateAccount(boolean isPrivate) {
        if (isPrivate)
        {
            setToolbarHeader(getString(R.string.add_private_network_title));
        }else {
            setToolbarHeader(getString(R.string.change_mail));
        }
    }

    @Override
    public void showConfirmationCodeError(@StringRes int error) {
        tv_verificationcontent.setError(getString(error));
    }

    @Override
    public void hideConfirmationCodeError() {
        tv_verificationcontent.setError(null);
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}