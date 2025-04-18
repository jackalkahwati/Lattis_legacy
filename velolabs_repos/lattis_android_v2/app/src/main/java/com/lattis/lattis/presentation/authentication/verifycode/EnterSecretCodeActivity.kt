package com.lattis.lattis.presentation.authentication.verifycode

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.text.Html
import android.view.View
import androidx.annotation.StringRes
import com.jakewharton.rxbinding4.widget.TextViewTextChangeEvent
import com.jakewharton.rxbinding4.widget.textChangeEvents
import com.lattis.lattis.presentation.authentication.signin.SignInActivity
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_enter_secret_code.*
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_signup.*
import javax.inject.Inject

class EnterSecretCodeActivity : BaseActivity<EnterSecretCodeActivityPresenter, EnterSecretCodeActivityView>(),
    EnterSecretCodeActivityView {


    private val REQUEST_CODE_VERIFY_USER = 464
    private val POP_UP_REQUEST_CODE = 4341


    companion object {
        val ARG_USER_ID = "ARG_USER_ID"
        val ARG_USER_ACCOUNT_TYPE = "ARG_USER_ACCOUNT_TYPE"
        val ARG_PASSWORD = "ARG_PASSWORD"
        val USER_ACCOUNT_TYPE_MAIN = "main_account"


        fun launchForResult(
            activity: Activity,
            requestCode: Int,
            userId: String?,
            accout_type: String?,
            password: String?
        ) {
            val intent = Intent(activity, EnterSecretCodeActivity::class.java)
            intent.putExtra(ARG_USER_ID, userId)
            intent.putExtra(ARG_USER_ACCOUNT_TYPE, accout_type)
            intent.putExtra(ARG_PASSWORD, password)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    @Inject
    override lateinit var presenter: EnterSecretCodeActivityPresenter
    override val activityLayoutId = R.layout.activity_enter_secret_code
    override var view: EnterSecretCodeActivityView = this


    override fun configureViews() {


        cl_already_have_account_verify.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finishMe()
        }

        btn_verify_submit_selected.setOnClickListener {
            showProgressLoading(getString(R.string.verifying))
            presenter.submitConfirmationCode()
        }
        verify_resend.setOnClickListener {
            presenter.sendConfirmationCode()
        }



    }

    override fun configureSubscriptions() {
        subscriptions.add(et_email_code_verification_code.textChangeEvents()
            .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                presenter.setConfirmationCode(et_email_code_verification_code.text.toString())
            }
        )
    }


    override fun onInformationValid() {
        btn_verify_submit_selected.visibility = View.VISIBLE
        btn_verify_submit_unselected.visibility = View.GONE
    }

    override fun onInformationInvalid() {
        btn_verify_submit_selected.visibility = View.GONE
        btn_verify_submit_unselected.visibility = View.VISIBLE
    }

    override fun showConfirmationCodeError(@StringRes error: Int) {

    }

    override fun hideConfirmationCodeError() {

    }

    override fun onSecretCodeFail() {
        hideProgressLoading()
    }

    override fun onSecretCodeConfirmed() {
        setResult(RESULT_OK)
        finish()
    }

    fun onSecretCodeResent() {

    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    fun showProgressLoading(message:String){
        enter_secret_code_loading.visibility= View.VISIBLE
        enter_secret_code_loading.ct_loading_title.text = message
    }

    fun hideProgressLoading(){
        enter_secret_code_loading.visibility= View.GONE
    }

}