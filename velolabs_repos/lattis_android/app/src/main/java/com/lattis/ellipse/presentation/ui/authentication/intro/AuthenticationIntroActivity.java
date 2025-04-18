package com.lattis.ellipse.presentation.ui.authentication.intro;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;

import com.lattis.ellipse.presentation.ui.authentication.signin.SignInActivity;
import com.lattis.ellipse.presentation.ui.authentication.signup.SignUpActivity;
import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;
import com.lattis.ellipse.presentation.ui.home.HomeActivity;
import com.lattis.ellipse.presentation.view.CustomTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

public class AuthenticationIntroActivity extends BaseActivity<AuthenticationIntroPresenter>
        implements AuthenticationIntroView {

    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_SIGN_UP = 1;

    @BindView(R.id.ct_signup_login_terms_condition)
    CustomTextView ct_signup_login_terms_condition;

    @Inject
    AuthenticationIntroPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Intent intent = new Intent(this, RegistrationIntentService.class);
//        startService(intent);
    }

    @Override
    protected void configureViews() {
        super.configureViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ct_signup_login_terms_condition.setText(Html.fromHtml(getString(R.string.signup_login_terms_conditions), Html.FROM_HTML_MODE_LEGACY));
        } else {
            ct_signup_login_terms_condition.setText(Html.fromHtml(getString(R.string.signup_login_terms_conditions)));
        }
        ct_signup_login_terms_condition.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected AuthenticationIntroPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_authentication;
    }


    @OnClick(R.id.signin_button)
    public void onSignInClicked(View v) {
        super.onClick(v);
        SignInActivity.launchForResult(this,REQUEST_CODE_SIGN_IN);
    }

    @OnClick(R.id.signup_button)
    public void onSignUpClicked(View v) {
        super.onClick(v);
        SignUpActivity.launchForResult(this,REQUEST_CODE_SIGN_UP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( (requestCode == REQUEST_CODE_SIGN_IN || requestCode == REQUEST_CODE_SIGN_UP) && resultCode == RESULT_OK)
        {
            restartApplication();

        }
    }

    private void restartApplication(){
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();

    }

    @Override
    public void showWalkThrough() {

    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
