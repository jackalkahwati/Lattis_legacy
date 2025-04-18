package com.lattis.ellipse.presentation.ui.profile.help;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import android.text.Html;

import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

public class HelpActivity  extends BaseBackArrowActivity<HelpPresenter> implements HelpView {
    private String phoneNumber;


    @BindView(R.id.tv_help_info)
    CustomTextView helpInfo;

    @Inject
    HelpPresenter helpPresenter;
    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected HelpPresenter getPresenter() {
        return helpPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_help;
    }

    @OnClick(R.id.tv_help_info)
    public void helpInfoButtonClicked()
    {
        startActivity( new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));

    }
    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.action_help));
        getPresenter().getRide();
    }

    @Override
    public void showDefaultLattisNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        showPhoneNumberText(getPhoneNumberText(phoneNumber));
    }

    @Override
    public void showOperatorNumber(String phoneNumber) {
        this.phoneNumber=phoneNumber;
        showPhoneNumberText(getPhoneNumberText(phoneNumber));
    }

    void showPhoneNumberText(String helpText){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            helpInfo.setText(Html.fromHtml(helpText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            helpInfo.setText(Html.fromHtml(helpText));
        }
    }


    String getPhoneNumberText(String phoneNumber){
        return getString(R.string.help_info, phoneNumber);
    }
    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
