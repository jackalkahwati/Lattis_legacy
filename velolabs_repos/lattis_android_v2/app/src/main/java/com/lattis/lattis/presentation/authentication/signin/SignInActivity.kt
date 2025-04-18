package com.lattis.lattis.presentation.authentication.signin

import android.content.Intent
import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.jakewharton.rxbinding4.widget.TextViewTextChangeEvent
import com.jakewharton.rxbinding4.widget.textChangeEvents
import com.lattis.lattis.presentation.authentication.forgotpassword.ForgotPasswordActivity
import com.lattis.lattis.presentation.authentication.signup.SignUpActivity
import com.lattis.lattis.presentation.authentication.verifycode.EnterSecretCodeActivity
import com.lattis.lattis.presentation.authentication.verifycode.EnterSecretCodeActivity.Companion.USER_ACCOUNT_TYPE_MAIN
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusActivity
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter.Companion.CURRENT_STATUS
import com.lattis.lattis.presentation.home.activity.HomeActivity
import com.lattis.lattis.presentation.utils.BuildConfigUtil.applicationName
import com.lattis.lattis.presentation.utils.BuildConfigUtil.privacyPolicy
import com.lattis.lattis.presentation.utils.BuildConfigUtil.termsOfService
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_signin.*
import javax.inject.Inject


class SignInActivity : BaseUserCurrentStatusActivity<SignInActivityPresenter, SignInActivityView>(),
    SignInActivityView {


    private val REQUEST_CODE_VERIFY_USER = 464
    private val POP_UP_REQUEST_CODE = 4341

    @Inject
    override lateinit var presenter: SignInActivityPresenter
    override val activityLayoutId = R.layout.activity_signin
    override var view: SignInActivityView = this


    override fun configureSubscriptions() {
        super.configureSubscriptions()
        subscriptions.add(et_sign_in_email.textChangeEvents()
            .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                onTyping()
                presenter.setEmail(et_sign_in_email.text.toString())
            }
        )
        subscriptions.add(et_sign_in_password.textChangeEvents()
            .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                onTyping()
                presenter.setPassword(et_sign_in_password.text.toString())
            }
        )
    }

    override fun configureViews() {
        super.configureViews()
        btn_sign_in_login_in_selected.setOnClickListener {
            hideKeyboard()
            showProgressLoading(getString(R.string.log_in_loader))
            presenter.trySignIn()
        }

        btn_sign_in_create_account.setOnClickListener {
            startSignUpActivity()
        }

        iv_sign_in_password_show.setOnClickListener {
            if(et_sign_in_password.transformationMethod==null) {
                et_sign_in_password.setTransformationMethod(PasswordTransformationMethod())
            }else{
                et_sign_in_password.setTransformationMethod(null)
            }
        }

        ct_sign_in_terms_policy.text = HtmlCompat.fromHtml(getString(R.string.welcome_terms_and_privacy_text,applicationName(),termsOfService(),privacyPolicy()),HtmlCompat.FROM_HTML_MODE_LEGACY)
        ct_sign_in_terms_policy.movementMethod = LinkMovementMethod.getInstance()

        ct_sign_in_forgot_password.setOnClickListener {
            startActivityForResult(Intent(this, ForgotPasswordActivity::class.java),1001)
        }
    }

    //// toggle login btn: start

    override fun passwordToggleButton(active: Boolean) {
        iv_sign_in_password_show.visibility = if(active)View.VISIBLE else View.GONE
    }

    override fun onEmailPasswordValid() {
        btn_sign_in_login_in_selected.visibility = View.VISIBLE
        btn_sign_in_login_in_unselected.visibility = View.GONE
    }

    override fun onEmailPasswordInvalid() {
        btn_sign_in_login_in_selected.visibility = View.GONE
        btn_sign_in_login_in_unselected.visibility = View.VISIBLE
    }
    //// show hide email password:end


    //// onuser verified or not :start
    override fun onUserVerified(userId: String?): Unit {
        presenter.userCurrentStatus()
    }

    override fun onUserNotVerified(userId: String?) {
        EnterSecretCodeActivity.launchForResult(
            this,
            REQUEST_CODE_VERIFY_USER,
            userId,
            USER_ACCOUNT_TYPE_MAIN,
            presenter.getPassword()
        )
        hideProgressLoading()
    }

    override fun onAuthenticationFailed() {
//        tl_sign_in_password.setStartIconTintList(
//            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_up_wrong_password))
//        )
        hideProgressLoading()
        iv_sign_in_password.setColorFilter(ContextCompat.getColor(this, R.color.sign_in_wrong_password));
        et_sign_in_password.setTextColor(ContextCompat.getColor(this, R.color.sign_up_wrong_password))
        et_sign_in_password.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_up_wrong_password)))
    }


    override fun onUserNotExists() {

//        tl_sign_in_email.setStartIconTintList(
//            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_in_wrong_password))
//        )

        hideProgressLoading()
        iv_sign_in_email.setColorFilter(ContextCompat.getColor(this, R.color.sign_in_wrong_password));

        et_sign_in_email.setTextColor(ContextCompat.getColor(this, R.color.sign_in_wrong_password))
        et_sign_in_email.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_in_wrong_password)))


//        tl_sign_in_password.setStartIconTintList(
//            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_in_wrong_password))
//        )

        iv_sign_in_password.setColorFilter(ContextCompat.getColor(this, R.color.sign_in_wrong_password));

        et_sign_in_password.setTextColor(ContextCompat.getColor(this, R.color.sign_in_wrong_password))
        et_sign_in_password.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_in_wrong_password)))

        ct_error_msg.text = getString(R.string.no_account_exists_message)
        ct_error_msg.visibility = View.VISIBLE

    }

    fun onTyping(){

        iv_sign_in_email.setColorFilter(ContextCompat.getColor(this, R.color.sign_in_right_password));

//        tl_sign_in_email.setStartIconTintList(
//            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_in_right_password))
//        )


        et_sign_in_email.setTextColor(ContextCompat.getColor(this, R.color.sign_in_right_password))
        et_sign_in_email.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_in_right_password)))



//        tl_sign_in_password.startIconDrawable=ContextCompat.getDrawable(this,R.drawable.password)
//        tl_sign_in_password.setStartIconTintList(
//            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_in_right_password))
//        )

        iv_sign_in_password.setColorFilter(ContextCompat.getColor(this, R.color.sign_in_right_password));

        et_sign_in_password.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_in_right_password)))
        ct_error_msg.visibility = View.GONE
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_VERIFY_USER) {
            if (resultCode == RESULT_OK) {
                presenter.userCurrentStatus()
            }
        }
    }

    private fun startHomeActivity() {
        val i = Intent(this, HomeActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        i.putExtra(CURRENT_STATUS, presenter.currentStatus)
        startActivity(i)
        finishMe()
    }

    private fun startSignUpActivity() {
        val i = Intent(this, SignUpActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finishMe()
    }

    override fun onUserCurrentStatusSuccess() {
        startHomeActivity()
    }

    override fun onUserCurrentStatusFailure() {
        startHomeActivity()
    }



    //// onuser verified or not :end


    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    fun showProgressLoading(message:String){
        sign_in_loading.visibility= View.VISIBLE
        sign_in_loading.ct_loading_title.text = message
    }

    fun hideProgressLoading(){
        sign_in_loading.visibility= View.GONE
    }


}