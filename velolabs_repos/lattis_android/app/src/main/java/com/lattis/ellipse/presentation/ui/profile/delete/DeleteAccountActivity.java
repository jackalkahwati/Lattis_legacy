package com.lattis.ellipse.presentation.ui.profile.delete;

import android.content.Intent;
import androidx.annotation.NonNull;
import android.view.View;

import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity2;

import javax.inject.Inject;

import butterknife.OnClick;
import io.lattis.ellipse.R;

public class DeleteAccountActivity extends BaseBackArrowActivity<DeleteAccountPresenter> implements DeleteAccountView {

    private final int REQUEST_CODE_FOR_DELETE_CONFIRMATION = 9024;
    private final int REQUEST_CODE_FOR_DELETE_FAIL = 9014;

    @Inject DeleteAccountPresenter presenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected DeleteAccountPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_delete_account;
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.delete_account));
        hideLoading();
    }

    @OnClick(R.id.btn_delete_account)
    public void onDeleteViewClicked(final View view) {
        //

        PopUpActivity2.launchForResult(this, REQUEST_CODE_FOR_DELETE_CONFIRMATION, getString(R.string.delete_account_title),
                getString(R.string.delete_account_text), null, getString(R.string.delete_account_submit));
    }

    @Override
    public void onAccountDeleted() {
        getPresenter().logOut();
    }

    @Override
    public void onAccountDeletionFailed() {

        PopUpActivity2.launchForResult(this, REQUEST_CODE_FOR_DELETE_FAIL, getString(R.string.alert_error_server_title),
                getString(R.string.alert_delete_account_error_message), null, getString(R.string.ok));

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_FOR_DELETE_CONFIRMATION && resultCode == RESULT_OK){
            getPresenter().deleteAccount();
        }
    }

    @Override
    public void onLogOutSuccessful() {
        setResult(RESULT_OK);
        finish();
    }
    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
