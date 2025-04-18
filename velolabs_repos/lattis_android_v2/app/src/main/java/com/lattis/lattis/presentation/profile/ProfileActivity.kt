package com.lattis.lattis.presentation.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import com.lattis.lattis.presentation.base.activity.BaseAuthenticatedActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.popup.edit.PopUpEditActivity
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import com.lattis.lattis.utils.AccountAuthenticatorHelper
import com.lattis.lattis.utils.AccountAuthenticatorHelper.getAppLogOutBundle
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_profile_settings.*
import javax.inject.Inject


class ProfileActivity : BaseAuthenticatedActivity<ProfileActivityPresenter, ProfileActivityView>(),
    ProfileActivityView {


    private val REQUEST_CODE_ERROR = 4393
    private val REQUEST_CODE_ADD_PHONE_NUMBER = 4394
    private val REQUEST_CODE_VALIDATE_PHONE_NUMBER = 4395
    private val REQUEST_CODE_EDIT_ERROR = 4396
    private val REQUEST_CODE_FIRST_NAME = 4397
    private val REQUEST_CODE_LAST_NAME = 4398
    private val REQUEST_CODE_CHANGE_PASSWORD = 4399
    private val REQUEST_CODE_ADD_EMAIL = 4400
    private val REQUEST_CODE_VALIDATE_EMAIL = 4401
    private val REQUEST_CODE_DELETE_ACCOUNT = 4402

    @Inject
    override lateinit var presenter: ProfileActivityPresenter
    override val activityLayoutId = R.layout.activity_profile_settings
    override var view: ProfileActivityView = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun configureViews() {
        super.configureViews()
        showLoadingForProfile()
        presenter.getUserProfile()
        configureClicks()
    }

    fun configureClicks(){

        iv_close_in_profile_settings.setOnClickListener {
            finish()
        }

        ct_phone_number_value_profile_settings.setOnClickListener {
            openPhoneNumberAddActivity()
        }

        cl_profile_settings_phone_number.setOnClickListener {
            openPhoneNumberAddActivity()
        }

        cl_profile_settings_first_name.setOnClickListener {
            openAddFirstNameActivity()
        }

        cl_profile_settings_first_name.setOnClickListener {
            openAddFirstNameActivity()
        }

        ct_last_name_value_profile_settings.setOnClickListener {
            openAddLastNameActivity()
        }

        cl_profile_settings_last_name.setOnClickListener {
            openAddLastNameActivity()
        }

        cl_profile_settings_security.setOnClickListener {
            openChangePasswordActivity()
        }
        ct_security_value_profile_settings.setOnClickListener {
            openChangePasswordActivity()
        }

        ct_email_value_profile_settings.setOnClickListener {
            openEmailAddActivity()
        }

        cl_profile_settings_email.setOnClickListener {
            openEmailAddActivity()
        }

        cl_profile_settings_delete_account.setOnClickListener {
            openDeleteAccountPopUp()
        }

    }


    override fun setLastName(lastName: String) {
        ct_last_name_value_profile_settings.text = lastName
    }

    override fun setFirstName(firstName: String) {
        ct_first_name_value_profile_settings.text = firstName
    }

    override fun setPhoneNumber(phoneNumber: String) {
        ct_phone_number_value_profile_settings.text = phoneNumber
    }

    override fun setEmail(email: String) {
        ct_email_value_profile_settings.text = email
    }

    override fun onProfileFetchError() {
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_CODE_ERROR){
            finish()
        }else if(requestCode==REQUEST_CODE_ADD_PHONE_NUMBER &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            showLoadingForProfile()
            presenter.phoneNumber = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.sendCodeToUpdatePhoneNumber()
        }else if(requestCode==REQUEST_CODE_VALIDATE_PHONE_NUMBER &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            showLoadingForProfile()
            presenter.code = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.validateCodeForUpdatePhoneNumber()
        }else if(requestCode==REQUEST_CODE_FIRST_NAME &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            showLoadingForProfile()
            presenter.user?.firstName = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.saveUser()
        }else if(requestCode==REQUEST_CODE_LAST_NAME &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            showLoadingForProfile()
            presenter.user?.lastName = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.saveUser()
        }else if(requestCode==REQUEST_CODE_CHANGE_PASSWORD &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT) &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_2_POP_UP_EDIT)
        ){
            showLoadingForProfile()
            presenter.changePassword(data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)!!,data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_2_POP_UP_EDIT)!!)
        }else if(requestCode==REQUEST_CODE_ADD_EMAIL &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            showLoadingForProfile()
            presenter.newEmail = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.sendCodeToUpdateEmail()
        }else if(requestCode==REQUEST_CODE_VALIDATE_EMAIL &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            showLoadingForProfile()
            presenter.codeForEmail = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.validateCodeForUpdateEmail()
        }else if(requestCode == REQUEST_CODE_DELETE_ACCOUNT &&
            resultCode == Activity.RESULT_OK &&
            data != null &&
            data.hasExtra(PopUpActivity.POSITIVE_LEVEL) &&
            data.getIntExtra(PopUpActivity.POSITIVE_LEVEL, -1) == 1) {

            presenter.deleteAccount()
        }

    }

    ///// Phone number add
    fun openPhoneNumberAddActivity(){
        PopUpEditActivity.launchForResult(
            this,
            REQUEST_CODE_ADD_PHONE_NUMBER,
            getString(R.string.phone_update_note),
            null,
            R.drawable.phone_number,
            getString(R.string.send_verification_code),
            String.format("+%d ", presenter.countryCode) ,
//            if (TextUtils.isEmpty(ct_phone_number_value_profile_settings.text)) presenter.countryCode else ct_phone_number_value_profile_settings.text.toString(),
            false,
            getString(R.string.phone_number),
            InputType.TYPE_CLASS_TEXT
        )
    }

    fun openCodeValidationActivity(){
        PopUpEditActivity.launchForResult(
            this,
            REQUEST_CODE_VALIDATE_PHONE_NUMBER,
            getString(R.string.validate_code_for_phone_number_title,presenter.phoneNumber),
            null,
            R.drawable.icon_confrimation_code,
            getString(R.string.submit),
            null,
//            if (TextUtils.isEmpty(ct_phone_number_value_profile_settings.text)) presenter.countryCode else ct_phone_number_value_profile_settings.text.toString(),
            false,
            getString(R.string.hint_enter_code),
            InputType.TYPE_CLASS_NUMBER
        )
    }

    fun openAddFirstNameActivity(){
        PopUpEditActivity.launchForResult(
            this,
            REQUEST_CODE_FIRST_NAME,
            getString(R.string.first_name),
            null,
            R.drawable.user,
            getString(R.string.submit),
            ct_first_name_value_profile_settings.text.toString(),
//            if (TextUtils.isEmpty(ct_phone_number_value_profile_settings.text)) presenter.countryCode else ct_phone_number_value_profile_settings.text.toString(),
            false,
            getString(R.string.first_name),
            InputType.TYPE_CLASS_TEXT
        )
    }

    fun openAddLastNameActivity(){
        PopUpEditActivity.launchForResult(
            this,
            REQUEST_CODE_LAST_NAME,
            getString(R.string.last_name),
            null,
            R.drawable.user,
            getString(R.string.submit),
            ct_last_name_value_profile_settings.text.toString(),
//            if (TextUtils.isEmpty(ct_phone_number_value_profile_settings.text)) presenter.countryCode else ct_phone_number_value_profile_settings.text.toString(),
            false,
            getString(R.string.last_name),
            InputType.TYPE_CLASS_TEXT
        )
    }

    fun openChangePasswordActivity(){
        PopUpEditActivity.launchForResult(
            this,
            REQUEST_CODE_CHANGE_PASSWORD,
            getString(R.string.change_password),
            null,
            R.drawable.password,
            getString(R.string.submit),
            null,
            true,
            getString(R.string.current_password),
            InputType.TYPE_CLASS_TEXT,
            getString(R.string.enter_new_password)
        )
    }

    fun openEmailAddActivity(){
        PopUpEditActivity.launchForResult(
            this,
            REQUEST_CODE_ADD_EMAIL,
            getString(R.string.email),
            null,
            R.drawable.email,
            getString(R.string.send_verification_code),
            null,
            false,
            getString(R.string.email),
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
    }

    fun openCodeValidationForEmailActivity(){
        PopUpEditActivity.launchForResult(
            this,
            REQUEST_CODE_VALIDATE_EMAIL,
            getString(R.string.verification_code_description),
            null,
            R.drawable.icon_confrimation_code,
            getString(R.string.submit),
            null,
            false,
            getString(R.string.hint_enter_code),
            InputType.TYPE_CLASS_NUMBER
        )
    }

    fun openDeleteAccountPopUp(){
        launchPopUpActivity(
            REQUEST_CODE_DELETE_ACCOUNT,
            getString(R.string.delete_account),
            getString(R.string.if_you_request_deletion_of_your_account_your_accou),
            null,
            getString(R.string.confirm),
            null,
            null,
            getString(R.string.cancel)
        )
    }

    override fun onCodeSentSuccessForUpdateEmail() {
        hideLoadingForProfile()
        openCodeValidationForEmailActivity()
    }

    override fun onCodeSentFailureForUpdateEmail() {
        hideLoadingForProfile()
        showServerGeneralError(REQUEST_CODE_EDIT_ERROR)
    }

    override fun onCodeSentSuccess() {
        hideLoadingForProfile()
        openCodeValidationActivity()
    }

    override fun onCodeSentFailure() {
        hideLoadingForProfile()
        showServerGeneralError(REQUEST_CODE_EDIT_ERROR)
    }

    override fun onCodeValidateSuccess() {
        hideLoadingForProfile()
        presenter.getUserProfile()
    }

    override fun onCodeValidateFailure() {
        hideLoadingForProfile()
        showServerGeneralError(REQUEST_CODE_EDIT_ERROR)
    }

    override fun showLoadingForProfile() {
        profile_settings_loading.ct_loading_title.text = getString(R.string.loading)
        profile_settings_loading.visibility= View.VISIBLE
    }

    override fun hideLoadingForProfile() {
        profile_settings_loading.visibility= View.GONE
    }

    override fun onUserSaveFailure() {
        hideLoadingForProfile()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun onPasswordChangeSuccess() {
        hideLoadingForProfile()
    }

    override fun onPasswordChangeFailure() {
        hideLoadingForProfile()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }


    override fun onDeleteAccountSuccess() {
        authenticateAccount(getAppLogOutBundle())
        finishMe()
    }

    override fun onDeleteAccountFailure() {
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}