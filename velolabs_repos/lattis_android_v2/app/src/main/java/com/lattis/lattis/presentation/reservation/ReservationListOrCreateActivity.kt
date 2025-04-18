package com.lattis.lattis.presentation.reservation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Card
import com.lattis.domain.models.Location
import com.lattis.lattis.presentation.base.activity.location.BaseLocationWithoutDrawerActivity
import com.lattis.lattis.presentation.bikelist.BikeListFragment
import com.lattis.lattis.presentation.parking.ParkingActivity
import com.lattis.lattis.presentation.payment.PaymentActivity
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.qrscan.ScanBikeQRCodeActivity
import com.lattis.lattis.presentation.utils.CurrencyUtil
import com.lattis.lattis.presentation.utils.FirebaseUtil
import com.lattis.lattis.presentation.utils.IsRidePaid
import com.lattis.lattis.presentation.utils.LocaleTranslatorUtils
import com.lattis.lattis.presentation.webview.WebviewActivity
import com.lattis.lattis.utils.ResourceHelper
import com.lattis.lattis.utils.UtilsHelper
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_reservation_create.*
import kotlinx.android.synthetic.main.activity_reservation_list.*
import kotlinx.android.synthetic.main.activity_reservation_list_edit.*
import kotlinx.android.synthetic.main.activity_reservation_list_or_create.*
import kotlinx.android.synthetic.main.fragment_bikelist_slider.*
import java.text.SimpleDateFormat
import javax.inject.Inject

class ReservationListOrCreateActivity : BaseLocationWithoutDrawerActivity<ReservationListOrCreateActivityPresenter,ReservationListOrCreateActivityView>(),
    ReservationListOrCreateActivityView, AvailableReservationsActionListener,ReservationFleetActionListener{

    @Inject
    override lateinit var presenter: ReservationListOrCreateActivityPresenter
    override val activityLayoutId = R.layout.activity_reservation_list_or_create
    override var view: ReservationListOrCreateActivityView = this

    private val RESERVATIONS =0
    private val INFO =1
    private val EDIT =2
    private val CREATE =3


    private val REQUEST_CODE_ERROR = 4393
    private val REQUEST_CODE_CANCEL_CONFIRMATION = 4394
    private val REQUEST_CODE_RESERVATION_CREATE = 4395
    private val REQUEST_CODE_TERMS_AND_CONDITION = 4396
    private val REQUEST_CODE_ALREADY_IN_RIDE = 4397
    private val PRIMARY_CARD_ERROR_REQUEST_CODE = 4398
    private val MAKE_CARD_PRIMARY_REQUEST_CODE = 4399

    private var reservationFleetListAdapter:ReservationFleetListAdapter?=null
    private var availableReservationsListAdapter:AvailableReservationsListAdapter?=null

    companion object{
        val ALREADY_ACTIVE_BOOKING = "ALREADY_ACTIVE_BOOKING"
        val RESERVATION_TRIP_START = "RESERVATION_TRIP_START"
        fun getIntent(context: Context,alreadyActiveBooking:Boolean): Intent {
            val intent = Intent(context, ReservationListOrCreateActivity::class.java)
            intent.putExtra(ALREADY_ACTIVE_BOOKING, alreadyActiveBooking)
            return intent
        }
    }


    override fun configureViews() {
        super.configureViews()
        startFetchingReservations()
        configureClicks()
        fetchLocation()

        val searchIcon = sv_in_reservation_create.findViewById<ImageView>(R.id.search_mag_icon)
        searchIcon.setColorFilter(Color.BLACK)

        val cancelIcon = sv_in_reservation_create.findViewById<ImageView>(R.id.search_close_btn)
        cancelIcon.setColorFilter(Color.BLACK)

        //Turn iconified to false:
        sv_in_reservation_create.setIconified(false);
        //The above line will expand it to fit the area as well as throw up the keyboard

        //To remove the keyboard, but make sure you keep the expanded version:
        sv_in_reservation_create.clearFocus();
    }

    fun startFetchingReservations(){
        showLoadingForReservation(getString(R.string.loading))
        presenter.getReservations()
    }

    fun configureClicks(){
        iv_close_in_reservation_list.setOnClickListener {
            finish()
        }

        iv_close_in_bike_slider.setOnClickListener {
            showReservationEdit()
        }

        cl_info_in_reservation_list_edit.setOnClickListener {
            showBikeInfoView()
        }

        iv_close_in_reservation_list_edit.setOnClickListener {
            showReservations()
        }

        cl_map_in_reservation_list_edit.setOnClickListener {
            FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.PARKING_VIEW, FirebaseUtil.WHILE_RESERVING_BIKE_PARKING_VIEW)
            startActivity(
                ParkingActivity.getIntent(this,
                    if(presenter.currentUserLocation!=null)presenter.currentUserLocation?.latitude else null,
                    if(presenter.currentUserLocation!=null) presenter.currentUserLocation?.longitude else null,
                    presenter.selectedReservation?.bike?.fleet?.fleet_id!!,presenter.selectedReservation?.bike?.bike_id!!))
        }

        btn_start_trip_in_reservation_list_edit.setOnClickListener {
            startRide()
        }

        btn_full_cancel_in_reservation_list_edit.setOnClickListener {
            showCancelReservationPopUp()
        }

        btn_cancel_in_reservation_list_edit.setOnClickListener {
            showCancelReservationPopUp()
        }

        iv_close_in_reservation_create.setOnClickListener {
            showReservations()
        }

        btn_create_reservation_in_reservation_list.setOnClickListener {
            showCreateReservation()
        }

        ct_terms_condition_in_bike_slider.setOnClickListener {
            openBikeTermsAndCondition()
        }

        iv_terms_condition_value_in_bike_slider.setOnClickListener {
            openBikeTermsAndCondition()
        }

        ct_parking_spot_zone_in_bike_slider.setOnClickListener {
            openParkingSpotAndZone()
        }

        iv_parking_spot_zone_value_in_bike_slider.setOnClickListener {
            openParkingSpotAndZone()
        }
    }

    fun showReservations(){
        presenter.stopeReservationAvailableTimer()
        presenter.selectedReservation=null

        if(view_flipper_in_reservation_list_or_create.displayedChild != RESERVATIONS)
            view_flipper_in_reservation_list_or_create.displayedChild = RESERVATIONS

        if(presenter.reservations!=null && presenter.reservations?.size!!>0) {
            rv_in_reservation_list.setLayoutManager(LinearLayoutManager(this))
            availableReservationsListAdapter =
                AvailableReservationsListAdapter(this, presenter.reservations, this)
            rv_in_reservation_list.setAdapter(availableReservationsListAdapter)

            ct_reservation_guide_label.visibility = View.GONE
            rv_in_reservation_list.visibility = View.VISIBLE
        }else{
            ct_reservation_guide_label.visibility = View.VISIBLE
            rv_in_reservation_list.visibility = View.GONE
        }


    }

    fun showCreateReservation() {
        if (view_flipper_in_reservation_list_or_create.displayedChild != CREATE)
            view_flipper_in_reservation_list_or_create.displayedChild = CREATE

        if(presenter.getUserFleets()!=null) {
            rv_fleets_in_reservation_create.setLayoutManager(LinearLayoutManager(this))
            reservationFleetListAdapter =ReservationFleetListAdapter(
                this,
                presenter.getUserFleets()!!,
                this
            )
            rv_fleets_in_reservation_create.setAdapter(
                reservationFleetListAdapter
            )

            sv_in_reservation_create.setOnQueryTextListener(
                object: androidx.appcompat.widget.SearchView.OnQueryTextListener{
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        reservationFleetListAdapter?.filter?.filter(newText)
                        return false
                    }

                }
            )

        }
    }

    override fun onReservationFleetSelected(fleet: Bike.Fleet) {
        var bike = Bike()
        bike.reservation_settings = fleet.reservation_settings
        bike.fleet = fleet
        startActivityForResult(ReservationActivity.getIntent(this,bike),
            REQUEST_CODE_RESERVATION_CREATE
        )
    }

    override fun onReservationsAvailable() {
        showReservations()
        hideLoadingForReservation()
    }

    override fun onReservationNotAvailable() {
        hideLoadingForReservation()
    }

    override fun onReservationSelected(position: Int) {
        presenter.selectedReservation = presenter.reservations?.get(position)!!
        showReservationEdit()
    }

    override fun onBikeSelected(position: Int) {

    }


    fun showReservationEdit(){

        if(view_flipper_in_reservation_list_or_create.displayedChild != EDIT)
            view_flipper_in_reservation_list_or_create.displayedChild = EDIT

        val bike = presenter.selectedReservation?.bike!!

        ct_reservation_value1_in_reservation_list_edit.text = UtilsHelper.getDateTimeWithAtLabel(
            this, UtilsHelper.dateFromUTC(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(presenter.selectedReservation?.reservation_start)
            )!!
        )

        ct_reservation_value2_in_reservation_list_edit.text = UtilsHelper.getDateTimeWithAtLabel(
            this, UtilsHelper.dateFromUTC(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(presenter.selectedReservation?.reservation_end)
            )!!
        )

        if(presenter.isBikeReservationTimeStarted()){
            cl_start_cancel_in_reservation_list_edit.visibility = View.VISIBLE
            btn_full_cancel_in_reservation_list_edit.visibility = View.GONE
            cl_available_time_in_reservation_list_edit.visibility = View.GONE
            presenter.stopeReservationAvailableTimer()
        }else{
            cl_start_cancel_in_reservation_list_edit.visibility = View.GONE
            btn_full_cancel_in_reservation_list_edit.visibility = View.VISIBLE
            cl_available_time_in_reservation_list_edit.visibility = View.VISIBLE
            presenter.startReservationAvailableTimer()
        }


        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(this)
            .load(bike.bike_group?.pic)
            .apply(requestOptions)
            .into(iv_bike_image_in_reservation_list_edit)

        ct_fleet_name_in_reservation_list_edit.setText(bike.fleet?.fleet_name)
        ct_bike_name_in_reservation_list_edit.setText(bike.bike_name)
        ct_bike_type_in_reservation_list_edit.setText(ResourceHelper.getBikeType(bike.bike_group?.type, this))

        ResourceHelper.setBatteryImageAndText(
            bike.bike_battery_level,
            iv_bike_battery_in_reservation_list_edit,
            ct_bike_battery_in_reservation_list_edit
        )

        setAllPrice(bike)
    }

    override fun onAvailableTimerValue(time: String) {
        ct_timer_in_reservation_list_edit.text = time
    }

    override fun onAvailableTimerFinish() {
        cl_start_cancel_in_reservation_list_edit.visibility = View.VISIBLE
        btn_full_cancel_in_reservation_list_edit.visibility = View.GONE
        cl_available_time_in_reservation_list_edit.visibility = View.GONE
    }


    fun showBikeInfoView(){

        presenter.stopeReservationAvailableTimer()
        val bike = presenter.selectedReservation?.bike!!

        if(view_flipper_in_reservation_list_or_create.displayedChild != INFO)
            view_flipper_in_reservation_list_or_create.displayedChild = INFO

        view_divider_in_bike_slider.visibility=View.GONE

        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(this)
            .load(bike?.bike_group?.pic)
            .apply(requestOptions)
            .into(iv_bike_in_bike_slider)

        ct_fleet_name_in_bike_slider.setText(bike.fleet?.fleet_name)
        ct_bike_name_in_bike_slider.setText(bike.bike_name)
        ct_bike_type_in_bike_slider.setText(ResourceHelper.getBikeType(bike.bike_group?.type, this))
        ct_bike_network_name_in_bike_slider.setText(bike.fleet?.fleet_name)

        ResourceHelper.setBatteryImageAndText(
            bike.bike_battery_level,
            iv_bike_battery_bike_slider,
            ct_bike_battery_bike_slider
        )
        ct_bike_about_name_value_in_bike_slider.setText(bike.bike_name)
        ct_bike_about_description_value_in_bike_slider.setText(bike.bike_group?.description)
        ct_bike_about_model_value_in_bike_slider.setText(
            ResourceHelper.getBikeType(bike.bike_group?.type, this)
        )

        setAllPrice(bike)

    }


    fun setAllPrice(bike:Bike){

        ct_bike_price_value_in_bike_slider.setText(getBikeCost(bike))
        ct_bike_price_value_in_reservation_list_edit.setText(getBikeCost(bike))

        setExcessUsageFee(bike)
        setExcessUsageFreq(bike)
        setParkingFee(bike)
        setUnlockFee(bike)
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
        if (IsRidePaid.isRidePaidForFleet(bike.fleet?.type)&&
            !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_fees) && bike.fleet?.fleet_payment_settings?.excess_usage_fees?.toFloatOrNull()!=null && bike.fleet?.fleet_payment_settings?.excess_usage_fees!!.toFloat()!=0F) {

            if (!TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_fees) && !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_type_value) && !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_type) ) {
                val cost =  CurrencyUtil.getCurrencySymbolByCode(bike.fleet?.fleet_payment_settings?.currency,bike.fleet?.fleet_payment_settings?.excess_usage_fees
                    .toString()) + " " + getString(R.string.label_per) + " " +
                       LocaleTranslatorUtils.getLocaleString(
                    this,
                    bike.fleet?.fleet_payment_settings?.excess_usage_type,
                           bike.fleet?.fleet_payment_settings?.excess_usage_type_value
                               .toString()
                ).toString()

                cl_surcharge_price_label_in_bike_slider.visibility = View.VISIBLE
                ct_surcharge_price_value_in_bike_slider.text = cost

                ct_surcharge_price_label_in_reservation_list_edit.visibility = View.VISIBLE
                ct_surcharge_price_value_in_reservation_list_edit.visibility = View.VISIBLE
                ct_surcharge_price_value_in_reservation_list_edit.text = cost

                setExcessUsageFreq(bike)

                return

            }else if (!TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_fees) ){
                val cost =  CurrencyUtil.getCurrencySymbolByCode(bike.fleet?.fleet_payment_settings?.currency,bike.fleet?.fleet_payment_settings?.excess_usage_fees
                    .toString())

                cl_surcharge_price_label_in_bike_slider.visibility = View.VISIBLE
                ct_surcharge_price_value_in_bike_slider.text = cost

                ct_surcharge_price_value_in_reservation_list_edit.visibility = View.VISIBLE
                ct_surcharge_price_value_in_reservation_list_edit.text = cost

                setExcessUsageFreq(bike)
                return
            }

        }else{
            cl_surcharge_price_label_in_bike_slider.visibility = View.GONE

            ct_surcharge_price_value_in_reservation_list_edit.visibility = View.GONE
            ct_surcharge_price_label_in_reservation_list_edit.visibility = View.GONE
        }



    }

    fun setExcessUsageFreq(bike:Bike){
        if(!TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_type_after_value) && !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_type_after_type)){
            val surchage_freq = getString(R.string.surcharge_description, LocaleTranslatorUtils.getLocaleString(
                this,
                bike.fleet?.fleet_payment_settings?.excess_usage_type_after_type,
                bike.fleet?.fleet_payment_settings?.excess_usage_type_after_value
            ).toString())


            ct_surcharge_description_in_bike_slider.visibility = View.VISIBLE
            ct_surcharge_description_in_bike_slider.text = surchage_freq

            ct_surcharge_description_in_reservation_list_edit.visibility = View.VISIBLE
            ct_surcharge_description_in_reservation_list_edit.text = surchage_freq

            return
        }else{
            ct_surcharge_description_in_bike_slider.visibility = View.GONE

            ct_surcharge_description_in_reservation_list_edit.visibility = View.GONE
        }


    }


    fun setUnlockFee(bike: Bike){

        if (IsRidePaid.isRidePaidForFleet(bike.fleet?.type) &&
            !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.price_for_bike_unlock) && bike.fleet?.fleet_payment_settings?.price_for_bike_unlock!!.toFloat()!=0F) {
            val cost = CurrencyUtil.getCurrencySymbolByCode(bike.fleet?.fleet_payment_settings?.currency, bike.fleet?.fleet_payment_settings?.price_for_bike_unlock
                .toString())

            cl_unlock_price_label_in_bike_slider.visibility = View.VISIBLE
            ct_unlock_price_value_in_bike_slider.text = cost

            ct_unlock_price_label_in_reservation_list_edit.visibility = View.VISIBLE
            ct_unlock_price_value_in_reservation_list_edit.visibility = View.VISIBLE
            ct_unlock_price_value_in_reservation_list_edit.text = cost

        }else{
            cl_unlock_price_label_in_bike_slider.visibility = View.GONE

            ct_unlock_price_label_in_reservation_list_edit.visibility = View.GONE
            ct_unlock_price_value_in_reservation_list_edit.visibility = View.GONE
        }



    }


    fun setParkingFee(bike:Bike){
        if (IsRidePaid.isRidePaidForFleet(bike.fleet?.type) &&
            !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.price_for_penalty_outside_parking) && bike.fleet?.fleet_payment_settings?.price_for_penalty_outside_parking!!.toFloat()!=0F) {
            val cost =
                CurrencyUtil.getCurrencySymbolByCode(bike.fleet?.fleet_payment_settings?.currency,bike.fleet?.fleet_payment_settings?.price_for_penalty_outside_parking
                    .toString())
            cl_parking_price_label_in_bike_slider.visibility = View.VISIBLE
            ct_bike_parking_value_in_bike_slider.text = cost

            ct_bike_parking_in_reservation_list_edit.visibility = View.VISIBLE
            ct_bike_parking_value_in_reservation_list_edit.visibility = View.VISIBLE
            ct_parking_description_in_reservation_list_edit.visibility = View.VISIBLE


            ct_bike_parking_value_in_reservation_list_edit.text = cost

        }else{
            cl_parking_price_label_in_bike_slider.visibility = View.GONE

            ct_bike_parking_in_reservation_list_edit.visibility = View.GONE
            ct_bike_parking_value_in_reservation_list_edit.visibility = View.GONE
            ct_parking_description_in_reservation_list_edit.visibility = View.GONE
        }
    }

    override fun handleCard(card: Card) {
        iv_bike_payment_img_in_reservation_list_edit.visibility= View.VISIBLE
        ct_bike_payment_value_in_reservation_list_edit.visibility= View.VISIBLE
        iv_add_credit_card_in_reservation_list_edit.visibility= View.GONE
        ct_add_credit_card_in_reservation_list_edit.visibility= View.GONE
        ct_bike_payment_value_in_reservation_list_edit.setText("XXXX-" + card.cc_no!!.substring(card.cc_no!!.length - 4))
    }

    override fun handleNoCard() {
        iv_bike_payment_img_in_reservation_list_edit.visibility= View.GONE
        ct_bike_payment_value_in_reservation_list_edit.visibility= View.GONE
        iv_add_credit_card_in_reservation_list_edit.visibility= View.VISIBLE
        ct_add_credit_card_in_reservation_list_edit.visibility= View.VISIBLE
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
        }else if(requestCode == REQUEST_CODE_RESERVATION_CREATE &&
            data!=null &&
            data.hasExtra(BikeListFragment.RESERVE)){
            startFetchingReservations()
        }else if (requestCode == MAKE_CARD_PRIMARY_REQUEST_CODE){
            presenter.fetchCardList()
        }else if(requestCode == PRIMARY_CARD_ERROR_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            launchMakeCardPrimaryActivity()
        }
    }


    //// cancel / start reservation : start

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
        showLoadingForReservation(getString(R.string.loading))
        presenter.cancelReservation()
    }

    fun startRide(){
        showLoadingForReservation(getString(R.string.starting_ride_loader))
        presenter.getBikeDetails()
    }

    override fun onCardMissingFailure() {
        hideLoadingForReservation()
        launchAddCardPopUp()
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

    fun launchAddPaymentCardActivity(){
        AddPaymentCardActivity.launchForResult(this,
            BikeListFragment.REQUEST_ADD_PAYMENT_CARD,null)
    }


    override fun onNoPrimaryCardFailure(){
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

    fun launchMakeCardPrimaryActivity(){
        startActivityForResult(
            Intent(this, PaymentActivity::class.java),
            MAKE_CARD_PRIMARY_REQUEST_CODE
        )
    }


    override fun onReservationCancelSuccess() {
        startFetchingReservations()
    }

    override fun onReservationCancelFailure() {
        hideLoadingForReservation()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun onReservationStartTripSuccess() {
        var intent = Intent()
        intent.putExtra(RESERVATION_TRIP_START, presenter.startReservation)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onReservationCStartTripFailure() {
        hideLoadingForReservation()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    //// cancel reservation : end

    fun openBikeTermsAndCondition(){
        if(!TextUtils.isEmpty(presenter.selectedReservation?.bike?.fleet?.t_and_c)) {
            WebviewActivity.launchForResult(this, REQUEST_CODE_TERMS_AND_CONDITION,presenter.selectedReservation?.bike?.fleet?.t_and_c!!)

        }
    }

    fun openParkingSpotAndZone(){
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.PARKING_VIEW, FirebaseUtil.WHILE_RESERVING_BIKE_PARKING_VIEW)
        startActivity(
            ParkingActivity.getIntent(this,
                if(presenter.currentUserLocation!=null)presenter.currentUserLocation?.latitude else null,
                if(presenter.currentUserLocation!=null) presenter.currentUserLocation?.longitude else null,
                presenter.selectedReservation?.bike?.fleet?.fleet_id!!,presenter.selectedReservation?.bike?.bike_id!!))
    }

    override fun onAlreadyOnRideFailure() {
        hideLoadingForReservation()
        launchPopUpActivity(
            REQUEST_CODE_ALREADY_IN_RIDE,
            getString(R.string.general_error_title),
            getString(R.string.general_error_message),
            null,
            null,
            null,
            null,
            getString(R.string.general_btn_ok)
        )
    }

    fun showLoadingForReservation(message:String) {
        activity_reservation_list_or_create_loading_view.ct_loading_title.text = message
        activity_reservation_list_or_create_loading_view.visibility= View.VISIBLE
    }

    fun hideLoadingForReservation() {
        activity_reservation_list_or_create_loading_view.visibility= View.GONE
    }

    override fun onLocationPermissionsAvailable() {
        requestLocationUpdates()
    }
    override fun onLocationPermissionsDenied() {

    }

    override fun setUserPosition(location: Location) {

    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }


    //// localNotificationHelper :start
    override fun startReservationTimerOverLocalNotification() {
        presenter.createLocalNotification(getString(R.string.reservation),getString(R.string.reservation_ending_soon))
    }

    //// locatNotificationHelper :end


    override fun onDestroy() {
        super.onDestroy()
        availableReservationsListAdapter?.cancelAllAvailalbeTimer()
    }
}