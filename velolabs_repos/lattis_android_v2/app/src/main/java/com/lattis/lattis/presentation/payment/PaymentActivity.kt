package com.lattis.lattis.presentation.payment

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.lattis.domain.models.Card
import com.lattis.lattis.presentation.fleet.PrivateFleetActivity
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity
import com.lattis.lattis.presentation.payment.add.AddPromotionActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_payment.*
import kotlinx.android.synthetic.main.activity_payment_card_list.*
import kotlinx.android.synthetic.main.activity_payment_no_card.*
import kotlinx.android.synthetic.main.activity_payment_promotion.view.*
import javax.inject.Inject

class PaymentActivity : BaseActivity<PaymentActivityPresenter, PaymentActivityView>(),
    PaymentActivityView, PaymentCardClickListener {


    private val REQUEST_GENERAL_ERROR = 4393
    private val REQUEST_ADD_PAYMENT_CARD = 4394
    private val REQUEST_CODE_ADD_PROMOTION_ACTIVITY = 4395
    private val REQUEST_CANNOT_DELETE_PAYMENT_CARD = 4396

    private val NO_CARD =0
    private val CARD_LIST = 1


    @Inject
    override lateinit var presenter: PaymentActivityPresenter
    override val activityLayoutId = R.layout.activity_payment
    override var view: PaymentActivityView = this


    override fun configureViews() {
        super.configureViews()
        configureClicks()
        presenter.promotions()
        fetchCardList()
    }

    fun configureClicks(){
        iv_close_in_payment_no_card.setOnClickListener {
            finish()
        }

        iv_close_in_payment_card_list.setOnClickListener {
            finish()
        }

        btn_add_credit_card_in_payment_no_card.setOnClickListener {
            AddPaymentCardActivity.launchForResult(this,REQUEST_ADD_PAYMENT_CARD,null)
        }

        btn_add_credit_card_in_payment_card_list.setOnClickListener {
            AddPaymentCardActivity.launchForResult(this,REQUEST_ADD_PAYMENT_CARD,null)
        }

        payment_promotion_in_payment_card_list.ct_add_promo_code_in_payment_promotion.setOnClickListener {
            openAddPromotionActivity()
        }

        payment_promotion_in_payment_no_card.ct_add_promo_code_in_payment_promotion.setOnClickListener {
            openAddPromotionActivity()
        }



    }

    fun fetchCardList(){
        showLoadingForPayment(getString(R.string.loading))
        presenter.getCards()
    }

    fun showNoCardView(){
        if(view_flipper_in_payment.displayedChild != NO_CARD)
            view_flipper_in_payment.displayedChild = NO_CARD

    }

    fun showCardListView() {
        if (view_flipper_in_payment.displayedChild != CARD_LIST)
            view_flipper_in_payment.displayedChild = CARD_LIST

        rv_cards_in_payment_card_list.setLayoutManager(LinearLayoutManager(this))
        rv_cards_in_payment_card_list.setAdapter(UserCardListAdapter(this, presenter.cards, true, this))
    }


    override fun onUpdateCardFailure() {
        showError()
    }



    //// override for card click :start
    override fun onCardCheckBoxClicked(position: Int) {
        showLoadingForPayment(getString(R.string.credit_card_selecting_loader))
        presenter.updateCard(presenter.cards?.get(position)!!.id)
    }

    override fun onCardClicked(position: Int) {
        AddPaymentCardActivity.launchForResult(
            this,
            REQUEST_ADD_PAYMENT_CARD,
            presenter.cards?.get(position)!!,
            null,
            null,
            presenter.cards?.size!!>1
        )
    }

    //// override for get card :start
    override fun onCardListSuccess(cards: List<Card>) {
        showCardListView()
    }

    override fun onNoCardSuccess() {
        showNoCardView()
    }

    override fun onGetCardFailure() {
        showError()
    }

    //// loading :start
    override fun showLoadingForPayment(message: String?) {
        payment_activity_loading_view.visibility = (View.VISIBLE)
        payment_activity_loading_view.ct_loading_title.text = (message)
    }

    override fun hideLoadingForPayment() {
        payment_activity_loading_view.visibility = (View.GONE)
    }


    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    fun showError() {
        showServerGeneralError(REQUEST_GENERAL_ERROR)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_ADD_PAYMENT_CARD && resultCode == Activity.RESULT_OK){
            fetchCardList()
        }else if(requestCode == REQUEST_CODE_ADD_PROMOTION_ACTIVITY && resultCode == Activity.RESULT_OK){
            presenter.promotions()
        }
    }


    fun openAddPromotionActivity(){
        startActivityForResult(
            Intent(this, AddPromotionActivity::class.java),
            REQUEST_CODE_ADD_PROMOTION_ACTIVITY
        )
    }

    override fun onPromotionsSuccess() {
        payment_promotion_in_payment_card_list.rv_promotions_in_payment_promotion.setLayoutManager(LinearLayoutManager(this))
        payment_promotion_in_payment_card_list.rv_promotions_in_payment_promotion.setAdapter(PromotionListAdapter(this, presenter.promotions))


        payment_promotion_in_payment_no_card.rv_promotions_in_payment_promotion.setLayoutManager(LinearLayoutManager(this))
        payment_promotion_in_payment_no_card.rv_promotions_in_payment_promotion.setAdapter(PromotionListAdapter(this, presenter.promotions))

    }

    override fun onPromotionsFailure() {

    }
}