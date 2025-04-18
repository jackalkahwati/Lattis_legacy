package com.lattis.lattis.presentation.ride

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lattis.domain.models.*
import com.lattis.domain.models.FirebasePushNotification.Companion.docked
import com.lattis.domain.models.FirebasePushNotification.Companion.docking
import com.lattis.lattis.presentation.base.fragment.bluetooth.BaseBluetoothFragment
import com.lattis.lattis.presentation.base.fragment.bluetooth.BaseBluetoothFragmentPresenter
import com.lattis.lattis.presentation.base.fragment.usercurrentlocation.BaseUserCurrentStatusPresenter
import com.lattis.lattis.presentation.base.fragment.usercurrentlocation.BaseUserCurrentStatusPresenter.Companion.CURRENT_STATUS
import com.lattis.lattis.presentation.help.SliderImageHelper.openSliderImage
import com.lattis.lattis.presentation.home.activity.HomeActivity
import com.lattis.lattis.presentation.library.sliding.SlideUp
import com.lattis.lattis.presentation.library.sliding.SlideUpBuilder
import com.lattis.lattis.presentation.parking.ParkingActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.popup.PopUpActivity.Companion.POSITIVE_LEVEL
import com.lattis.lattis.presentation.qrscan.ScanBikeQRCodeActivity
import com.lattis.lattis.presentation.utils.*
import com.lattis.lattis.presentation.utils.MapboxUtil.addPolyGons
import com.lattis.lattis.presentation.utils.MapboxUtil.convertDpToPixel
import com.lattis.lattis.presentation.utils.MapboxUtil.deselectAll
import com.lattis.lattis.presentation.utils.MapboxUtil.generateAndAddHubDockMarker
import com.lattis.lattis.presentation.utils.MapboxUtil.removeLayerAndSourceForLocation
import com.lattis.lattis.presentation.utils.MapboxUtil.removeLayerAndSourceForMarker
import com.lattis.lattis.presentation.utils.MapboxUtil.removeLayerAndSourceForPolgon
import com.lattis.lattis.presentation.utils.MapboxUtil.resetPreviousSelected
import com.lattis.lattis.presentation.utils.MapboxUtil.selectFeature
import com.lattis.lattis.presentation.utils.MapboxUtil.setFixedZoomForSinglePoint
import com.lattis.lattis.presentation.utils.MapboxUtil.showMarker
import com.lattis.lattis.presentation.utils.MapboxUtil.showUserCurrentLocation
import com.lattis.lattis.presentation.webview.WebviewActivity
import com.lattis.lattis.uimodel.model.LockModel
import com.lattis.lattis.utils.Extensions.deepForEach
import com.lattis.lattis.utils.ResourceHelper
import com.lattis.lattis.utils.ResourceHelper.getBikeType
import com.lattis.lattis.utils.UtilsHelper
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfJoins
import io.lattis.lattis.R
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.fragment_active_trip.*
import kotlinx.android.synthetic.main.fragment_active_trip.view.*
import kotlinx.android.synthetic.main.fragment_bike_booked.*
import kotlinx.android.synthetic.main.fragment_bike_booked.view.*
import kotlinx.android.synthetic.main.fragment_bike_booked_or_active_ride.*
import kotlinx.android.synthetic.main.fragment_bike_booked_or_active_ride_bike_card.view.*
import kotlinx.android.synthetic.main.fragment_bike_booked_or_active_ride_content.*
import kotlinx.android.synthetic.main.fragment_bike_booked_or_active_ride_content.view.*
import kotlinx.android.synthetic.main.fragment_bike_booked_or_in_active_ride_cancel_begin_trip_btns.view.*
import kotlinx.android.synthetic.main.fragment_bike_booked_or_in_active_ride_cancel_begin_trip_btns.view.btn_cancel_reserve_in_bike_booked
import kotlinx.android.synthetic.main.fragment_bike_booked_or_in_active_ride_trip_timer_stripe.view.*
import kotlinx.android.synthetic.main.fragment_bike_booked_or_in_active_trip_adapter_btns.view.*
import kotlinx.android.synthetic.main.fragment_bike_booked_or_in_active_trip_adapter_btns.view.cl_scan_adapter
import kotlinx.android.synthetic.main.fragment_bike_booked_or_in_active_trip_iot_btns.view.*
import kotlinx.android.synthetic.main.fragment_bike_booked_or_in_active_trip_manual_lock_btns.view.*
import kotlinx.android.synthetic.main.fragment_bike_booked_with_active_trip.*
import kotlinx.android.synthetic.main.fragment_bikelist_slider.*
import kotlinx.android.synthetic.main.fragment_parking_detail.*
import kotlinx.android.synthetic.main.fragment_parking_spot_zone.*
import kotlinx.android.synthetic.main.popup_manual_lock.view.*
import kotlinx.android.synthetic.main.sentinel_lock_layout.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList


class BikeBookedOrActiveRideFragment : BaseBluetoothFragment<BikeBookedOrActiveRidePresenter, BikeBookedOrActiveRideView>(),
    BikeBookedOrActiveRideView {
    @Inject
    override lateinit var presenter: BikeBookedOrActiveRidePresenter;
    override val fragmentLayoutId = R.layout.fragment_bike_booked_or_active_ride;
    override var view: BikeBookedOrActiveRideView = this

    private var slideUp: SlideUp?=null
    private lateinit var animationDown: Animation
    private lateinit var animationUp: Animation
    private var connectingAnimation: Animation? = null
    private var connectedAnimation: Animation? = null

    private val REQUEST_CODE_GENERAL_ERROR = 887
    private val REQUEST_CODE_END_RIDE = 888
    private val REQUEST_CODE_CANCEL_RIDE =889
    private val REQUEST_CODE_RIDE_SUMMARY =890
    private val REQUEST_CODE_FOR_GOOGLE_MAP_APP = 283

    private val REQUEST_SERVER_ERROR_CANCEL_BIKE = 898
    private val REQUEST_SERVER_ERROR_START_RIDE = 899
    private val REQUEST_SERVER_ERROR_PARKING = 900
    private val REQUEST_RESTRICTED_PARKING =901
    private val REQUEST_OUT_OF_ZONE_PARKING= 902
    private val REQUEST_OUT_OF_BOUND_PARKING= 903
    private val REQUEST_SERVER_ERROR_REPORT_THEFT = 904
    private val REQUEST_REPORT_THEFT_SUCCESS = 905
    val REQUEST_CODE_TERMS_AND_CONDITION = 906
    private val REQUEST_CODE_IOT_OR_ADAPTER_QR_CODE = 907
    private val REQUEST_CODE_IOT_ELLIPSE_COMBINE_LOCK = 908
    private val REQUEST_CODE_SHACKLE_JAM = 909
    private val REQUEST_CODE_DOCK_HUB_NOTIFICATION = 910
    private val REQUEST_CODE_DOCK_HUB_END_RIDE_FAILURE = 911
    private val REQUEST_CODE_GEO_FENCE_NOTICE = 912
    private val REQUEST_CODE_TAPKEY_POPUP = 913
    private val REQUEST_CODE_RESERVATION_TIMER_OVER_POPUP = 914
    private var isMapBoxAnimationRequied = true
    private var isEndRideAllowedDueToLockPosition = false


    private var isParkingRestricted = false
    private var previouslyNoInternet = false
    private var shackleJamPopupShowing = false
    private var dockHubNotificationShown = false

    companion object{

        private const val BIKE_BOOKING_ONLY = 0
        private const val BIKE_BOOKING_WITH_TRIP = 1
        private const val TRIP_ONLY = 2
        private const val PARKING_MAIN =3
        private const val PARKING_DETAILS =4

        fun getInstance(
            context: Context,
            currentStatus: BaseUserCurrentStatusPresenter.Companion.CurrentStatus,
            fromQrCode:Boolean = false
        ): Fragment {
            val bundle = Bundle()
            bundle.putSerializable(CURRENT_STATUS, currentStatus)
            bundle.putSerializable(ACTIVE_TRIP_SHOWN_FROM_QR_CODE,fromQrCode)
            var bikeBookedOrActiveRideFragment = BikeBookedOrActiveRideFragment()
            bikeBookedOrActiveRideFragment.arguments = bundle
            return bikeBookedOrActiveRideFragment
        }

        val RIDE_SUMMARY_DATA = "RIDE_SUMMARY_DATA"
        val GEO_FENCE_NOTIFICATION_CHANNEL_ID = "GEO_FENCE_NOTIFICATION_CHANNEL"
        val GEO_FENCE_NOTIFICATION_CHANNEL_NAME = "Geo Fence"
        val GEO_FENCE_NOTIFICATION_ID = 12
        val ACTIVE_TRIP_SHOWN_FROM_QR_CODE = "ACTIVE_TRIP_SHOWN_FROM_QR_CODE"
    }

    override fun configureViews() {
        super.configureViews()
        presenter.getRide()
        if(presenter.requireToShowTutorial()){
            openSliderImage(requireActivity(),bike_booked_or_active_ride_image_slider_parent)
        }
        presenter.serviceNotificationTitle = getString(R.string.app_name)
        animationDown = AnimationUtils.loadAnimation(context, R.anim.slide_out_up)
        animationUp = AnimationUtils.loadAnimation(context, R.anim.slide_in_up)
        view_flipper_in_bike_booked_or_active_trip.setInAnimation(animationUp)
        view_flipper_in_bike_booked_or_active_trip.setOutAnimation(animationDown)

//        Observable.timer(1000,TimeUnit.MILLISECONDS)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
                configureAllClickListeners()
//            },{
//
//            })


        lock_unlock_button.swipeProgressToFinish = 0.01
        lock_unlock_button.swipeProgressToStart = 0.01

        lock_unlock_disabled_button.isClickable=true
        lock_unlock_disabled_button.isEnabled=false
        cl_lock_unlock_disabled_button.isClickable=true
        cl_lock_unlock_disabled_button.isEnabled=true
        lock_unlock_disabled_view_for_click.isClickable=true


        lock_unlock_manual_lock_button.isClickable=true
        lock_unlock_manual_lock_button.isEnabled=false
        cl_lock_unlock_manual_lock_button.isClickable=true
        cl_lock_unlock_manual_lock_button.isEnabled=true
        lock_unlock_manual_lock_view_for_click.isClickable=true

    }

    fun configureAllClickListeners(){
        iv_close_in_bike_slider.setOnClickListener {
            slideUp?.hide()
        }

        hamburger_in_bike_booked_or_active_ride.setOnClickListener {
            (activity as HomeActivity).openDrawer()
        }

        fragment_active_trip_bike_card.iv_show_slider_in_booked_active_ride_bike_card.setOnClickListener {
            slideUp?.show()
        }

        fragment_bike_booked_bike_card.iv_show_slider_in_booked_active_ride_bike_card.setOnClickListener {
            slideUp?.show()
        }


        fragment_bike_booked_with_active_trip_bike_card.iv_show_slider_in_booked_active_ride_bike_card.setOnClickListener {
            slideUp?.show()
        }

        fragment_active_trip_timer_stripe.iv_show_slider_in_timer_stripe.setOnClickListener {
            slideUp?.show()
        }

        fragment_bike_booked_with_active_trip_timer_stripe.iv_show_slider_in_timer_stripe.setOnClickListener {
            slideUp?.show()
        }




        cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.cl_animating.setOnClickListener {
            if (presenter.connectionState==BaseBluetoothFragmentPresenter.ConnectionState.CONNECTED) {
                showProgressLoading(getString(R.string.starting_ride_loader))
                presenter.startRide()
            }
        }

        cl_cancel_begin_trip_btns_in_bike_booked.cl_animating.setOnClickListener {
            if (presenter.connectionState==BaseBluetoothFragmentPresenter.ConnectionState.CONNECTED){
                showProgressLoading(getString(R.string.starting_ride_loader))
                presenter.startRide()
            }
        }

        fragment_active_trip_parent_container.btn_end_ride_selected.setOnClickListener {
            checkForParkingFee()
        }

        fragment_active_trip_parent_container.btn_end_ride_find_a_station.setOnClickListener {
            showParkingMain()
        }

        lock_unlock_button.onSwipedListener={
            Log.e("BikeBookedOrActiveRide", "isEndRideAllowedDueToLockPosition:::onSwipedListener")
            isEndRideAllowedDueToLockPosition=false
        }


        lock_unlock_button.onSwipedOnListener={
            lock_unlock_button.updateProgressBarWith(
                true, ContextCompat.getColor(
                    activity as Context,
                    R.color.lock
                )
            )
            presenter.setPosition()
        }

        lock_unlock_button.onSwipedOffListener={
            isEndRidePossible(false)
            lock_unlock_button.updateProgressBarWith(
                true, ContextCompat.getColor(
                    activity as Context,
                    R.color.unlock
                )
            )
            presenter.setPosition()
        }

        cl_scan_iot_cancel_scan_btns_in_bike_booked_with_active_trip.cl_scan_iot.setOnClickListener {
            launchQRCodeActivity()
        }

        cl_scan_iot_cancel_scan_btns_in_bike_booked.cl_scan_iot.setOnClickListener {
            launchQRCodeActivity()
        }


        cl_manual_lock_begin_trip_cancel_btns_in_bike_booked.cl_begin_trip_manual_lock.setOnClickListener {
            launchQRCodeActivity()
        }

        cl_manual_lock_begin_trip_cancel_btns_in_bike_booked_with_active_trip.cl_begin_trip_manual_lock.setOnClickListener {
            launchQRCodeActivity()
        }

        popup_manual_lock.btn_ok.setOnClickListener {
            popup_manual_lock.visibility=View.GONE
        }

        cl_scan_adapter_cancel_scan_btns_in_bike_booked_with_active_trip.cl_scan_adapter.setOnClickListener {
            launchQRCodeActivity()
        }

        cl_scan_adapter_cancel_scan_btns_in_bike_booked.cl_scan_adapter.setOnClickListener {
            launchQRCodeActivity()
        }

        cl_cancel_begin_trip_btns_in_bike_booked.btn_cancel_reserve_in_bike_booked.setOnClickListener {
            launchCancelBikeBooking()
        }

        cl_scan_iot_cancel_scan_btns_in_bike_booked.btn_cancel_reserve_in_bike_booked_iot.setOnClickListener {
            launchCancelBikeBooking()
        }

        cl_scan_iot_cancel_scan_btns_in_bike_booked_with_active_trip.btn_cancel_reserve_in_bike_booked_iot.setOnClickListener {
            launchEndRideActivity(true)
        }

        cl_manual_lock_begin_trip_cancel_btns_in_bike_booked.btn_cancel_reserve_in_bike_booked_manual_lock.setOnClickListener {
            launchCancelBikeBooking()
        }

        cl_manual_lock_begin_trip_cancel_btns_in_bike_booked_with_active_trip.btn_cancel_reserve_in_bike_booked_manual_lock.setOnClickListener {
            launchEndRideActivity(true)
        }


        cl_scan_adapter_cancel_scan_btns_in_bike_booked.btn_cancel_reserve_in_bike_booked_adapter.setOnClickListener {
            launchCancelBikeBooking()
        }

        cl_scan_adapter_cancel_scan_btns_in_bike_booked_with_active_trip.btn_cancel_reserve_in_bike_booked_adapter.setOnClickListener {
            launchEndRideActivity(true)
        }

        cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.btn_cancel_reserve_in_bike_booked.setOnClickListener {
            launchEndRideActivity(true)
        }


        btn_connect_to_lock.setOnClickListener {
            presenter.startLockConnection()
        }

        lock_unlock_disabled_view_for_click.setOnClickListener {
            if(view_flipper_in_bike_booked_or_active_trip.displayedChild == TRIP_ONLY) {
                showConnectToLockPopup()
            }
        }

        lock_unlock_manual_lock_view_for_click.setOnClickListener {
            showManualLockPopScreen()
        }

        iv_cancel_connect_to_lock_popup.setOnClickListener {
            hideConnectToLockPopup()
        }

        reposition_gps.setOnClickListener {
            isMapBoxAnimationRequied=true
            if(presenter.currentUserLocation!=null){
                setUserPosition(presenter.currentUserLocation!!)
            }
        }

        parking_unselected.setOnClickListener {
            showParkingMain()
        }

        parking_selected.setOnClickListener {
            manageParkingIcons(true)
            removeLayerAndSourceForMarker(getMapboxMap())
            removeLayerAndSourceForPolgon(getMapboxMap())
            isMapBoxAnimationRequied=true
            showActiveTrip()
        }

        btn_parking_get_direction.setOnClickListener {
            openGoogleMapApp()
        }

        iv_parking_detail_cancel.setOnClickListener {
            deselectAll(getMapboxMap(), presenter?.featureCollection)
            if (view_flipper_in_bike_booked_or_active_trip.displayedChild != PARKING_MAIN)
                view_flipper_in_bike_booked_or_active_trip.displayedChild = PARKING_MAIN
        }

        iv_booking_timer_expired_cancel.setOnClickListener {
            hideBookingTimerExpired()
        }
        iv_booking_timer_running_cancel.setOnClickListener {
            hideBookingTimerRunning()
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

        iv_cancel_iot_hint_popup.setOnClickListener {
            showHideIotHint(false, null)
        }


        layout_sentinel_lock_layout_in_bike_booked_or_active_ride.iv_sentinel_close_pop_up.setOnClickListener {
            sentinelTapGuidance(false)
        }


    }
    
    fun openParkingSpotAndZone(){
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.PARKING_VIEW, FirebaseUtil.AFTER_BIKE_BOOKED_PARKING_VIEW)
        startActivity(
            ParkingActivity.getIntent(requireContext(),
            if(presenter.currentUserLocation!=null)presenter.currentUserLocation?.latitude else null,
            if(presenter.currentUserLocation!=null) presenter.currentUserLocation?.longitude else null,
            presenter.ride?.bike_fleet_id!!,presenter.ride?.bikeId!!))

    }

    fun openBikeTermsAndCondition(){
        if(!TextUtils.isEmpty(presenter.ride?.bike_terms_condition_url)) {
//            val url = presenter.ride?.bike_terms_condition_url!!
//            val i = Intent(Intent.ACTION_VIEW)
//            i.data = Uri.parse(url)
//            startActivity(i)
            WebviewActivity.launchForResultFromFragment(
                this,
                requireActivity(),
                REQUEST_CODE_TERMS_AND_CONDITION,
                presenter.ride?.bike_terms_condition_url!!
            )

        }
    }

    override fun showBikeOnly() {
        if(view_flipper_in_bike_booked_or_active_trip.displayedChild != BIKE_BOOKING_ONLY)
            view_flipper_in_bike_booked_or_active_trip.displayedChild = BIKE_BOOKING_ONLY



        Observable.timer(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val params =
                    bike_slider_in_bike_booked_or_active_trip.layoutParams as ConstraintLayout.LayoutParams
                params.bottomMargin = cl_cancel_begin_trip_btns_in_bike_booked.height
                bike_slider_in_bike_booked_or_active_trip.layoutParams = params
            }, {

            })

        presenter.startBikeReservationTimer()
        lock_unlock_button.visibility = View.GONE

        booking_timer_running_text.text = if (IsRidePaid.isRidePaidForFleet(presenter.ride?.bike_fleet_type)) getString(R.string.reservation_timer_hint) else getString(R.string.reservation_timer_hint_free)
    }

    override fun showBikeBookingWithTrip(confirm: Boolean) {
        if(view_flipper_in_bike_booked_or_active_trip.displayedChild != BIKE_BOOKING_WITH_TRIP)
            view_flipper_in_bike_booked_or_active_trip.displayedChild = BIKE_BOOKING_WITH_TRIP

        Observable.timer(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val params =
                    bike_slider_in_bike_booked_or_active_trip.layoutParams as ConstraintLayout.LayoutParams
                params.bottomMargin =
                    cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.height
                bike_slider_in_bike_booked_or_active_trip.layoutParams = params


            }, {

            })



        val bikeCardParamsInBikeBookedWithActiveTrip= (fragment_bike_booked_with_active_trip_bike_card.ct_bike_type_in_booked_active_ride_bike_card.layoutParams as ConstraintLayout.LayoutParams)
        bikeCardParamsInBikeBookedWithActiveTrip.topMargin =  convertDpToPixel(10.toDouble()).toInt()
        fragment_bike_booked_with_active_trip_bike_card.ct_bike_type_in_booked_active_ride_bike_card.layoutParams = bikeCardParamsInBikeBookedWithActiveTrip

        fragment_bike_booked_with_active_trip_bike_card.iv_show_slider_in_booked_active_ride_bike_card.visibility=View.GONE



        if(confirm) {
            presenter.userCurrentStatus()
        }else{
            presenter.startActiveTripService()
            presenter.startActiveTripTime()
        }

    }

    override fun showActiveTrip() {

        if(view_flipper_in_bike_booked_or_active_trip.displayedChild != TRIP_ONLY)
            view_flipper_in_bike_booked_or_active_trip.displayedChild = TRIP_ONLY

        Observable.timer(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (presenter.isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.NONE||
                    presenter.isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_ONLY ||
                    presenter.isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_IOT_COMBINE ||
                    presenter.isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.MANUAL_LOCK) hideProgressLoading()

                val params =
                    bike_slider_in_bike_booked_or_active_trip.layoutParams as ConstraintLayout.LayoutParams
                params.bottomMargin =
                    fragment_active_trip_parent_container.cl_end_ride_in_active_trip.height + 100
                bike_slider_in_bike_booked_or_active_trip.layoutParams = params
                (activity as HomeActivity).hideLoadingForHome()


                takeActionIfDockHubBikeIsDocked()

            }, {

            })

        val bikeCardParamsInActiveTrip= (fragment_active_trip_bike_card.ct_bike_type_in_booked_active_ride_bike_card.layoutParams as ConstraintLayout.LayoutParams)
        bikeCardParamsInActiveTrip.topMargin =  convertDpToPixel(10.toDouble()).toInt()
        fragment_active_trip_bike_card.ct_bike_type_in_booked_active_ride_bike_card.layoutParams = bikeCardParamsInActiveTrip

        fragment_active_trip_bike_card.iv_show_slider_in_booked_active_ride_bike_card.visibility=View.GONE

        if(presenter.connectionState == BaseBluetoothFragmentPresenter.ConnectionState.CONNECTED && presenter.lockPosition!=null){
            lock_unlock_button.setCheckedAnimated(presenter.lockPosition == Lock.Hardware.Position.LOCKED)
            isEndRideAllowedDueToLockPosition = presenter.lockPosition==Lock.Hardware.Position.LOCKED
            Observable.timer(3000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    isEndRideAllowedDueToLockPosition =
                        presenter.lockPosition == Lock.Hardware.Position.LOCKED
                }, {

                })
            cl_lock_unlock_disabled_button.visibility=View.GONE
            lock_unlock_button.visibility=View.VISIBLE
        }else if(presenter.connectionState != BaseBluetoothFragmentPresenter.ConnectionState.CONNECTED &&
            (presenter.isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_ONLY ||
            presenter.isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_IOT_COMBINE)){
            cl_lock_unlock_disabled_button.visibility = View.VISIBLE
            lock_unlock_button.visibility=View.GONE
        }else if(presenter.isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.IOT_ONLY && presenter.iotLockPosition!=null){
            lock_unlock_button.visibility=View.VISIBLE
            cl_lock_unlock_disabled_button.visibility = View.GONE
        }else if (presenter.isManualLock()){
            showManualLockActiveRideIfApplicable()
        }

        manageParkingIcons(true)
        reposition_gps.visibility= View.VISIBLE

        presenter.startActiveTripTime()
        presenter.startActiveTripService()
        removeLayerAndSourceForMarker(getMapboxMap())

        showGeoFenceIfApplicable()
    }

    fun showParkingMain() {
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.PARKING_VIEW, FirebaseUtil.AFTER_BIKE_BOOKED_PARKING_VIEW)
        manageParkingIcons(false)
        hideIotHintPopup()
        showProgressLoading(getString(R.string.loading))
        presenter.getDockHubParking()
        if (view_flipper_in_bike_booked_or_active_trip.displayedChild != PARKING_MAIN)
            view_flipper_in_bike_booked_or_active_trip.displayedChild = PARKING_MAIN

        cl_lock_unlock_manual_lock_button.visibility = View.GONE
        cl_lock_unlock_disabled_button.visibility=View.GONE
        lock_unlock_button.visibility = View.GONE



    }

    fun showParkingDetail() {
        if (view_flipper_in_bike_booked_or_active_trip.displayedChild != PARKING_DETAILS)
            view_flipper_in_bike_booked_or_active_trip.displayedChild = PARKING_DETAILS


        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()


        var imageURL:String?=null
        var title:String?=null
        var subTitle:String?=null
        if(presenter.previouslySelectedParking!=null){
            imageURL = presenter.previouslySelectedParking?.pic
            title = presenter.previouslySelectedParking?.name
            subTitle = presenter.previouslySelectedParking?.description
        }else if(presenter.previouslySelectedHubDock!=null){
            title = presenter.previouslySelectedHubDock?.hub_name
            subTitle = presenter.previouslySelectedHubDock?.description
            imageURL = presenter.previouslySelectedHubDock?.image
        }

        Glide.with(this)
            .load(imageURL)
            .apply(requestOptions)
            .into(iv_parking_img)




        ct_parking_detail_title.text = title
        tv_parking_detail_subtitle.text = subTitle

    }

    private fun openGoogleMapApp() {
        try {
            val uri = java.lang.String.format(
                Locale.ENGLISH,
                "http://maps.google.com/maps?daddr=%f,%f(%s)&mode=bicycling",
                presenter.previouslySelectedParking?.latitude,
                presenter.previouslySelectedParking?.longitude,
                presenter.previouslySelectedParking?.name
            )
            val gmmIntentUri = Uri.parse(uri)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(requireActivity().packageManager) == null) {
                // Google map not installed
            } else {
                startActivityForResult(mapIntent, REQUEST_CODE_FOR_GOOGLE_MAP_APP)
            }
        } catch (e: ActivityNotFoundException) {
        }
    }

    fun startFetchingLocation(){
        fetchLocation()
    }

    override fun onLocationPermissionsAvailable() {
        requestLocationUpdates()
        presenter.startLockConnection()
        presenter.doNeedfulWhenNoLockWithBike()
        presenter.startListeningToFirebasePushNotificationIfApplicable()
    }
    override fun onLocationPermissionsDenied() {

    }

    override fun setUserPosition(location: Location) {
        presenter.checkIfAnyThingBlockedDueLocation()
        showUserCurrentLocation(getMapboxMap(), location)
        if(isMapBoxAnimationRequied) {
            isMapBoxAnimationRequied = false

            if(view_flipper_in_bike_booked_or_active_trip.displayedChild == BIKE_BOOKING_ONLY ||
                view_flipper_in_bike_booked_or_active_trip.displayedChild == BIKE_BOOKING_WITH_TRIP ){
                showMarker()
            }else{
                setFixedZoomForSinglePoint(
                    getMapboxMap(),
                    LatLng(location.latitude, location.longitude),
                    convertDpToPixel(50.00),
                    convertDpToPixel(250.00)
                )
            }

        }

        presenter.subscribeToGeoFenceIntervalIfApplicable(true)
    }

    fun getMapboxMap(): MapboxMap {
        return (activity as HomeActivity).mapboxMap
    }


    //// mapbox override :start
    override fun onMapboxMoved(latlongBounds: LatLngBounds) {

    }

    override fun onMapClicked(screenPoint: PointF) {
        handleClickIcon(screenPoint)
    }

    private fun handleClickIcon(screenPoint: PointF): Boolean {
        val features: List<Feature> =
            getMapboxMap().queryRenderedFeatures(screenPoint, MapboxUtil.MARKER_LAYER)
        if (!features.isEmpty()) {
            var selectedFeature = features.get(0)

            if(selectedFeature.getProperty("BIKE")!=null && !TextUtils.isEmpty(
                    selectedFeature.getProperty(
                        "BIKE"
                    ).toString()
                ))
            return true

            for (feature in presenter.featureCollection?.features()!!) {
                if(selectedFeature.getNumberProperty(MapboxUtil.MARKER_ID).toInt()==feature.getNumberProperty(
                        MapboxUtil.MARKER_ID
                    ).toInt()) {
                    resetPreviousSelected(presenter?.featureCollection!!)
                    selectFeature(getMapboxMap(), presenter?.featureCollection!!, feature)
                    presenter.setSelectedParking(feature)
                    showParkingDetail()
                    break
                }
            }
            return true
        }

        else { // check if clustered clicked

            for(clusterQueryLayerId in presenter.clusterQueryLayerIds){
                if(clusterQueryLayerId==null)
                    return false
            }
            val rectF = RectF(
                screenPoint.x - 10,
                screenPoint.y - 10,
                screenPoint.x + 10,
                screenPoint.y + 10
            )
            val mapClickFeatureList: List<Feature> =
                getMapboxMap()!!.queryRenderedFeatures(rectF, *presenter.clusterQueryLayerIds)

            if (mapClickFeatureList!=null && mapClickFeatureList.size > 0) {
                val clusterLeavesFeatureCollection: FeatureCollection =
                    (getMapboxMap().style!!.getSource(MapboxUtil.MARKER_SOURCE) as GeoJsonSource).getClusterLeaves(
                        mapClickFeatureList[0],
                        8000, 0
                    )
                moveCameraToLeavesBounds(clusterLeavesFeatureCollection)
            }
            return true
        }
        return false
    }

    fun moveCameraToLeavesBounds(featureCollectionToInspect: FeatureCollection){

        MapboxUtil.moveCameraToLeavesBounds(getMapboxMap()!!, featureCollectionToInspect)
        Observable.timer(2000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                (activity as HomeActivity).centerLatLng = getMapboxMap().cameraPosition.target
            }, {

            })
    }




    //// mapbox override :end


    //// get ride :start
    override fun onRideSuccess(ride: Ride) {
        setBikeCardSlider(ride)
        setBatteryLevel(ride.bike_bike_battery_level)
        setBikeCardDetails(ride)
        configureBikeSlider(ride)

        Observable.timer(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // set unlock fee in bike card and confirm screen
                cl_unlock_price_label_in_bike_slider.visibility = View.GONE
                setUnlockFee(ride)

                // set surcharge fee in bike card and confirm screen
                cl_surcharge_price_label_in_bike_slider.visibility = View.GONE
                setExcessUsageFee(ride)

                cl_parking_price_label_in_bike_slider.visibility = View.GONE
                setParkingFee(ride)
            }, {

            })
        presenter.setScreen()
        startFetchingLocation()
    }

    override fun onRideFailure() {

    }

    fun setBikeCardDetails(ride: Ride){


        fragment_bike_booked_bike_card.background = ContextCompat.getDrawable(
            activity as Context,
            R.drawable.bike_card_top_radius
        )
        fragment_bike_booked_with_active_trip_bike_card.background = ContextCompat.getDrawable(
            activity as Context,
            R.color.bike_card_background
        )
        fragment_active_trip_bike_card.background = ContextCompat.getDrawable(
            activity as Context,
            R.color.bike_card_background
        )


        fragment_bike_booked_bike_card.ct_fleet_name_in_booked_active_ride_bike_card.setText(ride.bike_fleet_name)
        fragment_bike_booked_bike_card.ct_bike_name_in_booked_active_ride_bike_card.setText(ride.bike_bike_name)
        fragment_bike_booked_bike_card.ct_bike_type_in_booked_active_ride_bike_card.setText(
            getBikeType(
                ride.bike_type,
                requireContext()
            )
        )

        fragment_bike_booked_with_active_trip_bike_card.ct_fleet_name_in_booked_active_ride_bike_card.setText(
            ride.bike_fleet_name
        )
        fragment_bike_booked_with_active_trip_bike_card.ct_bike_name_in_booked_active_ride_bike_card.setText(
            ride.bike_bike_name
        )
        fragment_bike_booked_with_active_trip_bike_card.ct_bike_type_in_booked_active_ride_bike_card.setText(
            getBikeType(
                ride.bike_type,
                requireContext()
            )
        )

        fragment_active_trip_bike_card.ct_fleet_name_in_booked_active_ride_bike_card.setText(ride.bike_fleet_name)
        fragment_active_trip_bike_card.ct_bike_name_in_booked_active_ride_bike_card.setText(ride.bike_bike_name)
        fragment_active_trip_bike_card.ct_bike_type_in_booked_active_ride_bike_card.setText(
            getBikeType(
                ride.bike_type,
                requireContext()
            )
        )


        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(this)
            .load(ride.bike_pic)
            .apply(requestOptions)
            .into(fragment_bike_booked_bike_card.iv_bike_image_in_booked_active_ride_bike_card)

        Glide.with(this)
            .load(ride.bike_pic)
            .apply(requestOptions)
            .into(fragment_bike_booked_with_active_trip_bike_card.iv_bike_image_in_booked_active_ride_bike_card)

        Glide.with(this)
            .load(ride.bike_pic)
            .apply(requestOptions)
            .into(fragment_active_trip_bike_card.iv_bike_image_in_booked_active_ride_bike_card)


    }


    fun setBikeCardSlider(ride: Ride){

        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(this)
            .load(ride.bike_pic)
            .apply(requestOptions)
            .into(iv_bike_in_bike_slider)

        ct_fleet_name_in_bike_slider.setText(ride.bike_fleet_name)
        ct_bike_name_in_bike_slider.setText(ride.bike_bike_name)
        ct_bike_type_in_bike_slider.setText(getBikeType(ride.bike_type, requireContext()))
        ct_bike_network_name_in_bike_slider.setText(ride.bike_fleet_name)



        ct_bike_price_value_in_bike_slider.setText(getBikeCost(ride))
        ct_bike_about_name_value_in_bike_slider.setText(ride.bike_name)
        ct_bike_about_description_value_in_bike_slider.setText(ride.bike_description)
        ct_bike_about_model_value_in_bike_slider.setText(
            getBikeType(
                ride.bike_type,
                requireContext()
            )
        )

        if(ResourceHelper.parkingStation(ride.bike_type)){
            cl_parking_spot_zone_in_bike_slider.visibility = View.GONE
        }else{
            cl_parking_spot_zone_in_bike_slider.visibility = View.VISIBLE
        }

    }

    fun setBatteryLevel(batteryLevel:String?){
        ResourceHelper.setBatteryImageAndText(
            batteryLevel,
            fragment_bike_booked_bike_card.iv_bike_battery_in_booked_active_ride_bike_card,
            fragment_bike_booked_bike_card.ct_bike_battery_in_booked_active_ride_bike_card
        )

        ResourceHelper.setBatteryImageAndText(
            batteryLevel,
            fragment_bike_booked_with_active_trip_bike_card.iv_bike_battery_in_booked_active_ride_bike_card,
            fragment_bike_booked_with_active_trip_bike_card.ct_bike_battery_in_booked_active_ride_bike_card
        )

        ResourceHelper.setBatteryImageAndText(
            batteryLevel,
            fragment_active_trip_bike_card.iv_bike_battery_in_booked_active_ride_bike_card,
            fragment_active_trip_bike_card.ct_bike_battery_in_booked_active_ride_bike_card
        )

        ResourceHelper.setBatteryImageAndText(
            batteryLevel,
            iv_bike_battery_bike_slider,
            ct_bike_battery_bike_slider
        )
    }

    fun getBikeCost(ride: Ride):String{
        if (IsRidePaid.isRidePaidForFleet(ride.bike_fleet_type) &&
            !TextUtils.isEmpty(ride.bike_price_for_membership) && ride.bike_price_for_membership!!.toFloat()!=0F) {

            if (ride.bike_price_type != null && !ride.bike_price_type.equals("")) {
                var rideCost =
                    CurrencyUtil.getCurrencySymbolByCode(ride.currency,ride.bike_price_for_membership
                        .toString()) + " " + getString(R.string.label_per) + " " +
                            LocaleTranslatorUtils.getLocaleString(
                        requireContext(),
                        ride.bike_price_type,
                        ride.bike_price_type_value.toString()
                    ).toString()

                return rideCost
            }

        }else{
            return getString(R.string.bike_detail_bike_cost_free)
        }
        return "";
    }




    //// get ride :end


    //// Configure bike slider
    fun configureBikeSlider(ride: Ride){
        if(slideUp==null) {
            slideUp = SlideUpBuilder(bike_slider_in_bike_booked_or_active_trip)
                .withListeners(object : SlideUp.Listener.Events {
                    override fun onSlide(percent: Float) {


                        if (percent > 20) {

                        } else if (percent < 20) {

                        }
                    }

                    override fun onVisibilityChanged(visibility: Int) {


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
                .withSlideFromOtherView(view_flipper_in_bike_booked_or_active_trip)
                .withTouchEnabled(false)
                .build()
        }else{
            if(slideUp?.isVisible?:false)
                slideUp?.hide()
        }
    }

    fun setExcessUsageFee(ride: Ride){
        if (IsRidePaid.isRidePaidForFleet(ride.bike_fleet_type) &&
            !TextUtils.isEmpty(ride?.bike_excess_usage_fees) &&
            ride?.bike_excess_usage_fees!!.toFloatOrNull()!=null
            && ride?.bike_excess_usage_fees!!.toFloat()!=0F) {

            if (!TextUtils.isEmpty(ride.bike_excess_usage_fees) && !TextUtils.isEmpty(ride.bike_excess_usage_type_value) && !TextUtils.isEmpty(
                    ride.bike_excess_usage_type
                ) ) {
                val cost =  CurrencyUtil.getCurrencySymbolByCode(ride.currency,ride.bike_excess_usage_fees
                    .toString()) + " " + getString(R.string.label_per) + " " +
                        LocaleTranslatorUtils.getLocaleString(
                    requireContext(),
                    ride.bike_excess_usage_type,
                            ride.bike_excess_usage_type_value
                                .toString()
                ).toString()

                cl_surcharge_price_label_in_bike_slider.visibility = View.VISIBLE
                ct_surcharge_price_value_in_bike_slider.text = cost

                setExcessUsageFreq(ride)

                return

            }else if (!TextUtils.isEmpty(ride.bike_excess_usage_fees) ){
                val cost =  CurrencyUtil.getCurrencySymbolByCode(ride.currency,ride.bike_excess_usage_fees
                    .toString())

                cl_surcharge_price_label_in_bike_slider.visibility = View.VISIBLE
                ct_surcharge_price_value_in_bike_slider.text = cost

                setExcessUsageFreq(ride)
                return
            }

        }



    }

    fun setExcessUsageFreq(ride: Ride){
        if(!TextUtils.isEmpty(ride.bike_excess_usage_type_after_value) && !TextUtils.isEmpty(ride.bike_excess_usage_type_after_type)){
            val surchage_freq = getString(
                R.string.surcharge_description,
                LocaleTranslatorUtils.getLocaleString(
                    requireContext(),
                    ride.bike_excess_usage_type_after_type,
                            ride.bike_excess_usage_type_after_value
                ).toString()
            )

            ct_surcharge_description_in_bike_slider.visibility = View.VISIBLE
            ct_surcharge_description_in_bike_slider.text = surchage_freq
            return
        }




    }


    fun setUnlockFee(ride: Ride){

        if (IsRidePaid.isRidePaidForFleet(ride.bike_fleet_type) && !TextUtils.isEmpty(ride.price_for_bike_unlock) &&
            ride?.price_for_bike_unlock!!.toFloat()!=0F) {
            val cost = CurrencyUtil.getCurrencySymbolByCode(ride.currency,ride.price_for_bike_unlock
                .toString())

            cl_unlock_price_label_in_bike_slider.visibility = View.VISIBLE
            ct_unlock_price_value_in_bike_slider.text = cost

        }



    }


    fun setParkingFee(ride: Ride){
        if (IsRidePaid.isRidePaidForFleet(ride.bike_fleet_type) && !TextUtils.isEmpty(ride.price_for_penalty_outside_parking)
            && ride?.price_for_penalty_outside_parking!!.toFloat()!=0F) {
            val cost =
                CurrencyUtil.getCurrencySymbolByCode(ride.currency,ride.price_for_penalty_outside_parking
                    .toString())

            cl_parking_price_label_in_bike_slider.visibility = View.VISIBLE

            ct_bike_parking_value_in_bike_slider.text = cost

        }
    }


    //// Bike Booked :start
    override fun showBikeBookedTime(time: String) {
        tv_bike_booking_timer_in_bike_booked.text =  time
    }
    //// Bike Booked :end


    //// Bike Booked and active trip :start
    override fun onUserCurrentStatusSuccess() {
        if(presenter.currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_TRIP ||
            presenter.currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING_WITH_TRIP_STARTED){
            presenter.updateRideObjectWithLatestInformation()
            presenter.startActiveTripTime()
            presenter.startActiveTripService()
        }else{
            presenter.callGetUserCurrentLocationAfterDelay()    // server should start the ride so polling for it
        }
    }

    override fun onUserCurrentStatusFailure() {
        showServerGeneralError(REQUEST_CODE_GENERAL_ERROR)
    }

    override fun showActiveTripTime(time: String) {
        fragment_bike_booked_with_active_trip_timer_stripe.ct_trip_timer.text =  time
        fragment_active_trip_timer_stripe.ct_trip_timer.text =  time
    }

    override fun showActiveTripData(updateTripData: UpdateTripData) {
        if (IsRidePaid.isRidePaidForFleet(presenter.ride?.bike_fleet_type)) {
            if(updateTripData.cost!=null && updateTripData.currency!=null){
                fragment_bike_booked_with_active_trip_timer_stripe.ct_trip_cost.text = CurrencyUtil.getCurrencySymbolByCode(
                    updateTripData.currency ,UtilsHelper.getDotAfterNumber(updateTripData.cost.toString()))
                fragment_active_trip_timer_stripe.ct_trip_cost.text = CurrencyUtil.getCurrencySymbolByCode(
                    updateTripData.currency,UtilsHelper.getDotAfterNumber(updateTripData.cost.toString()))
            }
        } else {
            fragment_bike_booked_with_active_trip_timer_stripe.ct_trip_cost.text = (getString(R.string.bike_detail_bike_cost_free))
            fragment_active_trip_timer_stripe.ct_trip_cost.text = (getString(R.string.bike_detail_bike_cost_free))

        }

        //// show battery level if applicable
        if(!TextUtils.isEmpty(updateTripData.bike_battery_level)){
            setBatteryLevel(updateTripData.bike_battery_level)
        }


    }

    //// Bike Booked and active trip :end


    //// Lock overrides :start

    override fun showConnectingAsDeviceFound() {
        startConnectingAnimation(false)
    }

    override fun onLockConnected(lockModel: LockModel) {
        presenter.connectionState = BaseBluetoothFragmentPresenter.ConnectionState.CONNECTED
        startConnectedAnimation()
        presenter.observeLockPosition(lockModel)
        presenter.observeHardwareState(lockModel)
        presenter.observeConnectionState(lockModel)
        presenter.startLocationTrackingIfApplicable()

        cl_connect_to_popup.visibility=View.GONE
        hideConnectToLockPopup()
    }

    override fun onLockNotFound() {
        presenter.onLockNotFound()
    }

    override fun showConnectionTimeOut(){   // this is called only when active trip
        presenter.connectionState = BaseBluetoothFragmentPresenter.ConnectionState.DISCONNECTED
        cl_animating_in_active_trip.visibility=View.GONE
        btn_connect_to_lock.visibility = View.VISIBLE

        if(presenter.requireToShowConnectToLockPopup() &&
            view_flipper_in_bike_booked_or_active_trip.displayedChild == TRIP_ONLY){
            showConnectToLockPopup()
            cl_lock_unlock_disabled_button.visibility = View.VISIBLE
        }
        cl_end_ride_in_active_trip.visibility = View.INVISIBLE  // Dont use GONE, it will remove the space.


    }

    override fun onLockConnectionFailed() {
        onLockDisconnection(false)
    }

    override fun onLockConnectionAccessDenied() {
        onLockDisconnection(true)
    }

    private fun onLockDisconnection(accessDenied: Boolean){
        presenter.connectionState = BaseBluetoothFragmentPresenter.ConnectionState.DISCONNECTED
        if(!accessDenied){
            presenter.doNeedfulWhenDisconnected()
        }
    }

    override fun onSignedMessagePublicKeySuccess(signedMessage: String?, publicKey: String?) {
        presenter.disconnectAllLocks()
    }

    override fun onSetPositionStatus(status: Boolean) {
    }



    override fun showInvalidLockPositionError() {
        if(!shackleJamPopupShowing) {
            shackleJamPopupShowing=true
            launchPopUpActivity(
                REQUEST_CODE_SHACKLE_JAM,
                getString(R.string.general_error_title),
                getString(R.string.shackle_jam),
                null,
                null,
                null,
                null,
                getString(R.string.general_btn_ok)
            )
        }
    }

    override fun handleIoTLockPosition(){
        hideProgressLoading()
        when(presenter.iotLockPosition){
            null -> {
                cl_lock_unlock_disabled_button.visibility = View.VISIBLE
                lock_unlock_button.visibility = View.GONE
            }
            true -> {
                showLockPositionSuccess(Lock.Hardware.Position.LOCKED)
            }
            false -> {
                showLockPositionSuccess(Lock.Hardware.Position.UNLOCKED)
            }
        }
    }

    override fun showLockPositionSuccess(position: Lock.Hardware.Position) {
        // checked == end and unchecked == start

        if(presenter.isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.IOT_ONLY){
            hideProgressLoading()
            cl_lock_unlock_disabled_button.visibility=View.GONE
//            lock_unlock_button.visibility=View.VISIBLE
            cl_active_ride_connecting.visibility = View.GONE
        }

        if(lock_unlock_button.visibility==View.GONE &&
            presenter.startWithStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_TRIP &&
                view_flipper_in_bike_booked_or_active_trip.displayedChild == TRIP_ONLY){
                    Log.e("BikeBookedOrActiveRide","Button is GONE")
                    if(presenter.activeTripFromQrCode){
                        Log.e("BikeBookedOrActiveRide","Button is GONE::activeTripFromQrCode::UNLOCKING")
                        presenter.setHighIotPopUpDelay()
                        Observable.timer(5000,TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Log.e("BikeBookedOrActiveRide","Button is GONE::activeTripFromQrCode::LOCKING")
                                lock_unlock_button.setCheckedAnimated(position == Lock.Hardware.Position.LOCKED)
                                cl_lock_unlock_disabled_button.visibility=View.GONE
                                lock_unlock_button.visibility=View.VISIBLE
                            },{
                            })
                    }else{
                        Log.e("BikeBookedOrActiveRide","Button is GONE::NO activeTripFromQrCode::LOCKING")
                        lock_unlock_button.setCheckedAnimated(position == Lock.Hardware.Position.LOCKED)
                        cl_lock_unlock_disabled_button.visibility=View.GONE
                        lock_unlock_button.visibility=View.VISIBLE
                    }
        }else if(position==Lock.Hardware.Position.LOCKED && !lock_unlock_button.isChecked){   // reverse logic
            Log.e("BikeBookedOrActiveRide","Button is VISIBLE and UNLOCKED SO LOCKING IT")
            lock_unlock_button.setCheckedAnimated(true)
        }else if(position==Lock.Hardware.Position.UNLOCKED && lock_unlock_button.isChecked){
            Log.e("BikeBookedOrActiveRide","Button is VISIBLE and LOCKED SO UNLOCKING IT")
            lock_unlock_button.setCheckedAnimated(false)
        }

        lock_unlock_button.updateProgressBarWith(false)
        if(position==Lock.Hardware.Position.LOCKED){
            isEndRideAllowedDueToLockPosition = true
            btn_end_ride_selected.visibility = View.VISIBLE
            btn_end_ride_unselected.visibility = View.GONE
            lockGuidancePopup(false)    //axa lock popup gone

            if(presenter.requiredToShowIoTEllipsePopup()){
                launchPopUpActivity(
                    REQUEST_CODE_IOT_ELLIPSE_COMBINE_LOCK,
                    getString(R.string.turn_your_vehicle_off),
                    getString(R.string.we_can_temporarily_turn_off),
                    null,
                    getString(R.string.yes),
                    getString(R.string.no),
                    null,
                    null
                )
            }
            if(presenter.requiredToShowLockUnlockPopup())showHideIotHint(true, getString(R.string.iot_hint_locked))
        }else{
            Log.e(
                "BikeBookedOrActiveRide",
                "isEndRideAllowedDueToLockPosition:::showLockPositionSuccess"
            )
            isEndRideAllowedDueToLockPosition = false
            btn_end_ride_selected.visibility = View.GONE
            btn_end_ride_unselected.visibility = View.VISIBLE
            lockGuidancePopup(false)    //axa lock popup gone
            presenter.turnONIoTIfApplicable()
            if(presenter.requiredToShowLockUnlockPopup())showHideIotHint(true, getString(R.string.iot_hint_unlocked))
        }
    }

    fun showHideIotHint(show: Boolean, message: String?){
        if(show && view_flipper_in_bike_booked_or_active_trip.displayedChild == TRIP_ONLY ){
            ct_iot_hint_popup.text = message
            if(cl_iot_hint_popup.visibility==View.GONE)showIotHintPopup()
            reposition_gps.visibility = View.GONE
            parking_unselected.visibility = View.GONE
        }else{
            hideIotHintPopup()
            reposition_gps.visibility = View.VISIBLE
            manageParkingIcons(true)
        }



    }

    override fun isEndRidePossible(status: Boolean) {
        if(!status){
            Log.e("BikeBookedOrActiveRide", "isEndRideAllowedDueToLockPosition:::isEndRidePossible")
            isEndRideAllowedDueToLockPosition=false
            btn_end_ride_selected.visibility = View.GONE
            btn_end_ride_unselected.visibility = View.VISIBLE
        }
    }

    override fun onSetPositionFailure() {
    }
    override fun onSignedMessagePublicKeyFailure() {
        showServerGeneralError(REQUEST_CODE_GENERAL_ERROR)
    }

    override fun showDisconnected() {

    }

    override fun showConnected() {
    }

    //// Lock overrides :end



    //// Begin trip animation :start
    private fun startConnectedAnimation() {
        requireActivity().runOnUiThread {

            cl_cancel_begin_trip_btns_in_bike_booked.view_animating.startAnimation(
                connectedAnimation()
            )
            cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.view_animating.startAnimation(
                connectedAnimation()
            )
            view_animating_in_active_trip.startAnimation(connectedAnimation())
        }
    }

    override fun startDisconnectedAnimation() {
        requireActivity().runOnUiThread {

            cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.tv_animating.text = getString(
                R.string.walk_to_bike_label
            )
            cl_cancel_begin_trip_btns_in_bike_booked.tv_animating.text = getString(R.string.walk_to_bike_label)
            cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.tv_animating.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.begin_trip_grey_in_bike_booked_text
                )
            )
            cl_cancel_begin_trip_btns_in_bike_booked.tv_animating.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.begin_trip_grey_in_bike_booked_text
                )
            )


            if (connectingAnimation != null) {
                connectingAnimation!!.cancel()
                connectingAnimation!!.reset()
                connectingAnimation = null
            }
            if (connectedAnimation != null) {
                connectedAnimation!!.cancel()
                connectedAnimation!!.reset()
                connectedAnimation = null
            }

            cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.view_animating.setBackgroundColor(
                Color.TRANSPARENT
            )
            cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.cl_animating.background =(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.round_begin_trip_grey
                )
            )

            cl_cancel_begin_trip_btns_in_bike_booked.view_animating.setBackgroundColor(
                Color.TRANSPARENT
            )

            cl_cancel_begin_trip_btns_in_bike_booked.cl_animating.background =(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.round_begin_trip_grey
                    )
                    )
        }
    }

    override fun startConnectingAnimation(isActiveTripAnimationRequired: Boolean) {
        if(isActiveTripAnimationRequired){
            lock_unlock_button.visibility = View.GONE
            cl_lock_unlock_disabled_button.visibility = View.VISIBLE
            view_animating_in_active_trip.startAnimation(connectingAnimation())
        }else{
            cl_cancel_begin_trip_btns_in_bike_booked.view_animating.startAnimation(
                connectingAnimation()
            )
            cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.view_animating.startAnimation(
                connectingAnimation()
            )
        }
    }

    private fun connectingAnimation(): Animation {

        cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.tv_animating.text = getString(R.string.walk_to_bike_label)
        cl_cancel_begin_trip_btns_in_bike_booked.tv_animating.text = getString(R.string.walk_to_bike_label)
        cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.tv_animating.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.begin_trip_grey_in_bike_booked_text
            )
        )
        cl_cancel_begin_trip_btns_in_bike_booked.tv_animating.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.begin_trip_grey_in_bike_booked_text
            )
        )


        cl_animating_in_active_trip.visibility=View.VISIBLE
        btn_connect_to_lock.visibility = View.GONE
        cl_end_ride_in_active_trip.visibility = View.INVISIBLE


        tv_animating_in_active_trip.text = getString(R.string.connecting_loader)

        if (connectedAnimation != null) {
            connectedAnimation!!.cancel()
            connectedAnimation!!.reset()
            connectedAnimation = null
        }
        if (connectingAnimation == null || connectingAnimation!!.hasEnded()) {
            connectingAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f
            )

            cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.view_animating.background = ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.round_begin_trip_connected
            )

            cl_cancel_begin_trip_btns_in_bike_booked.view_animating.background = ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.round_begin_trip_connected
            )

            view_animating_in_active_trip.background = ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.round_begin_trip_connected
            )

            connectingAnimation!!.setRepeatCount(Animation.INFINITE)
            connectingAnimation!!.setDuration(1500)
            connectingAnimation!!.setInterpolator(AccelerateInterpolator())
        }
        return connectingAnimation!!
    }

    private fun connectedAnimation(): Animation {

        cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.tv_animating.text = getString(R.string.booking_begin_trip)
        cl_cancel_begin_trip_btns_in_bike_booked.tv_animating.text = getString(R.string.booking_begin_trip)
        cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.tv_animating.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.begin_trip_white_in_bike_booked_text
            )
        )
        cl_cancel_begin_trip_btns_in_bike_booked.tv_animating.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.begin_trip_white_in_bike_booked_text
            )
        )




        if (connectedAnimation != null) {
            connectedAnimation!!.cancel()
            connectedAnimation!!.reset()
            connectedAnimation = null
        }
        if (connectedAnimation == null || connectedAnimation!!.hasEnded()) {
            connectedAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f
            )
            cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.view_animating.background = (
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.round_begin_trip_connected
                )
            )

            cl_cancel_begin_trip_btns_in_bike_booked.view_animating.background = (
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.round_begin_trip_connected
                )
            )

            view_animating_in_active_trip.background = ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.round_begin_trip_connected
            )


            connectedAnimation?.setDuration(1000)
            connectedAnimation?.setInterpolator(AccelerateInterpolator())
        }
        connectedAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {

                cl_animating_in_active_trip.visibility = View.GONE
                btn_connect_to_lock.visibility = View.GONE
                cl_end_ride_in_active_trip.visibility = View.VISIBLE

//                cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.view_animating.setBackgroundColor(
//                    Color.TRANSPARENT
//                )
//
//                cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.cl_animating.background = ContextCompat.getDrawable(
//                    activity!!,
//                    R.drawable.round_begin_trip_connected
//                )
//
//                cl_cancel_begin_trip_btns_in_bike_booked.cl_animating.background = ContextCompat.getDrawable(
//                    activity!!,
//                    R.drawable.round_begin_trip_connected
//                )
//
//                cl_cancel_begin_trip_btns_in_bike_booked.view_animating.setBackgroundColor(
//                    Color.TRANSPARENT
//                )

            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        return connectedAnimation!!
    }

    fun hideBookingTimerExpired(){
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            0F,  // fromYDelta
            cl_booking_timer_expired.height.toFloat() + 50
        ) // toYDelta

        animate.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationEnd(animation: Animation) {
                cl_booking_timer_expired.visibility = View.GONE
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationStart(p0: Animation?) {

            }
        })

        animate.repeatCount=0
        animate.duration = 500
        cl_booking_timer_expired.startAnimation(animate)
    }

    fun hideBookingTimerRunning(){
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            0F,  // fromYDelta
            cl_booking_timer_running.height.toFloat() + 50
        ) // toYDelta

        animate.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationEnd(animation: Animation) {
                cl_booking_timer_running.visibility = View.GONE
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationStart(p0: Animation?) {

            }
        })

        animate.repeatCount=0
        animate.duration = 500
        cl_booking_timer_running.startAnimation(animate)
    }

    fun hideConnectToLockPopup(){
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            0F,  // fromYDelta
            cl_connect_to_popup.height.toFloat() + 50
        ) // toYDelta

        animate.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationEnd(animation: Animation) {
                cl_connect_to_popup.visibility = View.GONE
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationStart(p0: Animation?) {

            }
        })

        animate.repeatCount=0
        animate.duration = 500
        cl_connect_to_popup.startAnimation(animate)
    }

    fun showConnectToLockPopup(){
        cl_connect_to_popup.visibility=View.VISIBLE
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            cl_connect_to_popup.height.toFloat() + 50,  // fromYDelta
            0F
        ) // toYDelta

        animate.repeatCount=0
        animate.duration = 500
        cl_connect_to_popup.startAnimation(animate)
    }

    override fun hideIotHintPopupDueToTimer() {
        if(view_flipper_in_bike_booked_or_active_trip.displayedChild == TRIP_ONLY) {
            reposition_gps.visibility = View.VISIBLE
            manageParkingIcons(true)
        }
        hideIotHintPopup()
    }

    fun hideIotHintPopup(){
        if(cl_iot_hint_popup.visibility == View.GONE){
            return
        }
        presenter?.iotPopUpAction(false)
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            0F,  // fromYDelta
            cl_iot_hint_popup.height.toFloat()
        ) // toYDelta

        animate.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationEnd(animation: Animation) {
                cl_iot_hint_popup.visibility = View.GONE
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationStart(p0: Animation?) {

            }
        })

        animate.repeatCount=0
        animate.duration = 500
        cl_iot_hint_popup.startAnimation(animate)
    }

    fun showIotHintPopup(){
        Observable.timer(presenter.getIotPopUpDelay(),TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                presenter.setIotPopUpDelayToNormal()
                animateIoTHintPopUpToBeShown()
            },{
                presenter.setIotPopUpDelayToNormal()
                animateIoTHintPopUpToBeShown()
            })
    }

    fun animateIoTHintPopUpToBeShown(){
        cl_iot_hint_popup.visibility=View.VISIBLE
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            cl_iot_hint_popup.height.toFloat(),  // fromYDelta
            0F
        ) // toYDelta

        animate.repeatCount=0
        animate.duration = 500
        cl_iot_hint_popup.startAnimation(animate)
        presenter?.iotPopUpAction(true)
    }

    //// Begin trip animation :end


    //// Active trip animation :end
    override fun onStartRideFail() {
        showServerGeneralError(REQUEST_SERVER_ERROR_START_RIDE)
    }
    //// Active trip animation :start

    //// start qr code for segway ////
    fun launchQRCodeActivity(){
        startActivityForResult(ScanBikeQRCodeActivity.getIntent(
            requireContext(),
            presenter.ride?.bikeId!!,
            presenter.provideApplicableQRCode(),
            true,
            presenter.ride?.bike_bike_name
        ), REQUEST_CODE_IOT_OR_ADAPTER_QR_CODE)

//        showProgressLoading(getString(R.string.starting_ride_loader))
//        presenter.startRide()
    }
    //// end qr code for segway ////


    //// end ride :start

    fun launchCancelBikeBooking(){
        launchPopUpActivity(
            REQUEST_CODE_CANCEL_RIDE, getString(R.string.bookin_cancel_title),
            if (IsRidePaid.isRidePaidForFleet(presenter.ride?.bike_fleet_type) && !TextUtils.isEmpty(presenter.ride?.price_for_bike_unlock) &&
                presenter?.ride?.price_for_bike_unlock!!.toFloat()!=0F) {
                val cost = CurrencyUtil.getCurrencySymbolByCode(presenter.ride?.currency,presenter.ride?.price_for_bike_unlock
                    .toString())
                getString(R.string.unlock_fee_label_for_cancel_reservation,cost)
            }else{
                getString(R.string.booking_cancel_message)
                 },
            null,
            getString(R.string.booking_cancel_confirm),
            null,
            null,
            getString(R.string.cancel)
        )
    }

    fun checkForParkingFee(){
        if(isEndRideAllowedDueToLockPosition) {
            if(ResourceHelper.parkingStation(presenter.ride?.bike_type)){
                startEndRide()
                return
            }else{
                disableLockUnlockBeforeParkingFeeCheck()
                showProgressLoading(getString(R.string.checking_parking_label))
                presenter.subscribeToGeoFenceIntervalIfApplicable(false)
                presenter.getParkingFeeForFleet()
            }
        }else{
            Log.e("BikeBookedOrActive", "Sorry end ride not possible ::::::)))))))))")
        }
    }

    fun launchEndRideActivity(isForceEndRide: Boolean) {
        presenter.turnOFFIoTIfApplicable()
        startActivityForResult(
            EndRideActivity.getIntent(
                activity as Context,
                presenter.ride?.rideId!!,
                presenter.currentUserLocation,
                presenter.lock_battery,
                null,
                isForceEndRide
            ),
            REQUEST_CODE_END_RIDE
        )
        activity?.overridePendingTransition(0, 0);
        presenter.doNeedfulWhenEndRideActivityLaunched()
    }

    //// end ride :end


    override fun onLockDisconnectedAfterEndingRide() {
        presenter.onDestroy()
        ((activity) as HomeActivity).takeActionAfterEndingRide(presenter.endRideSummary)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_CODE_END_RIDE ){
            if(resultCode == RESULT_OK && data!=null && data.hasExtra(RIDE_SUMMARY_DATA)){
                endAllAndStartBikeSearching()
                presenter.endRideSummary = data.getSerializableExtra(RIDE_SUMMARY_DATA) as RideSummary
                launchRideSummaryActivity(presenter.endRideSummary!!)
            }else{
                presenter.doNeedfulWhenEndRideActivityFinished()
            }
        }else if(requestCode==REQUEST_CODE_CANCEL_RIDE && resultCode == RESULT_OK){
            cancelRideReservation()
        }else if(requestCode == REQUEST_RESTRICTED_PARKING && resultCode == RESULT_OK){
            //TODO show parking zones
            showParkingMain()
        }else if(requestCode == REQUEST_RESTRICTED_PARKING && resultCode == RESULT_CANCELED){
            // Do nothing
        }else if((requestCode == REQUEST_OUT_OF_BOUND_PARKING || requestCode == REQUEST_OUT_OF_ZONE_PARKING) && resultCode == RESULT_OK){
            if(data!=null && data.hasExtra(POSITIVE_LEVEL) && data.getIntExtra(POSITIVE_LEVEL, -1)==1){
                //TODO show parking zones
                showParkingMain()
            }else if(data!=null && data.hasExtra(POSITIVE_LEVEL) && data.getIntExtra(
                    POSITIVE_LEVEL,
                    -1
                )==2){
                startEndRide()
            }
        }else if(requestCode == REQUEST_CODE_IOT_OR_ADAPTER_QR_CODE && resultCode == RESULT_OK){
            showProgressLoading(getString(R.string.starting_ride_loader))
            presenter.startRide()
        }else if(requestCode == REQUEST_CODE_IOT_ELLIPSE_COMBINE_LOCK && resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra(POSITIVE_LEVEL) && data.getIntExtra(
                    POSITIVE_LEVEL,
                    -1
                ) == 1) {
                presenter.lockTurnOFFIotBike()
            }else if (data != null && data.hasExtra(POSITIVE_LEVEL) && data.getIntExtra(
                    POSITIVE_LEVEL,
                    -1
                ) == 2) {
                // do nothing as no change is required
            }
        }else if (requestCode == REQUEST_CODE_SHACKLE_JAM){
            shackleJamPopupShowing=false
            presenter.stopLockPositionError()
        }else if(requestCode==REQUEST_CODE_DOCK_HUB_NOTIFICATION ){
            dockHubNotificationShown = false
            if(data!=null &&
                data.hasExtra(POSITIVE_LEVEL) && data.getIntExtra(POSITIVE_LEVEL, -1)==1){
                presenter.doNeedfulIfKuhmuteIsUndockedFromPopUp()
            }else if (data!=null &&
                data.hasExtra(POSITIVE_LEVEL) && data.getIntExtra(POSITIVE_LEVEL, -1)==2){
                    presenter.doNeedfulIfKuhmuteBeforeEndRide()
                isEndRideAllowedDueToLockPosition=true
                checkForParkingFee()
            }

        }else if(requestCode == REQUEST_CODE_GEO_FENCE_NOTICE){
            presenter.geoFencePopUpShownPreviously = false
        }

    }

    fun endAllAndStartBikeSearching(){
        presenter.onDestroy()
        presenter.stopActiveTripService()
//        presenter.disconnectAllLocksAfterEndingRide()
    }


    fun handleReportTheft(){
        showProgressLoading(getString(R.string.theft_report_loader))
        presenter.reportTheft()
    }

    fun cancelRideReservationOrEndRide(){

    }

    override fun cancelRideReservation(){
        showProgressLoading(getString(R.string.booking_cancel_loading))
        presenter.cancelBikeReservation()
    }

    fun launchRideSummaryActivity(rideSummary: RideSummary){
        startActivityForResult(
            RideSummaryActivity.getIntent(activity as Context, rideSummary),
            REQUEST_CODE_RIDE_SUMMARY
        )
    }


    override fun onCancelBikeSuccess() {
        hideProgressLoading()
        presenter.disconnectAllLocksAfterEndingRide()
    }

    override fun onCancelBikeFail() {
        hideProgressLoading()
        showServerGeneralError(REQUEST_SERVER_ERROR_CANCEL_BIKE)
    }


    fun showProgressLoading(message: String){
        bike_booked_or_active_trip_loading.ct_loading_title.text = message
        bike_booked_or_active_trip_loading.visibility = View.VISIBLE
    }

    override fun hideProgressLoading(){
        bike_booked_or_active_trip_loading.visibility = View.GONE
    }


    //// Parking fee :start

    override fun enableLockUnlockAndHideProgressAfterParkingFeeCheck() {
        Observable.timer(2000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    enableLockUnlockAfterParkingFeeCheck()
                    hideProgressLoading()
                },
                {

                }
            )

    }

    fun enableLockUnlockAfterParkingFeeCheck(){
        lock_unlock_button.deepForEach { isEnabled = true }
    }

    fun disableLockUnlockBeforeParkingFeeCheck(){
        lock_unlock_button.deepForEach { isEnabled = false }
    }

    override fun showServerErrorForParkingFee() {
        hideProgressLoading()
        showServerGeneralError(REQUEST_SERVER_ERROR_PARKING)
        enableLockUnlockAndHideProgressAfterParkingFeeCheck()
    }

    override fun showRestrictedParking() {
        hideProgressLoading()
        launchPopUpActivity(
            REQUEST_RESTRICTED_PARKING,
            getString(R.string.label_note),
            getString(R.string.parking_restricted_warning_message),
            null,
            getString(R.string.ride_find_nearby_zone),
            null,
            null,
            getString(R.string.cancel_capital)
        )
        enableLockUnlockAndHideProgressAfterParkingFeeCheck()
    }

    override fun showOutOfBound(fee: Float, currency: String) {
        hideProgressLoading()
        launchPopUpActivity(
            REQUEST_OUT_OF_BOUND_PARKING,
            getString(R.string.active_ride_out_of_zones_title),
            getString(
                R.string.active_ride_parking_out_of_bounds_text,
                CurrencyUtil.getCurrencySymbolByCode(currency,java.lang.Float.toString(fee))
            ),
            null,
            getString(R.string.ride_find_nearby_zone),
            getString(R.string.end_ride),
            null,
            getString(R.string.cancel_capital)
        )
        enableLockUnlockAndHideProgressAfterParkingFeeCheck()
    }

    override fun showOutOfZone() {
        hideProgressLoading()
        launchPopUpActivity(
            REQUEST_OUT_OF_ZONE_PARKING,
            getString(R.string.active_ride_out_of_zones_title),
            getString(R.string.active_ride_out_of_zones_text),
            null,
            getString(R.string.ride_find_nearby_zone),
            getString(R.string.end_ride),
            null,
            getString(R.string.cancel_capital)
        )
        enableLockUnlockAndHideProgressAfterParkingFeeCheck()
    }

    override fun startEndRide() {
        enableLockUnlockAndHideProgressAfterParkingFeeCheck()
        launchEndRideActivity(presenter.ride?.bike_skip_parking_image!!)
    }

    //// Parking fee :end


    ////////////////////////// code to show parking zone: start ////////////////////////////

    ////////////////////////// code to show parking zone: start ////////////////////////////
    override fun onFindingZoneSuccess(parkingZones: List<ParkingZone>, isGeoFence:Boolean) {
        removeLayerAndSourceForPolgon(getMapboxMap())
        presenter.HOLE_COORDINATES?.clear()
        presenter.GEOFENCE_HOLE_COORDINATES.clear()

        for (i in parkingZones.indices) {
            val parkingZone: ParkingZone? = parkingZones[i]
            if (parkingZone != null) {
                val parkingZoneGeometries: List<ParkingZoneGeometry>? = parkingZone.parkingZoneGeometry
                if (parkingZoneGeometries != null && parkingZoneGeometries.size > 0) {
                    val polygon: ArrayList<Point> =
                        ArrayList()
                    if (parkingZone.type.equals("circle", true)
                    ) {   //parkingzone is circular
                        for (j in parkingZoneGeometries.indices) {
                            val parkingZoneGeometry: ParkingZoneGeometry =
                                parkingZoneGeometries[j]
                            if (parkingZoneGeometry != null) {
                                val centreLatLng =
                                    LatLng()
                                centreLatLng.longitude = parkingZoneGeometry.longitude
                                centreLatLng.latitude = parkingZoneGeometry.latitude
                                val radius: Double = parkingZoneGeometry.radius
                                val degreesBetweenPoints = 8 //45 sides
                                val numberOfPoints =
                                    Math.floor(360 / degreesBetweenPoints.toDouble()).toInt()
                                val distRadians =
                                    radius / 6371000.0 // earth radius in meters
                                val centerLatRadians =
                                    centreLatLng.latitude * Math.PI / 180
                                val centerLonRadians =
                                    centreLatLng.longitude * Math.PI / 180
                                for (index in 0 until numberOfPoints) {
                                    val degrees =
                                        index * degreesBetweenPoints.toDouble()
                                    val degreeRadians =
                                        degrees * Math.PI / 180
                                    val pointLatRadians = Math.asin(
                                        Math.sin(centerLatRadians) * Math.cos(
                                            distRadians
                                        ) + Math.cos(centerLatRadians) * Math.sin(
                                            distRadians
                                        ) * Math.cos(degreeRadians)
                                    )
                                    val pointLonRadians =
                                        centerLonRadians + Math.atan2(
                                            Math.sin(degreeRadians) * Math.sin(
                                                distRadians
                                            ) * Math.cos(centerLatRadians),
                                            Math.cos(distRadians) - Math.sin(
                                                centerLatRadians
                                            ) * Math.sin(pointLatRadians)
                                        )
                                    val pointLat =
                                        pointLatRadians * 180 / Math.PI
                                    val pointLon =
                                        pointLonRadians * 180 / Math.PI
                                    val latLng =
                                        LatLng(pointLat, pointLon)
                                    presenter.latLngBounds.include(latLng)
                                    presenter.points.add(
                                        Point.fromLngLat(
                                            latLng.longitude,
                                            latLng.latitude
                                        )
                                    )
                                    polygon.add(Point.fromLngLat(latLng.longitude, latLng.latitude))
                                }
                            }
                        }
                    } else {  // parkingzone type is non circular
                        for (j in parkingZoneGeometries.indices) {
                            val parkingZoneGeometry: ParkingZoneGeometry =
                                parkingZoneGeometries[j]
                            if (parkingZoneGeometry != null) {
                                val latLng =
                                    LatLng()
                                latLng.longitude = parkingZoneGeometry?.longitude
                                latLng.latitude = parkingZoneGeometry?.latitude
                                polygon.add(Point.fromLngLat(latLng.longitude, latLng.latitude))
                                presenter.latLngBounds.include(latLng)
                                presenter.points.add(
                                    Point.fromLngLat(
                                        latLng.longitude,
                                        latLng.latitude
                                    )
                                )
                            }
                        }
                    }
                    if (!polygon.isEmpty()) {
                        presenter.HOLE_COORDINATES.add(polygon)
                    }
                }
            }
        }
        if (presenter.HOLE_COORDINATES.size > 0) {
            isParkingRestricted = true
        }

        if(isGeoFence){
            presenter.GEOFENCE_HOLE_COORDINATES.addAll(presenter.HOLE_COORDINATES)
        }

        drawPolygon(isGeoFence)
    }


    private fun drawPolygon(isGeoFence: Boolean) {
        addPolyGons(
            getMapboxMap(), presenter.HOLE_COORDINATES,
            ContextCompat.getColor(requireContext(), if(isGeoFence)R.color.geofence_out_line_color else R.color.parking_zone_out_line_color),
            ContextCompat.getColor(requireContext(), if(isGeoFence)R.color.geofence_fill_color else R.color.parking_zone_fille_color), 0.1f
        )
    }
    ////////////////////////// code to show parking zone: end ////////////////////////////

    ////////////////////////// code to show parkings: start ////////////////////////////
    override fun onFindingParkingSuccess() {
        takeActionAfterParkingData()
        showMarker(
            getMapboxMap(),
            presenter.featureCollection!!,
            presenter.clusterQueryLayerIds,
            presenter.latLngBounds,
            presenter.points
        )
    }

    override fun onFindParkingFailure() {
        takeActionAfterParkingData()
        if(presenter.featureCollection!=null)showMarker(getMapboxMap(),presenter.featureCollection!!,presenter.clusterQueryLayerIds,presenter.latLngBounds,presenter.points)
        MapboxUtil.zoomToMarkers(
            getMapboxMap(),
            presenter.latLngBounds,
            presenter.points
        )
    }

    fun takeActionAfterParkingData(){
        hideProgressLoading()
        showHideDependingUponParking()
        addCurrentLocation()
    }

    fun addCurrentLocation(){
        if(presenter.currentUserLocation!=null){
            MapboxUtil.showUserCurrentLocation(getMapboxMap()!!, presenter.currentUserLocation!!)
            presenter.points.add(
                Point.fromLngLat(
                    presenter.currentUserLocation?.longitude!!,
                    presenter.currentUserLocation?.latitude!!
                )
            )
            presenter.latLngBounds.include(
                LatLng(
                    presenter.currentUserLocation?.latitude!!,
                    presenter.currentUserLocation?.longitude!!
                )
            )
        }
    }

    fun showHideDependingUponParking(){
        if(!presenter.doesParkingSpotZoneExists){
            ct_when_no_parking.visibility = View.VISIBLE
            cl_when_parking.visibility = View.GONE
        }else{
            ct_when_no_parking.visibility = View.GONE
            cl_when_parking.visibility = View.VISIBLE
        }
    }




    ////////////////////////// code to show parkings: end ////////////////////////////


    ////////////////////////// code for dock hub: start ////////////////////////////
    override fun onDockHubsSuccess() {
        for (dockHub in presenter.dockHubs!!) {
            if(dockHub.ports!=null){
                generateAndAddHubDockMarker(getMapboxMap(),requireActivity(),dockHub?.ports?.size!!,true)
            }
        }
        presenter.setDockHubsMarkerData()
    }
    ////////////////////////// code for dock hub: end ////////////////////////////


    override fun onDestroy() {
        super.onDestroy()
        removeLayerAndSourceForLocation(getMapboxMap())
        removeLayerAndSourceForMarker(getMapboxMap())
        removeLayerAndSourceForPolgon(getMapboxMap())
    }

    override fun onReportTheftSuccess() {
        hideProgressLoading()
        endAllAndStartBikeSearching()
        PopUpActivity.launchForResult(
            requireActivity(), REQUEST_REPORT_THEFT_SUCCESS,
            getString(R.string.theft_report_success_title),
            getString(R.string.theft_report_success_message),
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }

    override fun onReportTheftFailure() {
        hideProgressLoading()
        showServerGeneralError(REQUEST_SERVER_ERROR_REPORT_THEFT)
    }

    fun willHandleInternetChange(isConnected: Boolean):Boolean{

        if(presenter.startWithStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_TRIP && (presenter.isIotModule()==BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_IOT_COMBINE || presenter.isIotModule()==BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_ONLY)) {
           if(isConnected) layout_fragment_bike_booked_or_active_ride_content.bike_booked_or_active_trip_no_internet.visibility = View.GONE else layout_fragment_bike_booked_or_active_ride_content.bike_booked_or_active_trip_no_internet.visibility = View.VISIBLE
            return true
        }else if(presenter.startWithStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_TRIP && presenter.isIotModule()==BaseBluetoothFragmentPresenter.HardwareType.IOT_ONLY){
            if(isConnected && presenter.iotLockPosition == null && previouslyNoInternet) { // get new position
                previouslyNoInternet=false
                presenter.getIoTBikeStatus()
            }else if(isConnected && presenter.iotLockPosition != null && previouslyNoInternet) {   // restore previous position
                previouslyNoInternet=false
                handleIoTLockPosition()
            }else if(!isConnected){  // user cannot lock in IoT without internet so dont show unlock unlock button
                lock_unlock_button.visibility = View.GONE
                cl_lock_unlock_disabled_button.visibility = View.VISIBLE
                previouslyNoInternet=true
            }
        }
        return false    // if IoT dont show user can lock unlock
    }


    //// IoT : Start ////
    override fun showIotScreen() {
        cl_scan_iot_cancel_scan_btns_in_bike_booked.visibility = View.VISIBLE
        cl_scan_iot_cancel_scan_btns_in_bike_booked_with_active_trip.visibility = View.VISIBLE


        cl_scan_adapter_cancel_scan_btns_in_bike_booked.visibility = View.GONE
        cl_scan_adapter_cancel_scan_btns_in_bike_booked_with_active_trip.visibility = View.GONE


        cl_cancel_begin_trip_btns_in_bike_booked.cl_cancel_begin_trip_btns_in_bike_booked.visibility = View.GONE
        cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.visibility = View.GONE


        cl_manual_lock_begin_trip_cancel_btns_in_bike_booked.visibility = View.GONE
        cl_manual_lock_begin_trip_cancel_btns_in_bike_booked_with_active_trip.visibility = View.GONE

    }

    //// IoT : End ////


    //// Manual Lock : Start ////
    override fun showManualLockScreen() {
        cl_scan_iot_cancel_scan_btns_in_bike_booked.visibility = View.GONE
        cl_scan_iot_cancel_scan_btns_in_bike_booked_with_active_trip.visibility = View.GONE


        cl_scan_adapter_cancel_scan_btns_in_bike_booked.visibility = View.GONE
        cl_scan_adapter_cancel_scan_btns_in_bike_booked_with_active_trip.visibility = View.GONE


        cl_cancel_begin_trip_btns_in_bike_booked.cl_cancel_begin_trip_btns_in_bike_booked.visibility = View.GONE
        cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.visibility = View.GONE


        cl_manual_lock_begin_trip_cancel_btns_in_bike_booked.visibility = View.VISIBLE
        cl_manual_lock_begin_trip_cancel_btns_in_bike_booked_with_active_trip.visibility = View.VISIBLE

    }

    fun showManualLockActiveRideIfApplicable(){

        if(presenter.isManualLock()) {


            if(presenter.activeTripFromQrCode) {
                Observable.timer(7000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        configureManualLockTextAndVisibility()
                    }, {

                    })
            }else{
                configureManualLockTextAndVisibility()
            }


            cl_lock_unlock_disabled_button.visibility = View.GONE
            lock_unlock_button.visibility = View.GONE
            lock_unlock_disabled_button.visibility = View.GONE

            btn_end_ride_find_a_station.visibility = View.GONE
            btn_end_ride_selected.visibility = View.VISIBLE
            btn_end_ride_unselected.visibility = View.GONE
            cl_animating_in_active_trip.visibility = View.GONE

            isEndRideAllowedDueToLockPosition = true
        }
    }

    fun configureManualLockTextAndVisibility(){
        lock_unlock_manual_lock_button.checkedText =
            getString(R.string.code_manual_lock)
        lock_unlock_manual_lock_button.uncheckedText =
            getString(R.string.code_manual_lock)
        lock_unlock_manual_lock_button.setCheckedAnimated(false)
        cl_lock_unlock_manual_lock_button.visibility = View.VISIBLE
    }


    override fun showManualLockPopScreen() {
        val code = presenter.provideManualLockCode()
        if(!TextUtils.isEmpty(code)) {
            popup_manual_lock.cv_code.text = code
            popup_manual_lock.visibility = View.VISIBLE
        }
    }

    //// Manual Lock : End ////


    //// No bike lock :start ////

    override fun onNoLockAvailableForBike() {
        cl_lock_unlock_disabled_button.visibility = View.GONE
        lock_unlock_button.visibility = View.GONE
        isEndRideAllowedDueToLockPosition = true
        btn_end_ride_find_a_station.visibility = View.GONE
        btn_end_ride_selected.visibility = View.GONE
        btn_end_ride_unselected.visibility = View.GONE
        cl_animating_in_active_trip.visibility = View.GONE
        presenter.handleNoLockForBikeCondition()
    }

    override fun showAdapterScanAndCancelBtns() {
        btn_end_ride_find_a_station.visibility = View.VISIBLE


        cl_scan_iot_cancel_scan_btns_in_bike_booked.visibility = View.GONE
        cl_scan_iot_cancel_scan_btns_in_bike_booked_with_active_trip.visibility = View.GONE


        cl_scan_adapter_cancel_scan_btns_in_bike_booked.visibility = View.VISIBLE
        cl_scan_adapter_cancel_scan_btns_in_bike_booked_with_active_trip.visibility = View.VISIBLE


        cl_cancel_begin_trip_btns_in_bike_booked.cl_cancel_begin_trip_btns_in_bike_booked.visibility = View.GONE
        cl_cancel_begin_trip_btns_in_bike_booked_with_active_trip.visibility = View.GONE


        cl_manual_lock_begin_trip_cancel_btns_in_bike_booked.visibility = View.GONE
        cl_manual_lock_begin_trip_cancel_btns_in_bike_booked_with_active_trip.visibility = View.GONE
    }

    //// No bike lock :end ////


    //// show bike on map: start ////
    fun showMarker(){
        MapboxUtil.showSingleMarker(
            getMapboxMap(),
            Location(presenter.ride?.bike_latitude!!, presenter.ride?.bike_longitude!!),
            ResourceHelper.getBikeResource(presenter.ride?.bike_type,presenter.ride?.bike_bike_battery_level),
            presenter.ride?.bikeId!!,
            presenter.currentUserLocation!!
        )
    }

    //// show bike on map: end ////


    //// axa :start
    override fun lockGuidancePopup(status: Boolean) {
        if (status)
            layout_axa_lock_popup_in_bike_booked_or_active_ride.visibility=View.VISIBLE
        else
            layout_axa_lock_popup_in_bike_booked_or_active_ride.visibility=View.GONE
    }
    //// axa :end


    //// sentinel :start
    override fun showSentinelUnlockUI() {
        lock_unlock_button.visibility = View.VISIBLE
        lock_unlock_button.updateProgressBarWith(
            true, ContextCompat.getColor(
                activity as Context,
                R.color.unlock
            )
        )
    }

    override fun sentinelLockGuidance(active: Boolean) {
        lockGuidancePopup(active)
    }

    override fun sentinelTapGuidance(active: Boolean) {
        if(active) {
            layout_sentinel_lock_layout_in_bike_booked_or_active_ride.visibility = View.VISIBLE
        }else{
            layout_sentinel_lock_layout_in_bike_booked_or_active_ride.visibility = View.GONE
        }
    }

    //// sentinel :end


    ///////// HUB DOCK :start
    override fun showDockHubDockingNotification(firebasePushNotification: FirebasePushNotification) {

        if(!dockHubNotificationShown && firebasePushNotification.clickAction.equals(docking,true)) {
            dockHubNotificationShown=true
            launchPopUpActivity(
                REQUEST_CODE_DOCK_HUB_NOTIFICATION,
                LocaleTranslatorUtils.getLocaleString(
                    requireContext(),
                    firebasePushNotification.titleLocKey
                ).toString(),
                LocaleTranslatorUtils.getLocaleString(
                    requireContext(),
                    firebasePushNotification.bodyLocKey
                ).toString(),
                null,
                getString(R.string.unlock),
                getString(R.string.end_ride),
                null,
                null
            )
        }else if(firebasePushNotification.clickAction.equals(docked,true)){
            presenter.getRideSummaryForEndingRide(presenter.ride?.rideId!!)
        }
    }

    override fun onGetRideSummarySuccessForEndingRide(rideSummary: RideSummary) {
        endAllAndStartBikeSearching()
        launchRideSummaryActivity(rideSummary)
    }

    override fun onGetRideSummaryFailureForEndingRide() {
        showServerGeneralError(REQUEST_CODE_DOCK_HUB_END_RIDE_FAILURE)
    }

    fun takeActionIfDockHubBikeIsDocked(){
        if(presenter.isDockHubBikeDocked() && view_flipper_in_bike_booked_or_active_trip.displayedChild == TRIP_ONLY) {

            subscriptions.add(
                Observable.timer(1000,TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        showDockHubDockingNotification(FirebasePushNotification(null,null,"end_your_ride","your_vehicle_is_docked",
                            docking))
                    },{

                    })
            )

        }
    }

    ///////// HUB DOCK :end

    //// geoFence :start
    fun showGeoFenceIfApplicable() {
        presenter.showGeoFenceIfApplicable()
    }

    override fun applyGeoFenceRestrictionIfApplicable()
    {
        if(presenter.geoFenceRestrictionApplicable() &&
            view_flipper_in_bike_booked_or_active_trip.displayedChild == TRIP_ONLY &&
                !presenter.endRideActivityLaunched()){
            var insideGeoFence = false
            for(polygon in presenter.GEOFENCE_HOLE_COORDINATES){
                val polygons : java.util.ArrayList<java.util.ArrayList<Point>> = java.util.ArrayList()
                polygons.add(polygon)
                if(TurfJoins.inside(Point.fromLngLat(presenter.currentUserLocation!!.longitude,presenter.currentUserLocation!!.latitude), Polygon.fromLngLats(
                        polygons as List<MutableList<Point>>
                    ))){
                    insideGeoFence = true
                    break
                }
            }

            if(insideGeoFence){
                if(presenter.geoFencePopUpShownPreviously) removeGeoFenceNotification()
                presenter.geoFencePopUpShownPreviously = false
            }else if(!insideGeoFence && !presenter.geoFencePopUpShownPreviously){
                showGeoFencePopupAndNotification()
            }
        }
    }

    fun showGeoFencePopupAndNotification(){
        presenter.geoFencePopUpShownPreviously = true

        launchPopUpActivity(
            REQUEST_CODE_GEO_FENCE_NOTICE,
            getString(R.string.notice),
            getString(R.string.geo_fence_warning),
            null,
            null,
            null,
            null,
            getString(R.string.general_btn_ok)
        )

        createLocalNotification(
            getString(R.string.notice),
            getString(R.string.geo_fence_warning),
            GEO_FENCE_NOTIFICATION_CHANNEL_ID,
            GEO_FENCE_NOTIFICATION_CHANNEL_NAME,
            GEO_FENCE_NOTIFICATION_ID
        )

    }

    fun removeGeoFenceNotification(){
        removeNotification(GEO_FENCE_NOTIFICATION_ID)
    }
    //// geoFence :end


    //// lockUnlockPopUp :start
    override fun lockUnlockPopUp(show: Boolean, locking: Boolean?) {
        if(show){
            cl_lock_unlock_popup.visibility = View.VISIBLE
            ct_lock_unlock_popup.text = if(locking!!) getString(R.string.locking) else getString(R.string.unlocking)
        }else{
            cl_lock_unlock_popup.visibility = View.GONE
        }
    }
    //// lockUnlockPopUp :end

    //// Tapkey :start
    override fun showTapkeyUnlockSuccessFailurePopup(status: Boolean) {
        launchPopUpActivity(
            REQUEST_CODE_TAPKEY_POPUP,
            if(status) getString(R.string.success) else getString(R.string.unsuccessful),
            null,
            null,
            null,
            null,
            null,
            getString(R.string.general_btn_ok)
        )
    }
    //// Tapkey :end


    //// localnotification :start
    override fun showReservationTimerOverPopUp() {
        launchPopUpActivity(
            REQUEST_CODE_RESERVATION_TIMER_OVER_POPUP,
            getString(R.string.reservation_ending_soon),
            null,
            null,
            null,
            null,
            null,
            getString(R.string.general_btn_ok)
        )
    }
    //// localnotification :end


    fun manageParkingIcons(unselectionParking:Boolean){
        if(ResourceHelper.parkingStation(presenter.ride?.bike_type)){
            parking_unselected.visibility = View.GONE
            parking_selected.visibility = View.GONE
        }else{
            when(unselectionParking){
                true -> {
                    parking_unselected.visibility = View.VISIBLE
                    parking_selected.visibility = View.GONE
                }
                false ->{
                    parking_unselected.visibility = View.GONE
                    parking_selected.visibility = View.VISIBLE
                }
            }
        }
    }
}