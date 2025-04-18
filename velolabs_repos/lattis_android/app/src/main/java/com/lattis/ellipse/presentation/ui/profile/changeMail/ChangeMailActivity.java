package com.lattis.ellipse.presentation.ui.profile.changeMail;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.view.CustomEditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

import static com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailPresenter.ARG_USER_ACCOUNT_TYPE;
import static com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailPresenter.ARG_USER_ID;
import static com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailPresenter.USER_ACCOUNT_TYPE_PRIVATE;

/**
 * Created by lattis on 02/05/17.
 */

public class ChangeMailActivity extends BaseBackArrowActivity<ChangeMailPresenter> implements ChangeMailView {

    private final int REQUEST_CODE_CONFIRM_CODE_FAILURE = 9022;
    private final int REQUEST_CODE_NO_NEW_FLEET = 9023;
    private final int REQUEST_CODE_CONFIRMATION_FOR_ADD_PRIVATE_NETWORK = 9024;
    private String ACCOUNT_TYPE = null;
    private String userID = null;


    @BindView(R.id.et_mail)
    CustomEditText emailInput;


    @Inject
    ChangeMailPresenter changeMailPresenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @OnClick(R.id.button_send_code)
    public void sendCodeButtonclicked() {
        changeMailPresenter.updateEmail();
    }

    @Override
    protected void configureSubscriptions() {
        super.configureSubscriptions();
        subscriptions.add(RxTextView.textChangeEvents(emailInput)
                .subscribe(textViewTextChangeEvent -> getPresenter().setEmail(textViewTextChangeEvent.text().toString())));

    }

    @Override
    protected void configureViews() {
        super.configureViews();
    }

    @NonNull
    @Override
    protected ChangeMailPresenter getPresenter() {
        return changeMailPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_change_mail;
    }

    @Override
    public void onCodeSentSuccess(String email) {
        startActivityForResult(new Intent(this, ConfirmCodeForChangeEmailActivity.class)
                .putExtra("EMAIL", email)
                .putExtra(ARG_USER_ACCOUNT_TYPE, ACCOUNT_TYPE)
                .putExtra(ARG_USER_ID,userID),REQUEST_CODE_CONFIRMATION_FOR_ADD_PRIVATE_NETWORK);
    }

    @Override
    public void onCodeSentFail() {
        PopUpActivity.launchForResult(this, REQUEST_CODE_CONFIRM_CODE_FAILURE, getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle), "", getString(R.string.ok));
    }

    @Override
    public void onNoNewFleetWithCurrentFleetPresent() {
        PopUpActivity.launchForResult(this, REQUEST_CODE_NO_NEW_FLEET, getString(R.string.private_network),
                getString(R.string.private_network_content_has_fleets), "", getString(R.string.ok));
    }

    @Override
    public void onNoNewFleetWithNoCurrentFleetPresent() {
        PopUpActivity.launchForResult(this, REQUEST_CODE_NO_NEW_FLEET, getString(R.string.private_network),
                getString(R.string.private_network_content), "", getString(R.string.ok));
    }

    @Override
    public void onAccountType(String type) {
        this.ACCOUNT_TYPE = type;
        if (ACCOUNT_TYPE.equals(USER_ACCOUNT_TYPE_PRIVATE)) {
            setToolbarHeader(getString(R.string.add_private_network_title));
        } else {
            setToolbarHeader(getString(R.string.change_mail));
        }

    }

    @Override
    public void setUserId(String userId) {
        this.userID = userId;
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_NO_NEW_FLEET){
            finish();
        }else if(requestCode == REQUEST_CODE_CONFIRMATION_FOR_ADD_PRIVATE_NETWORK && resultCode == RESULT_OK){
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
