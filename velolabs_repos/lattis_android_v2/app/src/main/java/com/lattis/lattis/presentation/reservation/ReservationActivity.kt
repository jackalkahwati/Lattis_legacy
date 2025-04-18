package com.lattis.lattis.presentation.reservation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.InputType
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.gson.Gson
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Card
import com.lattis.domain.models.Location
import com.lattis.lattis.presentation.base.activity.location.BaseLocationWithoutDrawerActivity
import com.lattis.lattis.presentation.bikelist.BikeListFragment
import com.lattis.lattis.presentation.bikelist.BikeListFragment.Companion.RESERVE
import com.lattis.lattis.presentation.parking.ParkingActivity
import com.lattis.lattis.presentation.payment.PaymentActivity
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.popup.edit.PopUpEditActivity
import com.lattis.lattis.presentation.rentalfare.RentalFareAdapter
import com.lattis.lattis.presentation.rentalfare.RentalFareClickListener
import com.lattis.lattis.presentation.utils.*
import com.lattis.lattis.presentation.utils.StrictTCUtil.hasStrictTC
import com.lattis.lattis.presentation.webview.WebviewActivity
import com.lattis.lattis.utils.ResourceHelper
import com.lattis.lattis.utils.ResourceHelper.getBikeType
import com.lattis.lattis.utils.UtilsHelper.addMinutesToJavaUtilDate
import com.lattis.lattis.utils.UtilsHelper.addYearsMonthsDaysToJavaUtilDate
import com.lattis.lattis.utils.UtilsHelper.getDateTimeWithAtLabel
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_reservation.*
import kotlinx.android.synthetic.main.activity_reservation_bike_card.view.*
import kotlinx.android.synthetic.main.activity_reservation_confirm.*
import kotlinx.android.synthetic.main.activity_reservation_confirm.btn_confirm_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.cl_payment_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_add_credit_card_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_bike_parking_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_bike_parking_value_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_bike_preauth_label_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_bike_preauth_value_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_bike_price_value_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_parking_description_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_preauth_description_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_surcharge_description_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_surcharge_price_label_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_surcharge_price_value_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_unlock_price_label_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.ct_unlock_price_value_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.iv_add_credit_card_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.pb_progress_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.view.ct_add_credit_card_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.view.ct_bike_payment_value_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.view.iv_add_credit_card_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_confirm.view.iv_bike_payment_img_in_confirm_reserve
import kotlinx.android.synthetic.main.activity_reservation_main.*
import kotlinx.android.synthetic.main.activity_reservation_vehicles.*
import kotlinx.android.synthetic.main.activity_scan_bike_qr_code.*
import kotlinx.android.synthetic.main.fragment_bikelist_slider.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_confirm_reserve.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_confirm_reserve.view.*
import kotlinx.android.synthetic.main.layout_rental_fare_main.view.*
import kotlinx.android.synthetic.main.layout_strict_tc.view.*
import org.threeten.bp.Duration
import org.threeten.bp.Period
import java.util.*

import javax.inject.Inject


class ReservationActivity : BaseLocationWithoutDrawerActivity<ReservationActivityPresenter, ReservationActivityView>(),
    ReservationActivityView, AvailableVehiclesActionListener,RentalFareClickListener {


    private val REQUEST_CODE_ERROR = 4393
    private val REQUEST_CODE_TERMS_AND_CONDITION = 4394
    private val REQUEST_CODE_RESERVATION_SUCCESS = 4395
    private val MAIN =0
    private val VEHICLES =1
    private val INFO=2
    private val CONFIRM =3

    private val PICKUP_DATE_SELECTION=0
    private val RETURN_DATE_SELECTION=1

    companion object{
        val REFERENCED_BIKE = "REFERENCED_BIKE"

        fun getIntent(context: Context, bike:Bike): Intent{
            val intent = Intent(context, ReservationActivity::class.java)
            intent.putExtra(REFERENCED_BIKE, Gson().toJson(bike))
            return intent
        }
    }


    @Inject
    override lateinit var presenter: ReservationActivityPresenter
    override val activityLayoutId = R.layout.activity_reservation
    override var view: ReservationActivityView = this

    override fun configureViews() {
        super.configureViews()

        fetchLocation()
        presenter.fetchCardList()
        presenter.getUser

        showMainView()
        configureClicks()
    }

    fun configureClicks(){
        iv_close_in_reservation_main.setOnClickListener {
            finish()
        }

        iv_close_in_bike_slider.setOnClickListener {
            if(presenter.bikeInfoViewFromConfirm){
                showConfirmationView(presenter.selectedVehicle!!)
            }else{
                showVehiclesView()
            }
        }

        iv_close_in_reservation_vehicles.setOnClickListener {
            showMainView()
        }

        iv_close_in_reservation_confirm.setOnClickListener {
            showVehiclesView()
        }

        cl_pickup_in_reservation_main.setOnClickListener {
            showPickUpDateSelection()
        }
        cl_pickup_in_reservation_confirm.setOnClickListener {
            showPickUpDateSelection()
        }

        cl_return_in_reservation_main.setOnClickListener {
            showReturnDateSelection()
        }
        cl_return_in_reservation_confirm.setOnClickListener {
            showReturnDateSelection()
        }

        cl_vehicle_in_reservation_main.setOnClickListener {
            if(presenter.isVehicleClickEnabled) {
                showLoadingForReservation(getString(R.string.loading))
//                presenter.searchBike(Location(23.04177724902044,72.53154706189153),Location(23.034273646489922,72.51609753811732))
                presenter.startFetchAvailableVehicles()
            }
        }

        cl_pricing_options_in_reservation_main.setOnClickListener {
            if(presenter.isPricingOptionsClickEnabled)showRentalFarePop(presenter.referencedBike!!)
        }

        cl_pricing_options_in_confirm_reserve.setOnClickListener {
            if(presenter.isPricingOptionsClickEnabled)showRentalFarePop(presenter.referencedBike!!)
        }

        cl_vehicle_in_reservation_confirm.setOnClickListener {
            presenter.bikeInfoViewFromConfirm = true
            showBikeInfoView(presenter.selectedVehicle!!)
        }

        ct_bike_book_terms_policy_in_reservation_confirm.setOnClickListener {
            openBikeTermsAndCondition()
        }

        cl_confirm_in_reservation_confirm.setOnClickListener {
            checkConditionForReserve(false)
        }

        ct_add_credit_card_in_confirm_reserve.setOnClickListener {
            launchAddPaymentCardActivity()
        }

        iv_add_credit_card_in_confirm_reserve.setOnClickListener {
            launchAddPaymentCardActivity()
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


        ////rentalfare :start
        layout_rental_fare_in_reservation_activity.iv_close_in_rental_fare.setOnClickListener {
            hideRentalFarePop()
        }

        layout_rental_fare_in_reservation_activity.cl_pay_per_use_in_rental_fare.setOnClickListener {
            payPerUseClicked()
        }
        layout_rental_fare_in_reservation_activity.btn_confirm_in_rental_fare.setOnClickListener {
            rentalFareConfirmationClicked()
        }

        layout_rental_fare_in_reservation_activity.iv_close_in_rental_fare.setOnClickListener {
            rentalFareSelectionCancelled()
        }
        ////rentalfare :end
        
        ////Strict TC :start

        layout_strict_tc_in_reservation_confirm_reserve.btn_cancel_in_strict_tc.setOnClickListener {
            layout_strict_tc_in_reservation_confirm_reserve.visibility= GONE
            layout_strict_tc_in_reservation_confirm_reserve.sm_1_in_strict_tc.isChecked = false
            layout_strict_tc_in_reservation_confirm_reserve.sm_2_in_strict_tc.isChecked = false
            layout_strict_tc_in_reservation_confirm_reserve.btn_accept_unselected_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_reservation_confirm_reserve.btn_accept_selected_in_strict_tc.visibility = GONE
        }

        layout_strict_tc_in_reservation_confirm_reserve.btn_accept_selected_in_strict_tc.setOnClickListener {
            layout_strict_tc_in_reservation_confirm_reserve.visibility= GONE
            checkConditionForReserve(true)
        }

        layout_strict_tc_in_reservation_confirm_reserve.sm_1_in_strict_tc.setOnCheckedChangeListener { buttonView, isChecked ->
            handleAcceptCancelInStrictTC()
        }

        layout_strict_tc_in_reservation_confirm_reserve.sm_2_in_strict_tc.setOnCheckedChangeListener { buttonView, isChecked ->
            handleAcceptCancelInStrictTC()
        }
        ////Strict TC :end

    }


    fun handleAcceptCancelInStrictTC(){
        val isChecked = layout_strict_tc_in_reservation_confirm_reserve.sm_1_in_strict_tc.isChecked &&
                layout_strict_tc_in_reservation_confirm_reserve.sm_2_in_strict_tc.isChecked
        if(isChecked) {
            layout_strict_tc_in_reservation_confirm_reserve.btn_accept_selected_in_strict_tc.visibility =
                VISIBLE
            layout_strict_tc_in_reservation_confirm_reserve.btn_accept_unselected_in_strict_tc.visibility =
                GONE
        }else{
            layout_strict_tc_in_reservation_confirm_reserve.btn_accept_selected_in_strict_tc.visibility =
                GONE
            layout_strict_tc_in_reservation_confirm_reserve.btn_accept_unselected_in_strict_tc.visibility =
                VISIBLE
        }
    }


    fun handleStrictTCPopOpUI(){
        val strings = StrictTCUtil.getStrictTCString()
        if(strings==null) {
            layout_strict_tc_in_reservation_confirm_reserve.cl_consent_1_in_strict_tc.visibility = GONE
            layout_strict_tc_in_reservation_confirm_reserve.cl_consent_2_in_strict_tc.visibility = GONE
        }else if(strings?.size!! == 1){
            layout_strict_tc_in_reservation_confirm_reserve.ct_consent_1_in_strict_tc.text = strings?.get(0)
            layout_strict_tc_in_reservation_confirm_reserve.sm_1_in_strict_tc.isChecked = false
            layout_strict_tc_in_reservation_confirm_reserve.cl_consent_1_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_reservation_confirm_reserve.sm_2_in_strict_tc.isChecked = true  //if only one string, it will make the second true
            layout_strict_tc_in_reservation_confirm_reserve.cl_consent_2_in_strict_tc.visibility = GONE
        }else if(strings?.size!! == 2){
            layout_strict_tc_in_reservation_confirm_reserve.ct_consent_1_in_strict_tc.text = strings?.get(0)
            layout_strict_tc_in_reservation_confirm_reserve.ct_consent_2_in_strict_tc.text = strings?.get(1)
            layout_strict_tc_in_reservation_confirm_reserve.sm_1_in_strict_tc.isChecked = false
            layout_strict_tc_in_reservation_confirm_reserve.sm_2_in_strict_tc.isChecked = false
            layout_strict_tc_in_reservation_confirm_reserve.cl_consent_1_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_reservation_confirm_reserve.cl_consent_2_in_strict_tc.visibility = VISIBLE
        }

        val strictTCLink = StrictTCUtil.getStrictTCLink()
        if (TextUtils.isEmpty(strictTCLink)) {
            layout_strict_tc_in_reservation_confirm_reserve.ct_privacy_and_terms_in_strict_tc.visibility = GONE
        } else{
            layout_strict_tc_in_reservation_confirm_reserve.ct_privacy_and_terms_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_reservation_confirm_reserve.ct_privacy_and_terms_in_strict_tc.text =
                HtmlCompat.fromHtml(
                    String.format(
                        strictTCLink!!,
                        BuildConfigUtil.privacyPolicy(),
                        BuildConfigUtil.termsOfService()
                    ), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
        layout_strict_tc_in_reservation_confirm_reserve.ct_privacy_and_terms_in_strict_tc.movementMethod = LinkMovementMethod.getInstance()

        layout_strict_tc_in_reservation_confirm_reserve.btn_accept_unselected_in_strict_tc.visibility = VISIBLE
        layout_strict_tc_in_reservation_confirm_reserve.btn_accept_selected_in_strict_tc.visibility = GONE
        layout_strict_tc_in_reservation_confirm_reserve.visibility= VISIBLE
    }
    
    fun checkConditionForReserve(bypassStrictTC:Boolean){
        if (!presenter.phoneNumberCheckPassed()) {
            launchPopUpActivity(
                BikeListFragment.PHONE_NUMBER_ERROR_REQUEST_CODE,
                getString(R.string.label_note),
                getString(R.string.mandatory_phone_text),
                null,
                getString(R.string.mandatory_phone_action),
                null,
                null,
                getString(R.string.cancel_capital)
            )
        } else if(hasStrictTC() && !bypassStrictTC){
            handleStrictTCPopOpUI()
        }else {

            if (!IsRidePaid.isRidePaidForFleet(presenter.selectedVehicle?.fleet_type)) {
                tryReserveBike()
            }else {
                if (presenter.cards == null || presenter.cards?.size == 0) {
                    launchAddCardPopUp()
                } else if (presenter.getPrimaryCard()==null){
                    launchMakeCardPrimaryPopUp()
                } else {
                    tryReserveBike()
                }
            }
        }
    }

    fun launchMakeCardPrimaryPopUp(){
        launchPopUpActivity(
            BikeListFragment.PRIMARY_CARD_ERROR_REQUEST_CODE,
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

    fun tryReserveBike(){
        cl_confirm_in_reservation_confirm.isEnabled = false
        pb_progress_in_confirm_reserve.visibility = View.VISIBLE
        btn_confirm_in_confirm_reserve.visibility=View.GONE
        presenter.reserve()
    }

    fun launchMakeCardPrimaryActivity(){
        startActivityForResult(
            Intent(this, PaymentActivity::class.java),
            BikeListFragment.MAKE_CARD_PRIMARY_REQUEST_CODE
        )
    }

    fun showPickUpDateSelection(){
//        val max_window_duration: Duration = Duration.parse(presenter.referencedBike?.reservation_settings?.booking_window_duration)
        showDateTimePicker(PICKUP_DATE_SELECTION,null ,null,if(presenter.getPickUpDate()!=null) presenter.getPickUpDate() else addMinutesToJavaUtilDate(Date(),30))
    }


    fun showReturnDateSelection(){

        var minimumDate: Date? = null
        try {
            if (!TextUtils.isEmpty(presenter.referencedBike?.reservation_settings?.min_reservation_duration)) {
                var minReservationDuration = presenter.referencedBike?.reservation_settings?.min_reservation_duration
                if(minReservationDuration!!.contains("T",true)){
                    val min_duration_minute = Duration.parse("PT".plus(minReservationDuration!!.substringAfter("T")))
                    minimumDate = addMinutesToJavaUtilDate(presenter.getPickUpDate(),min_duration_minute.toMinutes().toInt())
                    minReservationDuration = minReservationDuration.substringBefore("T")
                }

                if(!minReservationDuration.equals("P",true)){   // There are months days
                    val min_period = Period.parse(minReservationDuration)
                    minimumDate = addYearsMonthsDaysToJavaUtilDate(if(minimumDate==null)presenter.getPickUpDate() else minimumDate,min_period.years,min_period.months,min_period.days)
                }
            }
        }catch (e:Exception){
            Log.e("Reservation",e.localizedMessage)
        }

        var maximumDate: Date? = null
        try {
            if (!TextUtils.isEmpty(presenter.referencedBike?.reservation_settings?.max_reservation_duration)) {
                var maxReservationDuration = presenter.referencedBike?.reservation_settings?.max_reservation_duration
                if(maxReservationDuration!!.contains("T",true)){
                    val min_duration_minute = Duration.parse("PT".plus(maxReservationDuration!!.substringAfter("T")))
                    maximumDate = addMinutesToJavaUtilDate(presenter.getPickUpDate(),min_duration_minute.toMinutes().toInt())
                    maxReservationDuration = maxReservationDuration.substringBefore("T")
                }

                if(!maxReservationDuration.equals("P",true)){   // There are months days
                    val min_period = Period.parse(maxReservationDuration)
                    maximumDate = addYearsMonthsDaysToJavaUtilDate(if(maximumDate==null)presenter.getPickUpDate() else maximumDate,min_period.years,min_period.months,min_period.days)
                }
            }
        }catch (e:Exception){
            Log.e("Reservation",e.localizedMessage)
        }

        if(presenter.isReturnClickEnabled) showDateTimePicker(RETURN_DATE_SELECTION,
            minimumDate,
            maximumDate,
            if(presenter.getReturnDate()==null)minimumDate else presenter.getReturnDate())
    }

    fun showMainView(){
        if(view_flipper_in_reservation.displayedChild != MAIN)
            view_flipper_in_reservation.displayedChild = MAIN
    }

    fun showVehiclesView(){
        if(view_flipper_in_reservation.displayedChild != VEHICLES)
            view_flipper_in_reservation.displayedChild = VEHICLES

        rv_in_reservation_vehicles.setLayoutManager(LinearLayoutManager(this))
        rv_in_reservation_vehicles.setAdapter(AvailableVehiclesListAdapter(this, presenter.availableVehicleList,  this))
    }

    fun showBikeInfoView(bike: Bike){
        presenter.selectedVehicle = bike
        if(view_flipper_in_reservation.displayedChild != INFO)
            view_flipper_in_reservation.displayedChild = INFO

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
        ct_bike_type_in_bike_slider.setText(getBikeType(bike.bike_group?.type, this))
        ct_bike_network_name_in_bike_slider.setText(bike.fleet?.fleet_name)

        ResourceHelper.setBatteryImageAndText(
            bike.bike_battery_level,
            iv_bike_battery_bike_slider,
            ct_bike_battery_bike_slider
        )

        ct_bike_about_name_value_in_bike_slider.setText(bike.bike_name)
        ct_bike_about_description_value_in_bike_slider.setText(bike.bike_group?.description)
        ct_bike_about_model_value_in_bike_slider.setText(
            getBikeType(bike.bike_group?.type, this)
        )

        setAllPrice(bike)

    }

    fun getBikeCost(bike: Bike):String{
        if (IsRidePaid.isRidePaidForFleet(bike.fleet?.type)&&
            !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.price_for_membership) && bike.fleet?.fleet_payment_settings?.price_for_membership!!.toFloat()!=0F) {

            if (bike.fleet?.fleet_payment_settings?.price_type != null && !bike.fleet?.fleet_payment_settings?.price_type.equals("")) {
                var rideCost =
                    CurrencyUtil.getCurrencySymbolByCode(bike.fleet?.fleet_payment_settings?.currency,bike.fleet?.fleet_payment_settings?.price_for_membership
                        .toString()) + " " + getString(R.string.label_per) + " " +
                             LocaleTranslatorUtils.getLocaleString(
                        this,
                        bike.fleet?.fleet_payment_settings?.price_type,
                        bike.fleet?.fleet_payment_settings?.price_type_value
                            .toString()
                    ).toString()

                return rideCost
            }

        }else{
            return getString(R.string.bike_detail_bike_cost_free)
        }
        return "";
    }

    fun showConfirmationView(bike:Bike){
        if(view_flipper_in_reservation.displayedChild != CONFIRM)
            view_flipper_in_reservation.displayedChild = CONFIRM


        ct_date_time_pickup_in_reservation_confirm.text = getDateTimeWithAtLabel(this,presenter.getPickUpDate()!!)
        ct_date_time_return_in_reservation_confirm.text = getDateTimeWithAtLabel(this,presenter.getReturnDate()!!)


        activity_reservation_bike_card_in_reservation_confirm.ct_bike_type_in_reservation_bike_card.setText(getBikeType(bike.bike_group?.type, this))
        activity_reservation_bike_card_in_reservation_confirm.ct_bike_name_in_reservation_reservation_bike_card.setText(bike.bike_name)
        activity_reservation_bike_card_in_reservation_confirm.ct_fleet_name_in_reservation_reservation_reservation_bike_card.setText(bike?.fleet?.fleet_name)

        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(this)
            .load(bike.bike_group?.pic)
            .apply(requestOptions)
            .into(activity_reservation_bike_card_in_reservation_confirm.iv_bike_image_in_reservation_bike_card)

        setAllPrice(bike)


        if(presenter.selectedVehicle!=null && !TextUtils.isEmpty(presenter.selectedVehicle?.fleet?.t_and_c)) {
            ct_bike_book_terms_policy_in_reservation_confirm.visibility = View.VISIBLE
            ct_bike_book_terms_policy_in_reservation_confirm.text = HtmlCompat.fromHtml(getString(R.string.bike_details_terms_policy, presenter.selectedVehicle?.fleet?.t_and_c), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }else{
            ct_bike_book_terms_policy_in_reservation_confirm.visibility = View.GONE
        }



    }

    fun setAllPrice(bike:Bike){

        ct_bike_price_value_in_bike_slider.setText(getBikeCost(bike))
//        ct_bike_price_value_in_confirm_reserve.setText(getBikeCost(bike)) // this is done from server response for cost estimation


        setExcessUsageFee(bike)
        setExcessUsageFreq(bike)
        setParkingFee(bike)
        setUnlockFee(bike)
        setPreAuth(bike)
    }

    fun setExcessUsageFee(bike: Bike){
        if (IsRidePaid.isRidePaidForFleet(bike.fleet?.type)&&
            !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_fees) && bike.fleet?.fleet_payment_settings?.excess_usage_fees?.toFloatOrNull()!=null &&  bike.fleet?.fleet_payment_settings?.excess_usage_fees!!.toFloat()!=0F) {

            if (!TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_fees) && !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_type_value) && !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_type) ) {
                val cost =  CurrencyUtil.getCurrencySymbolByCode(bike.fleet?.fleet_payment_settings?.currency,bike.fleet?.fleet_payment_settings?.excess_usage_fees
                    .toString()) + " " + getString(R.string.label_per) + " " +
                         LocaleTranslatorUtils.getLocaleString(
                    this,
                    bike.fleet?.fleet_payment_settings?.excess_usage_type,
                             bike.fleet?.fleet_payment_settings?.excess_usage_type_value
                                 .toString()
                ).toString()


                ct_surcharge_price_label_in_confirm_reserve.visibility = View.VISIBLE
                ct_surcharge_price_label_in_confirm_reserve.visibility = View.VISIBLE

                cl_surcharge_price_label_in_bike_slider.visibility = View.VISIBLE

                ct_surcharge_price_value_in_confirm_reserve.text = cost
                ct_surcharge_price_value_in_bike_slider.text = cost

                setExcessUsageFreq(bike)

                return

            }else if (!TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_fees) ){
                val cost =  CurrencyUtil.getCurrencySymbolByCode(bike.fleet?.fleet_payment_settings?.currency,bike.fleet?.fleet_payment_settings?.excess_usage_fees
                    .toString())

                ct_surcharge_price_value_in_confirm_reserve.visibility = View.VISIBLE
                ct_surcharge_price_label_in_confirm_reserve.visibility = View.VISIBLE

                cl_surcharge_price_label_in_bike_slider.visibility = View.VISIBLE

                ct_surcharge_price_value_in_confirm_reserve.text = cost
                ct_surcharge_price_value_in_bike_slider.text = cost

                setExcessUsageFreq(bike)
                return
            }

        }else{
            ct_surcharge_price_value_in_confirm_reserve.visibility = View.GONE
            ct_surcharge_price_label_in_confirm_reserve.visibility = View.GONE
            cl_surcharge_price_label_in_bike_slider.visibility = View.GONE
        }



    }

    fun setExcessUsageFreq(bike:Bike){
        if(!TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_type_after_value) && !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.excess_usage_type_after_type)){
            val surchage_freq = getString(R.string.surcharge_description , LocaleTranslatorUtils.getLocaleString(
                this,
                bike.fleet?.fleet_payment_settings?.excess_usage_type_after_type,
                bike.fleet?.fleet_payment_settings?.excess_usage_type_after_value
            ).toString())


            ct_surcharge_description_in_confirm_reserve.visibility = View.VISIBLE
            ct_surcharge_description_in_bike_slider.visibility = View.VISIBLE

            ct_surcharge_description_in_confirm_reserve.text = surchage_freq
            ct_surcharge_description_in_bike_slider.text = surchage_freq
            return
        }else{
            ct_surcharge_description_in_confirm_reserve.visibility = View.GONE
            ct_surcharge_description_in_bike_slider.visibility = View.GONE
        }




    }


    fun setUnlockFee(bike: Bike){

        if (IsRidePaid.isRidePaidForFleet(bike.fleet?.type) &&
            !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.price_for_bike_unlock) && bike.fleet?.fleet_payment_settings?.price_for_bike_unlock!!.toFloat()!=0F) {
            val cost = CurrencyUtil.getCurrencySymbolByCode(bike.fleet?.fleet_payment_settings?.currency,bike.fleet?.fleet_payment_settings?.price_for_bike_unlock
                .toString())

            ct_unlock_price_label_in_confirm_reserve.visibility = View.VISIBLE
            ct_unlock_price_value_in_confirm_reserve.visibility = View.VISIBLE
            cl_unlock_price_label_in_bike_slider.visibility = View.VISIBLE


            ct_unlock_price_value_in_confirm_reserve.text = cost
            ct_unlock_price_value_in_bike_slider.text = cost

        }else{
            ct_unlock_price_label_in_confirm_reserve.visibility = View.GONE
            ct_unlock_price_value_in_confirm_reserve.visibility = View.GONE
            cl_unlock_price_label_in_bike_slider.visibility = View.GONE
        }



    }


    fun setParkingFee(bike:Bike){
        if (IsRidePaid.isRidePaidForFleet(bike.fleet?.type) &&
            !TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.price_for_penalty_outside_parking) && bike.fleet?.fleet_payment_settings?.price_for_penalty_outside_parking!!.toFloat()!=0F) {
            val cost =
                CurrencyUtil.getCurrencySymbolByCode(bike.fleet?.fleet_payment_settings?.currency,bike.fleet?.fleet_payment_settings?.price_for_penalty_outside_parking
                    .toString())


            ct_bike_parking_in_confirm_reserve.visibility = View.VISIBLE
            ct_bike_parking_value_in_confirm_reserve.visibility = View.VISIBLE
            ct_parking_description_in_confirm_reserve.visibility = View.VISIBLE
            cl_parking_price_label_in_bike_slider.visibility = View.VISIBLE


            ct_bike_parking_value_in_confirm_reserve.text = cost
            ct_bike_parking_value_in_bike_slider.text = cost

        }else{
            ct_bike_parking_in_confirm_reserve.visibility = View.GONE
            ct_bike_parking_value_in_confirm_reserve.visibility = View.GONE
            ct_parking_description_in_confirm_reserve.visibility = View.GONE
            cl_parking_price_label_in_bike_slider.visibility = View.GONE
        }
    }

    fun setPreAuth(bike:Bike){
        if(!TextUtils.isEmpty(bike.fleet?.fleet_payment_settings?.enable_preauth) && (bike.fleet?.fleet_payment_settings?.enable_preauth.equals("1") || bike.fleet?.fleet_payment_settings?.enable_preauth.equals("true",true))){
            val preAuthAmount = CurrencyUtil.getCurrencySymbolByCode(bike.fleet?.fleet_payment_settings?.currency,bike.fleet?.fleet_payment_settings?.preauth_amount)
            ct_bike_preauth_value_in_confirm_reserve.text = preAuthAmount
            ct_bike_preauth_value_in_bike_slider.text = preAuthAmount
            ct_bike_preauth_label_in_confirm_reserve.visibility = View.VISIBLE
            ct_bike_preauth_value_in_confirm_reserve.visibility = View.VISIBLE
            ct_preauth_description_in_confirm_reserve.visibility = View.VISIBLE
            cl_preauth_value_label_in_bike_slider.visibility = View.VISIBLE
        }else{
            ct_bike_preauth_label_in_confirm_reserve.visibility = View.GONE
            ct_bike_preauth_value_in_confirm_reserve.visibility = View.GONE
            ct_preauth_description_in_confirm_reserve.visibility = View.GONE
            cl_preauth_value_label_in_bike_slider.visibility = View.GONE

        }
    }

    override fun handleCard(card: Card) {
        cl_payment_confirm_reserve.iv_bike_payment_img_in_confirm_reserve.visibility= View.VISIBLE
        cl_payment_confirm_reserve.ct_bike_payment_value_in_confirm_reserve.visibility= View.VISIBLE
        cl_payment_confirm_reserve.iv_add_credit_card_in_confirm_reserve.visibility= View.GONE
        cl_payment_confirm_reserve.ct_add_credit_card_in_confirm_reserve.visibility= View.GONE
        cl_payment_confirm_reserve.ct_bike_payment_value_in_confirm_reserve.setText("XXXX-" + card.cc_no!!.substring(card.cc_no!!.length - 4))
    }

    override fun handleNoCard() {
        cl_payment_confirm_reserve.iv_bike_payment_img_in_confirm_reserve.visibility= View.GONE
        cl_payment_confirm_reserve.ct_bike_payment_value_in_confirm_reserve.visibility= View.GONE

        cl_payment_confirm_reserve.iv_add_credit_card_in_confirm_reserve.visibility= View.VISIBLE
        cl_payment_confirm_reserve.ct_add_credit_card_in_confirm_reserve.visibility= View.VISIBLE
    }
    //// get card :end


    fun showDateTimePicker(mode:Int,minimumDate:Date?,maximumDate:Date?,defaultDate:Date?){


        val singleDateTimePicker = SingleDateAndTimePickerDialog.Builder(this)
            .title(if(mode==PICKUP_DATE_SELECTION)getString(R.string.pickup) else getString(R.string.return_label))
            .bottomSheet()
            .curved()
            .minutesStep(30)
            .titleTextColor(ContextCompat.getColor(this,R.color.reservation_text))
            .mainColor(ContextCompat.getColor(this,R.color.reservation_text))
            .listener {
                if(mode==PICKUP_DATE_SELECTION){
                    ct_date_time_pickup_in_reservation_main.text = getDateTimeWithAtLabel(this,it)
                    presenter.setPickUpDate(it)
                }else if(mode == RETURN_DATE_SELECTION){
                    ct_date_time_return_in_reservation_main.text = getDateTimeWithAtLabel(this,it)
                    presenter.setReturnDate(it)
                }
            }

        if(minimumDate==null){
            singleDateTimePicker.mustBeOnFuture()
        }else{
            singleDateTimePicker.minDateRange(minimumDate)
        }

        if(maximumDate!=null){
            singleDateTimePicker.maxDateRange(maximumDate)
        }

        if(defaultDate!=null){
            singleDateTimePicker.defaultDate(defaultDate)
        }
        singleDateTimePicker.display()
    }

    override fun resetReturnState(active: Boolean) {
        if(active){
            ct_return_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_text))
            ct_date_time_return_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_text))
            ct_date_time_return_in_reservation_main.text  = getString(R.string.select_date_time)
            iv_next_return_in_reservation_main.setImageDrawable(getDrawable(R.drawable.next_arrow))
            cl_pickup_in_reservation_main.isEnabled = true
        }else{
            cl_pickup_in_reservation_main.isEnabled = false
            ct_return_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_disabled_section))
            ct_date_time_return_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_disabled_section))
            iv_next_return_in_reservation_main.setImageDrawable(getDrawable(R.drawable.next_arrow_disabled))
        }

        showMainView()
    }

    override fun resetPricingOptionsState(active: Boolean) {
        if(active){
            ct_pricing_options_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_text))
            ct_pricing_options_vehicle_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_text))
            iv_next_pricing_options_in_reservation_main.setImageDrawable(getDrawable(R.drawable.next_arrow))
            cl_pricing_options_in_reservation_main.isEnabled = true

            showMainView()

        }else{
            cl_pricing_options_in_reservation_main.isEnabled = false
            ct_pricing_options_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_disabled_section))
            ct_pricing_options_vehicle_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_disabled_section))
            iv_next_pricing_options_in_reservation_main.setImageDrawable(getDrawable(R.drawable.next_arrow_disabled))
        }

    }


    override fun resetVehicleState(active: Boolean) {
        if(active){
            ct_vehicle_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_text))
            ct_date_time_vehicle_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_text))
            iv_next_vehicle_in_reservation_main.setImageDrawable(getDrawable(R.drawable.next_arrow))
            cl_vehicle_in_reservation_main.isEnabled = true

            showMainView()

        }else{
            cl_vehicle_in_reservation_main.isEnabled = false
            ct_vehicle_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_disabled_section))
            ct_date_time_vehicle_in_reservation_main.setTextColor(ContextCompat.getColor(this,R.color.reservation_disabled_section))
            iv_next_vehicle_in_reservation_main.setImageDrawable(getDrawable(R.drawable.next_arrow_disabled))
        }

    }

    fun showCostCalculationLoading(){
        cl_cost_loading_in_confirm_reserve_2.visibility = View.VISIBLE
        cl_cost_loading_in_confirm_reserve_1.ct_loading_title.text = getString(R.string.calculating_cost)
        cl_cost_loading_in_confirm_reserve_1.visibility = View.VISIBLE
    }

    fun hideCostCalculationLoading(){
        cl_cost_loading_in_confirm_reserve_2.visibility = View.GONE
        cl_cost_loading_in_confirm_reserve_1.visibility = View.GONE
    }

    override fun showLoadingForReservation(message:String) {
        reservation_activity_loading_view.ct_loading_title.text = message
        reservation_activity_loading_view.visibility= View.VISIBLE
    }

    override fun hideLoadingForReservation() {
        reservation_activity_loading_view.visibility= View.GONE
    }

    override fun onAvailableVehiclesSuccess() {
        showVehiclesView()
        hideLoadingForReservation()
    }

    override fun onAvailableVehiclesFailure() {
        hideLoadingForReservation()
        showServerGeneralError(REQUEST_CODE_ERROR)
     }

    override fun onCostEstimationSuccess() {
        ct_bike_price_value_in_confirm_reserve.setText(CurrencyUtil.getCurrencySymbolByCode(presenter.costEstimate?.currency,presenter.costEstimate?.amount))
        hideCostCalculationLoading()
    }

    override fun onCostEstimationFailure() {
        hideLoadingForReservation()
        showServerGeneralError(REQUEST_CODE_ERROR)
    }

    override fun onBikeSelected(position: Int) {
        presenter.selectedVehicle = presenter.availableVehicleList?.get(position)
        calculateCostEstimate()
        showConfirmationView(presenter.selectedVehicle!!)
    }

    fun calculateCostEstimate(){
        showCostCalculationLoading()
        presenter.calculateCostEstimate()
    }

    override fun onMapSelected(position: Int) {
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.PARKING_VIEW, FirebaseUtil.WHILE_RESERVING_BIKE_PARKING_VIEW)
        startActivity(
            ParkingActivity.getIntent(this,
            if(presenter.currentUserLocation!=null)presenter.currentUserLocation?.latitude else null,
            if(presenter.currentUserLocation!=null) presenter.currentUserLocation?.longitude else null,
                presenter.availableVehicleList?.get(position)?.fleet_id!!,presenter.availableVehicleList?.get(position)?.bike_id!!))
    }

    override fun onLocationPermissionsAvailable() {
        requestLocationUpdates()
    }
    override fun onLocationPermissionsDenied() {

    }

    override fun setUserPosition(location: Location) {

    }

    override fun onBikeInfoSelected(position: Int) {
        presenter.bikeInfoViewFromConfirm =false
        showBikeInfoView(presenter.availableVehicleList?.get(position)!!)
    }

    override fun onReservenSuccess() {
        PopUpActivity.launchForResult(
            this,
            REQUEST_CODE_RESERVATION_SUCCESS,
            getString(R.string.confirmation),
            getString(R.string.reservation_confirmed),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }

    override fun onReserveFailure() {
        cl_confirm_in_reservation_confirm.isEnabled = true
        btn_confirm_in_confirm_reserve.visibility=View.VISIBLE
        pb_progress_in_confirm_reserve.visibility = View.GONE
    }

    fun openBikeTermsAndCondition(){
        if(!TextUtils.isEmpty(presenter.selectedVehicle?.fleet?.t_and_c)) {
            WebviewActivity.launchForResult(this,
                REQUEST_CODE_TERMS_AND_CONDITION,presenter.selectedVehicle?.fleet?.t_and_c!!)
        }
    }

    fun openParkingSpotAndZone(){
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.PARKING_VIEW, FirebaseUtil.WHILE_RESERVING_BIKE_PARKING_VIEW)
        if(presenter.selectedVehicle!=null && presenter.selectedVehicle?.fleet!=null && presenter.selectedVehicle?.fleet?.fleet_id!=null) {
            startActivity(
                ParkingActivity.getIntent(
                    this,
                    if (presenter.currentUserLocation != null) presenter.currentUserLocation?.latitude else null,
                    if (presenter.currentUserLocation != null) presenter.currentUserLocation?.longitude else null,
                    presenter.selectedVehicle?.fleet?.fleet_id!!,presenter.selectedVehicle?.bike_id!!
                )
            )
        }
    }


    //// open different activity :start
    fun launchPhoneNumberFillingActivity() {
        PopUpEditActivity.launchForResult(
            this,
            BikeListFragment.REQUEST_CODE_ADD_PHONE_NUMBER,
            getString(R.string.phone_update_note),
            null,
            R.drawable.phone_number,
            getString(R.string.send_verification_code),
            String.format("+%d ", presenter.countryCode) ,
            false,
            getString(R.string.phone_number),
            InputType.TYPE_CLASS_TEXT
        )
    }

    fun openCodeValidationActivity(){
        PopUpEditActivity.launchForResult(
            this,
            BikeListFragment.REQUEST_CODE_VALIDATE_PHONE_NUMBER,
            getString(R.string.validate_code_for_phone_number_title,presenter.phoneNumber),
            null,
            R.drawable.icon_confrimation_code,
            getString(R.string.submit),
            null,
            false,
            getString(R.string.hint_enter_code),
            InputType.TYPE_CLASS_NUMBER
        )
    }

    override fun onCodeSentSuccess() {
        openCodeValidationActivity()
    }

    override fun onCodeSentFailure() {
        showServerGeneralError(BikeListFragment.REQUEST_CODE_ADD_PHONE_NUMBER_ERROR)
    }

    override fun onCodeValidateFailure() {
        showServerGeneralError(BikeListFragment.REQUEST_CODE_ADD_PHONE_NUMBER_ERROR)
    }

    override fun onUserProfileSuccess() {
        hideLoadingForReservation()
    }

    //// open different activity :end


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BikeListFragment.PHONE_NUMBER_ERROR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            launchPhoneNumberFillingActivity()
        } else if(requestCode== BikeListFragment.REQUEST_CODE_ADD_PHONE_NUMBER &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            showLoadingForReservation(getString(R.string.loading))
            presenter.phoneNumber = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.sendCodeToUpdatePhoneNumber()
        }else if(requestCode== BikeListFragment.REQUEST_CODE_VALIDATE_PHONE_NUMBER &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            showLoadingForReservation(getString(R.string.loading))
            presenter.code = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.validateCodeForUpdatePhoneNumber()
        }else if(requestCode == BikeListFragment.CARD_ERROR_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            launchAddPaymentCardActivity()
        }else if(requestCode== BikeListFragment.REQUEST_ADD_PAYMENT_CARD && resultCode == Activity.RESULT_OK){
            presenter.fetchCardList()
        }else if(requestCode == REQUEST_CODE_RESERVATION_SUCCESS){
            finishWithSuccess()
        }else if (requestCode == BikeListFragment.MAKE_CARD_PRIMARY_REQUEST_CODE){
            presenter.fetchCardList()
        }else if(requestCode == BikeListFragment.PRIMARY_CARD_ERROR_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            launchMakeCardPrimaryActivity()
        }
    }

    fun finishWithSuccess(){
        val intent = Intent()
        intent.putExtra(RESERVE,presenter.reserve)
        setResult(RESULT_OK,intent)
        finish()
    }

    fun launchAddPaymentCardActivity(){
        AddPaymentCardActivity.launchForResult(this,
            BikeListFragment.REQUEST_ADD_PAYMENT_CARD,null)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
    
    
    
      ////rentalfare :start

    fun showRentalFarePop(bike:Bike){
        if(presenter.bikeHasPricingOptionSelection()){
            layout_rental_fare_in_reservation_activity.visibility= VISIBLE
            layout_rental_fare_in_reservation_activity.ct_pay_per_use_value_in_rental_fare.text = getBikeCost(bike)
            layout_rental_fare_in_reservation_activity.rv_rental_fare_in_rental_fare.setLayoutManager(
                LinearLayoutManager(this)
            )
            layout_rental_fare_in_reservation_activity.rv_rental_fare_in_rental_fare.adapter = RentalFareAdapter(this,this,bike.fleet?.pricing_options,presenter.rentalFareSelected.finalRentalFareSelectedIndex)
        }
    }

    fun hideRentalFarePop(){
        layout_rental_fare_in_reservation_activity.visibility= GONE
    }

    fun payPerUseClicked(){
        presenter.onPayPerUseClicked()
        layout_rental_fare_in_reservation_activity.iv_pay_per_use_value_in_rental_fare.setImageResource(R.drawable.check_mark)
        (layout_rental_fare_in_reservation_activity.rv_rental_fare_in_rental_fare.adapter as RentalFareAdapter).reset()
    }

    override fun onRentalFareSelected(position:Int){
        layout_rental_fare_in_reservation_activity.iv_pay_per_use_value_in_rental_fare.setImageResource(0)
        presenter.onRentalFareClicked(position)
    }

    fun rentalFareConfirmationClicked(){
        presenter.onRentalFareConfirmationClicked(if(view_flipper_in_reservation.displayedChild == CONFIRM)ReservationActivityPresenter.PricingOptionsSelectionReason.COST else ReservationActivityPresenter.PricingOptionsSelectionReason.MAIN)
    }

    fun rentalFareSelectionCancelled(){
        presenter.onRentalFareSelectionCancelled()
        hideRentalFarePop()
    }

    fun resetRentalFare(){
        presenter.resetRentalFare()
        if(layout_rental_fare_in_reservation_activity.rv_rental_fare_in_rental_fare.adapter is RentalFareAdapter){
            (layout_rental_fare_in_reservation_activity.rv_rental_fare_in_rental_fare.adapter as RentalFareAdapter).reset()
        }
        layout_rental_fare_in_reservation_activity.iv_pay_per_use_value_in_rental_fare.setImageResource(0)
    }

    override fun showPayPerUse() {
        setPayPerUseOrPricingOption(presenter.referencedBike!!)
        hideRentalFarePop()
    }

    override fun showRentalFare() {
        setPayPerUseOrPricingOption(presenter.referencedBike!!)
        hideRentalFarePop()
    }

    override fun onBikeReserveFailureDuePricingOptionSelectionRemaining() {
        showRentalFarePop(presenter.referencedBike!!)
    }


    fun setPayPerUseOrPricingOption(bike:Bike){
        if(bike.fleet?.pricing_options!=null && bike.fleet?.pricing_options?.size!!>0 ){
            if(presenter.isPayPerUse()){
                setPayPerUse(bike)
            }else if(presenter.isPricingOption()){
                setPricingOption()
            }else{  //nothing selected
                setSelectPricingOption()
            }
        } else{ // no pricing option
            setPayPerUse(bike)
        }
    }

    override fun enableCalculateCostEstimate(){
        if(view_flipper_in_reservation.displayedChild == CONFIRM){
            calculateCostEstimate()
        }
    }

    fun setSelectPricingOption(){
        ct_pricing_options_vehicle_in_reservation_main.text = getString(R.string.select_pricing)
        ct_pricing_options_vehicle_in_confirm_reserve.text = getString(R.string.select_pricing)
    }

    fun setPricingOption(){
        ct_pricing_options_vehicle_in_reservation_main.text =
            BikeFareUtil.getRentalFare(this, presenter.getSelectedRentalFare())

        ct_pricing_options_vehicle_in_confirm_reserve.text =
            BikeFareUtil.getRentalFare(this, presenter.getSelectedRentalFare())
    }

    fun setPayPerUse(bike:Bike){
        ct_pricing_options_vehicle_in_reservation_main.text = getBikeCost(bike)
        ct_pricing_options_vehicle_in_confirm_reserve.text = getBikeCost(bike)
    }

    override fun setPricingOptionsUI(state: Boolean) {
        cl_pricing_options_in_reservation_main.visibility = if(state) VISIBLE else GONE
        cl_pricing_options_in_confirm_reserve.visibility = if(state) VISIBLE else GONE
    }

    ////rentalfare :end
}