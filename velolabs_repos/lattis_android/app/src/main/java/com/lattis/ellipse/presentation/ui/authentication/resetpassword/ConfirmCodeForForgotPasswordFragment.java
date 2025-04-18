package com.lattis.ellipse.presentation.ui.authentication.resetpassword;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;


public class ConfirmCodeForForgotPasswordFragment extends BaseFragment<ConfirmCodeForForgotPasswordPresenter> implements ConfirmCodeForForgotPasswordView{

    public final static String ARG_EMAIL = "email";
    public  static String CONFIRMATION_CODE = null;


    public interface Listener {
        public void onConfirmationCodeSubmitted(String confirmationCode);
    }

    @Inject
    ConfirmCodeForForgotPasswordPresenter presenter;

    private ConfirmCodeForForgotPasswordFragment.Listener listener;

    @BindView(R.id.et_code)
    EditText et_code;

    @NonNull
    @Override
    public String getFragmentTag() {
        return ConfirmCodeForForgotPasswordFragment.class.getSimpleName();
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected ConfirmCodeForForgotPasswordPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.confirm_code_forgot_password;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConfirmCodeForForgotPasswordFragment.Listener) {
            listener = (ConfirmCodeForForgotPasswordFragment.Listener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @OnClick(R.id.cb_verify_code)
    public void confirmCode(){
        listener.onConfirmationCodeSubmitted(presenter.getCode());
    }

    @OnClick(R.id.tv_resend)
    public void reSendCode(){
        presenter.sendConfirmationCode();
    }

    @Override
    protected void configureViews() {
    }

    @Override
    protected void configureSubscriptions() {
        super.configureSubscriptions();
        subscriptions.add(RxTextView.textChangeEvents(et_code)
                .subscribe(textViewTextChangeEvent -> {
                    getPresenter().setCode(textViewTextChangeEvent.text().toString()); }));

    }

    @Override
    public void onSecretCodeResent() {

    }

    @Override
    public void onSecretCodeResentFailed() {

    }

    @Override
    public void onSecretCodeSent() {

    }

    public static ConfirmCodeForForgotPasswordFragment newInstance(String email) {
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        ConfirmCodeForForgotPasswordFragment fragment = new ConfirmCodeForForgotPasswordFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
