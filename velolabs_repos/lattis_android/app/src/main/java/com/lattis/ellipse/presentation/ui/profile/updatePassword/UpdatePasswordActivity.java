package com.lattis.ellipse.presentation.ui.profile.updatePassword;

import android.graphics.Color;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomButton;
import com.lattis.ellipse.presentation.view.CustomEditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.lattis.ellipse.R;

/**
 * Created by lattis on 01/05/17.
 */

public class UpdatePasswordActivity extends BaseBackArrowActivity<UpdatePasswordPresenter> implements
        UpdatePasswordView, TextWatcher {

    private final int REQUEST_CODE_PASSWORD_SUCCESS = 9024;

    @BindView(R.id.et_current_password)
    TextInputEditText currentPasswordInput;
    @BindView(R.id.et_new_password)
    TextInputEditText newPasswordInput;
    @BindView(R.id.et_repeat_password)
    TextInputEditText repeatPasswordInput;
    @BindView(R.id.button_update_pwd)
    CustomButton changePasswordButton;

    @OnClick(R.id.button_update_pwd)
    public void updatePasswordClicked() {

        updatePasswordPresenter.upatePassword();
    }

    @Inject
    UpdatePasswordPresenter updatePasswordPresenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.change_password));
        changePasswordButton.setEnabled(true);
        repeatPasswordInput.addTextChangedListener(this);

    }

    @NonNull
    @Override
    protected UpdatePasswordPresenter getPresenter() {
        return updatePasswordPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_update_password;
    }

    @Override
    protected void configureSubscriptions() {
        super.configureSubscriptions();
        subscriptions.add(RxTextView.textChangeEvents(currentPasswordInput)
                .subscribe(textViewTextChangeEvent -> getPresenter().setPassword(textViewTextChangeEvent.text().toString())));
        subscriptions.add(RxTextView.textChangeEvents(newPasswordInput)
                .subscribe(textViewTextChangeEvent -> getPresenter().setNewPassword(textViewTextChangeEvent.text().toString())));
        subscriptions.add(RxTextView.textChangeEvents(repeatPasswordInput)
                .subscribe(textViewTextChangeEvent -> getPresenter().setRepeatPassword(textViewTextChangeEvent.text().toString())));


    }

    @Override
    public void showInCorrectPassword() {
        Crouton.makeText(this, getString(R.string.incorrect_password), Style.ALERT, R.id.crouton).show();
    }

    @Override
    public void passwordUpdateSuccess() {
        PopUpActivity.launchForResult(this, REQUEST_CODE_PASSWORD_SUCCESS, getString(R.string.success),
                getString(R.string.password_update_success), "", getString(R.string.ok));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        CharSequence cs = newPasswordInput.getText().toString().trim();
        if (cs.equals(s.toString())) {
            changePasswordButton.setEnabled(true);
            changePasswordButton.setBackgroundColor(Color.parseColor("#00AAD1"));
        }
        else{
            changePasswordButton.setEnabled(false);
            changePasswordButton.setBackgroundColor(Color.parseColor("#DEE1E6"));
        }

    }
    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
