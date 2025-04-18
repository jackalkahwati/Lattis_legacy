package com.lattis.lattis.presentation.reservation.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lattis.domain.models.Reservation
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Card
import com.lattis.domain.models.Location
import com.lattis.lattis.presentation.base.activity.location.BaseLocationWithoutDrawerActivity
import com.lattis.lattis.presentation.bikelist.BikeListFragment
import com.lattis.lattis.presentation.payment.PaymentActivity
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.qrscan.ScanBikeQRCodeActivity
import com.lattis.lattis.presentation.utils.CurrencyUtil
import com.lattis.lattis.presentation.utils.IsRidePaid
import com.lattis.lattis.presentation.utils.LocaleTranslatorUtils
import com.lattis.lattis.utils.ResourceHelper
import com.lattis.lattis.utils.UtilsHelper.dateFromUTC
import com.lattis.lattis.utils.UtilsHelper.getDateTimeWithAtLabel
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_reservation_edit.*
import kotlinx.android.synthetic.main.activity_reservation_edit.ct_add_credit_card_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.ct_bike_parking_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.ct_bike_parking_value_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.ct_bike_payment_value_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.ct_bike_price_value_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.ct_parking_description_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.ct_surcharge_description_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.ct_surcharge_price_label_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.ct_surcharge_price_value_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.ct_unlock_price_label_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.ct_unlock_price_value_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.iv_add_credit_card_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_edit.iv_bike_payment_img_in_confirm_reserve
import kotlinx.android.synthetic.main.fragment_bikelist_with_confirm_reserve.*
import java.text.SimpleDateFormat
import javax.inject.Inject

class ReservationEditActivity : BaseLocationWithoutDrawerActivity<ReservationEditActivityPresenter, ReservationEditActivityView>(),
    ReservationEditActivityView {


    private val REQUEST_CODE_ERROR = 4393
    private val REQUEST_CODE_CANCEL_CONFIRMATION = 4394
    private val PRIMARY_CARD_ERROR_REQUEST_CODE = 4395
    private val MAKE_CARD_PRIMARY_REQUEST_CODE = 4396

    @Inject
    override lateinit var presenter: ReservationEditActivityPresenter
    override val activityLayoutId = R.layout.activity_reservation_edit
    override var view: ReservationEditActivityView = this

    companion object{
        val RESERVATION_CANCEL = "RESERVATION_CANCEL"
        val RESERVATION_TRIP_START = "RESERVATION_TRIP_START"
        val RESERVATIONINCURRENTSTATUS = "RESERVATIONINCURRENTSTATUS"
        val ALREADY_ACTIVE_BOOKING = "ALREADY_ACTIVE_BOOKING"


        fun getIntent(context: Context, reservation: Reservation?, alreadyActiveBooking:Boolean):Intent{
            val intent = Intent(context, ReservationEditActivity::class.java)
            intent.putExtra(ALREADY_ACTIVE_BOOKING, alreadyActiveBooking)
            return intent
        }
    }

    override fun configureViews() {
        super.configureViews()
        configureClicks()
        showLoadingForReservationEdit(getString(R.string.loading))
        fetchLocation()
    }

    override fun onLocationPermissionsAvailable() {
        requestLocationUpdates()
    }
    override fun onLocationPermissionsDenied() {

    }

    override fun setUserPosition(location: Location) {

    }

    fun configureClicks(){
        ct_add_credit_card_in_confirm_reserve.setOnClickListener {
            launchAddPaymentCardActivity()
        }

        iv_add_credit_card_in_confirm_reserve.setOnClickListener {
            launchAddPaymentCardActivity()
        }

        btn_start_trip_in_reservation_edit.setOnClickListener {
            startRide()
        }

        btn_full_cancel_in_reservation_edit.setOnClickListener {
            showCancelReservationPopUp()
        }

        btn_cancel_in_reservation_edit.setOnClickListener {
            showCancelReservationPopUp()
        }

        iv_close_in_reservation_edit.setOnClickListener {
            finish()
        }
    }
    fun showCancelReservationPopUp(){
        launchPopUpActivity(
            REQUEST_CODE_CANCEL_CONFIRMATION,
            getString(R.string.general_error_title),
            getString(R.string.reservation_cancel_warning),
            null,
            getString(R.string.confirm),
            null,
            null,
            getString(R.string.cancel)
        )
    }

    fun cancelReservation(){
        showLoadingForReservationEdit(getString(R.string.loading))
        presenter.cancelReservation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== BikeListFragment.REQUEST_ADD_PAYMENT_CARD && resultCode == Activity.RESULT_OK){
            presenter.fetchCardList()
        }else if(requestCode == BikeListFragment.CARD_ERROR_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            launchAddPaymentCardActivity()
        }else if (requestCode == REQUEST_CODE_CANCEL_CONFIRMATION &&
            data!=null &&
            data.hasExtra(PopUpActivity.POSITIVE_LEVEL) &&
            data.getIntExtra(PopUpActivity.POSITIVE_LEVEL,-1)==1){
            cancelReservation()
        }else if (requestCode == MAKE_CARD_PRIMARY_REQUEST_CODE){
            presenter.fetchCardList()
        }else if(requestCode == PRIMARY_CARD_ERROR_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            launchMakeCardPrimaryActivity()
        }
    }

    fun startRide(){
        if (presenter.cards == null || presenter.cards?.size == 0) {
            launchAddCardPopUp()
        }else if (presenter.getPrimaryCard()==null){
            launchMakeCardPrimaryPopUp()
        }else{
            showLoadingForReservationEdit(getString(R.string.starting_ride_loader))
            presenter.startTrip()
        }
    }

    fun launchMakeCardPrimaryPopUp(){
        launchPopUpActivity(
            PRIMARY_CARD_ERROR_REQUEST_CODE,
            getString(R.string.general_error_title),
            getString(R.string.primary_card_selection_warning),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            getString(R.string.cancel_capital)
        )
    }

    fun launchAddCardPopUp(){
        launchPopUpActivity(
            BikeListFragment.CARD_ERROR_REQUEST_CODE,
            getString(R.string.add_credit_card),
            getString(R.string.add_card_description),
            null,
            getString(R.string.add_card),
            null,
            null,
            getString(R.string.cancel_capital)
        )
    }

    fun launchMakeCardPrimaryActivity(){
        startActivityForResult(
            Intent(this, PaymentActivity::class.java),
            MAKE_CARD_PRIMARY_REQUEST_CODE
        )
    }

    override fun onReservationInformationSuccess(bike: Bike){


        ct_reservation_value1_in_reservation_edit.text = getDateTimeWithAtLabel(this,dateFromUTC(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(presenter.reservation?.reservation_start)
        )!!)

        ct_reservation_value2_in_reservation_edit.text = getDateTimeWithAtLabel(this,dateFromUTC(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(presenter.reservation?.reservation_end)
        )!!)


        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(this)
            .load(bike.pic)
            .apply(requestOptions)
            .into(iv_bike_image_in_reservation_edit)

        ct_fleet_name_in_reservation_edit.setText(bike.fleet_name)
        ct_bike_name_in_reservation_edit.setText(bike.bike_name)
        ct_bike_type_in_reservation_edit.setText(ResourceHelper.getBikeType(bike.type, this))

        ResourceHelper.setBatteryImageAndText(
            bike.bike_battery_level,
            iv_bike_battery_in_reservation_edit,
            ct_bike_battery_in_reservation_edit
        )

        setAllPrice(bike)

        hideLoadingForReservationEdit()
    }

    override fun onReservationInformationFailure() {
        hideLoadingForReservationEdit()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    fun setAllPrice(bike:Bike){

        ct_bike_price_value_in_confirm_reserve.setText(getBikeCost(bike))

        setExcessUsageFee(bike)
        setExcessUsageFreq(bike)
        setParkingFee(bike)
        setUnlockFee(bike)
        setPreAuth(bike)
    }



    fun getBikeCost(bike: Bike):String{
        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type)&&
            !TextUtils.isEmpty(bike.price_for_membership) && bike.price_for_membership!!.toFloat()!=0F) {

            if (bike.price_type != null && !bike.price_type.equals("")) {
                var rideCost =
                    CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.price_for_membership
                        .toString()) + " " + getString(R.string.label_per) + " " +
                             LocaleTranslatorUtils.getLocaleString(
                        this,
                        bike.price_type,
                                 bike.price_type_value
                                     .toString()
                    ).toString()

                return rideCost
            }

        }else{
            return getString(R.string.bike_detail_bike_cost_free)
        }
        return "";
    }


    fun setExcessUsageFee(bike: Bike){
        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type)) {

            if (!TextUtils.isEmpty(bike.excess_usage_fees) && !TextUtils.isEmpty(bike.excess_usage_type_value) && !TextUtils.isEmpty(bike.excess_usage_type) ) {
                val cost =  CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.excess_usage_fees
                    .toString()) + " " + getString(R.string.label_per) + " " +
                         LocaleTranslatorUtils.getLocaleString(
                    this,
                    bike.excess_usage_type,
                             bike.excess_usage_type_value
                                 .toString()
                ).toString()


                ct_surcharge_price_label_in_confirm_reserve.visibility = View.VISIBLE
                ct_surcharge_price_value_in_confirm_reserve.visibility = View.VISIBLE
                ct_surcharge_price_value_in_confirm_reserve.text = cost
                setExcessUsageFreq(bike)
                return

            }else if (!TextUtils.isEmpty(bike.excess_usage_fees) ){
                val cost =  CurrencyUtil.getCurrencySymbolByCode(bike.currency, bike.excess_usage_fees
                    .toString())
                ct_surcharge_price_value_in_confirm_reserve.visibility = View.VISIBLE
                ct_surcharge_price_value_in_confirm_reserve.text = cost
                setExcessUsageFreq(bike)
                return
            }

        }else{
            ct_surcharge_price_value_in_confirm_reserve.visibility = View.GONE
            ct_surcharge_price_label_in_confirm_reserve.visibility = View.GONE
        }



    }

    fun setExcessUsageFreq(bike:Bike){
        if(!TextUtils.isEmpty(bike.excess_usage_type_after_value) && !TextUtils.isEmpty(bike.excess_usage_type_after_type)){
            val surchage_freq = getString(R.string.surcharge_description , LocaleTranslatorUtils.getLocaleString(
                this,
                bike.excess_usage_type_after_type,
                bike.excess_usage_type_after_value
            ).toString())


            ct_surcharge_description_in_confirm_reserve.visibility = View.VISIBLE
            ct_surcharge_description_in_confirm_reserve.text = surchage_freq
            return
        }else{
            ct_surcharge_description_in_confirm_reserve.visibility = View.GONE
        }

    }


    fun setUnlockFee(bike: Bike) {

        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) &&
            !TextUtils.isEmpty(bike.price_for_bike_unlock) && bike.price_for_bike_unlock!!.toFloat()!=0F) {
            val cost =
                CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.price_for_bike_unlock
                    .toString())

            ct_unlock_price_label_in_confirm_reserve.visibility = View.VISIBLE
            ct_unlock_price_value_in_confirm_reserve.visibility = View.VISIBLE
            ct_unlock_price_value_in_confirm_reserve.text = cost

        } else {
            ct_unlock_price_label_in_confirm_reserve.visibility = View.GONE
            ct_unlock_price_value_in_confirm_reserve.visibility = View.GONE
        }
    }


    fun setParkingFee(bike:Bike){
        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) &&
            !TextUtils.isEmpty(bike.price_for_penalty_outside_parking)&& bike.price_for_penalty_outside_parking!!.toFloat()!=0F) {
            val cost =
                CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.price_for_penalty_outside_parking
                    .toString())


            ct_bike_parking_in_confirm_reserve.visibility = View.VISIBLE
            ct_bike_parking_value_in_confirm_reserve.visibility = View.VISIBLE
            ct_parking_description_in_confirm_reserve.visibility = View.VISIBLE


            ct_bike_parking_value_in_confirm_reserve.text = cost

        }else{
            ct_bike_parking_in_confirm_reserve.visibility = View.GONE
            ct_bike_parking_value_in_confirm_reserve.visibility = View.GONE
            ct_parking_description_in_confirm_reserve.visibility = View.GONE
        }
    }

    fun setPreAuth(bike:Bike){
        if(!TextUtils.isEmpty(bike.enable_preauth) && (bike.enable_preauth.equals("1") || bike.enable_preauth.equals("true",true))){
            ct_bike_preauth_value_in_confirm_reserve.text = CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.preauth_amount)
            ct_bike_preauth_label_in_confirm_reserve.visibility = View.VISIBLE
            ct_bike_preauth_value_in_confirm_reserve.visibility = View.VISIBLE
            ct_preauth_description_in_confirm_reserve.visibility = View.VISIBLE
        }else{
            ct_bike_preauth_label_in_confirm_reserve.visibility = View.GONE
            ct_bike_preauth_value_in_confirm_reserve.visibility = View.GONE
            ct_preauth_description_in_confirm_reserve.visibility = View.GONE
        }
    }

    override fun onReservationCancelSuccess() {
        var intent = Intent()
        intent.putExtra(RESERVATION_CANCEL, true)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onReservationCancelFailure() {
        hideLoadingForReservationEdit()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun onReservationStartTripSuccess() {
        var intent = Intent()
        intent.putExtra(RESERVATION_TRIP_START, presenter.startReservation)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onReservationCStartTripFailure() {
        hideLoadingForReservationEdit()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun handleCard(card: Card) {
        iv_bike_payment_img_in_confirm_reserve.visibility= View.VISIBLE
        ct_bike_payment_value_in_confirm_reserve.visibility= View.VISIBLE
        iv_add_credit_card_in_confirm_reserve.visibility= View.GONE
        ct_add_credit_card_in_confirm_reserve.visibility= View.GONE
        ct_bike_payment_value_in_confirm_reserve.setText("XXXX-" + card.cc_no!!.substring(card.cc_no!!.length - 4))
    }

    override fun handleNoCard() {
        iv_bike_payment_img_in_confirm_reserve.visibility= View.GONE
        ct_bike_payment_value_in_confirm_reserve.visibility= View.GONE
        iv_add_credit_card_in_confirm_reserve.visibility= View.VISIBLE
        ct_add_credit_card_in_confirm_reserve.visibility= View.VISIBLE
    }

    override fun showTripStart(status: Boolean) {
        if(status){
            cl_start_cancel_in_reservation_edit.visibility = View.VISIBLE
            btn_full_cancel_in_reservation_edit.visibility = View.GONE
        }else{
            cl_start_cancel_in_reservation_edit.visibility = View.GONE
            btn_full_cancel_in_reservation_edit.visibility = View.VISIBLE
        }
    }

    fun launchAddPaymentCardActivity(){
        AddPaymentCardActivity.launchForResult(this,
            BikeListFragment.REQUEST_ADD_PAYMENT_CARD,null)
    }


    fun showLoadingForReservationEdit(message:String) {
        reservation_edit_activity_loading_view.ct_loading_title.text = message
        reservation_edit_activity_loading_view.visibility= View.VISIBLE
    }

    fun hideLoadingForReservationEdit() {
        reservation_edit_activity_loading_view.visibility= View.GONE
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}