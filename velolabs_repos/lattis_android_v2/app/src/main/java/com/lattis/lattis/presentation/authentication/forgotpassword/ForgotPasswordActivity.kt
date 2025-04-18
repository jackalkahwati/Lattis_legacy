package com.lattis.lattis.presentation.authentication.forgotpassword

import android.content.Intent
import android.text.method.PasswordTransformationMethod
import android.view.View
import com.jakewharton.rxbinding4.widget.TextViewTextChangeEvent
import com.jakewharton.rxbinding4.widget.textChangeEvents
import com.lattis.lattis.presentation.help.HelpActivityPresenter
import com.lattis.lattis.presentation.help.HelpActivityView
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_forgot_password_enter_email.*
import kotlinx.android.synthetic.main.activity_forgot_password_validate_code_password.*
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_signin.*
import javax.inject.Inject

class ForgotPasswordActivity : BaseActivity<ForgotPasswordActivityPresenter, ForgotPasswordActivityView>(),
    ForgotPasswordActivityView {


    private val REQUEST_CODE_ERROR = 4393
    private val REQUEST_CODE_RESET_PASSWORD_SUCCESS= 4394
    private val GET_EMAIL =0
    private val RESET_PASSWORD =1

    @Inject
    override lateinit var presenter: ForgotPasswordActivityPresenter
    override val activityLayoutId = R.layout.activity_forgot_password
    override var view: ForgotPasswordActivityView = this


    override fun configureViews() {
        super.configureViews()
        configureClicks()
    }

    fun configureClicks(){
        ct_forgot_password_email_log_in.setOnClickListener {
            finish()
        }
        ct_forgot_password_validate_log_in.setOnClickListener {
            finish()
        }

        btn_forgot_password_send_code_selected.setOnClickListener {
            showProgressLoading(getString(R.string.sending_confirmation_code))
            presenter.sendConfirmationCodeForResetPassword()
        }
        ct_forgot_password_verify_resend.setOnClickListener {
            showProgressLoading(getString(R.string.sending_confirmation_code))
            presenter.sendConfirmationCodeForResetPassword()
        }

        btn_forgot_password_submit_selected.setOnClickListener {
            showProgressLoading(getString(R.string.verifying))
            presenter.resetPassword()
        }

        ct_forgot_password_change.setOnClickListener {
            showGetEmail()
        }

        iv_forgot_password_password_show.setOnClickListener {
            if(et_forgot_password_password.transformationMethod==null) {
                et_forgot_password_password.setTransformationMethod(PasswordTransformationMethod())
            }else{
                et_forgot_password_password.setTransformationMethod(null)
            }
        }
    }

    override fun configureSubscriptions() {
        super.configureSubscriptions()
        subscriptions.add(
            et_forgot_password_email.textChangeEvents()
            .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                presenter.setEmail(et_forgot_password_email.text.toString())
            })

        subscriptions.add(
            et_forgot_password_code.textChangeEvents()
                .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                    presenter.setCode(et_forgot_password_code.text.toString())
                })

        subscriptions.add(
            et_forgot_password_password.textChangeEvents()
                .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                    presenter.setPassword(et_forgot_password_password.text.toString())
                })
    }

    fun showGetEmail(){
        if(view_flipper_in_forgot_password.displayedChild!=GET_EMAIL){
            view_flipper_in_forgot_password.displayedChild = GET_EMAIL
        }
    }
    fun showResetPassword(){
        if(view_flipper_in_forgot_password.displayedChild!=RESET_PASSWORD){
            view_flipper_in_forgot_password.displayedChild = RESET_PASSWORD
        }
        ct_forgot_password_email_value.text = presenter.getEmail()
    }

    override fun toggleSendVerificationButton(status: Boolean) {
        if(status){
            btn_forgot_password_send_code_selected.visibility = View.VISIBLE
            btn_forgot_password_send_code_unselected.visibility = View.GONE
        }else{
            btn_forgot_password_send_code_selected.visibility = View.GONE
            btn_forgot_password_send_code_unselected.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_RESET_PASSWORD_SUCCESS){
            finish()
        }
    }

    override fun toggleSubmitButton(status: Boolean) {
        if(status){
            btn_forgot_password_submit_selected.visibility = View.VISIBLE
            btn_forgot_password_submit_unselected.visibility = View.GONE
        }else{
            btn_forgot_password_submit_selected.visibility = View.GONE
            btn_forgot_password_submit_unselected.visibility = View.VISIBLE
        }
    }

    override fun onPasswordResetSuccess() {
        hideProgressLoading()
        launchPopUpActivity(
            REQUEST_CODE_RESET_PASSWORD_SUCCESS,
            getString(R.string.password_updated_message),
            null,
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )

    }

    override fun onPasswordResetFailure() {
        hideProgressLoading()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun onCodeSentSuccess() {
        hideProgressLoading()
        showResetPassword()
    }

    override fun onCodeSentFailure() {
        hideProgressLoading()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun passwordToggleButton(active: Boolean) {
        iv_forgot_password_password_show.visibility = if(active)View.VISIBLE else View.GONE
    }

    fun showProgressLoading(message:String){
        forgot_password_activity_loading_view.visibility= View.VISIBLE
        forgot_password_activity_loading_view.ct_loading_title.text = message
    }

    fun hideProgressLoading(){
        forgot_password_activity_loading_view.visibility= View.GONE
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}