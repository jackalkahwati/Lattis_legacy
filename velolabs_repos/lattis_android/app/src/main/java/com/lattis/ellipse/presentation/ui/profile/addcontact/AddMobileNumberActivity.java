package com.lattis.ellipse.presentation.ui.profile.addcontact;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import android.telephony.PhoneNumberFormattingTextWatcher;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomEditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

/**
 * Created by lattis on 29/04/17.
 */

public class AddMobileNumberActivity extends BaseBackArrowActivity<AddMobileNumberPersenter> implements AddMobileNumberView {

    private final int REQUEST_CODE_PHONE_NUMBER_FAILURE = 9302;
  @BindView(R.id.et_mobilenumber)
    CustomEditText phoneNumberInput;

    @Inject
    AddMobileNumberPersenter addMobileNumberPersenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.add_phone_number));
        phoneNumberInput.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

    }
    @OnClick(R.id.button_add_number)
    public void onAddNumberclicked()
    {
        addMobileNumberPersenter.sendCodeToPhoneNumber();
    }

    @Override
    protected void configureSubscriptions() {
        super.configureSubscriptions();
        subscriptions.add(RxTextView.textChangeEvents(phoneNumberInput)
                .subscribe(textViewTextChangeEvent -> {
                    getPresenter().setPhoneNumber(textViewTextChangeEvent.text().toString());
                }));

    }

    @NonNull
    @Override
    protected AddMobileNumberPersenter getPresenter() {
        return addMobileNumberPersenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_add_mobile_number;
    }

    @Override
    public void showPhoneNumberCountryPrefix(int phoneNumberCountryPrefix) {
        phoneNumberInput.setText(String.format("+%d ", phoneNumberCountryPrefix));

    }

    @Override
    public void onPhoneNumberUpdated() {
        Intent intent = new Intent(this, ConfirmCodeForChangePhoneNumberActivity.class);
        intent.putExtra("PHONE_NUMBER",phoneNumberInput.getText().toString());
        startActivity(intent);
        finish();

    }

    @Override
    public void showPhoneNumberError(@StringRes int error) {
        phoneNumberInput.setError(getString(error));

    }

    @Override
    public void hidePhoneNumberError() {
        phoneNumberInput.setError(null);
    }

    @Override
    public void onPhoneNumberUpdateFail() {
        PopUpActivity.launchForResult(this, REQUEST_CODE_PHONE_NUMBER_FAILURE, getString(R.string.alert_error_title),
                getString(R.string.alert_phone_number_update_error_message), "", getString(R.string.ok));
    }
    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
