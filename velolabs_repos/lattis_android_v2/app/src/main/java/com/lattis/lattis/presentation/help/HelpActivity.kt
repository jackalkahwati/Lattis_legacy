package com.lattis.lattis.presentation.help

import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import com.lattis.lattis.presentation.help.SliderImageHelper.imageSliderApplies
import com.lattis.lattis.presentation.help.SliderImageHelper.openSliderImage
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import com.lattis.lattis.presentation.utils.FirebaseUtil
import com.lattis.lattis.presentation.webview.WebviewActivity
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_help.*
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.layout_image_slider_parent.view.*
import javax.inject.Inject

class HelpActivity : BaseActivity<HelpActivityPresenter, HelpActivityView>(),
    HelpActivityView {

    private val REQUEST_CODE_FAQ = 4394

    @Inject
    override lateinit var presenter: HelpActivityPresenter
    override val activityLayoutId = R.layout.activity_help
    override var view: HelpActivityView = this


    override fun configureViews() {
        super.configureViews()

        showLoadingForAddPromotion(getString(R.string.loading))

        if(imageSliderApplies()){
            btn_open_image_slider.visibility= View.VISIBLE
        }else{
            btn_open_image_slider.visibility = View.GONE
        }

        presenter.getHelpInfo()

        iv_close_in_help.setOnClickListener {
            finish()
        }



        ct_phone_number_label_help.setOnClickListener {
            startCalling()
        }

        ct_phone_number_value_help.setOnClickListener {
            startCalling()
        }


        ct_faq_label_help.setOnClickListener {
            openLink()
        }
        ct_faq_value_help.setOnClickListener {
            openLink()
        }


        btn_open_image_slider.setOnClickListener {
            openSliderImage(this, help_image_slider_parent)
        }

        help_image_slider_parent.ct_skip.setOnClickListener {
            help_image_slider_parent.visibility = View.GONE
        }

        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.HELP, FirebaseUtil.HELP)
    }

    fun startCalling(){
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + presenter.phoneNumber)))
        finish()
    }

    fun openLink(){
        WebviewActivity.launchForResult(
            this,
            REQUEST_CODE_FAQ, presenter.web_link
        )
    }

    override fun showPhoneNumber(phoneNumber: String) {
        ct_phone_number_label_help.visibility = View.VISIBLE
        ct_phone_number_value_help.visibility = View.VISIBLE

        ct_phone_number_value_help.text = phoneNumber
    }

    override fun showEmail(email: String) {
        ct_email_value_help.visibility = View.VISIBLE
        ct_email_label_help.visibility = View.VISIBLE

        ct_email_value_help.text = email
    }

    override fun showFaq(faq: String) {
        ct_faq_label_help.visibility = View.VISIBLE
        ct_faq_value_help.visibility = View.VISIBLE

        val content = SpannableString(faq)
        content.setSpan(UnderlineSpan(), 0, faq.length, 0)
        ct_faq_value_help.text = content
    }

    //// loading :start
    fun showLoadingForAddPromotion(message: String?) {
        help_activity_loading_view.visibility = (View.VISIBLE)
        help_activity_loading_view.ct_loading_title.text = (message)
    }

    override fun hideLoadingForAddPromotion() {
        help_activity_loading_view.visibility = (View.GONE)
    }



    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }

}