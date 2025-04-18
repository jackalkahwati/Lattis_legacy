package com.lattis.ellipse.presentation.ui.authentication.signin;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.presentation.ui.authentication.resetpassword.ResetPasswordActivity;
import com.lattis.ellipse.presentation.ui.authentication.verification.fragment.EnterSecretCodeActivity;
import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

import static com.lattis.ellipse.presentation.ui.authentication.verification.fragment.EnterSecretCodeActivity.USER_ACCOUNT_TYPE_MAIN;

public class SignInActivity extends BaseActivity<SignInPresenter> implements SignInView {

    private static final int REQUEST_CODE_VERIFY_USER = 46;


    @Inject
    SignInPresenter presenter;

    @BindView(R.id.et_email)
    EditText emailEditText;
    @BindView(R.id.et_password)
    EditText passwordEditText;
    private int POP_UP_REQUEST_CODE = 4341;

    @OnClick(R.id.cv_forgot_password)
    public void forgotPasswordClicked() {
        startActivity(new Intent(this, ResetPasswordActivity.class));
    }


    public static void launchForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, SignInActivity.class), requestCode);
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected SignInPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_signin;
    }

    @Override
    protected void configureSubscriptions() {
        super.configureSubscriptions();
        subscriptions.add(RxTextView.textChangeEvents(emailEditText)
                .subscribe(textViewTextChangeEvent -> {
                    hidePhoneNumberError();
                    getPresenter().setEmail(textViewTextChangeEvent.text().toString());
                }));
        subscriptions.add(RxTextView.textChangeEvents(passwordEditText)
                .subscribe(textViewTextChangeEvent -> {
                    hidePasswordError();
                    getPresenter().setPassword(textViewTextChangeEvent.text().toString());
                }));
    }

    @OnClick(R.id.signin_button)
    public void onClickSignIn() {
        getPresenter().trySignIn();
    }


    @Override
    public void showPhoneNumberError(@StringRes int string) {
        //phoneNumberEditText.setError(getString(R.string.error_invalid_mobilenumber));
    }

    @Override
    public void hidePhoneNumberError() {
        //phoneNumberEditText.setError(null);
    }

    @Override
    public void showPasswordError(@StringRes int string) {
        passwordEditText.setError(getString(R.string.error_invalid_password));
    }

    @Override
    public void hidePasswordError() {
        passwordEditText.setError(null);
    }

    @Override
    public void onUserVerified(String userId) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onUserNotVerified(String userId) {
        EnterSecretCodeActivity.launchForResult(this, REQUEST_CODE_VERIFY_USER, userId, USER_ACCOUNT_TYPE_MAIN,getPresenter().getPassword());
    }

    @Override
    public void onAuthenticationFailed() {
        PopUpActivity.launchForResult(this, POP_UP_REQUEST_CODE, getString(R.string.action_loginfailed),
                getString(R.string.action_loginfailed_description), "", getString(R.string.ok));

    }


    @Override
    public void onUserNotExists() {
        PopUpActivity.launchForResult(this, POP_UP_REQUEST_CODE, getString(R.string.action_loginfailed),
                getString(R.string.action_loginfailed_no_user_exists_description), "", getString(R.string.ok));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VERIFY_USER) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    public void openCodeConfirmationScreen() {
        Intent resetPasswordIntent = new Intent(this, ResetPasswordActivity.class);
        //resetPasswordIntent.putExtra("PHONE_NUMBER",phoneNumberEditText.getText().toString());
        startActivity(resetPasswordIntent);
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
