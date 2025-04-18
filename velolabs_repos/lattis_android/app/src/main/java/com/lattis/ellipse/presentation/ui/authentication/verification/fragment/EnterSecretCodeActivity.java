package com.lattis.ellipse.presentation.ui.authentication.verification.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import android.text.Html;
import android.view.View;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.presentation.ui.authentication.signin.SignInActivity;
import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

public class EnterSecretCodeActivity extends BaseActivity<EnterSecretCodePresenter> implements EnterSecretCodeView {

    public final static String ARG_USER_ID = "ARG_USER_ID";
    public final static String ARG_USER_ACCOUNT_TYPE = "ARG_USER_ACCOUNT_TYPE";
    public final static String ARG_PASSWORD = "ARG_PASSWORD";
    public final static String USER_ACCOUNT_TYPE_PRIVATE = "private_account";
    public final static String USER_ACCOUNT_TYPE_MAIN = "main_account";


    public final static String TAG = EnterSecretCodeActivity.class.getSimpleName();

    @Inject EnterSecretCodePresenter presenter;
    @BindView(R.id.cv_tv_already_account)
    CustomTextView label_TexTView;

    @BindView(R.id.ct_incorrect_code)
    CustomTextView ct_incorrect_code;

    @BindView(R.id.et_code)
    EditText confirmationCodeInput;

    public static void launchForResult(Activity activity, int requestCode, String userId, String accout_type, String password) {
        Intent intent = new Intent(activity, EnterSecretCodeActivity.class);
        intent.putExtra(ARG_USER_ID,userId);
        intent.putExtra(ARG_USER_ACCOUNT_TYPE,accout_type);
        intent.putExtra(ARG_PASSWORD,password);
        activity.startActivityForResult(intent,requestCode);
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected EnterSecretCodePresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.fragment_enter_secret_code;
    }

    @Override
    protected void configureViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            label_TexTView.setText(Html.fromHtml(getString(R.string.onboarding_log_in_question
            ), Html.FROM_HTML_MODE_LEGACY));
        } else {
            label_TexTView.setText(Html.fromHtml(getString(R.string.onboarding_log_in_question
            )));
        }
    }
    @OnClick(R.id.cv_tv_already_account)
    public void onAleardyAccountButtonClicked()
    {
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    @Override
    protected void configureSubscriptions() {
        subscriptions.add(RxTextView.textChangeEvents(confirmationCodeInput)
                .subscribe(textViewTextChangeEvent -> getPresenter().setConfirmationCode(textViewTextChangeEvent.text().toString())));
    }

    @OnClick(R.id.cb_verify_code)
    public void onSubmitViewClicked(final View v) {
        ct_incorrect_code.setVisibility(View.INVISIBLE);
        getPresenter().submitConfirmationCode();
    }

    @OnClick(R.id.tv_resend)
    public void onResendViewClicked(final View v) {
        getPresenter().sendConfirmationCode();
    }

    @Override
    public void showConfirmationCodeError(@StringRes int error) {
        confirmationCodeInput.setError(getString(error));
        ct_incorrect_code.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideConfirmationCodeError() {
        confirmationCodeInput.setError(null);
        ct_incorrect_code.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSecretCodeFail() {
        ct_incorrect_code.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSecretCodeConfirmed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onSecretCodeResent() {
        //TODO
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }
}
