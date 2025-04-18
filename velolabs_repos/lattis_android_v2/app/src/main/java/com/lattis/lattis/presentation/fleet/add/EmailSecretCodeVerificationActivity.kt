package com.lattis.lattis.presentation.fleet.add

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import com.jakewharton.rxbinding4.widget.TextViewTextChangeEvent
import com.jakewharton.rxbinding4.widget.textChangeEvents
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_email_secret_code.*
import kotlinx.android.synthetic.main.activity_email_secret_code_email.*
import kotlinx.android.synthetic.main.activity_email_secret_code_verification.*
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_private_fleets.*
import javax.inject.Inject

class EmailSecretCodeVerificationActivity : BaseActivity<EmailSecretCodeVerificationActivityPresenter, EmailSecretCodeVerificationActivityView>(),
    EmailSecretCodeVerificationActivityView {


    private val REQUEST_CODE_CONFIRM_CODE_FAILURE = 9022
    private val REQUEST_CODE_NO_NEW_FLEET = 9023
    private val REQUEST_SERVER_GENERAL_ERROR = 9024

    private val EMAIL =0
    private val VERIFICATION_CODE =1


    companion object {
        val ARG_USER_FLEET_PRESENT = "ARG_USER_FLEET_PRESENT"


        fun launchForResult(
            activity: Activity,
            requestCode: Int,
            userFleetPresent:Boolean
        ) {
            val intent = Intent(activity, EmailSecretCodeVerificationActivity::class.java)
                .putExtra(ARG_USER_FLEET_PRESENT,userFleetPresent)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    @Inject
    override lateinit var presenter: EmailSecretCodeVerificationActivityPresenter
    override val activityLayoutId = R.layout.activity_email_secret_code
    override var view: EmailSecretCodeVerificationActivityView = this


    override fun configureViews() {
        presenter.getUser

        btn_email_secret_send_verification_code_email.setOnClickListener {
            showLoadingForEmailSecretCode(getString(R.string.loading))
            presenter.addPrivateNetwork()
        }
        verify_resend.setOnClickListener {
            showLoadingForEmailSecretCode(getString(R.string.loading))
            presenter.addPrivateNetwork()
        }

        btn_verify_submit_selected.setOnClickListener {
            showLoadingForEmailSecretCode(getString(R.string.verifying))
            presenter.submitConfirmationCode()
        }

        iv_close_in_email_secret_email.setOnClickListener {
            finish()
        }

        iv_close_in_email_verification_code.setOnClickListener {
            finish()
        }

    }

    override fun configureSubscriptions() {
        super.configureSubscriptions()

        subscriptions.add(et_email_email_secret_email.textChangeEvents()
            .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                presenter.setEmail(et_email_email_secret_email.text.toString())
            }
        )
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
        showServerGeneralError(REQUEST_SERVER_GENERAL_ERROR)
    }

    override fun onSecretCodeConfirmed() {
        setResult(RESULT_OK)
        finish()
    }

    fun onSecretCodeResent() {

    }

    override fun onNoNewFleetWithCurrentFleetPresent() {
        PopUpActivity.launchForResult(
            this, REQUEST_CODE_NO_NEW_FLEET,
            getString(R.string.private_fleet),
            getString(R.string.private_network_content_has_fleets),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }

    override fun onNoNewFleetWithNoCurrentFleetPresent() {
        PopUpActivity.launchForResult(
            this, REQUEST_CODE_NO_NEW_FLEET,
            getString(R.string.private_fleet),
            getString(R.string.private_network_content_no_access),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }

    override fun onCodeSentSuccess() {
        if(view_flipper_in_secret_code.displayedChild!= VERIFICATION_CODE)
            view_flipper_in_secret_code.displayedChild= VERIFICATION_CODE
    }

    override fun onCodeSentFail() {
        showServerGeneralError(REQUEST_SERVER_GENERAL_ERROR)
    }



    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    //// loading :start
    override fun showLoadingForEmailSecretCode(message: String?) {
        secret_code_activity_loading_view.visibility = (View.VISIBLE)
        secret_code_activity_loading_view.ct_loading_title.text = (message)
    }

    override fun hideLoadingForEmailSecretCode() {
        secret_code_activity_loading_view.visibility = (View.GONE)
    }



}