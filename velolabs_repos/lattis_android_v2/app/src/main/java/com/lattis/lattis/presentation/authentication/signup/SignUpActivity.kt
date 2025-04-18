package com.lattis.lattis.presentation.authentication.signup

import android.content.Intent
import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.jakewharton.rxbinding4.widget.TextViewTextChangeEvent
import com.jakewharton.rxbinding4.widget.textChangeEvents
import com.lattis.lattis.presentation.authentication.signin.SignInActivity
import com.lattis.lattis.presentation.authentication.verifycode.EnterSecretCodeActivity
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusActivity
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter.Companion.CURRENT_STATUS
import com.lattis.lattis.presentation.home.activity.HomeActivity
import com.lattis.lattis.presentation.utils.BuildConfigUtil
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.ct_error_msg
import javax.inject.Inject


class SignUpActivity : BaseUserCurrentStatusActivity<SignUpActivityPresenter, SignUpActivityView>(),
    SignUpActivityView {


    private val VERIFICATION_CODE_REQUEST = 1001
    private val REQUEST_CODE_VERIFY_USER = 464
    private val POP_UP_REQUEST_CODE = 4341

    @Inject
    override lateinit var presenter: SignUpActivityPresenter
    override val activityLayoutId = R.layout.activity_signup
    override var view: SignUpActivityView = this


    override fun configureSubscriptions() {
        super.configureSubscriptions()
        subscriptions.add(
            et_sign_up_email.textChangeEvents()
            .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                onEmailTyping()
                presenter.setEmail(et_sign_up_email.text.toString())
            }
        )
        subscriptions.add(
            et_sign_up_password.textChangeEvents()
            .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                onPasswordTyping()
                presenter.setPassword(et_sign_up_password.text.toString())
            }
        )

        subscriptions.add(
            et_sign_up_first_name.textChangeEvents()
                .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                    presenter.setFirstname(et_sign_up_first_name.text.toString())
                }
        )
        subscriptions.add(
            et_sign_up_last_name.textChangeEvents()
                .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                    presenter.setLastName(et_sign_up_last_name.text.toString())
                }
        )
    }

    override fun configureViews() {
        super.configureViews()

        cl_already_have_account_sign_in.setOnClickListener {
            startSignInActivity()
        }

        btn_sign_up_submit_selected.setOnClickListener {
            hideKeyboard()
            showProgressLoading(getString(R.string.log_in_loader))
            presenter.trySignUp()
        }

        iv_sign_up_password_show.setOnClickListener {
            if(et_sign_up_password.transformationMethod==null) {
                et_sign_up_password.setTransformationMethod(PasswordTransformationMethod())
            }else{
                et_sign_up_password.setTransformationMethod(null)
            }
        }

        ct_sign_up_terms_policy.text = HtmlCompat.fromHtml(getString(R.string.welcome_terms_and_privacy_text,
            BuildConfigUtil.applicationName(),
            BuildConfigUtil.termsOfService(),
            BuildConfigUtil.privacyPolicy()
        ), HtmlCompat.FROM_HTML_MODE_LEGACY)
        ct_sign_up_terms_policy.movementMethod = LinkMovementMethod.getInstance()
    }



    //// on registration success or not :start
    override fun onRegistrationSuccess(userId: String?): Unit {
        presenter.userCurrentStatus()
    }

    override fun onUserNotVerified(userId: String?) {
        hideProgressLoading()
        EnterSecretCodeActivity.launchForResult(
            this,
            VERIFICATION_CODE_REQUEST,
            userId,
            EnterSecretCodeActivity.USER_ACCOUNT_TYPE_MAIN,
            presenter.getPassword()
        )
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == VERIFICATION_CODE_REQUEST) {
            if (resultCode == RESULT_OK) {
                presenter.userCurrentStatus()
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showDuplicateError() {
        hideProgressLoading()
        ct_error_msg.text = getString(R.string.signup_account_exists_text)
        ct_error_msg.visibility = View.VISIBLE
    }

    override fun onRegistrationFailed() {
        hideProgressLoading()
        ct_error_msg.text = getString(R.string.general_error_message)
        ct_error_msg.visibility = View.VISIBLE
    }


    fun onEmailTyping(){
        ct_error_msg.visibility = View.GONE
    }

    fun onPasswordTyping(){
//        tl_sign_up_password.startIconDrawable=ContextCompat.getDrawable(this,R.drawable.password)
//        tl_sign_up_password.setStartIconTintList(
//            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_up_right_password))
//        )
        et_sign_up_password.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_up_right_password)))
        et_sign_up_password.setHintTextColor(ContextCompat.getColor(this, R.color.sign_up_hint))
        ct_error_msg.visibility = View.GONE
    }

    override fun onPasswordInvalid(){
//        tl_sign_up_password.setStartIconTintList(
//            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_up_wrong_password))
//        )
        hideProgressLoading()
        et_sign_up_password.setTextColor(ContextCompat.getColor(this, R.color.sign_up_wrong_password))
        et_sign_up_password.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sign_up_wrong_password)))
        ct_error_msg.text = getString(R.string.password_invalid)
        ct_error_msg.visibility = View.VISIBLE
    }


    //// on registartion success or not :end



    override fun onInformationValid() {
        btn_sign_up_submit_selected.visibility = View.VISIBLE
        btn_sign_up_submit_unselected.visibility = View.GONE
    }

    override fun onInformationInvalid() {
        btn_sign_up_submit_selected.visibility = View.GONE
        btn_sign_up_submit_unselected.visibility = View.VISIBLE
    }



    private fun startSignInActivity() {
        val i = Intent(this, SignInActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finishMe()
    }

    private fun startHomeActivity() {
        val i = Intent(this, HomeActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        i.putExtra(CURRENT_STATUS, presenter.currentStatus)
        startActivity(i)
        finishMe()
    }

    override fun onUserCurrentStatusSuccess() {
        startHomeActivity()
    }

    override fun onUserCurrentStatusFailure() {
        startHomeActivity()
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    fun showProgressLoading(message:String){
        sign_up_loading.visibility= View.VISIBLE
        sign_up_loading.ct_loading_title.text = message
    }

    fun hideProgressLoading(){
        sign_up_loading.visibility= View.GONE
    }


    override fun passwordToggleButton(active: Boolean) {
        iv_sign_up_password_show.visibility = if(active)View.VISIBLE else View.GONE
    }
}