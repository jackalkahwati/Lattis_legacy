package com.lattis.lattis.presentation.home.activity

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.lattis.domain.models.*
import com.lattis.lattis.presentation.base.activity.DrawerMenu
import com.lattis.lattis.presentation.base.activity.location.BaseLocationActivity
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter.Companion.CURRENT_STATUS
import com.lattis.lattis.presentation.base.fragment.BaseFragment
import com.lattis.lattis.presentation.bikelist.BikeListFragment
import com.lattis.lattis.presentation.damage.ReportDamageActivity
import com.lattis.lattis.presentation.fleet.PrivateFleetActivity
import com.lattis.lattis.presentation.help.HelpActivity
import com.lattis.lattis.presentation.history.RideHistoryActivity
import com.lattis.lattis.presentation.membership.MembershipActivity
import com.lattis.lattis.presentation.payment.PaymentActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.profile.ProfileActivity
import com.lattis.lattis.presentation.reservation.ReservationListOrCreateActivity
import com.lattis.lattis.presentation.reservation.ReservationListOrCreateActivity.Companion.RESERVATION_TRIP_START
import com.lattis.lattis.presentation.ride.BikeBookedOrActiveRideFragment
import com.lattis.lattis.presentation.utils.MapboxUtil.cart
import com.lattis.lattis.presentation.utils.MapboxUtil.charging_spots
import com.lattis.lattis.presentation.utils.MapboxUtil.cluster
import com.lattis.lattis.presentation.utils.MapboxUtil.e_bike
import com.lattis.lattis.presentation.utils.MapboxUtil.e_kick_scooter_25
import com.lattis.lattis.presentation.utils.MapboxUtil.e_kick_scooter_100
import com.lattis.lattis.presentation.utils.MapboxUtil.e_kick_scooter_50
import com.lattis.lattis.presentation.utils.MapboxUtil.e_kick_scooter_75
import com.lattis.lattis.presentation.utils.MapboxUtil.generic_parking
import com.lattis.lattis.presentation.utils.MapboxUtil.kayak
import com.lattis.lattis.presentation.utils.MapboxUtil.kick_scooter
import com.lattis.lattis.presentation.utils.MapboxUtil.locker
import com.lattis.lattis.presentation.utils.MapboxUtil.moped
import com.lattis.lattis.presentation.utils.MapboxUtil.nesw
import com.lattis.lattis.presentation.utils.MapboxUtil.parking_meter
import com.lattis.lattis.presentation.utils.MapboxUtil.parking_racks
import com.lattis.lattis.presentation.utils.MapboxUtil.parking_station
import com.lattis.lattis.presentation.utils.MapboxUtil.regular
import com.lattis.lattis.presentation.utils.MapboxUtil.user_current_location
import com.lattis.lattis.utils.AccountAuthenticatorHelper.getAppLogOutBundle
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.geometry.VisibleRegion
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMoveListener
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.TransitionOptions
import com.mapbox.turf.TurfMeasurement
import io.lattis.lattis.R
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.content_layout_home.*
import kotlinx.android.synthetic.main.nav_header_home.*
import kotlinx.android.synthetic.main.no_internal_layout.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class HomeActivity : BaseLocationActivity<HomeActivityPresenter, HomeActivityView>(),HomeActivityView,
    OnMapReadyCallback, OnMoveListener, MapboxMap.OnFlingListener,MapboxMap.OnMapClickListener{

    @Inject
    override lateinit var presenter: HomeActivityPresenter
    override var view: HomeActivityView = this
    private var currentFragment: Fragment?=null
    var centerLatLng: LatLng?=null
    private val moveThreshold:Double = 0.4  // 1 kms is threshold for move detected
//    private val BOUND_CORNER_NW =
//        LatLng(82.85338229176081, -141.328125)
//    private val BOUND_CORNER_SE =
//        LatLng(-62.59334083012023, 167.34375)
    private val BOUND_CORNER_NW =
        LatLng(82.85338229176081, -141.328125)
    private val BOUND_CORNER_SE =
        LatLng(-62.59334083012023, 167.34375)

    private val RESTRICTED_BOUNDS_AREA =
        LatLngBounds.Builder()
            .include(BOUND_CORNER_NW)
            .include(BOUND_CORNER_SE)
            .build()

    private val REQUEST_CODE_LOGOUT_RIDE_ERROR=2244
    private val REQUEST_CODE_LOGOUT=2245
    private val REQUEST_CODE_THEFT_DAMAGE_SELECTION=2246
    private val REQUEST_CODE_DAMAGE=2247
    private val REQUEST_CODE_THEFT=2248
    private val REQUEST_CODE_PAYMENT_ACTIVITY=2249
    private val REQUEST_CODE_ERROR=2250
    private val REQUEST_CODE_RESERVATION_EDIT=2251
    private val REQUEST_CODE_LOGOUT_ERROR=2252
    private val REQUEST_CODE_RESERVATION_LIST_OR_CREATE=2253
    private val REQUEST_CODE_PRIVATE_FLEET_ACTIVITY=2254
    private val REQUEST_CODE_MEMBERSHIP_ACTIVITY=2255
    private val REQUEST_CODE_APP_SETTINGS = 2256
    var isFirstTimeLoadingApp = true


    companion object{
        val SUBSCRIPTION_LIST = "SUBSCRIPTION_LIST"
    }

    //// Drawer navigation ////////
    override val activityContentLayoutId
        protected get() =  R.layout.content_layout_home


//    override val defaultSelectedItemId
//        protected get() = R.id.nav_home

    var menuItem: DrawerMenu? = null
    var mToolbar: Toolbar? = null

    lateinit var mapboxMap: MapboxMap

    override fun onDrawerItemClicked(menuItem: DrawerMenu) {
        super.onDrawerItemClicked(menuItem)
    }
    override fun configureViews() {
        super.configureViews()
        invalidateOptionsMenu()
        hideToolbar()
    }
    override fun onDrawerItemSelected(menuItem: DrawerMenu) {
        this.menuItem = menuItem
        if(menuItem.itemId!=null) {
            replaceDrawerFragment(menuItem.itemId!!)
        }
    }

    private fun replaceDrawerFragment(menuItemId: Int) {
        supportFragmentManager.popBackStack()
        when (menuItemId) {

            R.id.menu_profile_settings -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }

            R.id.menu_payment -> {
                startActivityForResult(
                    Intent(this, PaymentActivity::class.java),
                    REQUEST_CODE_PAYMENT_ACTIVITY
                )
            }

            R.id.menu_ride_history -> {
                startActivity(Intent(this, RideHistoryActivity::class.java))
            }

            R.id.menu_private_fleets -> {
                startActivityForResult(
                    Intent(this, PrivateFleetActivity::class.java),
                    REQUEST_CODE_PRIVATE_FLEET_ACTIVITY
                )
            }

            R.id.menu_membership -> {
                startActivityForResult(
                    Intent(this, MembershipActivity::class.java),
                    REQUEST_CODE_MEMBERSHIP_ACTIVITY
                )
            }
            R.id.menu_report_issue -> {

                // Its required to remove the report theft so commenting the code and starting report damage directly
//                PopUpActivity.launchForResult(
//                    this, REQUEST_CODE_THEFT_DAMAGE_SELECTION,
//                    null,
//                    null,
//                    null,
//                    getString(R.string.report_damage),
//                    getString(R.string.report_theft),
//                    null,
//                    getString(R.string.cancel)
//                )

                startActivityForResult(
                    Intent(this, ReportDamageActivity::class.java),
                    REQUEST_CODE_DAMAGE
                )
            }
            R.id.menu_help -> {
                startActivity(Intent(this, HelpActivity::class.java))
            }
            R.id.menu_logout -> {
                startLogoutActivitIfNotInRideOrError()
            }
            R.id.menu_reservation -> {
                startActivityForResult(
                    ReservationListOrCreateActivity.getIntent(
                        this,
                        (currentFragment is BikeBookedOrActiveRideFragment)
                    ), REQUEST_CODE_RESERVATION_LIST_OR_CREATE
                )
            }
        }
    }

    fun refreshReservationCount(){
        presenter.getReservations()
    }

    fun setDrawerMenuWithRide() {
        setBaseDrawerMenuWithRide()
        refreshReservationCount()
    }

    fun setDrawerMenuWithoutRide() {
        setBaseDrawerMenuWithoutRide()
        refreshReservationCount()
    }

    override fun handleUser(user: User) {
        if(drawer_user_email == null || drawer_user_name == null){
            Observable.timer(2000,TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    setUsernameAndEmail(user)
                }, {

                })
        }else{
            setUsernameAndEmail(user)
        }
    }

    fun setUsernameAndEmail(user:User){
        if(drawer_user_email!=null){
            drawer_user_email.text = user.email
        }

        if(drawer_user_name!=null){
            drawer_user_name.text = user.firstName + " " + user.lastName
        }
    }
    //// drawer navigation :end

    //// tool bar :start
    override fun getToolbar(toolbar: Toolbar?) {
        super.getToolbar(toolbar)
        mToolbar = toolbar
        if(toolbar!=null) {
            toolbar.findViewById<View>(R.id.toolbar_subtitle)
                .setOnClickListener {

                }
            toolbar.findViewById<View>(R.id.iv_arrow)
                .setOnClickListener {

                }
        }
    }


    //// tool bar :end

    //// activity override :start
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        checkForSubscription()
    }

    fun checkForSubscription(){
        if(intent.hasExtra(SUBSCRIPTION_LIST) && intent.getSerializableExtra(SUBSCRIPTION_LIST)!=null){
            presenter.subscriptionList = intent.getSerializableExtra(SUBSCRIPTION_LIST)as List<Subscription>
        }else{
            presenter.getSubscriptions()
        }
    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        presenter.getUser
        mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState!!)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_CODE_LOGOUT &&
            data!=null &&
            data.hasExtra(PopUpActivity.POSITIVE_LEVEL) &&
            data.getIntExtra(PopUpActivity.POSITIVE_LEVEL, -1)==1) {

            Observable.timer(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showLoadingForHome(getString(R.string.log_out_title))
                    presenter.logOut()
                }, {

                })

        }else if(requestCode == REQUEST_CODE_THEFT_DAMAGE_SELECTION && resultCode == Activity.RESULT_OK){
            if(data!=null && data.hasExtra(PopUpActivity.POSITIVE_LEVEL) && data.getIntExtra(
                    PopUpActivity.POSITIVE_LEVEL, -1
                )==1){
                startActivityForResult(
                    Intent(this, ReportDamageActivity::class.java),
                    REQUEST_CODE_DAMAGE
                )
            }else if(data!=null && data.hasExtra(PopUpActivity.POSITIVE_LEVEL) && data.getIntExtra(
                    PopUpActivity.POSITIVE_LEVEL, -1
                )==2){

                PopUpActivity.launchForResult(
                    this, REQUEST_CODE_THEFT,
                    getString(R.string.report_theft_title),
                    getString(R.string.report_theft_message),
                    null,
                    getString(R.string.report_theft),
                    null,
                    null,
                    getString(R.string.cancel)
                )

            }
        }else if(requestCode == REQUEST_CODE_DAMAGE && resultCode == Activity.RESULT_OK){
            if(data!=null && data.hasExtra(ReportDamageActivity.END_RIDE_AFTER_DAMAGE) && data.getBooleanExtra(
                    ReportDamageActivity.END_RIDE_AFTER_DAMAGE, false
                )){

                if(currentFragment!=null && currentFragment is BikeBookedOrActiveRideFragment){
                    (currentFragment as BikeBookedOrActiveRideFragment).launchEndRideActivity(true)
                }

            }else if(data!=null && data.hasExtra(ReportDamageActivity.CANCEL_BIKE_BOOKING_AFTER_DAMAGE) && data.getBooleanExtra(
                    ReportDamageActivity.CANCEL_BIKE_BOOKING_AFTER_DAMAGE, false
                )){

                if(currentFragment!=null && currentFragment is BikeBookedOrActiveRideFragment){
                    (currentFragment as BikeBookedOrActiveRideFragment).cancelRideReservation()
                }

            }
        }else if(requestCode == REQUEST_CODE_THEFT && resultCode == Activity.RESULT_OK){
            if(currentFragment!=null && currentFragment is BikeBookedOrActiveRideFragment){
                (currentFragment as BikeBookedOrActiveRideFragment).handleReportTheft()
            }
        }else if(requestCode == REQUEST_CODE_PAYMENT_ACTIVITY){
            if(currentFragment!=null && currentFragment is BikeListFragment){
                (currentFragment as BikeListFragment).fetchCardList()
            }
        }else if(requestCode == REQUEST_CODE_ERROR){
            finish()
        }else if(requestCode == REQUEST_CODE_RESERVATION_LIST_OR_CREATE){
            if(resultCode == Activity.RESULT_OK && data!=null &&
                data.hasExtra(RESERVATION_TRIP_START) &&
                data.getSerializableExtra(RESERVATION_TRIP_START)!=null){
                val startReservation = data.getSerializableExtra(RESERVATION_TRIP_START) as StartReservation
                setDrawerMenuWithRide()
                startShowingActiveTripFragment(true)
            }else{
                refreshReservationCount()
            }
        }
        else if (requestCode == REQUEST_CODE_PRIVATE_FLEET_ACTIVITY && resultCode == Activity.RESULT_OK &&
            currentFragment!=null && currentFragment is BikeListFragment){
            (currentFragment as BikeListFragment).showBikesAndCurrentLocation()
        }else if(requestCode == REQUEST_CODE_MEMBERSHIP_ACTIVITY && resultCode == RESULT_OK &&
            currentFragment!=null && currentFragment is BikeListFragment){
            presenter.getSubscriptions(true)
        }else if(requestCode == REQUEST_CODE_APP_SETTINGS){
            openAppSettings()
        }
    }


    //// activity override :end


    ////

    //// map :start
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        this.mapboxMap.uiSettings.isRotateGesturesEnabled = false
        this.mapboxMap.uiSettings.isCompassEnabled = false


        mapboxMap.setStyle(Style.LIGHT, Style.OnStyleLoaded {
            // Disable any type of fading transition when icons collide on the map. This enhances the visual
            // look of the data clustering together and breaking apart.
            // Disable any type of fading transition when icons collide on the map. This enhances the visual
            // look of the data clustering together and breaking apart.
//            mapboxMap.setLatLngBoundsForCameraTarget(RESTRICTED_BOUNDS_AREA);
            it.setTransition(TransitionOptions(0, 0, false))
            addMarkerImages()
            startFetchingLocation()
        })
        this.mapboxMap?.uiSettings?.isRotateGesturesEnabled = false
        this.mapboxMap?.uiSettings?.isCompassEnabled = false
        this.mapboxMap?.addOnMoveListener(this)
        this.mapboxMap?.addOnFlingListener(this)
        this.mapboxMap?.addOnMapClickListener(this)

    }

    fun addMarkerImages(){



        val ebike_image =
            BitmapFactory.decodeResource(resources, R.drawable.e_bike)
        mapboxMap.style!!.addImage(e_bike, ebike_image)

        val e_kick_scooter_image_25 =
            BitmapFactory.decodeResource(resources, R.drawable.e_bike_25)
        mapboxMap.style!!.addImage(e_kick_scooter_25, e_kick_scooter_image_25)

        val e_kick_scooter_image_50 =
            BitmapFactory.decodeResource(resources, R.drawable.e_bike_50)
        mapboxMap.style!!.addImage(e_kick_scooter_50, e_kick_scooter_image_50)

        val e_kick_scooter_image_75 =
            BitmapFactory.decodeResource(resources, R.drawable.e_bike_75)
        mapboxMap.style!!.addImage(e_kick_scooter_75, e_kick_scooter_image_75)

        val e_kick_scooter_image_100 =
            BitmapFactory.decodeResource(resources, R.drawable.e_bike_100)
        mapboxMap.style!!.addImage(e_kick_scooter_100, e_kick_scooter_image_100)


        val kick_scooter_image =
            BitmapFactory.decodeResource(resources, R.drawable.kick_scooter)
        mapboxMap.style!!.addImage(kick_scooter, kick_scooter_image)

        val moped_image =
            BitmapFactory.decodeResource(resources, R.drawable.moped)
        mapboxMap.style!!.addImage(moped, moped_image)

        val regular_image =
            BitmapFactory.decodeResource(resources, R.drawable.regular_bike)
        mapboxMap.style!!.addImage(regular, regular_image)

        val locker_image =
            BitmapFactory.decodeResource(resources, R.drawable.locker)
        mapboxMap.style!!.addImage(locker, locker_image)

        val cart_image =
            BitmapFactory.decodeResource(resources, R.drawable.cart)
        mapboxMap.style!!.addImage(cart, cart_image)

        val marker1 =
            BitmapFactory.decodeResource(resources, R.drawable.marker)
        mapboxMap.style!!.addImage(nesw, marker1)

        val cluster_image =
            BitmapFactory.decodeResource(resources, R.drawable.cluster)
        mapboxMap.style!!.addImage(cluster, cluster_image)

        val kayak_image =
            BitmapFactory.decodeResource(resources, R.drawable.kayak)
        mapboxMap.style!!.addImage(kayak, kayak_image)


        val parking_image =
            BitmapFactory.decodeResource(resources, R.drawable.parking_hub)
        mapboxMap.style!!.addImage(parking_station, parking_image)

        val user_current_location_image = BitmapFactory.decodeResource(
            resources,
            R.drawable.current_location_icon
        )
        mapboxMap?.style!!.addImage(user_current_location, user_current_location_image)


        //// parking icons
        val generic_parking_icon =
            BitmapFactory.decodeResource(resources, R.drawable.generic_parking_half)
        mapboxMap.style!!.addImage(generic_parking, generic_parking_icon)

        val parking_meter_icon =
            BitmapFactory.decodeResource(resources, R.drawable.parking_meter_half)
        mapboxMap.style!!.addImage(parking_meter, parking_meter_icon)

        val parking_rack_icon =
            BitmapFactory.decodeResource(resources, R.drawable.parking_rack_half)
        mapboxMap.style!!.addImage(parking_racks, parking_rack_icon)

        val charging_spot_icon =
            BitmapFactory.decodeResource(resources, R.drawable.charging_spot_half)
        mapboxMap.style!!.addImage(charging_spots, charging_spot_icon)
    }

    override fun onFling() {
        Log.e("BikeListFragment", "onFling")

    }

    override fun onMoveBegin(detector: MoveGestureDetector) {
    }

    override fun onMove(detector: MoveGestureDetector) {
    }

    override fun onMoveEnd(detector: MoveGestureDetector) {
        Log.e("BikeListFragment", "onMoveEnd")
        if(isMoveSufficient() && currentFragment!=null){
            centerLatLng = mapboxMap.cameraPosition.target
            (currentFragment as BaseFragment<*, *>).onMapboxMoved(
                mapboxMap.projection.getVisibleRegion(
                    false
                ).latLngBounds
            )
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        if(currentFragment!=null) {
            (currentFragment as BaseFragment<*, *>).onMapClicked(
                mapboxMap.projection.toScreenLocation(
                    point
                )
            )
        }
        return true;
    }
    //// map :end


    //// getLocation :start
    fun startFetchingLocation(){
        fetchLocation()
    }

    override fun onLocationPermissionsAvailable() {

        if(isFirstTimeLoadingApp) {
            isFirstTimeLoadingApp=false
            requestFreshLocationUpdates()
//        MapboxUtil.activateLocationComponent(this,mapboxMap!!)    //Mapbox crashing so will be using custom current location
            presenter.getScreen(intent.getSerializableExtra(CURRENT_STATUS) as BaseUserCurrentStatusPresenter.Companion.CurrentStatus)
        }
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
            data = Uri.fromParts("package", packageName, null)
        })
        finish()
    }

    //// getLocation :end


    fun takeActionAfterEndingRide(endRideSummary: RideSummary?){
//        if(presenter.reservationInCurrentStatus!=null &&
//                    presenter.reservationInCurrentStatus?.trip_payment_transaction!=null &&
//                    presenter.reservationInCurrentStatus?.trip_payment_transaction!!.trip_id == endRideSummary?.trip_id!!){
//            presenter.reservationInCurrentStatus =null
//        }
        startShowingBikeListFragment()
    }

    //// User current status :start
    override fun startShowingBikeListFragment(){
        setDrawerMenuWithoutRide()
        currentFragment = BikeListFragment.getInstance(this)
        replaceFragment(R.id.fragment_container, currentFragment!!)
    }


    override fun startShowingBikeBookedFragment(){
        setDrawerMenuWithRide()
        currentFragment = BikeBookedOrActiveRideFragment.getInstance(
            this,
            com.lattis.lattis.presentation.base.fragment.usercurrentlocation.BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING
        )
        replaceFragment(R.id.fragment_container, currentFragment!!)
    }

    override fun startShowingBikeBookedWithActiveTrip(){
        setDrawerMenuWithRide()
        currentFragment = BikeBookedOrActiveRideFragment.getInstance(
            this,
            com.lattis.lattis.presentation.base.fragment.usercurrentlocation.BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING_WITH_TRIP_STARTED
        )
        replaceFragment(R.id.fragment_container, currentFragment!!)
    }

    override fun startShowingActiveTripFragment(fromQrCode:Boolean){
        setDrawerMenuWithRide()
        currentFragment = BikeBookedOrActiveRideFragment.getInstance(
            this,
            com.lattis.lattis.presentation.base.fragment.usercurrentlocation.BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_TRIP,
            fromQrCode
        )
        replaceFragment(R.id.fragment_container, currentFragment!!)
    }

    override fun onRideSuccess(ride: Ride) {
        startShowingActiveTripFragment(false)
    }

    override fun onRideFailure() {
        showServerError()
    }

    override fun showServerError() {
        showServerConnectError(REQUEST_CODE_ERROR)
    }




    //// User current status :end


    //// User location :end
    override fun setUserPosition(location: Location) {
        requestStopLocationUpdates()
    }




    //// User location :end



    //// back press :start
    override fun onBackPressed(): Unit {
        if (supportFragmentManager.backStackEntryCount === 1) { //setCheckedItem(DrawerMenu.HOME);
            setHomeForBackPressed()
        }
        super.onBackPressed()
    }

    //// back press :end

    private fun getVisibleBounds(): LatLngBounds {
        val v: VisibleRegion = mapboxMap.getProjection().getVisibleRegion()
        val b =
            LatLngBounds.Builder()
        b.include(v.nearLeft)
        b.include(v.nearRight)
        b.include(v.farLeft)
        b.include(v.farRight)
        return b.build()
    }

    private fun isMoveSufficient():Boolean{
        if(centerLatLng!=null){
            if(TurfMeasurement.distance(
                    Point.fromLngLat(
                        centerLatLng?.longitude!!,
                        centerLatLng?.latitude!!
                    ),
                    Point.fromLngLat(
                        mapboxMap.cameraPosition.target.longitude,
                        mapboxMap.cameraPosition.target.latitude
                    )
                ) < moveThreshold){
                return false
            }
        }
        return true
    }

    fun showLoadingForHome(message: String?) {
        home_activity_loading_operation_view.visibility = (View.VISIBLE)
        home_activity_loading_operation_view.ct_loading_title.text = (message)
    }

    fun hideLoadingForHome() {
        home_activity_loading_operation_view.visibility = (View.GONE)
    }


    //// logout :start

    fun startLogoutActivitIfNotInRideOrError(){
        if(currentFragment is BikeListFragment){

            launchPopUpActivity(
                REQUEST_CODE_LOGOUT,
                getString(R.string.log_out),
                getString(R.string.log_out_message),
                null,
                getString(R.string.logout),
                null,
                null,
                getString(R.string.cancel)
            )


        }else{
            launchPopUpActivity(
                REQUEST_CODE_LOGOUT_RIDE_ERROR,
                getString(R.string.logout_ride_error_label),
                null,
                null,
                null,
                null,
                null,
                getString(R.string.cancel_capital)
            )
        }
    }

    override fun onLogOutSuccessfull() {
        authenticateAccount(getAppLogOutBundle())
        finishMe()
    }



    override fun onLogOutFailure() {
        hideLoadingForHome()
        showServerGeneralError(REQUEST_CODE_LOGOUT_ERROR)
    }

    //// logout :end


    override fun onInternetConnectionChanged(isConnected: Boolean) {
        super.onInternetConnectionChanged(isConnected)
//        if(currentFragment==null){
            Observable.timer(3000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    checkForNoInternetInActiveTrip(isConnected)
                }, {

                })

//        }else{
//            checkForNoInternetInActiveTrip(isConnected)
//        }
    }


    fun checkForNoInternetInActiveTrip(isConnected: Boolean){
        if(currentFragment!=null &&
            currentFragment is BikeBookedOrActiveRideFragment &&
            (currentFragment as BikeBookedOrActiveRideFragment).willHandleInternetChange(isConnected)
                && rl_no_internet!=null){
                rl_no_internet.visibility=View.GONE
            }
        }

    //// membership:start

    fun getMembershipDiscount(fleet_id: Int):String?{
        return presenter.getMembershipDiscount(fleet_id)
    }

    override fun onSubscriptionSuccess() {
        if(currentFragment!=null && currentFragment is BikeListFragment){
            (currentFragment as BikeListFragment).refreshBikeCard()
        }
    }

    //// membership:end


    //// reservation: start
    override fun onReservationsAvailable() {
        setReservationsNumber(if(presenter.reservations==null) 0 else presenter.reservations?.size!!)
    }

    override fun onReservationNotAvailable() {

    }

    //// reservation: end

}