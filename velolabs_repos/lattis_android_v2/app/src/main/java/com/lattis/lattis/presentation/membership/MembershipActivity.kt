package com.lattis.lattis.presentation.membership

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.lattis.domain.models.Membership
import com.lattis.lattis.presentation.bikelist.BikeListFragment
import com.lattis.lattis.presentation.payment.PaymentActivity
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import com.lattis.lattis.presentation.utils.CurrencyUtil
import com.lattis.lattis.presentation.utils.LocaleTranslatorUtils
import com.lattis.lattis.presentation.webview.WebviewActivity
import com.lattis.lattis.utils.UtilsHelper
import com.lattis.lattis.utils.UtilsHelper.isDate2GreaterThanDate1
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_membership.*
import kotlinx.android.synthetic.main.activity_membership_create.*
import kotlinx.android.synthetic.main.activity_membership_edit.*
import kotlinx.android.synthetic.main.activity_membership_list.*
import kotlinx.android.synthetic.main.activity_membership_main_list.*
import kotlinx.android.synthetic.main.activity_private_fleets_fleet_list.*
import kotlinx.android.synthetic.main.activity_reservation_create.*
import kotlinx.android.synthetic.main.activity_reservation_list_or_create.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_confirm_reserve.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_confirm_reserve.view.*
import java.text.SimpleDateFormat
import javax.inject.Inject

class MembershipActivity : BaseActivity<MembershipActivityPresenter, MembershipActivityView>(),
    MembershipActivityView , MembershipActionListener{


    private val REQUEST_CODE_ERROR = 4393
    private val REQUEST_ADD_PAYMENT_CARD = 4394
    private val REQUEST_CODE_ALREADY_MEMBERSHIP_EXISTS = 4395
    private val REQUEST_CODE_CONFIRM_UNSUBSCRIBE = 4396
    private val REQUEST_CODE_UNSUBSCRIBE_SUCCESS = 4397
    private val REQUEST_CODE_TERMS_AND_CONDITION = 4398
    private val REQUEST_CODE_PAYMENT_CARD = 4399
    private var membershipList1Adapter:MembershipListAdapter?=null
    private var membershipListOnlyAdapter:MembershipListAdapter?=null
    private var subscriptionListAdapter:MembershipListAdapter?=null
    private val MEMBERSHIP_LIST =0
    private val MEMBERSHIP_CREATE =1
    private val MEMBERSHIP_EDIT =2
    private val MEMBERSHIP_LIST_ONLY =3
    private var membershipCreateShownFromMainList = true

    @Inject
    override lateinit var presenter: MembershipActivityPresenter
    override val activityLayoutId = R.layout.activity_membership
    override var view: MembershipActivityView = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoadingForMembership(getString(R.string.loading))
        presenter.getMembershipsAndSubscriptions()
        presenter.getCards()
    }

    override fun configureViews() {
        super.configureViews()

        val searchIcon = sv_in_membership_list_only.findViewById<ImageView>(R.id.search_mag_icon)
        searchIcon.setColorFilter(Color.BLACK)

        val cancelIcon = sv_in_membership_list_only.findViewById<ImageView>(R.id.search_close_btn)
        cancelIcon.setColorFilter(Color.BLACK)

        //Turn iconified to false:
        sv_in_membership_list_only.setIconified(false);
        //The above line will expand it to fit the area as well as throw up the keyboard

        //To remove the keyboard, but make sure you keep the expanded version:
        sv_in_membership_list_only.clearFocus();

        configureClicks()

    }


    fun configureClicks(){
        iv_close_in_membership_list.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent())
            finish()
        }

        iv_close_in_membership_create.setOnClickListener {
            if(membershipCreateShownFromMainList)showMembershipAndSubscriptionList() else showMembershipListOnly()
        }

        btn_confirm_in_membership_create.setOnClickListener {
            if(presenter.alreadySubscribedToFleet()){
                showAlreadyMembershipExistForThisFleet()
            }else{
                subscribe()
            }

        }

        btn_unsubcribe_in_membership_edit.setOnClickListener {
            confirmUnSubscribe()
        }

        iv_close_in_membership_edit.setOnClickListener {
            showMembershipAndSubscriptionList()
        }

        ct_show_all_your_membership_in_membership_list.setOnClickListener {
            var showOnlyOne = false
            if(ct_show_all_your_membership_in_membership_list.text.equals(getString(R.string.show_all))) {
                ct_show_all_your_membership_in_membership_list.text = getString(R.string.hide)
                showOnlyOne=false
            }else{
                ct_show_all_your_membership_in_membership_list.text = getString(R.string.show_all)
                showOnlyOne=true
            }

            subscriptionListAdapter = MembershipListAdapter(
                this,
                presenter.memberships?.subscriptions?.map { it.fleet_membership!! }!!,
                true,
                showOnlyOne,
                this
            )
            rv_subscriptions.adapter = subscriptionListAdapter
        }

        btn_add_credit_card_in_membership_create.setOnClickListener {
            launchPaymentActivity()
        }

        cl_payment_in_membership_create.setOnClickListener {
            launchPaymentActivity()
        }

        iv_payment_next_in_membership_edit.setOnClickListener {
            launchPaymentActivity()
        }

        iv_search_memberships_in_membership_list.setOnClickListener {
            showMembershipListOnly()
        }

        iv_close_in_membership_list_only.setOnClickListener {
            showMembershipAndSubscriptionList()
        }

        ct_terms_condition_in_membership_create.setOnClickListener {
            showMembershipTAndC()
        }
    }


    fun showMembershipAndSubscriptionList(){
        membershipCreateShownFromMainList = true

        ct_title_your_membership_in_membership_list.text = getString(R.string.your_memberships)+ " (" + presenter.memberships?.subscriptions?.size!! + ")"

        rv_subscriptions.setLayoutManager(LinearLayoutManager(this))
        rv_memberships.setLayoutManager(LinearLayoutManager(this))

        if(presenter.memberships?.subscriptions?.size!! == 0){
            rv_subscriptions.visibility=View.GONE
            ct_show_all_your_membership_in_membership_list.visibility=View.GONE
        }else{
            rv_subscriptions.visibility=View.VISIBLE
            ct_show_all_your_membership_in_membership_list.visibility=View.VISIBLE
            ct_show_all_your_membership_in_membership_list.text = getString(R.string.show_all)
            subscriptionListAdapter = MembershipListAdapter(this, presenter.memberships?.subscriptions?.map { it.fleet_membership!! }!!,true,true,this)
            rv_subscriptions.adapter = subscriptionListAdapter
        }

        if(presenter.memberships?.memberships?.size!! == 0){
            rv_memberships.visibility=View.GONE
            iv_search_memberships_in_membership_list.visibility=View.GONE
        }else {
            membershipList1Adapter = MembershipListAdapter(this,presenter.memberships?.memberships!!,false,false,this)
            rv_memberships.adapter = membershipList1Adapter
        }


        if (view_flipper_in_membership.displayedChild != MEMBERSHIP_LIST)
            view_flipper_in_membership.displayedChild = MEMBERSHIP_LIST
    }


    fun showMembershipCreate(){
        if (view_flipper_in_membership.displayedChild != MEMBERSHIP_CREATE)
            view_flipper_in_membership.displayedChild = MEMBERSHIP_CREATE

        if(presenter.primaryUserCard==null){
            cl_payment_in_membership_create.visibility= View.GONE
            btn_confirm_in_membership_create.visibility= View.GONE
            ct_terms_condition_in_membership_create.visibility= View.GONE
            btn_add_credit_card_in_membership_create.visibility = View.VISIBLE
        }else{
            cl_payment_in_membership_create.visibility= View.VISIBLE
            btn_confirm_in_membership_create.visibility= View.VISIBLE

            if(presenter.selectedMembership!=null && presenter.selectedMembership?.fleet?.t_and_c!=null){
                ct_terms_condition_in_membership_create.visibility= View.VISIBLE
                ct_terms_condition_in_membership_create.text = HtmlCompat.fromHtml(getString(R.string.bike_details_terms_policy, presenter.selectedMembership?.fleet?.t_and_c), HtmlCompat.FROM_HTML_MODE_LEGACY)
            }else{
                ct_terms_condition_in_membership_create.visibility= View.GONE
            }

            btn_add_credit_card_in_membership_create.visibility = View.GONE
            ct_credit_card_number_in_membership_create.setText("XXXX-" + presenter.primaryUserCard?.cc_no!!.substring(presenter.primaryUserCard?.cc_no!!.length - 4))
        }

        ct_membership_name_in_membership_create.text = presenter.selectedMembership?.fleet?.fleet_name
        ct_for_just_in_membership_create.text = getString(R.string.membership_price,CurrencyUtil.getCurrencySymbolByCode(presenter.selectedMembership?.membership_price_currency!!,presenter.selectedMembership?.membership_price!!
            .toString()),LocaleTranslatorUtils.getLocaleString(
            this,
            presenter.selectedMembership?.payment_frequency))
        ct_savings_in_membership_create.text = getString(R.string.membership_perk,presenter.selectedMembership?.membership_incentive!!)

    }


    fun showMembershipEdit(){
        if (view_flipper_in_membership.displayedChild != MEMBERSHIP_EDIT)
            view_flipper_in_membership.displayedChild = MEMBERSHIP_EDIT

        ct_membership_name_in_membership_edit.text = presenter.selectedSubscription?.fleet_membership?.fleet?.fleet_name

        ct_membership_billing_in_membership_edit.text = LocaleTranslatorUtils.getLocaleString(
            this,
            presenter.selectedSubscription?.fleet_membership?.payment_frequency)

        ct_membership_charges_in_membership_edit.text = CurrencyUtil.getCurrencySymbolByCode(presenter.selectedSubscription?.fleet_membership?.membership_price_currency!!,presenter.selectedSubscription?.fleet_membership?.membership_price!!
            .toString())+ "/" + LocaleTranslatorUtils.getLocaleString(
        this,
        presenter.selectedSubscription?.fleet_membership?.payment_frequency)


        ct_membership_perk_in_membership_edit.text = getString(R.string.perk_template,presenter.selectedSubscription?.fleet_membership?.membership_incentive!!)


        if(presenter.primaryUserCard!=null) {
            cl_payment_in_membership_edit.visibility = View.VISIBLE
            ct_membership_payment_in_membership_edit.setText(
                "XXXX-" + presenter.primaryUserCard?.cc_no!!.substring(
                    presenter.primaryUserCard?.cc_no!!.length - 4
                )
            )
        }else{
            cl_payment_in_membership_edit.visibility = View.VISIBLE
        }

        if(presenter.selectedSubscription?.fleet_membership?.membership_subscription_payments!=null &&
                presenter.selectedSubscription?.fleet_membership?.membership_subscription_payments?.size!!>0){



            var latestMembershipSubscriptionPayment : Membership.MembershipSubscriptionPayment?=null
            for(membershipSubscriptionPayment in presenter.selectedSubscription?.fleet_membership?.membership_subscription_payments!!){
                if(latestMembershipSubscriptionPayment == null || isDate2GreaterThanDate1(latestMembershipSubscriptionPayment.period_start,membershipSubscriptionPayment?.period_start)){
                    latestMembershipSubscriptionPayment = membershipSubscriptionPayment
                }
            }


            ct_membership_start_date_in_membership_edit.text =
                UtilsHelper.getDateOnlyWithYear(
                    UtilsHelper.dateFromUTC(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(presenter.selectedSubscription?.activation_date)
                    )!!
                )

            if(latestMembershipSubscriptionPayment!=null) {

                ct_membership_last_payment_in_membership_edit.text =
                    UtilsHelper.getDateOnlyWithYear(
                        UtilsHelper.dateFromUTC(
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(latestMembershipSubscriptionPayment?.paid_on)
                        )!!
                    )

                ct_membership_next_payment_in_membership_edit.text =
                    UtilsHelper.getDateOnlyWithYear(
                        UtilsHelper.dateFromUTC(
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(latestMembershipSubscriptionPayment?.period_end)
                        )!!
                    )

            }else{
                ct_membership_start_date_in_membership_edit.text = "-"
                ct_membership_end_date_in_membership_edit.text = "-"
                ct_membership_last_payment_in_membership_edit.text = "-"
                ct_membership_next_payment_in_membership_edit.text = "-"
            }

            if(!TextUtils.isEmpty(presenter.selectedSubscription?.deactivation_date)){

                if(latestMembershipSubscriptionPayment!=null) {
                    cl_end_date_in_membership_edit.visibility = View.VISIBLE
                    ct_membership_end_date_in_membership_edit.text =
                        UtilsHelper.getDateOnlyWithYear(
                            UtilsHelper.dateFromUTC(
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(latestMembershipSubscriptionPayment?.period_end)
                            )!!
                        )
                }else{
                    cl_end_date_in_membership_edit.visibility = View.GONE
                }

                ct_membership_next_payment_in_membership_edit.text = "-"
                btn_unsubcribe_in_membership_edit.visibility = View.GONE
            }else{
                cl_end_date_in_membership_edit.visibility = View.GONE
                btn_unsubcribe_in_membership_edit.visibility = View.VISIBLE
            }

        }
    }

    fun showMembershipListOnly(){

        membershipCreateShownFromMainList = false
        rv_memberships_in_membership_only.setLayoutManager(LinearLayoutManager(this))

        membershipListOnlyAdapter = MembershipListAdapter(this,presenter.memberships?.memberships!!,false,false,this)
        rv_memberships_in_membership_only.adapter = membershipListOnlyAdapter

        sv_in_membership_list_only.setOnQueryTextListener(
            object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    membershipListOnlyAdapter?.filter?.filter(newText)
                    return false
                }

            }
        )

        if (view_flipper_in_membership.displayedChild != MEMBERSHIP_LIST_ONLY)
            view_flipper_in_membership.displayedChild = MEMBERSHIP_LIST_ONLY
    }

    override fun onMembershipsSuccess() {
        showMembershipAndSubscriptionList()
        hideLoadingForMembership()
    }

    override fun onMembershipsFailure() {
        handleApiFailure()
    }

    override fun onMembershipSubscribeFailure() {
        handleApiFailure()
    }

    override fun onMembershipUnSubscribeFailure() {
        handleApiFailure()
    }

    fun handleApiFailure(){
        hideLoadingForMembership()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun onMembershipClicked(position:Int, alreadyMember: Boolean) {
        if(alreadyMember) {
            presenter.selectedMembership=null
            presenter.selectedSubscription = presenter.memberships?.subscriptions?.get(position)
            showMembershipEdit()
        } else {
            presenter.selectedSubscription=null
            presenter.selectedMembership=presenter.memberships?.memberships?.get(position)
            showMembershipCreate()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ADD_PAYMENT_CARD  && resultCode == Activity.RESULT_OK){
            presenter.getCards()
        }else if(requestCode == REQUEST_CODE_CONFIRM_UNSUBSCRIBE && data != null &&
            data.hasExtra(PopUpActivity.POSITIVE_LEVEL)){
            unsubscribe()
        }else if(requestCode == REQUEST_CODE_PAYMENT_CARD){
            presenter.getCards()
        }
    }

    override fun onCardListSuccess() {
        if(view_flipper_in_membership.displayedChild == MEMBERSHIP_CREATE){
            showMembershipCreate()
        }else if(view_flipper_in_membership.displayedChild == MEMBERSHIP_EDIT){
            showMembershipEdit()
        }
    }

    fun launchPaymentActivity(){
        startActivityForResult(
            Intent(this, PaymentActivity::class.java),
            REQUEST_CODE_PAYMENT_CARD
        )
    }

    fun launchAddPaymentCardActivity(){
        AddPaymentCardActivity.launchForResult(this,
            REQUEST_ADD_PAYMENT_CARD,null)
    }

    fun showAlreadyMembershipExistForThisFleet(){
        launchPopUpActivity(
            REQUEST_CODE_ALREADY_MEMBERSHIP_EXISTS,
            getString(R.string.general_error_title),
            getString(R.string.duplicated_membership_alert),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }

    fun confirmUnSubscribe(){
        launchPopUpActivity(
            REQUEST_CODE_CONFIRM_UNSUBSCRIBE,
            null,
            getString(R.string.cancel_membership_confirmation),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            getString(R.string.cancel)
        )
    }

    override fun onMembershipUnSubscribeSuccess(){
        launchPopUpActivity(
            REQUEST_CODE_UNSUBSCRIBE_SUCCESS,
            null,
            getString(R.string.cancel_membership_success),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }

    fun subscribe(){
        showLoadingForMembership(getString(R.string.subscribing))
        presenter.subscribe()
    }

    fun unsubscribe(){
        showLoadingForMembership(getString(R.string.cancelling_subscription))
        presenter.unsubscribe()
    }

    fun showMembershipTAndC(){
        if(presenter.selectedMembership?.fleet!=null && !TextUtils.isEmpty(presenter.selectedMembership?.fleet?.t_and_c)) {
            WebviewActivity.launchForResult(
                this,
                REQUEST_CODE_TERMS_AND_CONDITION, presenter.selectedMembership?.fleet?.t_and_c
            )
        }
    }

    fun showLoadingForMembership(message:String){
        membership_activity_loading_view.ct_loading_title.text = message
        membership_activity_loading_view.visibility = View.VISIBLE
    }

    fun hideLoadingForMembership() {
        membership_activity_loading_view.visibility= View.GONE
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}