package com.lattis.lattis.presentation.payment.add

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.lattis.domain.models.Card
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
//import com.mercadopago.lite.core.MercadoPagoServices
//import com.mercadopago.lite.model.ApiException
//import com.mercadopago.lite.model.CardToken
//import com.mercadopago.lite.model.Token
import com.stripe.android.ApiResultCallback
import com.stripe.android.SetupIntentResult
import com.stripe.android.model.ConfirmSetupIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.StripeIntent
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_add_payment_card.*
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_payment_add_new_card.*
import kotlinx.android.synthetic.main.activity_payment_edit_card.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class AddPaymentCardActivity : BaseActivity<AddPaymentCardActivityPresenter, AddPaymentCardActivityView>(),
    AddPaymentCardActivityView {


    private val REQUEST_GENERAL_ERROR = 4393
    private val REQUEST_FAILURE_CARD_OPERATION = 4859
    private val REQUEST_CARD_DELETE_OPERATION = 4860
    private val REQUEST_CARD_EXPIRATION_UPDATE_ERROR = 4861
    private val REQUEST_CARD_CANNOT_DELETE_OPERATION = 4862

    private val ADD_NEW_CARD = 0
    private val EDIT_CARD = 1


    @Inject
    override lateinit var presenter: AddPaymentCardActivityPresenter
    override val activityLayoutId = R.layout.activity_add_payment_card
    override var view: AddPaymentCardActivityView = this

    private var mLastInputForExpiration:String=""

    companion object{
        val CARD_DETAILS = "CARD_DETAILS"
        val FLEET_ID = "FLEET_ID"
        val PAYMENT_VENDOR = "PAYMENT_VENDOR"
        val HAS_OTHER_CARD = "HAS_OTHER_CARD"

        fun launchForResult(
            activity: Activity,
            requestCode: Int,
            card: Card?,
            fleet_id:Int?=null,
            vendor:String?=null,
            canDelete:Boolean=false
        ) {
            val intent = Intent(activity, AddPaymentCardActivity::class.java)
            if(card!=null) {
                intent.putExtra(CARD_DETAILS, Gson().toJson(card))
            }

            if(fleet_id!=null){
                intent.putExtra(FLEET_ID,fleet_id)
            }
            if(vendor!=null){
                intent.putExtra(PAYMENT_VENDOR, vendor)
            }
            intent.putExtra(HAS_OTHER_CARD,canDelete)
            activity.startActivityForResult(intent, requestCode)
        }

        fun launchForResultForFragment(
            activity: Activity,
            fragment: Fragment,
            requestCode: Int,
            card: Card?,
            fleet_id:Int?=null,
            vendor:String?=null
        ) {
            val intent = Intent(activity, AddPaymentCardActivity::class.java)
            if(card!=null) {
                intent.putExtra(CARD_DETAILS, Gson().toJson(card))
            }

            if(fleet_id!=null){
                intent.putExtra(FLEET_ID,fleet_id)
            }
            if(vendor!=null){
                intent.putExtra(PAYMENT_VENDOR, vendor)
            }
            fragment.startActivityForResult(intent, requestCode)
        }

    }


    override fun configureViews() {
        super.configureViews()
        configureClicks()
    }

    fun configureClicks(){
        iv_close_in_payment_add_new_card.setOnClickListener {
            finish()
        }

        iv_close_in_payment_edit_card.setOnClickListener {
            finish()
        }

        btn_save_credit_card_in_payment_add_new_card.setOnClickListener {
            val card: com.stripe.android.model.Card? = card_multiline_widget_in_payment_add_credit_card.card
            if (card != null && card.validateCard()) {
                showLoadingForAddPaymentCard(getString(R.string.saving_payment_card_loader))
                presenter.startAddingCard()
            } else {
                onCardInvalid()
            }
        }

        btn_delete_in_payment_edit_card.setOnClickListener {
            presenter.processDeleteAction()
        }

        btn_update_in_payment_edit_card.setOnClickListener {
            if(validateExpirationMonthAndYear()) {
                val expirationDateString = et_card_expiration_value_in_payment_edit_card.getText().toString()
                val exp_month = expirationDateString.substringBefore("/").toIntOrNull()
                val exp_year = expirationDateString.substringAfter("/").toIntOrNull()
                showLoadingForAddPaymentCard(getString(R.string.loading))
                presenter.updateCardExpiration(exp_month!!, exp_year!!)
            }
        }

    }

    override fun showDeleteCardWarning(){
        showCardOperationPopUp(
            getString(R.string.general_error_title),
            getString(R.string.credit_card_delete_message),
            getString(R.string.delete),
            REQUEST_CARD_DELETE_OPERATION
        )
    }

    override fun showSingleCardDeleteError(){
        showCardOperationPopUp(
            getString(R.string.general_error_title),
            getString(R.string.removing_single_card_alert),
            getString(R.string.general_btn_ok),
            REQUEST_CARD_CANNOT_DELETE_OPERATION,
            false
        )
    }

    override fun showPrimaryCardDeleteError(){
        showCardOperationPopUp(
            getString(R.string.general_error_title),
            getString(R.string.removing_single_card_alert),
            getString(R.string.general_btn_ok),
            REQUEST_CARD_CANNOT_DELETE_OPERATION,
            false
        )
    }

    fun validateExpirationMonthAndYear():Boolean{
        val expirationDateString = et_card_expiration_value_in_payment_edit_card.getText().toString()
        if(TextUtils.isEmpty(expirationDateString) || expirationDateString.indexOf("/")==-1){
            et_card_expiration_value_in_payment_edit_card.setError("")
            return false
        }

        val exp_month = expirationDateString.substringBefore("/").toIntOrNull()
        val exp_year = expirationDateString.substringAfter("/").toIntOrNull()

        if(exp_month==null || exp_year==null || exp_year < 999 || exp_year > 9999 || exp_month==0 || exp_month>12) {
            et_card_expiration_value_in_payment_edit_card.setError("")
            return false
        }

        return true
    }

    fun showAddNewCardView() {
        if (view_flipper_in_add_payment_card.displayedChild != ADD_NEW_CARD)
            view_flipper_in_add_payment_card.displayedChild = ADD_NEW_CARD
    }

    override fun showEditCardView() {
        if (view_flipper_in_add_payment_card.displayedChild != EDIT_CARD)
            view_flipper_in_add_payment_card.displayedChild = EDIT_CARD

        ct_card_number_value_in_payment_edit_card.text = presenter.card?.cc_no
        var exp_month = if(presenter.card?.exp_month!!<10) "0"+presenter.card?.exp_month!! else presenter.card?.exp_month!!
        et_card_expiration_value_in_payment_edit_card.setText(""+exp_month + "/" + presenter.card?.exp_year!!)
        mLastInputForExpiration = ""+exp_month + "/" + presenter.card?.exp_year!!
        startExpirationDateValidation()
    }

    fun startExpirationDateValidation(){
        et_card_expiration_value_in_payment_edit_card.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                et_card_expiration_value_in_payment_edit_card.setError(null)
                val input = s.toString();
                val formatter = SimpleDateFormat("MM/yyyy");
                val expiryDateDate = Calendar.getInstance();
                try {
                    expiryDateDate.setTime(formatter.parse(input));
                } catch (e: ParseException) {
                    if (s?.length == 2 && !mLastInputForExpiration?.endsWith("/")!!) {
                        val month = input.toIntOrNull()
                        if (month!=null && month <= 12) {
                            et_card_expiration_value_in_payment_edit_card.setText(
                                et_card_expiration_value_in_payment_edit_card.getText()
                                    .toString() + "/"
                            );
                            et_card_expiration_value_in_payment_edit_card.setSelection(
                                et_card_expiration_value_in_payment_edit_card.getText().toString()
                                    .length
                            );
                        }else if(month!=null && month >12){
                            et_card_expiration_value_in_payment_edit_card.setText("");
                            et_card_expiration_value_in_payment_edit_card.setSelection(
                                et_card_expiration_value_in_payment_edit_card.getText()
                                    .toString()
                                    .length
                            );
                        }
                    } else if (s?.length == 2 && mLastInputForExpiration?.endsWith("/")!!) {
                        val month = input.toIntOrNull()
                        if(month!=null) {
                            if (month <= 12) {
                                et_card_expiration_value_in_payment_edit_card.setText(
                                    et_card_expiration_value_in_payment_edit_card.getText()
                                        .toString()
                                        .substring(0, 1)
                                );
                                et_card_expiration_value_in_payment_edit_card.setSelection(
                                    et_card_expiration_value_in_payment_edit_card.getText()
                                        .toString()
                                        .length
                                );
                            } else {
                                et_card_expiration_value_in_payment_edit_card.setText("");
                                et_card_expiration_value_in_payment_edit_card.setSelection(
                                    et_card_expiration_value_in_payment_edit_card.getText()
                                        .toString()
                                        .length
                                );
                            }
                        }
                    } else if (s?.length == 1) {
                        val month = input.toIntOrNull()
                        if (month!=null && month > 1) {
                            et_card_expiration_value_in_payment_edit_card.setText(
                                "0" + et_card_expiration_value_in_payment_edit_card.getText()
                                    .toString() + "/"
                            );
                            et_card_expiration_value_in_payment_edit_card.setSelection(
                                et_card_expiration_value_in_payment_edit_card.getText().toString()
                                    .length
                            );
                        }
                    } else {

                    }
                    mLastInputForExpiration = et_card_expiration_value_in_payment_edit_card.getText().toString();
                    return;
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, start: Int, removed: Int, added: Int) {

            }
        })
    }

//    override fun confirmSetupIntentForMercadoPago() {
//        val card: com.stripe.android.model.Card? = card_multiline_widget_in_payment_add_credit_card.card
//        val cardToken = CardToken()
//        cardToken.cardNumber = card?.number
//        cardToken.expirationMonth = card?.expMonth
//        cardToken.expirationYear = card?.expYear
//        cardToken.securityCode = card?.cvc
//
//        val mercadoPagoServices: MercadoPagoServices = MercadoPagoServices.Builder()
//            .setContext(this)
//            .setPublicKey(presenter.mpPublicKey?.publicKey)
//            .build()
//
//        mercadoPagoServices.createToken(cardToken, object : com.mercadopago.lite.callbacks.Callback<Token>() {
//            override fun success(token: Token) {
//                //DONE!
//                presenter.mpCardToken = token.id
//                presenter.addMPCard()
//            }
//
//            override fun failure(apiException: ApiException?) {
//                //Manage API Failure
//                onCardAddFailure()
//            }
//        })
//
//
//    }


    override fun confirmSetupIntentForStripe() {

        if(presenter.setUpIntent!=null && presenter.setUpIntent?.client_secret!=null) {

            val billingDetails = PaymentMethod.BillingDetails.Builder()
                .setEmail(("").toString())
                .build()

            val paymentMethodCard = card_multiline_widget_in_payment_add_credit_card.paymentMethodCard

            if (paymentMethodCard != null) {
                val paymentMethodParams = PaymentMethodCreateParams
                    .create(paymentMethodCard, billingDetails, null)
                val confirmParams = ConfirmSetupIntentParams
                    .create(paymentMethodParams, presenter.setUpIntent?.client_secret!!)
                presenter.stripe.confirmSetupIntent(this, confirmParams)
            }

        }else{
            showError()
        }
    }

    override fun onCardAddSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CARD_DELETE_OPERATION && resultCode == RESULT_OK) {
            deleteCard()
        } else if (requestCode == REQUEST_GENERAL_ERROR) {
            finish()
        } else {
            presenter.stripe.onSetupResult(requestCode, data,
                object : ApiResultCallback<SetupIntentResult> {
                    override fun onSuccess(result: SetupIntentResult) {
                        // If confirmation and authentication succeeded,
                        // the SetupIntent will have user actions resolved;
                        // otherwise, handle the failure as appropriate
                        // (e.g. the customer may need to choose a new payment
                        // method)
                        val setupIntent = result.intent
                        val status = setupIntent.status
                        if (status == StripeIntent.Status.Succeeded) {
                            // show success UI
                            presenter.addCard(
                                setupIntent,
                                card_multiline_widget_in_payment_add_credit_card?.card!!
                            )
                        } else {
                            hideLoadingForAddPaymentCard()
                            showError()
                        }
                    }

                    override fun onError(e: Exception) {
                        // handle error
                        hideLoadingForAddPaymentCard()
//                        showError()
                    }
                })
        }
    }



    override fun onCardAddFailure() {
        hideLoadingForAddPaymentCard()
        showCardOperationPopUp(
            getString(R.string.error),
            getString(R.string.card_adding_error),
            getString(R.string.general_btn_ok),
            REQUEST_FAILURE_CARD_OPERATION
        )
    }




    override fun onCardInvalid() {

        showCardOperationPopUp(
            getString(R.string.error),
            getString(R.string.card_details_error),
            getString(R.string.general_btn_ok),
            REQUEST_FAILURE_CARD_OPERATION
        )
    }

    override fun onCardAlreadyExists() {
        showCardOperationPopUp(
            getString(R.string.error),
            getString(R.string.card_already_registered),
            getString(R.string.general_btn_ok),
            REQUEST_FAILURE_CARD_OPERATION
        )
    }

    private fun showCardOperationPopUp(
        title: String,
        subTitle: String,
        actionBtn: String,
        requestCode: Int,
        showCancelButton:Boolean = true
    ) {
        PopUpActivity.launchForResult(
            this, requestCode,
            title,
            subTitle, null, actionBtn,
            null,
            null,
            if(showCancelButton) getString(R.string.cancel_capital) else null
        )
    }

    fun deleteCard() {
        showLoadingForAddPaymentCard(getString(R.string.removing_payment_method_loader))
        presenter.deleteCard()
    }

    override fun onDeleteCardSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    override fun onDeleteCardFailure() {
        showError()
    }

    override fun onUpdateCardExpirationSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    override fun onUpdateCardExpirationFailure() {
        hideLoadingForAddPaymentCard()
        showServerGeneralError(REQUEST_CARD_EXPIRATION_UPDATE_ERROR)
    }

    override fun showError() {
        showServerGeneralError(REQUEST_GENERAL_ERROR)
    }

    //// loading :start
    override fun showLoadingForAddPaymentCard(message: String?) {
        add_payment_card_activity_loading_view.visibility = (View.VISIBLE)
        add_payment_card_activity_loading_view.ct_loading_title.text = (message)
    }

    override fun hideLoadingForAddPaymentCard() {
        add_payment_card_activity_loading_view.visibility = (View.GONE)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}