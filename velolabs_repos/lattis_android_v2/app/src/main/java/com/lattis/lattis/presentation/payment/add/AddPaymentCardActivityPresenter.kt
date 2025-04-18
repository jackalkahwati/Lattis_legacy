package com.lattis.lattis.presentation.payment.add

import android.os.Bundle
import android.text.TextUtils
import com.google.gson.Gson
import com.lattis.domain.models.SetUpIntent
import com.lattis.domain.models.Card
import com.lattis.domain.models.MPPublicKey
import com.lattis.domain.usecase.card.*
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity.Companion.CARD_DETAILS
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity.Companion.FLEET_ID
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity.Companion.HAS_OTHER_CARD
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity.Companion.PAYMENT_VENDOR
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.FirebaseUtil
import com.stripe.android.Stripe
import com.stripe.android.model.SetupIntent
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named

class AddPaymentCardActivityPresenter @Inject constructor(
    val addCardUseCase: AddCardUseCase,
    val updateCardExpirationUseCase: UpdateCardExpirationUseCase,
    val deleteCardUseCase: DeleteCardUseCase,
    val stripe: Stripe,
    val getSetUpIntentUseCase: GetSetUpIntentUseCase,
    val getMPPublicKeyUseCase: GetMPPublicKeyUseCase,
    val addMPCardUseCase: AddMPCardUseCase,
    @param:Named("Force-Payment-Method")private val forcePaymentMethod:String?,
    @param:Named("Force-Payment-Fleet-Id")private val forcePaymentFleetId:Int?
) : ActivityPresenter<AddPaymentCardActivityView>(){

    var setUpIntent: SetUpIntent? = null
    var card:Card?=null
    var fleet_id:Int?=null
    var selectedPaymentVendor:PaymentVendors= PaymentVendors.STRIPE
    var mpPublicKey:MPPublicKey?=null
    var mpCardToken:String?=null
    var hasOtherCard = false


    enum class PaymentVendors{
        STRIPE,
        MERCADOPAGO
    }

    fun startAddingCard(){
        if(selectedPaymentVendor==PaymentVendors.MERCADOPAGO){
//            view?.confirmSetupIntentForMercadoPago()
            FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.SAVE_CREDIT_CARD, FirebaseUtil.MERCADOPEGO)
        }else{
            view?.confirmSetupIntentForStripe()
            FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.SAVE_CREDIT_CARD, FirebaseUtil.STRIPE)
        }
    }

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(CARD_DETAILS)) {
            this.card = Gson().fromJson(
                arguments.getString(CARD_DETAILS)
                , Card::class.java
            )
            hasOtherCard = if(arguments.containsKey(HAS_OTHER_CARD)) arguments.getBoolean(HAS_OTHER_CARD) else false

            view?.showEditCardView()
        } else {
            fetchSetUpIntent()
        }


        if(forcePaymentFleetId!=null) {
            fleet_id = forcePaymentFleetId
        }else if (arguments != null && arguments.containsKey(FLEET_ID)) {
            fleet_id = arguments.getInt(FLEET_ID)
        }

        var vendor:String? = null
        if(forcePaymentMethod!=null) {
            vendor = forcePaymentMethod
        }else if (arguments != null && arguments.containsKey(PAYMENT_VENDOR)) {
            vendor = arguments.getString(PAYMENT_VENDOR)
        }

        if(!TextUtils.isEmpty(vendor)) {
            when (vendor?.toLowerCase()) {
                "mercadopago" -> selectedPaymentVendor = PaymentVendors.MERCADOPAGO
                "stripe" -> selectedPaymentVendor = PaymentVendors.STRIPE
            }
        }


        if(selectedPaymentVendor == PaymentVendors.MERCADOPAGO && fleet_id!=null){
            getMPPublicKey()
        }
    }


    //// add new card :start

    fun fetchSetUpIntent() {
        subscriptions.add(getSetUpIntentUseCase.execute(object :
            RxObserver<SetUpIntent>() {
            override fun onNext(setUpIntentRes: SetUpIntent) {
                super.onNext(setUpIntentRes)
                setUpIntent= setUpIntentRes
            }

            override fun onError(e: Throwable) {
                super.onError(e!!)
            }
        }))
    }

    fun addCard(setupIntent: SetupIntent, addNewCard:com.stripe.android.model.Card) {
        subscriptions.add(
            addCardUseCase
                .withCardNumber(addNewCard!!.number)
                .withExpiryMonth(addNewCard!!.expMonth!!)
                .withExpiryYear(addNewCard!!.expYear!!)
                .withCVC(addNewCard!!.cvc!!)
                .withIntent(getSetupIntent(setupIntent!!))
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        if (status) {
                            FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.ADD_CREDIT_CARD, FirebaseUtil.STRIPE)
                            view?.onCardAddSuccess()
                        } else {
                            view?.onCardAddFailure()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.hideLoadingForAddPaymentCard()
                        if (e is HttpException) {
                            val exception =
                                e
                            if (exception.code() == 410) {
                                view?.onCardAlreadyExists()
                            } else if (exception.code() == 409) {
                                view?.onCardInvalid()
                            } else {
                                view?.onCardAddFailure()
                            }
                        } else {
                            view?.onCardAddFailure()
                        }
                    }
                })
        )
    }

    fun updateCardExpiration(expMonth:Int,expYear:Int) {
        subscriptions.add(
            updateCardExpirationUseCase
                .withCardId(card?.card_id!!)
                .withExpiryMonth(expMonth)
                .withExpiryYear(expYear)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        view?.onUpdateCardExpirationSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onUpdateCardExpirationFailure()
                    }
                })
        )
    }


    fun getSetupIntent(setupIntent: SetupIntent): JSONObject? {
        try {
            val intent = JSONObject()
            intent.put("id", setupIntent.id)
            intent.put("object", "setup_intent")
            intent.put("cancellation_reason", setupIntent.cancellationReason)
            intent.put("client_secret", setupIntent.clientSecret)
            intent.put("created", setupIntent.created)
            intent.put("description", setupIntent.description)
            intent.put("last_setup_error", setupIntent.lastSetupError)
            intent.put("livemode", setupIntent.isLiveMode)
            intent.put("next_action", setupIntent.nextActionType)
            intent.put("payment_method", setupIntent.paymentMethodId)
            if (setupIntent.paymentMethodTypes != null && setupIntent.paymentMethodTypes.size > 0) {
                val jsonArray = JSONArray()
                for (methodTypes in setupIntent.paymentMethodTypes) {
                    jsonArray.put(methodTypes)
                }
                intent.put("payment_method_types", jsonArray)
            }
            intent.put("status", setupIntent.status)
            intent.put("usage", setupIntent.usage)
            return intent
        } catch (e: JSONException) {
        }
        return null
    }

    //// add new card :end



    //// delete card :start
    open fun deleteCard(): Unit {
        subscriptions.add(
            deleteCardUseCase
                .setCardId(card?.id!!)
                .execute(object : RxObserver<Boolean>() {
                    override fun onNext(status:Boolean) {
                        super.onNext(status)
                        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.DELETE_CREDIT_CARD, FirebaseUtil.DELETE_CREDIT_CARD)
                        view?.onDeleteCardSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onDeleteCardFailure()
                    }
                })
        )
    }
    //// delete card :end


    //// fetch MP Public key
    fun getMPPublicKey() {
        subscriptions.add(getMPPublicKeyUseCase
            .withFleetId(fleet_id!!)
            .execute(object :
            RxObserver<MPPublicKey>() {
            override fun onNext(newMPPublicKey: MPPublicKey) {
                super.onNext(newMPPublicKey)
                mpPublicKey = newMPPublicKey
            }

            override fun onError(e: Throwable) {
                super.onError(e!!)
            }
        }))
    }

    fun addMPCard() {
        subscriptions.add(addMPCardUseCase
            .withToken(mpCardToken!!)
            .withFleetId(fleet_id!!)
            .execute(object :
                RxObserver<Boolean>() {
                override fun onNext(status:Boolean) {
                    super.onNext(status)
                    FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.ADD_CREDIT_CARD, FirebaseUtil.MERCADOPEGO)
                    view?.onCardAddSuccess()
                }

                override fun onError(e: Throwable) {
                    super.onError(e!!)
                    view?.onCardAddFailure()
                }
            }))
    }

    fun processDeleteAction(){
        if(card!=null && card?.is_primary!!){
            view?.showPrimaryCardDeleteError()
        }else if(!hasOtherCard){
            view?.showSingleCardDeleteError()
        }else{
            view?.showDeleteCardWarning()
        }
    }


}