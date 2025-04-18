package com.lattis.ellipse.presentation.ui.authentication.resetpassword;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import android.view.View;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

public class ResetPasswordFragment extends BaseFragment<ResetPasswordFragmentPresenter> implements
        ResetPasswordFragmentView {

    public final static String TAG = ResetPasswordFragment.class.getSimpleName();

    public final static String ARG_EMAIL = "email";
    public final static String ARG_CONFIRMATION_CODE = "confirmation_code";


    public interface Listener {
        public void onPasswordChangedSuccess();
        public void onPasswordChangedFailure();
    }

    @Inject
    ResetPasswordFragmentPresenter presenter;

    @BindView(R.id.et_password1)
    EditText passwordInput;

    @BindView(R.id.et_password2)
    EditText confirmPasswordInput;


    private ResetPasswordFragment.Listener listener;


    @NonNull
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected ResetPasswordFragmentPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.fragment_password_reset;
    }

    @Override
    protected void configureViews() {
    }

    @Override
    protected void configureSubscriptions() {
        super.configureSubscriptions();
        subscriptions.add(RxTextView.textChangeEvents(passwordInput)
                .subscribe(textViewTextChangeEvent -> {
                    getPresenter().setPassword(textViewTextChangeEvent.text().toString());
                }));

        subscriptions.add(RxTextView.textChangeEvents(confirmPasswordInput)
                .subscribe(textViewTextChangeEvent -> {
                    getPresenter().setConfirmPassword(textViewTextChangeEvent.text().toString());
                }));

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ResetPasswordFragment.Listener) {
            listener = (ResetPasswordFragment.Listener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @OnClick(R.id.reset_password_button)
    public void onSavePasswordViewClicked(final View view) {
        getPresenter().resetPassword();
    }

    @Override
    public void showPasswordError(@StringRes int error) {
        //passwordInputLayout.setPasswordVisibilityToggleEnabled(false);
        passwordInput.setError(getString(error));
        confirmPasswordInput.setError(getString(error));
    }

    @Override
    public void hidePasswordError() {
        passwordInput.setError(null);
        confirmPasswordInput.setError(null);
        //passwordInputLayout.setPasswordVisibilityToggleEnabled(true);
    }

    @Override
    public void showFailToSaveDialog() {
        if (listener != null) {
            listener.onPasswordChangedFailure();
        }
    }

    @Override
    public void onPasswordChanged() {
        if (listener != null) {
            listener.onPasswordChangedSuccess();
        }
    }

    public static ResetPasswordFragment newInstance(String phoneNumber, String confirmationCode) {
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, phoneNumber);
        args.putString(ARG_CONFIRMATION_CODE, confirmationCode);

        ResetPasswordFragment f = new ResetPasswordFragment();
        f.setArguments(args);
        return f;
    }





}
