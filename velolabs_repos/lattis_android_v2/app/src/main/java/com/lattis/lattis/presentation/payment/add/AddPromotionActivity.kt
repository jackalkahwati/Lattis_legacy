package com.lattis.lattis.presentation.payment.add

import android.content.Intent
import android.view.View
import com.jakewharton.rxbinding4.widget.TextViewTextChangeEvent
import com.jakewharton.rxbinding4.widget.textChangeEvents
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_add_promotion.*
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_payment.*
import javax.inject.Inject

class AddPromotionActivity : BaseActivity<AddPromotionActivityPresenter, AddPromotionActivityView>(),
    AddPromotionActivityView {

    @Inject
    override lateinit var presenter: AddPromotionActivityPresenter
    override val activityLayoutId = R.layout.activity_add_promotion
    override var view: AddPromotionActivityView = this
    private val REQUEST_CODE_ADD_PROMOE_ERROR = 903

    override fun configureViews() {
        super.configureViews()
        configureClicks()
    }

    override fun configureSubscriptions() {
        super.configureSubscriptions()
        subscriptions.add(
            et_promo_code_in_add_promotion.textChangeEvents()
            .subscribe { textViewTextChangeEvent: TextViewTextChangeEvent ->
                presenter.setPromoCode(textViewTextChangeEvent.text.toString())
            }
        )
    }

    fun configureClicks(){
        btn_add_promo_code_in_add_promotion.setOnClickListener {
            addPromoCode()
        }

        iv_close_in_add_promotion.setOnClickListener {
            finish()
        }
    }

    fun addPromoCode(){
        showLoadingForAddPromotion(getString(R.string.loading))
        presenter.addPromoCode()
    }

    override fun onAddPromotionSuccess() {
        val intent = Intent()
        setResult(RESULT_OK,intent)
        finish()
    }

    override fun onAddPromotionFailure() {
        hideLoadingForAddPromotion()
        launchPopUpActivity(
            REQUEST_CODE_ADD_PROMOE_ERROR,
            getString(R.string.notice),
            getString(R.string.invalid_promo_code),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

    //// loading :start
    fun showLoadingForAddPromotion(message: String?) {
        add_promotion_activity_loading_view.visibility = (View.VISIBLE)
        add_promotion_activity_loading_view.ct_loading_title.text = (message)
    }

    fun hideLoadingForAddPromotion() {
        add_promotion_activity_loading_view.visibility = (View.GONE)
    }


}