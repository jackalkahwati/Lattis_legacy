package com.lattis.lattis.presentation.bikelist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lattis.domain.models.Reserve
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Card
import com.lattis.domain.models.Location
import com.lattis.domain.models.User
import com.lattis.lattis.presentation.base.fragment.location.BaseLocationFragment
import com.lattis.lattis.presentation.bikelist.BikeListFragmentPresenter.Companion.BIKE_SELECTED
import com.lattis.lattis.presentation.home.activity.HomeActivity
import com.lattis.lattis.presentation.library.sliding.SlideUp
import com.lattis.lattis.presentation.library.sliding.SlideUpBuilder
import com.lattis.lattis.presentation.parking.ParkingActivity
import com.lattis.lattis.presentation.payment.PaymentActivity
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.popup.edit.PopUpEditActivity
import com.lattis.lattis.presentation.qrscan.ScanBikeQRCodeActivity
import com.lattis.lattis.presentation.qrscan.ScanBikeQRCodeActivity.Companion.QR_CODE_SCANNING_RIDE_STARTED
import com.lattis.lattis.presentation.rentalfare.RentalFareAdapter
import com.lattis.lattis.presentation.rentalfare.RentalFareClickListener
import com.lattis.lattis.presentation.reservation.AvailableVehiclesActionListener
import com.lattis.lattis.presentation.reservation.ReservationActivity
import com.lattis.lattis.presentation.search_places.SearchPlacesActivity
import com.lattis.lattis.presentation.search_places.SearchPlacesActivity.Companion.SEARCH_BIKE_ID
import com.lattis.lattis.presentation.search_places.SearchPlacesActivity.Companion.SEARCH_LOCATION_LATITUDE
import com.lattis.lattis.presentation.search_places.SearchPlacesActivity.Companion.SEARCH_LOCATION_LONGITUDE
import com.lattis.lattis.presentation.utils.*
import com.lattis.lattis.presentation.utils.BikeFareUtil.getRentalFare
import com.lattis.lattis.presentation.utils.BikeFareUtil.getRentalTimeLimit
import com.lattis.lattis.presentation.utils.MapboxUtil.CLUSTER_LAYER_ARRAY
import com.lattis.lattis.presentation.utils.MapboxUtil.CLUSTER_LAYER_NUMBER
import com.lattis.lattis.presentation.utils.MapboxUtil.COUNT_LAYER
import com.lattis.lattis.presentation.utils.MapboxUtil.MARKER_ID
import com.lattis.lattis.presentation.utils.MapboxUtil.MARKER_LAYER
import com.lattis.lattis.presentation.utils.MapboxUtil.MARKER_SOURCE
import com.lattis.lattis.presentation.utils.MapboxUtil.MARKER_TYPE
import com.lattis.lattis.presentation.utils.MapboxUtil.POINT_COUNT
import com.lattis.lattis.presentation.utils.MapboxUtil.cluster
import com.lattis.lattis.presentation.utils.MapboxUtil.convertDpToPixel
import com.lattis.lattis.presentation.utils.MapboxUtil.generateAndAddHubDockMarker
import com.lattis.lattis.presentation.utils.MapboxUtil.generateHubParkingMarker
import com.lattis.lattis.presentation.utils.MapboxUtil.hub_dock_bike
import com.lattis.lattis.presentation.utils.StrictTCUtil.getStrictTCLink
import com.lattis.lattis.presentation.utils.StrictTCUtil.getStrictTCString
import com.lattis.lattis.presentation.utils.StrictTCUtil.hasStrictTC
import com.lattis.lattis.presentation.webview.WebviewActivity
import com.lattis.lattis.utils.ResourceHelper
import com.lattis.lattis.utils.ResourceHelper.getBikeType
import com.lattis.lattis.utils.ResourceHelper.parkingStation
import com.lattis.lattis.utils.ResourceHelper.setBatteryImageAndText
import com.lattis.lattis.utils.communication.AndroidBus
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.lattis.lattis.R
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_bikelist.*
import kotlinx.android.synthetic.main.fragment_bikelist_slider.*
import kotlinx.android.synthetic.main.fragment_bikelist_slider.view.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_bike_card.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_bike_card.bike_card_parent
import kotlinx.android.synthetic.main.fragment_bikelist_with_bike_card.cl_bike_card_bike_details
import kotlinx.android.synthetic.main.fragment_bikelist_with_bike_card.view.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_confirm_reserve.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_confirm_reserve.cl_payment_confirm_reserve
import kotlinx.android.synthetic.main.fragment_bikelist_with_confirm_reserve.view.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_find_bike_card.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_find_bike_card.view.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_hub_bikes.*
import kotlinx.android.synthetic.main.layout_rental_fare_main.view.*
import kotlinx.android.synthetic.main.layout_strict_tc.view.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class BikeListFragment : BaseLocationFragment<BikeListFragmentPresenter,BikeListFragmentView>()
    ,BikeListFragmentView,AvailableVehiclesActionListener, RentalFareClickListener {
    @Inject
    override lateinit var presenter: BikeListFragmentPresenter;
    override val fragmentLayoutId = R.layout.fragment_bikelist;
    override var view: BikeListFragmentView = this

    private var slideUp: SlideUp?=null
    private lateinit var animationDown:Animation
    private lateinit var animationUp:Animation

    private var firstTimeLoading=true
    private var mapDelayTimerDisposable:Disposable?=null
    private var MILLISECONDS_FOR_MAP_MOVE_BUFFER = 1000
    private var publishSubjectDisposable:Disposable?=null

    private val clusterQueryLayerIds:Array<String?> = arrayOfNulls<String>(CLUSTER_LAYER_ARRAY.size)



    companion object{

        val PHONE_NUMBER_ERROR_REQUEST_CODE = 3242
        val REQUEST_CODE_PHONE_NUMBER_ADD = 3243
        val CARD_ERROR_REQUEST_CODE = 3244
        val REQUEST_CODE_QR_CODE_SCAN = 3245
        val REQUEST_CODE_SEARCH_PLACES = 3246
        val REQUEST_ADD_PAYMENT_CARD = 3247
        val REQUEST_CODE_ADD_PHONE_NUMBER = 3248
        val REQUEST_CODE_VALIDATE_PHONE_NUMBER = 3249
        val REQUEST_CODE_ADD_PHONE_NUMBER_ERROR = 3250
        val REQUEST_CODE_TERMS_AND_CONDITION = 3251
        val REQUEST_CODE_RESERVATION = 3252
        val REQUEST_CODE_RESERVATION_CONFIRMATION = 3253
        val REQUEST_CODE_APP_SETTINGS = 3254
        val REQUEST_PREAUTH_ERROR_REQUEST_CODE =3255
        val MAKE_CARD_PRIMARY_REQUEST_CODE = 3256
        val PRIMARY_CARD_ERROR_REQUEST_CODE = 3257
        val REQUEST_BIKE_ALREADY_RENTED_REQUEST_CODE =3258
        val RESERVE = "RESERVE"

        fun getInstance(context: Context):Fragment{
            return BikeListFragment()
        }

        private const val VIEW_FIND_BIKE = 0
        private const val VIEW_BIKE_CARD = 1
        private const val VIEW_CONFIRM_RESERVE = 2
        private const val VIEW_DOCK_HUBS_BIKES = 3
        private const val VIEW_DOCK_HUB_BIKE_INFO = 4
    }




    override fun onDestroy() {
        super.onDestroy()
        disposeEventListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposeEventListener()
        publishSubjectDisposable =  AndroidBus.stringPublishSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
            {
                if (it.equals(QR_CODE_SCANNING_RIDE_STARTED)) {
                    removeLayerAndSource()
                    (activity as HomeActivity).showLoadingForHome(getString(R.string.starting_ride_loader))
                    (activity as HomeActivity).startShowingActiveTripFragment(true)
                }
            },{
                Log.e("","")

            }
        )
    }

    fun disposeEventListener(){
        if(publishSubjectDisposable!=null){
            publishSubjectDisposable?.dispose()
            publishSubjectDisposable=null
        }
    }

    override fun configureViews() {
        super.configureViews()

        presenter.getUser
        fetchCardList()

        startFetchingLocation()

        animationDown = AnimationUtils.loadAnimation(context, R.anim.slide_out_up)
        animationUp = AnimationUtils.loadAnimation(context, R.anim.slide_in_up)


        cl_confirm_in_confirm_reserve.setOnClickListener {
            checkConditionForReserve(false)
        }


        hamburger_in_bike_list.setOnClickListener {
            (activity as HomeActivity).openDrawer()
        }

        iv_close_in_bike_slider.setOnClickListener {
            slideUp?.hide()
        }

        iv_show_in_bike_card.setOnClickListener {
            slideUp?.show()
        }

        refresh_bike_list_in_bike_card.setOnClickListener {
            showBikesAndCurrentLocation()
        }

        refresh_bike_list_in_find_bike.setOnClickListener {
            showBikesAndCurrentLocation()
        }

        btn_reserve_in_bike_card.setOnClickListener {
            if(presenter.previouslySelectedBike?.reservation_settings!=null){
                showScheduleOrQuickReservationOptionPopup()
            }else {
                showConfirmReserve()
            }
        }
        btn_close_in_confirm_reserve.setOnClickListener {
            animateConfirmBackground(R.color.confirm_reserve_semi_transparent_background,android.R.color.transparent,false)
            if(presenter.previouslySelectedParkingHub!=null){   // if parking
                presenter.setSelectedBikeFromHubCardBikeModel() // this is to show hub data and not selected port data
                showBikeCard(presenter.getSelectedBike())
            }else if(presenter.previouslySelectedDockHub!=null){  // if dock hub
                resetPreviousSelected()
                resetSource()
                showFindBike()
            }else { // if normal bike
                showBikeCard(presenter.getSelectedBike())
            }
        }

        parent_container_find_bike.ll_scan_in_find_bike.setOnClickListener {
            FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.QR_CODE_SCAN_MAIN, FirebaseUtil.QR_CODE_SCAN_MAIN)
            startActivityForResult(
                Intent(activity, ScanBikeQRCodeActivity::class.java),
                BikeListFragment.REQUEST_CODE_QR_CODE_SCAN
            )
        }

        bike_card_parent.ll_scan_in_bike_card.setOnClickListener {
            FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.QR_CODE_SCAN_VEHICLE, FirebaseUtil.QR_CODE_SCAN_VEHICLE)
            startActivityForResult(
                Intent(activity, ScanBikeQRCodeActivity::class.java),
                BikeListFragment.REQUEST_CODE_QR_CODE_SCAN
            )
        }

        cv_search_bar.setOnClickListener {
            startActivityForResult(
                Intent(activity, SearchPlacesActivity::class.java),
                REQUEST_CODE_SEARCH_PLACES
            )
        }

        ct_bike_book_terms_policy.setOnClickListener {
            openBikeTermsAndCondition()
        }

        ct_terms_condition_in_bike_slider.setOnClickListener {
            openBikeTermsAndCondition()
        }
        fragment_bikelist_hub_bike.ct_terms_condition_in_bike_slider.setOnClickListener {
            openBikeTermsAndCondition()
        }


        iv_terms_condition_value_in_bike_slider.setOnClickListener {
            openBikeTermsAndCondition()
        }

        ct_add_credit_card_in_confirm_reserve.setOnClickListener {
            launchAddPaymentCardActivity()
        }

        iv_add_credit_card_in_confirm_reserve.setOnClickListener {
            launchAddPaymentCardActivity()
        }


        iv_parking_spot_zone_value_in_bike_slider.setOnClickListener {
            launchParkingActivity()
        }
        ct_parking_spot_zone_in_bike_slider.setOnClickListener {
            launchParkingActivity()
        }
        fragment_bikelist_hub_bike.ct_parking_spot_zone_in_bike_slider.setOnClickListener {
            launchParkingActivity()
        }

        iv_close_in_dock_hub_bike_list_vehicles.setOnClickListener {
            if(presenter.previouslySelectedParkingHub!=null){
                showBikeCard(presenter.getSelectedBike())
            }else{
                resetPreviousSelected()
                resetSource()
                showFindBike()
            }
        }

        fragment_bikelist_hub_bike.iv_close_in_bike_slider.setOnClickListener {
            showHubOrParkingBikes()
        }

        view_flipper_in_bike_list.setInAnimation(animationUp)
        view_flipper_in_bike_list.setOutAnimation(animationDown)


        ct_rates_in_bike_card.setOnClickListener {
            showRentalFarePop(presenter.getSelectedBike()!!)
        }

        layout_rental_fare_in_bikelist.iv_close_in_rental_fare.setOnClickListener {
            hideRentalFarePop()
        }

        layout_rental_fare_in_bikelist.cl_pay_per_use_in_rental_fare.setOnClickListener {
            payPerUseClicked()
        }
        layout_rental_fare_in_bikelist.btn_confirm_in_rental_fare.setOnClickListener {
            rentalFareConfirmationClicked()
        }

        layout_rental_fare_in_bikelist.iv_close_in_rental_fare.setOnClickListener {
            rentalFareSelectionCancelled()
        }

        cl_bike_price_in_confirm_reserve.setOnClickListener {
            showRentalFarePop(presenter.getSelectedBike()!!)
        }

        ////Strict TC :start
        layout_strict_tc_in_confirm_reserve.btn_cancel_in_strict_tc.setOnClickListener {
            layout_strict_tc_in_confirm_reserve.visibility= GONE
            layout_strict_tc_in_confirm_reserve.sm_1_in_strict_tc.isChecked = false
            layout_strict_tc_in_confirm_reserve.sm_2_in_strict_tc.isChecked = false
            layout_strict_tc_in_confirm_reserve.btn_accept_unselected_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_confirm_reserve.btn_accept_selected_in_strict_tc.visibility = GONE
        }

        layout_strict_tc_in_confirm_reserve.btn_accept_selected_in_strict_tc.setOnClickListener {
            layout_strict_tc_in_confirm_reserve.visibility= GONE
            checkConditionForReserve(true)
        }

        layout_strict_tc_in_confirm_reserve.sm_1_in_strict_tc.setOnCheckedChangeListener { buttonView, isChecked ->
            handleAcceptCancelInStrictTC()
        }

        layout_strict_tc_in_confirm_reserve.sm_2_in_strict_tc.setOnCheckedChangeListener { buttonView, isChecked ->
            handleAcceptCancelInStrictTC()
        }
        ////Strict TC :end



    }

    fun handleAcceptCancelInStrictTC(){
        val isChecked = layout_strict_tc_in_confirm_reserve.sm_1_in_strict_tc.isChecked &&
                layout_strict_tc_in_confirm_reserve.sm_2_in_strict_tc.isChecked
        if(isChecked) {
            layout_strict_tc_in_confirm_reserve.btn_accept_selected_in_strict_tc.visibility =
                VISIBLE
            layout_strict_tc_in_confirm_reserve.btn_accept_unselected_in_strict_tc.visibility =
                GONE
        }else{
            layout_strict_tc_in_confirm_reserve.btn_accept_selected_in_strict_tc.visibility =
                GONE
            layout_strict_tc_in_confirm_reserve.btn_accept_unselected_in_strict_tc.visibility =
                VISIBLE
        }
    }


    fun handleStrictTCPopOpUI() {
        val strings = getStrictTCString()
        if (strings == null) {
            layout_strict_tc_in_confirm_reserve.cl_consent_1_in_strict_tc.visibility = GONE
            layout_strict_tc_in_confirm_reserve.cl_consent_2_in_strict_tc.visibility = GONE
        } else if (strings?.size!! == 1) {
            layout_strict_tc_in_confirm_reserve.ct_consent_1_in_strict_tc.text = strings?.get(0)
            layout_strict_tc_in_confirm_reserve.sm_1_in_strict_tc.isChecked = false
            layout_strict_tc_in_confirm_reserve.cl_consent_1_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_confirm_reserve.sm_2_in_strict_tc.isChecked =
                true  //if only one string, it will make the second true
            layout_strict_tc_in_confirm_reserve.cl_consent_2_in_strict_tc.visibility = GONE
        } else if (strings?.size!! == 2) {
            layout_strict_tc_in_confirm_reserve.ct_consent_1_in_strict_tc.text = strings?.get(0)
            layout_strict_tc_in_confirm_reserve.ct_consent_2_in_strict_tc.text = strings?.get(1)
            layout_strict_tc_in_confirm_reserve.sm_1_in_strict_tc.isChecked = false
            layout_strict_tc_in_confirm_reserve.sm_2_in_strict_tc.isChecked = false
            layout_strict_tc_in_confirm_reserve.cl_consent_1_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_confirm_reserve.cl_consent_2_in_strict_tc.visibility = VISIBLE
        }

        val strictTCLink = getStrictTCLink()
        if (TextUtils.isEmpty(strictTCLink)) {
            layout_strict_tc_in_confirm_reserve.ct_privacy_and_terms_in_strict_tc.visibility = GONE
        } else{
            layout_strict_tc_in_confirm_reserve.ct_privacy_and_terms_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_confirm_reserve.ct_privacy_and_terms_in_strict_tc.text =
                HtmlCompat.fromHtml(
                    String.format(
                        strictTCLink!!,
                        BuildConfigUtil.privacyPolicy(),
                        BuildConfigUtil.termsOfService()
                    ), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
        layout_strict_tc_in_confirm_reserve.ct_privacy_and_terms_in_strict_tc.movementMethod = LinkMovementMethod.getInstance()

        layout_strict_tc_in_confirm_reserve.btn_accept_unselected_in_strict_tc.visibility = VISIBLE
        layout_strict_tc_in_confirm_reserve.btn_accept_selected_in_strict_tc.visibility = GONE
        layout_strict_tc_in_confirm_reserve.visibility= VISIBLE
    }

    fun launchAddPhoneNumberPopUp(){
        launchPopUpActivity(
            PHONE_NUMBER_ERROR_REQUEST_CODE,
            getString(R.string.label_note),
            getString(R.string.mandatory_phone_text),
            null,
            getString(R.string.mandatory_phone_action),
            null,
            null,
            getString(R.string.cancel_capital)
        )
    }

    fun launchAddCardPopUp(){
        launchPopUpActivity(
            CARD_ERROR_REQUEST_CODE,
            getString(R.string.add_credit_card),
            getString(R.string.add_card_description),
            null,
            getString(R.string.add_card),
            null,
            null,
            getString(R.string.cancel_capital)
        )
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

    fun launchParkingActivity(){
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.PARKING_VIEW, FirebaseUtil.BEFORE_BIKE_BOOKED_PARKING_VIEW)
        startActivity(ParkingActivity.getIntent(requireContext(),
            if(presenter.currentUserLocation!=null)presenter.currentUserLocation?.latitude else null,
            if(presenter.currentUserLocation!=null) presenter.currentUserLocation?.longitude else null,
            presenter.getSelectedBike()?.fleet_id!!,presenter.getSelectedBike()?.bike_id!!))
    }

    fun fetchCardList(){
        presenter.fetchCardList()
    }

    fun launchMakeCardPrimaryActivity(){
        startActivityForResult(
            Intent(requireContext(), PaymentActivity::class.java),
            MAKE_CARD_PRIMARY_REQUEST_CODE
        )
    }


    fun launchAddPaymentCardActivity(){
        AddPaymentCardActivity.launchForResultForFragment(requireActivity(),
            this,
            REQUEST_ADD_PAYMENT_CARD,
            null,
            presenter.previouslySelectedBike?.fleet_id,
            presenter.previouslySelectedBike?.payment_gateway
        )
    }

    fun openBikeTermsAndCondition(){
        if(!TextUtils.isEmpty(presenter.getSelectedBike()?.fleet_t_and_c)) {
            WebviewActivity.launchForResultFromFragment(this,requireActivity(),REQUEST_CODE_TERMS_AND_CONDITION,presenter.getSelectedBike()?.fleet_t_and_c!!)
        }
    }



    fun animateConfirmBackground(colorFrom:Int,colorTo:Int,withDelay: Boolean=true){

        if(withDelay) {

            Observable.timer(550, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    bike_card_with_confirm_parent.background =
                        ContextCompat.getDrawable(requireActivity(), colorTo)
                }, {

                })
        }else{
            bike_card_with_confirm_parent.background =
                ContextCompat.getDrawable(requireActivity(), colorTo)
        }
    }

    fun checkConditionForReserve(bypassStrictTC:Boolean){
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.CONFIRM,FirebaseUtil.NORMAL_CONFIRM)
        if(presenter.previouslySelectedBike!=null) {

            if (!presenter.phoneNumberCheckPassed()) {
                launchAddPhoneNumberPopUp()
            }else if(hasStrictTC() && !bypassStrictTC){
                handleStrictTCPopOpUI()
            } else{
                if (!IsRidePaid.isRidePaidForFleet(presenter.previouslySelectedBike?.fleet_type)) {
                    tryReserveBike()
                } else {
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
    }

    fun tryReserveBike(){
        cl_confirm_in_confirm_reserve.isEnabled = false
        pb_progress_in_confirm_reserve.visibility = View.VISIBLE
        btn_confirm_in_confirm_reserve.visibility=View.GONE
        presenter.reserveBike()
    }

    fun startFetchingLocation(){
        fetchLocation()
    }

    override fun onLocationPermissionsAvailable() {
        requestLocationUpdates()
    }

    override fun onLocationPermissionsDenied() {
        launchPopUpActivity(
            REQUEST_CODE_APP_SETTINGS,
            getString(R.string.general_error_title),
            getString(R.string.app_settings_label),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }

    fun openAppSettings(){
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity?.packageName, null)
        })
        activity?.finish()
    }

    override fun setUserPosition(location: Location) {  //mapbox padding given by this
        if(firstTimeLoading){
            firstTimeLoading=false
            showBikesAndCurrentLocation()
        }
    }

    fun showBikesAndCurrentLocation(){
        if(presenter.currentUserLocation!=null) {
            MapboxUtil.showUserCurrentLocation(getMapboxMap(), presenter?.currentUserLocation!!)
            MapboxUtil.setFixedZoomForSinglePoint(
                getMapboxMap(),
                LatLng(presenter?.currentUserLocation!!.latitude, presenter?.currentUserLocation!!.longitude),
                (cv_search_bar.height + convertDpToPixel(50.00)).toDouble(),
                (cl_in_find_bike_bottom_view.height + 350.00).toDouble()
            )
            getBikesForFirstTime()
        }else{
            firstTimeLoading=true
        }
    }


    fun configureBikeSlider(bike: Bike?){
        if(slideUp==null) {
            slideUp = SlideUpBuilder(cl_bike_details_slide_up)
                .withListeners(object : SlideUp.Listener.Events {
                    override fun onSlide(percent: Float) {
                        Log.e("BikeListFragment", "Percentage: " + percent)

                        if (percent > 20) {

                        } else if (percent < 20) {

                        }
                    }

                    override fun onVisibilityChanged(visibility: Int) {

                        Log.e("BikeListFragment", "Visibility: " + visibility)
                        if (visibility == View.GONE) {
//                            cl_bike_card_bike_details.visibility = View.VISIBLE
                        } else if (visibility == View.VISIBLE) {
//                            cl_bike_card_bike_details.visibility = View.GONE
                        }
                    }
                })
                .withStartGravity(Gravity.BOTTOM)
                .withLoggingEnabled(true)
                .withGesturesEnabled(true)
                .withStartState(SlideUp.State.HIDDEN)
                .withSlideFromOtherView(cl_bike_card_bike_details)
                .build()
        }else{
            if(slideUp?.isVisible?:false)
                slideUp?.hide()
        }



    }

    fun showScheduleOrQuickReservationOptionPopup(){
        PopUpActivity.launchForResultFromFragment(
            this,
            requireActivity(),
            REQUEST_CODE_RESERVATION,
            getString(R.string.reserve_now_title),
            getString(R.string.reserve_now_message),
            null,
            getString(R.string.reserve_now_button),
            getString(R.string.schedule_reservation),
            null,
            getString(R.string.cancel_capital)
        )
    }

    fun showConfirmReserve(){
        if(presenter.previouslySelectedParkingHub!=null){
            showParkingHubBikes()
        }else{
            showConfirmReserveScreen()
        }
    }

    fun showConfirmReserveScreen(){
        if(view_flipper_in_bike_list.displayedChild != VIEW_CONFIRM_RESERVE)
            view_flipper_in_bike_list.displayedChild = VIEW_CONFIRM_RESERVE

        if(!TextUtils.isEmpty(presenter.getSelectedBike()?.fleet_t_and_c)) {
            ct_bike_book_terms_policy.text = HtmlCompat.fromHtml(getString(R.string.bike_details_terms_policy, presenter.getSelectedBike()?.fleet_t_and_c), HtmlCompat.FROM_HTML_MODE_LEGACY)
            ct_bike_book_terms_policy.visibility = View.VISIBLE
        }else{
            ct_bike_book_terms_policy.visibility = INVISIBLE
        }

        animateConfirmBackground(android.R.color.transparent,R.color.confirm_reserve_semi_transparent_background)

    }

    fun showFindBike(){
        if(view_flipper_in_bike_list.displayedChild != VIEW_FIND_BIKE)
            view_flipper_in_bike_list.displayedChild = VIEW_FIND_BIKE

    }

    fun refreshBikeCard(){
        showBikeCard(presenter.getSelectedBike())
    }

    fun showHubOrParkingBikes(){
        if(presenter.previouslySelectedDockHub!=null)showDockHubBikes() else if(presenter.previouslySelectedParkingHub!=null) showParkingHubBikes()
    }

    fun showDockHubBikes(){
        presenter.resetPreviouslySelectedBike()
        presenter.resetPreviouslySelectedParkingHub()
        val dockHub = presenter.getSelectedDockHub()
        if(dockHub==null || dockHub.bikes==null)
            return

        ct_title_in_dock_hub_bike_list_vehicles.text = dockHub.hub_name + "(" + dockHub.bikes?.size!! + ")"

        rv_in_dock_hub_bike_list_vehicles.setLayoutManager(LinearLayoutManager(requireContext()))
        rv_in_dock_hub_bike_list_vehicles.setAdapter(DockHubBikesListAdapater(requireContext(), presenter.getDockHubBikes(dockHub!!),  this))

        if(view_flipper_in_bike_list.displayedChild != VIEW_DOCK_HUBS_BIKES)
            view_flipper_in_bike_list.displayedChild = VIEW_DOCK_HUBS_BIKES
    }

    fun showParkingHubBikes(){
//        presenter.resetPreviouslySelectedBike()
        presenter.resetPreviouslySelectedDockHub()
        val dockHub = presenter.getSelectedParkingHub()
        if(dockHub==null || dockHub.ports==null)
            return

        ct_title_in_dock_hub_bike_list_vehicles.text = dockHub.hub_name + "(" + dockHub.ports?.size!! + ")"

        rv_in_dock_hub_bike_list_vehicles.setLayoutManager(LinearLayoutManager(requireContext()))
        rv_in_dock_hub_bike_list_vehicles.setAdapter(ParkingHubPortsListAdapater(requireContext(), presenter.getSelectedParkingHub()!!,  this))

        if(view_flipper_in_bike_list.displayedChild != VIEW_DOCK_HUBS_BIKES)
            view_flipper_in_bike_list.displayedChild = VIEW_DOCK_HUBS_BIKES
    }

    fun showBikeInfoViewForHubs(){
        val bike = presenter.getSelectedBike()!!

        if(view_flipper_in_bike_list.displayedChild != VIEW_DOCK_HUB_BIKE_INFO)
            view_flipper_in_bike_list.displayedChild = VIEW_DOCK_HUB_BIKE_INFO

        fragment_bikelist_hub_bike.view_divider_in_bike_slider.visibility=View.GONE

        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(this)
            .load(bike?.pic)
            .apply(requestOptions)
            .into(fragment_bikelist_hub_bike.iv_bike_in_bike_slider)

        fragment_bikelist_hub_bike.ct_fleet_name_in_bike_slider.setText(bike.fleet?.fleet_name)
        fragment_bikelist_hub_bike.ct_bike_name_in_bike_slider.setText(bike.bike_name)
        fragment_bikelist_hub_bike.ct_bike_type_in_bike_slider.setText(ResourceHelper.getBikeType(bike.bike_group?.type, requireContext()))
        fragment_bikelist_hub_bike.ct_bike_network_name_in_bike_slider.setText(bike.fleet?.fleet_name)

        ResourceHelper.setBatteryImageAndText(
            bike.bike_battery_level,
            fragment_bikelist_hub_bike.iv_bike_battery_bike_slider,
            fragment_bikelist_hub_bike.ct_bike_battery_bike_slider
        )
        fragment_bikelist_hub_bike.ct_bike_about_name_value_in_bike_slider.setText(bike.bike_name)
        fragment_bikelist_hub_bike.ct_bike_about_description_value_in_bike_slider.setText(bike.bike_group?.description)
        fragment_bikelist_hub_bike.ct_bike_about_model_value_in_bike_slider.setText(
            ResourceHelper.getBikeType(bike.bike_group?.type, requireContext())
        )

        setAllPriceForHubs(bike)

    }

    fun setAllPriceForHubs(bike:Bike){

        fragment_bikelist_hub_bike.ct_bike_price_value_in_bike_slider.setText(getBikeCost(bike))
        fragment_bikelist_hub_bike.cl_unlock_price_label_in_bike_slider.visibility = GONE
        fragment_bikelist_hub_bike.cl_parking_price_label_in_bike_slider.visibility = GONE
        fragment_bikelist_hub_bike.cl_surcharge_price_label_in_bike_slider.visibility = GONE

        setExcessUsageFee(bike)
        setExcessUsageFreq(bike)
        setParkingFee(bike)
        setUnlockFee(bike)
        setPromotionDiscount(bike)
        setPreAuth(bike)
        setRentalTimeLimit(bike)
    }


    override fun onBikeSelected(position: Int) {
        presenter.takeActionAfterBikeSelectedInDockHub(position)
        fillBikeCardConfirmAndSliderData(presenter.getSelectedBike()!!)
        showConfirmReserveScreen()
    }

    override fun onBikeInfoSelected(position: Int) {
        presenter.takeActionAfterBikeSelectedInDockHub(position)
        showBikeInfoViewForHubs()
    }

    override fun onMapSelected(position: Int) {

    }

    fun showBikeCard(bike: Bike?){
        if(bike==null){
            return
        }
        fillBikeCardConfirmAndSliderData(bike)
        if(view_flipper_in_bike_list.displayedChild != VIEW_BIKE_CARD)
            view_flipper_in_bike_list.displayedChild = VIEW_BIKE_CARD

    }

    fun fillBikeCardConfirmAndSliderData(bike: Bike){
        setBikeCardDetails(bike)
        setBikeCardInConfirmReserve(bike)
        setBikeCardSlider(bike)
        configureBikeSlider(bike)

        val membershipDiscount = (activity as HomeActivity).getMembershipDiscount(bike.fleet_id!!)
        if(membershipDiscount!=null){
            cl_membership_popup_bike_card.visibility = View.VISIBLE
            ct_fleet_name_membership_popup_bike_card.text = bike.fleet_name + " " +getString(R.string.member)
            ct_fleet_discount_membership_popup_bike_card.text = getString(R.string.perk_template_bike,membershipDiscount)
//            ct_rates_in_bike_card.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(),R.drawable.menu_membership),null,null,null)

        }else{
            cl_membership_popup_bike_card.visibility = View.GONE
            ct_rates_in_bike_card.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null)
        }


        ct_unlock_price_label_in_confirm_reserve.visibility = GONE
        ct_unlock_price_value_in_confirm_reserve.visibility = GONE
        cl_unlock_price_label_in_bike_slider.visibility = GONE

        ct_bike_parking_in_confirm_reserve.visibility = GONE
        ct_bike_parking_value_in_confirm_reserve.visibility = GONE
        ct_parking_description_in_confirm_reserve.visibility = GONE
        cl_parking_price_label_in_bike_slider.visibility = GONE


        // set surcharge fee in bike card and confirm screen
        ct_surcharge_description_in_confirm_reserve.visibility = GONE
        ct_surcharge_price_value_in_confirm_reserve.visibility = GONE
        ct_surcharge_price_label_in_confirm_reserve.visibility = GONE
        cl_surcharge_price_label_in_bike_slider.visibility = GONE
        setUnlockFee(bike)
        setExcessUsageFee(bike)
        setParkingFee(bike)
        setPreAuth(bike)
        setRentalTimeLimit(bike)
        setPromotionDiscount(bike)
        setMembershipDiscount(bike)

        handleCardDetailsAfterBikeSelection(IsRidePaid.isRidePaidForFleet(bike.fleet_type))

        if(parkingStation(bike.type)){
            cl_parking_spot_zone_in_bike_slider.visibility = GONE
            fragment_bikelist_hub_bike.cl_parking_spot_zone_in_bike_slider.visibility = GONE
        }else{
            cl_parking_spot_zone_in_bike_slider.visibility = VISIBLE
            fragment_bikelist_hub_bike.cl_parking_spot_zone_in_bike_slider.visibility = VISIBLE
        }

    }

    override fun onMapboxMoved(latlongBounds: LatLngBounds){
        Log.e("BikeListFragment","onMapboxMoved")
        mapMoveDelayTimer()
    }

    override fun onMapClicked(screenPoint: PointF) {
        handleClickIcon(screenPoint)
    }

    fun getMapboxMap():MapboxMap{
        return (activity as HomeActivity).mapboxMap
    }

    //// bike list handle :start
    override fun handleBikesAndDockHubs() {

        Observable.timer(
            500,
            TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                startMapboxWork()
            }) { throwable ->

            }
    }

    override fun handleNoBikes() {
        removeLayerAndSource()
    }

    override fun handleError() {
        removeLayerAndSource()
    }

    //// bike list handle :end


    //// mapbox marker :start

    fun startMapboxWork(){
        Log.e("BikeListFragment","startMapboxWork")
        setUpSource()
        setUpLayer(getMapboxMap().style!!)
        setUpClusteredSource()
        if(presenter.getSelectedBike()!=null) {
            showBikeCard(presenter.getSelectedBike())
        }else{
            showFindBike()
        }
    }

    fun setUpSource(){
        removeLayerAndSource()
        val geoJsonSource= GeoJsonSource(MARKER_SOURCE, presenter.featureCollection,
        GeoJsonOptions().withCluster(true).withClusterRadius(50).withClusterMaxZoom(14)
        )
        getMapboxMap().style!!.addSource(geoJsonSource!!)

    }

    fun setUpClusteredSource(){
        // Use the earthquakes GeoJSON source to create three point ranges.
        // Use the earthquakes GeoJSON source to create three point ranges.


        for (i in CLUSTER_LAYER_ARRAY.indices) {

            clusterQueryLayerIds[i] = CLUSTER_LAYER_NUMBER+"$i"
            //Add clusters' SymbolLayers images
            val symbolLayer = SymbolLayer(CLUSTER_LAYER_NUMBER+"$i", MARKER_SOURCE)
            symbolLayer.setProperties(
                iconImage(cluster)
            )
            val pointCount =
                toNumber(get(POINT_COUNT))

            // Add a filter to the cluster layer that hides the icons based on "point_count"
            symbolLayer.setFilter(
                if (i == 0) all(
                    has(POINT_COUNT),
                    gte(pointCount, literal(CLUSTER_LAYER_ARRAY[i]))
                ) else all(
                    has(POINT_COUNT),
                    gte(pointCount, literal(CLUSTER_LAYER_ARRAY[i])),
                    lt(pointCount, literal(CLUSTER_LAYER_ARRAY[i - 1]))
                )
            )
            getMapboxMap().style!!.addLayer(symbolLayer)
        }

        //Add a SymbolLayer for the cluster data number point count

        //Add a SymbolLayer for the cluster data number point count
        getMapboxMap().style!!.addLayer(
            SymbolLayer(COUNT_LAYER, MARKER_SOURCE).withProperties(
                textField(toString(get(POINT_COUNT))),
                textSize(16f),
                textColor(Color.WHITE),
                textIgnorePlacement(true),
//                textOffset(arrayOf(0f, .5f)),
                textAllowOverlap(true)
            )
        )
    }

    private fun setUpLayer(@NonNull loadedMapStyle: Style) {
        Log.e("BikeListFragment","setUpLayer")

            loadedMapStyle.addLayer(
                SymbolLayer(MARKER_LAYER, MARKER_SOURCE)
                    .withProperties(
                        iconImage("{poi}"),  /* allows show all icons */
                        iconAllowOverlap(true), /* when feature is in selected state, grow icon */
                        iconIgnorePlacement(true),
                        iconOffset(
                            arrayOf(0f, -9f)),
                        iconSize(
                            match(
                                Expression.toString(get(BIKE_SELECTED)), // property selected is a number
                                literal(1.0f),        // default value
                                stop("false", 1.0),
                                stop("true", 1.4)         // if selected set it to original size
                            )
                        )
                    )
            )
    }

    private fun handleClickIcon(screenPoint: PointF): Boolean {
        val features: List<Feature> =
            getMapboxMap().queryRenderedFeatures(screenPoint, MARKER_LAYER)
        if (!features.isEmpty()) {
            var selectedFeature = features.get(0)
            for (feature in presenter.featureCollection.features()!!) {
                if(selectedFeature.hasProperty(MARKER_TYPE) &&
                    selectedFeature.hasProperty(MARKER_ID) &&
                    selectedFeature.getStringProperty(MARKER_TYPE).equals(hub_dock_bike) &&
                    feature.hasProperty(MARKER_ID) &&
                    feature.getNumberProperty(MARKER_ID).toInt() == selectedFeature.getNumberProperty(MARKER_ID).toInt()
                ){
                    resetPreviousSelected()
                    selectFeature(feature)
                    presenter.setSelectedDockHub(feature)
                    showDockHubBikes()
                    break
                } else if(selectedFeature.hasProperty(MARKER_TYPE) &&
                    selectedFeature.hasProperty(MARKER_ID) &&
                    selectedFeature.getStringProperty(MARKER_TYPE).equals(MapboxUtil.hub_parking_bike) &&
                    feature.hasProperty(MARKER_ID) &&
                    feature.getNumberProperty(MARKER_ID).toInt() == selectedFeature.getNumberProperty(MARKER_ID).toInt()
                ){
                    resetPreviousSelected()
                    selectFeature(feature)
                    resetRentalFare()
                    presenter.setSelectedParkingHub(feature)
//                    showParkingHubBikes()
                    presenter.setSelectedBikeFromHubCardBikeModel()
                    showBikeCard(presenter.getSelectedBike())
                    break
                } else if(selectedFeature.hasProperty("bike_id") &&
                    feature.hasProperty("bike_id") &&
                    selectedFeature.getNumberProperty("bike_id").toInt()==feature.getNumberProperty("bike_id").toInt()) {
                    resetPreviousSelected()
                    selectFeature(feature)
                    resetRentalFare()
                    presenter.setSelectedBike(feature)
                    showBikeCard(presenter.getSelectedBike())
                    break
                }
            }
            return true
        }

        else { // check if clustered clicked
            val rectF = RectF(screenPoint.x - 10, screenPoint.y - 10, screenPoint.x + 10, screenPoint.y + 10)
            val mapClickFeatureList: List<Feature>? =
                getMapboxMap()!!.queryRenderedFeatures(rectF, *clusterQueryLayerIds)

            if (mapClickFeatureList!=null && mapClickFeatureList.size > 0) {
                val clusterLeavesFeatureCollection: FeatureCollection =
                    (getMapboxMap().style!!.getSource(MARKER_SOURCE) as GeoJsonSource).getClusterLeaves(
                        mapClickFeatureList[0],
                        8000, 0
                    )
                moveCameraToLeavesBounds(clusterLeavesFeatureCollection)
            }
            return true
        }
        return false
    }


    private fun resetSource(){
        Log.e("BikeListFragment","resetSource")
        (getMapboxMap().style!!.getSource(MARKER_SOURCE) as GeoJsonSource).setGeoJson(presenter.featureCollection)
    }


    /**
     * Deselects the state of all the features
     */
    private fun deselectAll() {
        Log.e("BikeListFragment","deselectAll")
        for (feature in presenter.featureCollection.features()!!) {
            feature.properties()!!.addProperty(BIKE_SELECTED, false)
        }
        resetSource()
    }


    /**
     * Selects the state of a feature
     *
     * @param feature the feature to be selected.
     */
    private fun selectFeature(feature: Feature) {
        Log.e("BikeListFragment","selectFeature")
        feature.properties()!!.addProperty(BIKE_SELECTED, true)
        resetSource()
    }

    private fun resetPreviousSelected(){
        Log.d("BikeListFragment","resetPreviousSelected")
        var feature =presenter.getSelectedFeature()
        feature?.properties()?.addProperty(BIKE_SELECTED, false)
    }



    fun removeLayerAndSource(){
        Log.d("BikeListFragment","removeLayerAndSource")
        getMapboxMap().style!!.removeLayer(MARKER_LAYER)

        for (i in CLUSTER_LAYER_ARRAY.indices) {
            getMapboxMap().style!!.removeLayer(CLUSTER_LAYER_NUMBER+"$i")
        }
        getMapboxMap().style!!.removeLayer(COUNT_LAYER)
        getMapboxMap().style?.removeSource(MARKER_SOURCE)
    }


    private fun moveCameraToLeavesBounds(featureCollectionToInspect: FeatureCollection) {
        val latLngList: ArrayList<LatLng> = ArrayList()
        if (featureCollectionToInspect.features() != null) {
            for (singleClusterFeature in featureCollectionToInspect.features()!!) {
                val clusterPoint: Point = singleClusterFeature.geometry() as Point
                if (clusterPoint != null) {
                    latLngList.add(
                        LatLng(
                            clusterPoint.latitude(),
                            clusterPoint.longitude()
                        )
                    )
                }
            }
            if (latLngList.size > 1) {
                val latLngBounds =
                    LatLngBounds.Builder()
                        .includes(latLngList)
                        .build()

                getMapboxMap().easeCamera(
                    CameraUpdateFactory.newLatLngBounds(latLngBounds, convertDpToPixel(20.0).toInt(),cv_search_bar.height+ convertDpToPixel(50.00).toInt(),convertDpToPixel(20.0).toInt(),cl_in_find_bike_bottom_view.height + 350),
                    1000
                )

                Observable.timer(2000,TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        (activity as HomeActivity).centerLatLng = getMapboxMap().cameraPosition.target
                    },{

                    })
            }
        }
    }


    fun mapMoveDelayTimer(){
        Log.d("BikeListFragment","mapMoveDelayTimer")
        mapDelayTimerDisposable?.dispose()
        mapDelayTimerDisposable = Observable.timer(
            MILLISECONDS_FOR_MAP_MOVE_BUFFER.toLong(),
            TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                getBikes()

            }) { throwable ->

            }
    }

    fun getBikesForFirstTime(){
        Observable.timer(
            500.toLong(),
            TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                (activity as HomeActivity).centerLatLng = getMapboxMap().cameraPosition.target
                getBikes()
            }) { throwable ->

            }
    }

    fun getBikes(){
        var latlongBounds = getMapboxMap().projection.getVisibleRegion(false).latLngBounds;
        //  This is for showing the corner data and center point: start
//        presenter.ne = latlongBounds.northEast
//        presenter.sw = latlongBounds.southWest
//        presenter.center = getMapboxMap().cameraPosition.target
        //  This is for showing the corner data and center point: end
        presenter.searchBike(Location(latlongBounds.northEast.latitude,latlongBounds.northEast.longitude)
            ,Location(latlongBounds.southWest.latitude,latlongBounds.southWest.longitude))
    }


    //// bike details :start

    fun setBikeCardDetails(bike: Bike){
        ct_fleet_name_in_bike_card.setText(bike.fleet_name)
        ct_bike_name_in_bike_card.setText(bike.bike_name)
        ct_bike_type_in_bike_card.setText(getBikeType(bike.type,requireContext()))

        if(presenter.previouslySelectedParkingHub!=null){
            iv_bike_battery_bike_card.visibility = GONE
            ct_bike_battery_bike_card.visibility = GONE
            ct_ports_available_bike_card.visibility = GONE
            if(bike.hud!=null && bike.hud?.ports!=null){
                ct_ports_available_bike_card.visibility = VISIBLE
                ct_ports_available_bike_card.text = getString(R.string.available_template,bike?.hud?.ports?.size!!.toString())
            }
        }else{
            ct_ports_available_bike_card.visibility = GONE
            setBatteryImageAndText(bike.bike_battery_level,iv_bike_battery_bike_card,ct_bike_battery_bike_card)
        }



        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(this)
            .load(bike.pic)
            .apply(requestOptions)
            .into(iv_bike_image_in_bike_card)

        setPayPerUseOrPricingOption(bike)

        Glide.with(this)
            .load(bike.fleet_logo)
            .apply(requestOptions)
            .into(iv_fleet_logo_in_bike_card)

    }



    fun setPayPerUseOrPricingOption(bike:Bike){
        if(bike.pricing_options!=null && bike.pricing_options?.size!!>0 ){
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

    fun setSelectPricingOption(){
        iv_select_pricing_option_in_confirm_reserve.visibility = VISIBLE
        ct_rates_in_bike_card.text = getString(R.string.select_pricing)
        ct_bike_price_value_in_confirm_reserve.text = getString(R.string.select_pricing)
    }

    fun setPricingOption(){
        ct_rates_in_bike_card.text = getRentalFare(requireContext(),presenter.getSelectedRentalFare())
        ct_bike_price_value_in_confirm_reserve.text = getRentalFare(requireContext(),presenter.getSelectedRentalFare())
    }

    fun setPayPerUse(bike:Bike){
        ct_rates_in_bike_card.setText(getPayPerUse(bike))
        ct_bike_price_value_in_confirm_reserve.text = getBikeCost(bike)
    }

    fun getPayPerUse(bike:Bike):String{
        val bikeCost = getBikeCost(bike)
        val surchargeCost = getSurchargeCost(bike)
        val unlockCost = getUnlockCost(bike)

        if(bikeCost.equals(getString(R.string.bike_detail_bike_cost_free)) && unlockCost!=null){ // only unlock cost
            return unlockCost
        }else if(bikeCost.equals(getString(R.string.bike_detail_bike_cost_free)) && surchargeCost!=null && unlockCost==null){   // only surcharge cost
            return surchargeCost
        }else if(!bikeCost.equals(getString(R.string.bike_detail_bike_cost_free)) && unlockCost==null){   // only base fare
            return bikeCost.replace(" " + getString(R.string.label_per) + " "," / ")
        }else if(!bikeCost.equals(getString(R.string.bike_detail_bike_cost_free)) && unlockCost!=null){   // base fare and unlock fee
            return unlockCost + " + " + bikeCost.replace(" " + getString(R.string.label_per) + " "," / ")
        }else{  // no surcharge and unlock fee
            return bikeCost.replace(" " + getString(R.string.label_per) + " "," / ")
        }
    }

    fun setBikeCardSlider(bike:Bike){

        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(this)
            .load(bike.pic)
            .apply(requestOptions)
            .into(iv_bike_in_bike_slider)

        ct_fleet_name_in_bike_slider.setText(bike.fleet_name)
        ct_bike_name_in_bike_slider.setText(bike.bike_name)
        ct_bike_type_in_bike_slider.setText(getBikeType(bike.type,requireContext()))
        ct_bike_network_name_in_bike_slider.setText(bike.fleet_name)

        setBatteryImageAndText(bike.bike_battery_level,iv_bike_battery_bike_slider,ct_bike_battery_bike_slider)

        ct_bike_price_value_in_bike_slider.setText(getBikeCost(bike))
        ct_bike_about_name_value_in_bike_slider.setText(bike.bike_name)
        ct_bike_about_description_value_in_bike_slider.setText(bike.description)
        ct_bike_about_model_value_in_bike_slider.setText(getBikeType(bike.type,requireContext()))

    }

    fun setBikeCardInConfirmReserve(bike: Bike){
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(this)
            .load(bike.pic)
            .apply(requestOptions)
            .into(iv_bike_image_in_confirm_reserve)

        ct_bike_type_in_confirm_reserve.setText(getBikeType(bike.type,requireContext()))
        ct_bike_name_in_confirm_reserve.setText(bike.bike_name)
        ct_fleet_name_in_confirm_reserve.setText(bike.fleet_name)

        //1. bike type
        //2. bike name
        //3. fleet_name

        setBatteryImageAndText(bike.bike_battery_level,iv_bike_battery_confirm_reserve,ct_bike_battery_confirm_reserve)



    }

    fun getUnlockCost(bike:Bike):String?{
        if(!TextUtils.isEmpty(bike.price_for_bike_unlock) && bike.price_for_bike_unlock!!.toFloat()!=0F) {
            return getString(R.string.unlock_template,CurrencyUtil.getCurrencySymbolByCode(bike.currency , bike.price_for_bike_unlock
                .toString()))
        }
        return null
    }

    fun getSurchargeCost(bike: Bike):String?{
        if (!TextUtils.isEmpty(bike.excess_usage_fees)
                && (bike.excess_usage_fees?.toFloatOrNull()!=null)
                && (bike.excess_usage_fees?.toFloatOrNull()!=0F)
                && !TextUtils.isEmpty(bike.excess_usage_type_value)
                && !TextUtils.isEmpty(bike.excess_usage_type)
                && !TextUtils.isEmpty(bike.excess_usage_type_after_value)
                && !TextUtils.isEmpty(bike.excess_usage_type_after_type)) {
                return getString(R.string.free_for_duration,
                              LocaleTranslatorUtils.getLocaleString(
                        requireContext(),
                        bike.excess_usage_type_after_type, bike.excess_usage_type_after_value
                                      .toString()
                    ))
        }

        return null
    }

    fun getBikeCost(bike: Bike):String{
        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) &&
            !TextUtils.isEmpty(bike.price_for_membership) && bike.price_for_membership!!.toFloatOrNull()!=null && bike.price_for_membership!!.toFloatOrNull()!=0F) {

            if (bike.price_type != null && !bike.price_type.equals("")) {
                var rideCost =
                    CurrencyUtil.getCurrencySymbolByCode(bike.currency, bike.price_for_membership
                        .toString()) + " " + getString(R.string.label_per) + " " +
                             LocaleTranslatorUtils.getLocaleString(
                        requireContext(),
                        bike.price_type, bike.price_type_value.toString()
                    ).toString()

                return rideCost
            }

        }else{
            return getString(R.string.bike_detail_bike_cost_free)
        }
        return "";
    }

    fun setExcessUsageFee(bike: Bike){
        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) &&
            !TextUtils.isEmpty(bike.excess_usage_fees)
            && (bike.excess_usage_fees?.toFloatOrNull()!=null)
            && (bike.excess_usage_fees?.toFloatOrNull()!=0F)) {

            if (!TextUtils.isEmpty(bike.excess_usage_fees) && !TextUtils.isEmpty(bike.excess_usage_type_value) && !TextUtils.isEmpty(bike.excess_usage_type) ) {
                val cost =  CurrencyUtil.getCurrencySymbolByCode(bike.currency, bike.excess_usage_fees
                        .toString()) + " " + getString(R.string.label_per) + " " +
                            LocaleTranslatorUtils.getLocaleString(
                        requireContext(),
                        bike.excess_usage_type,
                                bike.excess_usage_type_value
                                    .toString()
                            ).toString()


                ct_surcharge_price_value_in_confirm_reserve.visibility = VISIBLE
                ct_surcharge_price_label_in_confirm_reserve.visibility = VISIBLE
//
//                ct_surcharge_price_in_bike_slider.visibility = VISIBLE
//                ct_surcharge_price_value_in_bike_slider.visibility = VISIBLE

                cl_surcharge_price_label_in_bike_slider.visibility = VISIBLE
                fragment_bikelist_hub_bike.cl_surcharge_price_label_in_bike_slider.visibility = VISIBLE

                ct_surcharge_price_value_in_confirm_reserve.text = cost
                ct_surcharge_price_value_in_bike_slider.text = cost
                fragment_bikelist_hub_bike.ct_surcharge_price_value_in_bike_slider.text = cost

                setExcessUsageFreq(bike)

                return

            }else if (!TextUtils.isEmpty(bike.excess_usage_fees) ){
                val cost =  CurrencyUtil.getCurrencySymbolByCode(bike.currency, bike.excess_usage_fees
                    .toString())

                ct_surcharge_price_value_in_confirm_reserve.visibility = VISIBLE
                ct_surcharge_price_label_in_confirm_reserve.visibility = VISIBLE
//
//                ct_surcharge_price_in_bike_slider.visibility = VISIBLE
//                ct_surcharge_price_value_in_bike_slider.visibility = VISIBLE

                cl_surcharge_price_label_in_bike_slider.visibility = VISIBLE
                fragment_bikelist_hub_bike.cl_surcharge_price_label_in_bike_slider.visibility = VISIBLE

                ct_surcharge_price_value_in_confirm_reserve.text = cost
                ct_surcharge_price_value_in_bike_slider.text = cost
                fragment_bikelist_hub_bike.ct_surcharge_price_value_in_bike_slider.text = cost

                setExcessUsageFreq(bike)
                return
            }

        }



    }

    fun setExcessUsageFreq(bike:Bike){
        if(!TextUtils.isEmpty(bike.excess_usage_type_after_value) && !TextUtils.isEmpty(bike.excess_usage_type_after_type)){
            val surchage_freq = getString(R.string.surcharge_description, LocaleTranslatorUtils.getLocaleString(
                requireContext(),
                bike.excess_usage_type_after_type,
                bike.excess_usage_type_after_value!!.toString()
            ).toString())


            ct_surcharge_description_in_confirm_reserve.visibility = VISIBLE
            ct_surcharge_description_in_bike_slider.visibility = VISIBLE
            fragment_bikelist_hub_bike.ct_surcharge_description_in_bike_slider.visibility = VISIBLE

            ct_surcharge_description_in_confirm_reserve.text = surchage_freq
            ct_surcharge_description_in_bike_slider.text = surchage_freq
            fragment_bikelist_hub_bike.ct_surcharge_description_in_bike_slider.text = surchage_freq
            return
        }




    }


    fun setUnlockFee(bike: Bike){

        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) &&
            !TextUtils.isEmpty(bike.price_for_bike_unlock) && bike.price_for_bike_unlock!!.toFloat()!=0F) {
                val cost = CurrencyUtil.getCurrencySymbolByCode(bike.currency, bike.price_for_bike_unlock
                    .toString())

            ct_unlock_price_label_in_confirm_reserve.visibility = VISIBLE
            ct_unlock_price_value_in_confirm_reserve.visibility = VISIBLE
//
//
//            ct_unlock_price_label_in_bike_slider.visibility = VISIBLE
//            ct_unlock_price_value_in_bike_slider.visibility = VISIBLE

            cl_unlock_price_label_in_bike_slider.visibility = VISIBLE
            fragment_bikelist_hub_bike.cl_unlock_price_label_in_bike_slider.visibility = VISIBLE


            ct_unlock_price_value_in_confirm_reserve.text = cost
            ct_unlock_price_value_in_bike_slider.text = cost
            fragment_bikelist_hub_bike.ct_unlock_price_value_in_bike_slider.text = cost

        }



    }


    fun setParkingFee(bike:Bike){
        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) &&
            !TextUtils.isEmpty(bike.price_for_penalty_outside_parking) && bike.price_for_penalty_outside_parking!!.toFloat()!=0F) {
            val cost =
                CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.price_for_penalty_outside_parking
                    .toString())


            ct_bike_parking_in_confirm_reserve.visibility = VISIBLE
            ct_bike_parking_value_in_confirm_reserve.visibility = VISIBLE
            ct_parking_description_in_confirm_reserve.visibility = VISIBLE
            cl_parking_price_label_in_bike_slider.visibility = VISIBLE
            fragment_bikelist_hub_bike.cl_parking_price_label_in_bike_slider.visibility = VISIBLE


            ct_bike_parking_value_in_confirm_reserve.text = cost
            ct_bike_parking_value_in_bike_slider.text = cost
            fragment_bikelist_hub_bike.ct_bike_parking_value_in_bike_slider.text = cost

        }
    }

    fun setPromotionDiscount(bike:Bike){
        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) && bike.promotions!=null && bike.promotions?.size!!>0){
            ct_promotion_label_in_confirm_reserve.visibility = VISIBLE
            ct_promotion_value_in_confirm_reserve.visibility = VISIBLE
            ct_promotion_value_in_confirm_reserve.text = getString(R.string.membership_discount_template,bike.promotions?.get(0)?.amount)
        }else{
            ct_promotion_label_in_confirm_reserve.visibility = GONE
            ct_promotion_value_in_confirm_reserve.visibility = GONE
        }
    }


    fun setMembershipDiscount(bike:Bike){

        val membershipDiscount = (activity as HomeActivity).getMembershipDiscount(bike.fleet_id!!)
        if(membershipDiscount!=null){
            ct_membership_discount_label_in_confirm_reserve.visibility = View.VISIBLE
            ct_membership_discount_value_in_confirm_reserve.visibility = View.VISIBLE
            ct_membership_discount_value_in_confirm_reserve.text = getString(R.string.membership_discount_template,membershipDiscount)
        }else{
            ct_membership_discount_label_in_confirm_reserve.visibility = View.GONE
            ct_membership_discount_value_in_confirm_reserve.visibility = View.GONE
        }

    }


    fun setPreAuth(bike:Bike){
        if(!TextUtils.isEmpty(bike.enable_preauth) && (bike.enable_preauth.equals("1") || bike.enable_preauth.equals("true",true))){
            val preAuthAmount = CurrencyUtil.getCurrencySymbolByCode(bike.currency, bike.preauth_amount)
            ct_bike_preauth_value_in_confirm_reserve.text = preAuthAmount
            ct_bike_preauth_value_in_bike_slider.text = preAuthAmount
            fragment_bikelist_hub_bike.ct_bike_preauth_value_in_bike_slider.text = preAuthAmount
            ct_bike_preauth_label_in_confirm_reserve.visibility = VISIBLE
            ct_bike_preauth_value_in_confirm_reserve.visibility = VISIBLE
            ct_preauth_description_in_confirm_reserve.visibility = VISIBLE
            cl_preauth_value_label_in_bike_slider.visibility = VISIBLE
            fragment_bikelist_hub_bike.cl_preauth_value_label_in_bike_slider.visibility = VISIBLE
        }else{
            ct_bike_preauth_label_in_confirm_reserve.visibility = GONE
            ct_bike_preauth_value_in_confirm_reserve.visibility = GONE
            ct_preauth_description_in_confirm_reserve.visibility = GONE
            cl_preauth_value_label_in_bike_slider.visibility = GONE
            fragment_bikelist_hub_bike.cl_preauth_value_label_in_bike_slider.visibility = GONE
        }
    }

    fun setRentalTimeLimit(bike:Bike){
        val rentalTimeLimit = getRentalTimeLimit(requireContext(),bike.reservation)
        if(TextUtils.isEmpty(rentalTimeLimit)){
            ct_rental_time_limit_label_in_confirm_reserve.visibility = GONE
            ct_rental_time_limit_value_in_confirm_reserve.visibility = GONE
            ct_rental_time_limit_description_in_confirm_reserve.visibility = GONE
        }else{
            ct_rental_time_limit_value_in_confirm_reserve.text = rentalTimeLimit
            ct_rental_time_limit_description_in_confirm_reserve.text = getString(R.string.rental_time_limit_description,rentalTimeLimit)
            ct_rental_time_limit_label_in_confirm_reserve.visibility = VISIBLE
            ct_rental_time_limit_value_in_confirm_reserve.visibility = VISIBLE
            ct_rental_time_limit_description_in_confirm_reserve.visibility = VISIBLE
        }
    }

    //// bike details :end


    //// get user :start
    override fun handleUser(user: User?) {
        if(user!=null){
            greetings_in_find_bike.setText(LocaleTranslatorUtils.getLocaleString(
                requireContext(),
                presenter.getGreetingMessage()
            ).toString() + ", ")
            user_name_in_find_bike.setText(user.firstName)
        }
    }
    //// get user :end

    //// get card :start
    fun handleCardDetailsAfterBikeSelection(isRidePaid:Boolean){
        if(presenter.getPrimaryCard()==null ) {
            if (!isRidePaid) {
                handleNoCardForFreeRide()
            }else{
                handleNoCard()
            }
        }else{
            handleCard(presenter.getPrimaryCard()!!)
        }
    }

    fun handleNoCardForFreeRide(){
        cl_payment_confirm_reserve.iv_bike_payment_img_in_confirm_reserve.visibility=GONE
        cl_payment_confirm_reserve.ct_bike_payment_value_in_confirm_reserve.visibility= GONE
        cl_payment_confirm_reserve.iv_add_credit_card_in_confirm_reserve.visibility=GONE
        cl_payment_confirm_reserve.ct_add_credit_card_in_confirm_reserve.visibility=GONE
        cl_payment_confirm_reserve.ct_bike_payment_in_confirm_reserve.visibility = GONE
    }


    override fun handleCard(card: Card) {
        cl_payment_confirm_reserve.ct_bike_payment_in_confirm_reserve.visibility = VISIBLE
        cl_payment_confirm_reserve.iv_bike_payment_img_in_confirm_reserve.visibility=VISIBLE
        cl_payment_confirm_reserve.ct_bike_payment_value_in_confirm_reserve.visibility= VISIBLE
        cl_payment_confirm_reserve.iv_add_credit_card_in_confirm_reserve.visibility=GONE
        cl_payment_confirm_reserve.ct_add_credit_card_in_confirm_reserve.visibility=GONE
        cl_payment_confirm_reserve.ct_bike_payment_value_in_confirm_reserve.setText("XXXX-" + card.cc_no!!.substring(card.cc_no!!.length - 4))
    }

    override fun handleNoCard() {
        cl_payment_confirm_reserve.ct_bike_payment_in_confirm_reserve.visibility = VISIBLE
        cl_payment_confirm_reserve.iv_bike_payment_img_in_confirm_reserve.visibility=GONE
        cl_payment_confirm_reserve.ct_bike_payment_value_in_confirm_reserve.visibility=GONE

        cl_payment_confirm_reserve.iv_add_credit_card_in_confirm_reserve.visibility= VISIBLE
        cl_payment_confirm_reserve.ct_add_credit_card_in_confirm_reserve.visibility= VISIBLE
    }
    //// get card :end


    //// onActivity result :start
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHONE_NUMBER_ERROR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            launchPhoneNumberFillingActivity()
        }else if (requestCode == REQUEST_CODE_QR_CODE_SCAN) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.hasExtra(QR_CODE_SCANNING_RIDE_STARTED)) {
                    if (data.getBooleanExtra(QR_CODE_SCANNING_RIDE_STARTED, false)) {

                    }
                }
            }else{
                presenter.getUser
                fetchCardList()
            }
        } else if(requestCode == REQUEST_CODE_SEARCH_PLACES &&
            resultCode == Activity.RESULT_OK &&
            data!==null && data!!.hasExtra(SEARCH_LOCATION_LATITUDE) &&
            data!!.hasExtra(SEARCH_LOCATION_LONGITUDE )) {

                if(data?.hasExtra(SEARCH_BIKE_ID)){
                    presenter.searchedBikeId = data?.getIntExtra(SEARCH_BIKE_ID,-1)
                }

                MapboxUtil.setFixedZoomForSinglePoint(
                    getMapboxMap(),
                    LatLng(data!!.getDoubleExtra(SEARCH_LOCATION_LATITUDE,presenter.currentUserLocation?.latitude!!), data!!.getDoubleExtra(SEARCH_LOCATION_LONGITUDE,presenter.currentUserLocation?.longitude!!)),
                    (cv_search_bar.height + convertDpToPixel(50.00)).toDouble(),
                    (cl_in_find_bike_bottom_view.height + 350.00).toDouble()
                )
            getBikesForFirstTime()

        }else if(requestCode==REQUEST_ADD_PAYMENT_CARD && resultCode == Activity.RESULT_OK){
            fetchCardList()
        }else if(requestCode == CARD_ERROR_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            launchAddPaymentCardActivity()
        }else if(requestCode==REQUEST_CODE_ADD_PHONE_NUMBER &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            (activity as HomeActivity).showLoadingForHome(getString(R.string.loading))
            presenter.phoneNumber = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.sendCodeToUpdatePhoneNumber()
        }else if(requestCode==REQUEST_CODE_VALIDATE_PHONE_NUMBER &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            (activity as HomeActivity).showLoadingForHome(getString(R.string.loading))
            presenter.code = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.validateCodeForUpdatePhoneNumber()
        }else if(requestCode == REQUEST_CODE_RESERVATION && resultCode == Activity.RESULT_OK){
            if(data!=null && data.hasExtra(PopUpActivity.POSITIVE_LEVEL) && data.getIntExtra(
                    PopUpActivity.POSITIVE_LEVEL,-1)==1){
                showConfirmReserve()
            }else if(data!=null && data.hasExtra(PopUpActivity.POSITIVE_LEVEL) && data.getIntExtra(
                    PopUpActivity.POSITIVE_LEVEL,-1)==2){
                        presenter.convertBikePricingOptionsFormatAsPerReservation()
                startActivityForResult(ReservationActivity.getIntent(requireContext(),presenter.previouslySelectedBike!!),REQUEST_CODE_RESERVATION_CONFIRMATION)
            }
        }else if(requestCode == REQUEST_CODE_RESERVATION_CONFIRMATION &&
            data!=null &&
            data.hasExtra(RESERVE)){
            val reserve = data.getSerializableExtra(RESERVE) as Reserve
            (activity as HomeActivity).refreshReservationCount()
        }else if(requestCode == REQUEST_CODE_APP_SETTINGS){
            openAppSettings()
        }else if(requestCode == REQUEST_PREAUTH_ERROR_REQUEST_CODE){
            onMissingUserCard()
        }else if (requestCode == MAKE_CARD_PRIMARY_REQUEST_CODE){
            fetchCardList()
        }else if(requestCode == PRIMARY_CARD_ERROR_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            launchMakeCardPrimaryActivity()
        }

    }

    //// onActivity result :end


    //// open different activity :start
    fun launchPhoneNumberFillingActivity() {
        PopUpEditActivity.launchForResultFromFragment(
            this,
            requireActivity(),
            REQUEST_CODE_ADD_PHONE_NUMBER,
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
        PopUpEditActivity.launchForResultFromFragment(
            this,
            requireActivity(),
            REQUEST_CODE_VALIDATE_PHONE_NUMBER,
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
        (activity as HomeActivity).hideLoadingForHome()
        openCodeValidationActivity()
    }

    override fun onCodeSentFailure() {
        (activity as HomeActivity).hideLoadingForHome()
        showServerGeneralError(REQUEST_CODE_ADD_PHONE_NUMBER_ERROR)
    }

    override fun onUserProfileSuccess() {
        (activity as HomeActivity).hideLoadingForHome()
    }

    override fun onCodeValidateFailure() {
        (activity as HomeActivity).hideLoadingForHome()
        showServerGeneralError(REQUEST_CODE_ADD_PHONE_NUMBER_ERROR)
    }
    //// open different activity :end


    //// bike booking responses :start
    override fun OnReserveBikeSuccess(startTime: Long, countDownTime: Int): Unit {
        presenter.previouslySelectedBike?.booked_on=startTime
        presenter.previouslySelectedBike?.expires_in = countDownTime
        removeLayerAndSource()
        (activity as HomeActivity?)!!.setDrawerMenuWithRide()
        (activity as HomeActivity?)!!.startShowingBikeBookedFragment()
    }

    override fun onPreAuthFailure() {
        setUIWhenReserveBikeFail()

        launchPopUpActivity(
            REQUEST_PREAUTH_ERROR_REQUEST_CODE,
            getString(R.string.general_error_title),
            getString(R.string.preauthorization_warning),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }

    override fun onBikeAlreadyRented() {
        setUIWhenReserveBikeFail()
        launchPopUpActivity(
            REQUEST_BIKE_ALREADY_RENTED_REQUEST_CODE,
            getString(R.string.general_error_title),
            getString(R.string.bike_already_rented_label),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }

    override fun onMissingUserCard() {
        setUIWhenReserveBikeFail()
        launchAddCardPopUp()
    }

    override fun OnReserveBikeFail() {
        setUIWhenReserveBikeFail()
//        PopUpActivity.launchForResult(
//            activity, REQUEST_RESERVE_BIKE_FAIL, getString(R.string.alert_error_server_title),
//            getString(R.string.alert_error_server_subtitle), null, getString(R.string.ok)
//        )
    }

    override fun OnReserveBikeNotFound() {
        setUIWhenReserveBikeFail()
//        PopUpActivity.launchForResult(
//            activity,
//            REQUEST_RESERVE_BIKE_FAIL,
//            getString(R.string.route_to_bike_booked_alert_title),
//            getString(R.string.route_to_bike_booked_alert_text),
//            null,
//            getString(R.string.ok)
//        )
    }

    fun setUIWhenReserveBikeFail(){
        cl_confirm_in_confirm_reserve.isEnabled = true
        btn_confirm_in_confirm_reserve.visibility=View.VISIBLE
        pb_progress_in_confirm_reserve.visibility = View.GONE
    }


    fun doOnReserveBikeSuccess(){

    }

    //// bike booking responses :end


    ////////////////////////// code for dock hub: start ////////////////////////////
    override fun handleDockHubs() {
        for (dockHub in presenter.dockHubs!!) {
            if(dockHub.bikes!=null){
                generateAndAddHubDockMarker(
                    getMapboxMap(),
                    requireActivity(),
                    dockHub?.bikes?.size!!,
                    false
                )
            }
        }
        presenter.setHubDockMarkerData()
    }

    override fun handleParkingHubs() {
        for (dockHub in presenter.parkingHubs!!) {
            if(dockHub.ports!=null){
                generateHubParkingMarker(
                    getMapboxMap(),
                    requireActivity(),
                    dockHub?.ports?.size!!
                )
            }
        }
        presenter.setHubParkingMarkerData()
    }

    ////////////////////////// code for dock hub: end ////////////////////////////



    ////rentalfare :start
    fun showRentalFarePop(bike:Bike){
        if(presenter.bikeHasPricingOptionSelection()){
            layout_rental_fare_in_bikelist.visibility= VISIBLE
            layout_rental_fare_in_bikelist.ct_pay_per_use_value_in_rental_fare.text = getPayPerUse(bike)
            layout_rental_fare_in_bikelist.rv_rental_fare_in_rental_fare.setLayoutManager(LinearLayoutManager(requireContext()))
            layout_rental_fare_in_bikelist.rv_rental_fare_in_rental_fare.adapter = RentalFareAdapter(requireContext(),this,bike.pricing_options,presenter.rentalFareSelected.finalRentalFareSelectedIndex)
        }
    }

    fun hideRentalFarePop(){
        layout_rental_fare_in_bikelist.visibility= GONE
    }

    fun payPerUseClicked(){
        presenter.onPayPerUseClicked()
        layout_rental_fare_in_bikelist.iv_pay_per_use_value_in_rental_fare.setImageResource(R.drawable.check_mark)
        (layout_rental_fare_in_bikelist.rv_rental_fare_in_rental_fare.adapter as RentalFareAdapter).reset()
    }

    override fun onRentalFareSelected(position:Int){
        layout_rental_fare_in_bikelist.iv_pay_per_use_value_in_rental_fare.setImageResource(0)
        presenter.onRentalFareClicked(position)
    }

    fun rentalFareConfirmationClicked(){
        presenter.onRentalFareConfirmationClicked()
    }

    fun rentalFareSelectionCancelled(){
        presenter.onRentalFareSelectionCancelled()
        hideRentalFarePop()
    }

    fun resetRentalFare(){
        presenter.resetRentalFare()
        if(layout_rental_fare_in_bikelist.rv_rental_fare_in_rental_fare.adapter is RentalFareAdapter){
            (layout_rental_fare_in_bikelist.rv_rental_fare_in_rental_fare.adapter as RentalFareAdapter).reset()
        }
        layout_rental_fare_in_bikelist.iv_pay_per_use_value_in_rental_fare.setImageResource(0)
    }

    override fun showPayPerUse() {
        setPayPerUseOrPricingOption(presenter.getSelectedBike()!!)
        hideRentalFarePop()
    }

    override fun showRentalFare() {
        setPayPerUseOrPricingOption(presenter.getSelectedBike()!!)
        hideRentalFarePop()
    }

    override fun onBikeReserveFailureDuePricingOptionSelectionRemaining() {
        setUIWhenReserveBikeFail()
        showRentalFarePop(presenter.getSelectedBike()!!)
    }

    ////rentalfare :end

}