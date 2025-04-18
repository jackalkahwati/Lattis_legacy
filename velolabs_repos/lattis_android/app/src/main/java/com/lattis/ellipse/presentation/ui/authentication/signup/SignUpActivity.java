package com.lattis.ellipse.presentation.ui.authentication.signup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import android.text.Html;
import android.text.InputType;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.presentation.ui.authentication.verification.fragment.EnterSecretCodeActivity;
import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

public class SignUpActivity extends BaseActivity<SignUpPresenter> implements SignUpView {

    private final static int VERIFICATION_CODE_REQUEST = 1001;

    @Inject
    SignUpPresenter presenter;

    @BindView(R.id.et_firstname)
    EditText firstNameEditText;
    @BindView(R.id.et_lastname)
    EditText lastNameEditText;
    @BindView(R.id.et_email)
    EditText emailEditText;
    @BindView(R.id.et_pwd)
    EditText passwordEditText;
    @BindView(R.id.cv_tv_already_account)
    CustomTextView label_TexTView;

    @BindView(R.id.tv_show_hide)
    CustomTextView label_hide_show;
    private Typeface typeface_temp;
    private int POP_UP_REQUEST_CODE = 4341;


    public static void launchForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, SignUpActivity.class), requestCode);
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected SignUpPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_signup;
    }

    @OnClick(R.id.tv_show_hide)
    public void onHideShowClicked() {
        typeface_temp = label_hide_show.getTypeface();
        if (label_hide_show.getTag().equals(getString(R.string.label_show))) {
            label_hide_show.setText(getString(R.string.label_hide));
            label_hide_show.setTag(getString(R.string.label_hide));
            passwordEditText.setTransformationMethod(null);

        } else {
            label_hide_show.setText(getString(R.string.label_show));
            label_hide_show.setTag(getString(R.string.label_show));
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordEditText.setTypeface(Typeface.DEFAULT);

        }
        passwordEditText.setTypeface(typeface_temp);

    }

    @OnClick(R.id.cv_tv_already_account)
    public void onAleardyAccountButtonClicked() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            label_TexTView.setText(Html.fromHtml(getString(R.string.onboarding_log_in_question
            ), Html.FROM_HTML_MODE_LEGACY));
        } else {
            label_TexTView.setText(Html.fromHtml(getString(R.string.onboarding_log_in_question
            )));
        }
    }

    @Override
    protected void configureSubscriptions() {
        super.configureSubscriptions();


        subscriptions.add(RxTextView.textChangeEvents(emailEditText)
                .subscribe(textViewTextChangeEvent -> {
                    hideEmailError();
                    getPresenter().setEmail(textViewTextChangeEvent.text().toString());
                }));
        subscriptions.add(RxTextView.textChangeEvents(firstNameEditText)
                .subscribe(textViewTextChangeEvent -> {
                    hidePhoneNumberError();
                    getPresenter().setFirstName(textViewTextChangeEvent.text().toString());
                }));
        subscriptions.add(RxTextView.textChangeEvents(passwordEditText)
                .subscribe(textViewTextChangeEvent -> {
                    hidePasswordError();
                    getPresenter().setPassword(textViewTextChangeEvent.text().toString());
                }));
        subscriptions.add(RxTextView.textChangeEvents(lastNameEditText)
                .subscribe(textViewTextChangeEvent -> {
                    getPresenter().setLastName(textViewTextChangeEvent.text().toString());
                }));
    }

    @OnClick(R.id.signup_button)
    public void onClickSignUp() {
        getPresenter().trySignUp();
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
    public void showEmailError(@StringRes int string) {
        emailEditText.setError(getString(R.string.error_invalid_email));
    }

    @Override
    public void hideEmailError() {
        emailEditText.setError(null);
    }

    @Override
    public void showHomePage(String userId) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void showRequestCode(String userId) {
        EnterSecretCodeActivity.launchForResult(this, VERIFICATION_CODE_REQUEST, userId, EnterSecretCodeActivity.USER_ACCOUNT_TYPE_MAIN,getPresenter().getPassword());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VERIFICATION_CODE_REQUEST) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showFailureScreen() {
        PopUpActivity.launchForResult(this, POP_UP_REQUEST_CODE, getString(R.string.label_problem),
                getString(R.string.label_enter_valid_email_address), getString(R.string.password_must_be)
                , getString(R.string.please_try_again));

    }

    @Override
    public void showDuplicateError() {

        PopUpActivity.launchForResult(this, POP_UP_REQUEST_CODE, getString(R.string.alert_error_title),
                getString(R.string.alert_signup_duplicate_error_message), null
                , getString(R.string.ok_label));
    }

    @Override
    public void onRegistrationFailed() {
        showFailureScreen();
    }


    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
