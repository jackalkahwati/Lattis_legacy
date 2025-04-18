package com.lattis.ellipse.presentation.ui.authentication.resetpassword;

import android.content.Intent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

public class ResetPasswordActivity extends BaseActivity<ResetPasswordPresenter> implements ResetPasswordView, ConfirmCodeForForgotPasswordFragment.Listener, ResetPasswordFragment.Listener{


    private String TAG = ResetPasswordActivity.class.getSimpleName();
    private static int POP_UP_REQUEST_CODE = 45341;

    @Inject
    ResetPasswordPresenter presenter;
    private final int REQUEST_PASSWORD_FAIL = 8301;
    private final int REQUEST_PASSWORD_SUCCESS = 8302;


    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected ResetPasswordPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_password_reset;
    }

    @OnClick(R.id.next_button)
    public void onNextButtonPressed(){

        getPresenter().sendCodeForForgotPassword();
    }
    @BindView(R.id.et_email)
    EditText emailEditText;


    @Override
    public void onConfirmationCodeSubmitted(String confirmationCode) {
        Fragment resetPasswordFragment = ResetPasswordFragment.newInstance(presenter.getEmail(), confirmationCode);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.reset_password_fragment, resetPasswordFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPasswordChangedSuccess() {
        PopUpActivity.launchForResult(this,REQUEST_PASSWORD_SUCCESS,getString(R.string.alert_success_title),
                getString(R.string.alert_forgetpassword_success_message),null,getString(R.string.ok));
    }

    @Override
    public void onPasswordChangedFailure() {
        PopUpActivity.launchForResult(this,REQUEST_PASSWORD_FAIL,getString(R.string.alert_forgetpassword_error_title),
                getString(R.string.alert_forgetpassword_error_message),null,getString(R.string.ok));
    }


    public void openConfirmCodeFragment(String email) {
        Fragment confirmCodeFragment = ConfirmCodeForForgotPasswordFragment.newInstance(email);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.reset_password_fragment, confirmCodeFragment);
        ft.commit();
    }


    @Override
    public void onSendForgotPasswordCodeSuccess(String email) {
        openConfirmCodeFragment(email);
    }




    @Override
    public void onSendForgotPasswordCodeFailure() {
        PopUpActivity.launchForResult(this,POP_UP_REQUEST_CODE,getString(R.string.label_forgot_password_error_title),getString(R.string.label_forgot_password_error_sub_title1),null,null);
    }

    @Override
    protected void configureSubscriptions() {
        super.configureSubscriptions();
        subscriptions.add(RxTextView.textChangeEvents(emailEditText)
                .subscribe(textViewTextChangeEvent -> {
                    getPresenter().setEmail(textViewTextChangeEvent.text().toString());
                }));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==POP_UP_REQUEST_CODE){
            return;
        }else if(requestCode == REQUEST_PASSWORD_SUCCESS){
            finish();
        }
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
