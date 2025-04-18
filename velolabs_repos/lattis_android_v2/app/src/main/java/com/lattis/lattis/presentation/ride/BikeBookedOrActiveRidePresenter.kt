package com.lattis.lattis.presentation.ride

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import com.lattis.domain.models.*
import com.lattis.domain.usecase.bike.CancelBikeReservationUseCase
import com.lattis.domain.usecase.maintenance.ReportTheftUseCase
import com.lattis.domain.usecase.parking.GetParkingFeeForFleetUseCase
import com.lattis.domain.usecase.parking.GetParkingZoneUseCase
import com.lattis.domain.usecase.parking.GetParkingsForFleetUseCase
import com.lattis.domain.usecase.ride.StartRideUseCase
import com.lattis.domain.usecase.parking.GetDockHubUseCase
import com.lattis.domain.models.Parking
import com.lattis.domain.models.ParkingZone
import com.lattis.domain.models.Ride
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.usecase.parking.GetGeoFenceUseCase
import com.lattis.domain.usecase.v2.BookingsUseCase
import com.lattis.domain.usecase.v2.CancelBookingUseCase
import com.lattis.domain.usecase.v2.StartTripUseCase
import com.lattis.lattis.infrastructure.di.module.SettingsModule
import com.lattis.lattis.infrastructure.di.module.SettingsModule.Companion.KEY_SHOWING_CONNECT_TO_LOCK
import com.lattis.lattis.presentation.base.fragment.bluetooth.BaseBluetoothFragmentPresenter
import com.lattis.lattis.presentation.help.SliderImageHelper.imageSliderApplies
import com.lattis.lattis.presentation.ride.BikeBookedOrActiveRideFragment.Companion.ACTIVE_TRIP_SHOWN_FROM_QR_CODE
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.FirebaseMessagingHelper
import com.lattis.lattis.presentation.utils.FirebaseUtil
import com.lattis.lattis.presentation.utils.IsRidePaid
import com.lattis.lattis.presentation.utils.MapboxUtil
import com.lattis.lattis.presentation.utils.MapboxUtil.MARKER_ID
import com.lattis.lattis.presentation.utils.MapboxUtil.MARKER_SELECTED
import com.lattis.lattis.presentation.utils.MapboxUtil.MARKER_TYPE
import com.lattis.lattis.presentation.utils.MapboxUtil.hub_dock_parking
import com.lattis.lattis.utils.ResourceHelper.getResourcesByParkingType
import com.lattis.lattis.utils.ResourceHelper.parkingStation
import com.lattis.lattis.utils.SentinelHelper.isSentinel
import com.lattis.lattis.utils.UtilsHelper
import com.lattis.lattis.utils.settings.IntPref
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import io.reactivex.rxjava3.functions.Function

class BikeBookedOrActiveRidePresenter @Inject constructor(
    val startRideUseCase: StartRideUseCase,
    val startTripUseCase: StartTripUseCase,
    val cancelBikeReservationUseCase: CancelBikeReservationUseCase,
    val cancelBookingsUseCase: CancelBookingUseCase,
    val getParkingFeeForFleetUseCase: GetParkingFeeForFleetUseCase,
    @param:Named(KEY_SHOWING_CONNECT_TO_LOCK) private val connectToLockCounter: IntPref?,
    @param:Named(SettingsModule.KEY_SHOWING_TUTORIAL) private val tutorialCounter: IntPref?,
    val getParkingZoneUseCase: GetParkingZoneUseCase,
    val getParkingsForFleetUseCase: GetParkingsForFleetUseCase,
    val reportTheftUseCase: ReportTheftUseCase,
    val getDockHubUseCase: GetDockHubUseCase,
    val firebaseMessagingHelper: FirebaseMessagingHelper,
    val getGeoFenceUseCase: GetGeoFenceUseCase
): BaseBluetoothFragmentPresenter<BikeBookedOrActiveRideView>() {

    var startWithStatus: Companion.CurrentStatus?=null;
    val CURRENT_STATUS = "CURRENT_STATUS"
    var bikeBookTimer: CountDownTimer? = null
    var iotPopUpTimer: CountDownTimer? = null
    val IOT_POP_UP_DURATION = 10000
    private var isRideStartingRequiredAfterLocationUpdate = false
    private var isParkingFeeCheckRequiredAfterLocationUpdate = false
    private var isDockHubCheckRequiredAfterLocationUpdate = false
    private var isStartRideInProgress = false
    private var activeTripDisposable:Disposable?=null
    private val connectToLockThreshold = 3
    private val tutorialThreshold = 1

    var HOLE_COORDINATES: java.util.ArrayList<java.util.ArrayList<Point>> = java.util.ArrayList()
    var GEOFENCE_HOLE_COORDINATES: java.util.ArrayList<java.util.ArrayList<Point>> = java.util.ArrayList()  //This is used only for checking if location is inside or not
    var latLngBounds = LatLngBounds.Builder()
    var points: java.util.ArrayList<Point> = ArrayList()
    var featureCollection: FeatureCollection?=null
    val clusterQueryLayerIds:Array<String?> = arrayOfNulls<String>(MapboxUtil.CLUSTER_LAYER_ARRAY.size)
    var parkings:List<Parking>?=null
    var dockHubs:List<DockHub>?=null
    var parkingMarkerCoordinates: ArrayList<Feature> = ArrayList()
    var previouslySelectedParking:Parking?=null
    var previouslySelectedHubDock:DockHub?=null
    var endRideSummary : RideSummary?=null
    var doesParkingSpotZoneExists = false
    var geoFences:List<GeoFence>?=null
    var compatibleParkingZones : ArrayList<ParkingZone> = ArrayList()
    var geoFencePopUpShownPreviously = false
    var activeTripFromQrCode = false
    var geoFenceTimerSubscription : Disposable?=null
    var GET_GEO_FENCE_INTERVAL_MILLIS = 10000
    private var iotPopUpDelay = 50L



    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if(arguments!=null && arguments.containsKey(CURRENT_STATUS)){
            startWithStatus = arguments.get(CURRENT_STATUS) as Companion.CurrentStatus
        }

        if(arguments!=null && arguments.containsKey(ACTIVE_TRIP_SHOWN_FROM_QR_CODE)){
            activeTripFromQrCode = arguments.get(ACTIVE_TRIP_SHOWN_FROM_QR_CODE) as Boolean
        }
    }

    fun setScreen(){
        if(startWithStatus== Companion.CurrentStatus.ACTIVE_BOOKING){
            view?.showBikeOnly()
        }else if(startWithStatus == Companion.CurrentStatus.ACTIVE_BOOKING_WITH_TRIP_STARTED){
            view?.showBikeBookingWithTrip(false)
        }else if(startWithStatus == Companion.CurrentStatus.ACTIVE_TRIP){
            handleActiveTripView(false)
        }
    }

    ////////////////////////// Bluetooth connection : start ///////////////////////////

    fun startLockConnection(){
        Log.e("BaseBleFragPres","startLockConnection")
        if(startWithStatus== Companion.CurrentStatus.ACTIVE_BOOKING){
            when(isIotModule()) {
                HardwareType.IOT_ONLY -> {view?.showIotScreen()}
                HardwareType.MANUAL_LOCK -> {view?.showManualLockScreen()}
                HardwareType.ELLIPSE_ONLY -> {handleLockConnectionForBLE()}
                HardwareType.ELLIPSE_IOT_COMBINE -> {handleLockConnectionForBLE()}
            }
        }else if(startWithStatus == Companion.CurrentStatus.ACTIVE_BOOKING_WITH_TRIP_STARTED){
            when(isIotModule()) {
                HardwareType.IOT_ONLY -> {view?.showIotScreen()}
                HardwareType.MANUAL_LOCK -> {view?.showManualLockScreen()}
                HardwareType.ELLIPSE_ONLY -> {handleLockConnectionForBLE()}
                HardwareType.ELLIPSE_IOT_COMBINE ->{ handleLockConnectionForBLE()}
            }
        }else if(startWithStatus == Companion.CurrentStatus.ACTIVE_TRIP){
            when(isIotModule()) {
                HardwareType.IOT_ONLY -> {view?.showIotScreen()}
                HardwareType.ELLIPSE_ONLY -> {handleLockConnectionForBLE()}
                HardwareType.ELLIPSE_IOT_COMBINE -> {handleLockConnectionForBLE()}
            }
        }
    }

    override fun startLockConnectionForBLE(){
        if((isIotModule() == HardwareType.ELLIPSE_IOT_COMBINE || isIotModule() == HardwareType.ELLIPSE_ONLY)){
            if(getLockVendor() == UseCase.LockVendor.TAPKEY){
                view?.showIotScreen()
            }else{
                connectToLastLockedLock()
            }
        }
    }

    fun doNeedfulWhenNoLockWithBike(){
        when(isIotModule()) {
            HardwareType.NONE -> {view?.onNoLockAvailableForBike()}
        }
    }

    fun startListeningToFirebasePushNotificationIfApplicable(){
        if(isItValidKuhmute() || omniLock()){
            startListeningToFirebasePushNotification()
        }
    }

    fun doNeedfulWhenDisconnected(){
        Log.e("BikeBookPresenter","doNeedfulWhenDisconnected")
        stopLocationTrackInActiveTripService()
        if(startWithStatus== Companion.CurrentStatus.ACTIVE_BOOKING){
            getSignedMessagePublicKey()
            view?.startDisconnectedAnimation()
        }else if(startWithStatus == Companion.CurrentStatus.ACTIVE_BOOKING_WITH_TRIP_STARTED){
            getSignedMessagePublicKey()
            view?.startDisconnectedAnimation()
        }else if(startWithStatus == Companion.CurrentStatus.ACTIVE_TRIP){
            getSignedMessagePublicKey()
            view?.startConnectingAnimation(true)
        }
    }

    fun onLockNotFound(){
        if(startWithStatus== Companion.CurrentStatus.ACTIVE_BOOKING){
            getSignedMessagePublicKey()
        }else if(startWithStatus == Companion.CurrentStatus.ACTIVE_BOOKING_WITH_TRIP_STARTED){
            getSignedMessagePublicKey()
        }else if(startWithStatus == Companion.CurrentStatus.ACTIVE_TRIP){
            view?.showConnectionTimeOut()
        }
    }


    ////////////////////////// Bluetooth connection : end ///////////////////////////

    //// Bike Booked :start
    fun startBikeReservationTimer() {
        val timeRemaining: Long = calculateTimeRemaining()
        if (timeRemaining == 0L || timeRemaining < 0) {
            showBikeBookingWithTripOrCancelBikeBookingAfterBookingTimer()
            return
        }
        if (bikeBookTimer != null) {
            bikeBookTimer?.cancel()
        }
        bikeBookTimer = object : CountDownTimer((timeRemaining * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val time =
                    String.format("%02d", seconds / 60) + ":" + String.format(
                        "%02d",
                        seconds % 60
                    )
                view?.showBikeBookedTime(time)
            }

            override fun onFinish() {
                showBikeBookingWithTripOrCancelBikeBookingAfterBookingTimer()
            }
        }.start()
    }

    private fun calculateTimeRemaining(): Long {
        val currentTime: Long = UtilsHelper.getTime()
        val diffTime: Long = currentTime - ride?.bike_booked_on!!
        return if (diffTime > ride?.bike_expires_in!!) {
            0
        } else {
            (ride?.bike_expires_in!! + ride?.bike_booked_on!! - currentTime)
        }
    }
    private fun showBikeBookingWithTripOrCancelBikeBookingAfterBookingTimer() {
        if (IsRidePaid.isRidePaidForFleet(ride?.bike_fleet_type)) {
            startWithStatus=Companion.CurrentStatus.ACTIVE_BOOKING_WITH_TRIP_STARTED
            view?.showBikeBookingWithTrip(true)
        } else {
            // TODO cancel bike booking
                Observable.timer(2000,TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {view?.cancelRideReservation()},{}
                    )
        }
    }

    fun stopBikeReservationTimer(){
        if(bikeBookTimer!=null){
            bikeBookTimer?.cancel()
            bikeBookTimer=null
        }
    }

    fun cancelBikeReservation() {
        subscriptions.add(
            cancelBookingsUseCase
                .withBookingId(ride?.bike_booking_id!!)
                .withDamage(false)
                .withLockIssue(false)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        if (view != null) {
                            stopBikeReservationTimer()
                            view?.onCancelBikeSuccess()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        if (view != null) {
                            view?.onCancelBikeFail()
                        }
                    }
                })
        )
    }

    //// Bike Booked :end


    //// Bike booked and active trip :start

    fun callGetUserCurrentLocationAfterDelay(){
        subscriptions.add(Observable.timer(1000,TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                userCurrentStatus()
            },{}))
    }


    @Synchronized
    fun startActiveTripTime() {

        stopBikeReservationTimer()

        if(activeTripDisposable!=null)activeTripDisposable?.dispose()
        subscriptions.add(Observable.interval(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view?.showActiveTripTime(
                    UtilsHelper.getDurationBreakdown(
                        (UtilsHelper.getTime() - ride?.ride_booked_on!!).toLong()
                    )
                )
            }).also {
                activeTripDisposable = it
            }
        )
    }


    fun startLocationTrackingIfApplicable(){
        if ((ride?.do_not_track_trip == null || ride?.do_not_track_trip ==false) &&
            startWithStatus == Companion.CurrentStatus.ACTIVE_TRIP &&
            connectionState !=null &&
            connectionState==ConnectionState.CONNECTED) {
                subscriptions.add(Observable.timer(7000,TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        startLocationTrackInActiveTripService()
                    },{

                    }))
        } else {
            stopLocationTrackInActiveTripService()
        }
    }

    //// Bike booked and active trip :end


    //// dirty work for ride object :start
    fun updateRideObjectWithLatestInformation(){

        ride?.rideId = rideInUserCurrentStatus?.rideId!!
        ride?.bikeId = (rideInUserCurrentStatus?.bikeId!!)
        ride?.ride_booked_on = rideInUserCurrentStatus?.ride_booked_on!!
        ride?.do_not_track_trip= rideInUserCurrentStatus?.do_not_track_trip
        ride?.isFirst_lock_connect = rideInUserCurrentStatus?.isFirst_lock_connect?:false

    }
    //// dirty work for ride object :end

    //// Active Trip Service :start

    fun startRide() {
        if (currentUserLocation == null) {
            isRideStartingRequiredAfterLocationUpdate = true
            requestLocationUpdates()
            return
        }
        if (isStartRideInProgress) {
            return
        }
        isStartRideInProgress = true
        isRideStartingRequiredAfterLocationUpdate = false
        subscriptions.add(
            startTripUseCase
                .withRide(ride)
                .withLocation(currentUserLocation)
                .withFirstLockConnect(startWithStatus!=Companion.CurrentStatus.ACTIVE_BOOKING_WITH_TRIP_STARTED)
                .withDeviceToken(firebaseMessagingHelper.getFirebaseToken())
                .execute(object : RxObserver<Ride>(view) {
                    override fun onNext(ride: Ride) {
                        super.onNext(ride)
                        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.startRideEventName,String.format(FirebaseUtil.startRideEventMessage,ride.rideId))
                        isStartRideInProgress = false
                        startWithStatus = Companion.CurrentStatus.ACTIVE_TRIP
                        startLocationTrackingIfApplicable()
                        handleActiveTripView(true)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        isStartRideInProgress = false
                        view?.hideProgressLoading()
                        view?.onStartRideFail()
                    }
                })
        )
    }

    fun handleActiveTripView(fromStartRide:Boolean){
        if(fromStartRide) {
            if(isIotModule() == HardwareType.IOT_ONLY || isIotModule() == HardwareType.ELLIPSE_IOT_COMBINE){
                if(isSentinel(ride)) {
                    view?.showSentinelUnlockUI()
                    view?.hideProgressLoading()
                }
                lockUnlockIotBike()
            }else if(isManualLock()){
                view?.showManualLockPopScreen()
            }
            handleTapkeyAfterStartRideIfApplicable()
            doNeedfulIfKuhmuteAfterStartRide()
        }else{
            handleTapkeyAfterStartRideIfApplicable()
            if (activeTripFromQrCode){
                doNeedfulIfKuhmuteAfterStartRide()
                if(isSentinel(ride)) {
                    // Do not call getIotBIkeStatus but show unlocking button and start unlock procedure
                    view?.showSentinelUnlockUI()
                    lockUnlockIotBike()
                } else if(linkaLock()){
                    lockUnlockIotBike()
                }else{
                    if ((isIotModule() == HardwareType.IOT_ONLY || isIotModule() == HardwareType.ELLIPSE_IOT_COMBINE)) getIoTBikeStatus()
                }
                if(isManualLock())view?.showManualLockPopScreen()
            }else{
                if ((isIotModule() == HardwareType.IOT_ONLY || isIotModule() == HardwareType.ELLIPSE_IOT_COMBINE)) getIoTBikeStatus()
            }
        }
        doNeedfulForSentinelBikeForActiveTrip()
        getGeoFencesIfApplicable()
        startListeningToLocalNotification()
        view?.showActiveTrip()
    }


    //// Active Trip Service :end


    //// Parking fee :start

    fun getParkingFeeForFleet() {
        if (currentUserLocation != null) {
            isParkingFeeCheckRequiredAfterLocationUpdate=false
            subscriptions.add(
                getParkingFeeForFleetUseCase
                    .withFleetId(ride?.bike_fleet_id!!)
                    .withLocation(currentUserLocation!!)
                    .execute(object : RxObserver<ParkingFeeForFleet>(view) {
                        override fun onNext(parkingFeeForFleet: ParkingFeeForFleet) {
                            super.onNext(parkingFeeForFleet)
                            showAppropiateUIForParking(parkingFeeForFleet)
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e!!)
                            view?.showServerErrorForParkingFee()
                        }
                    })
            )
        } else {
            isParkingFeeCheckRequiredAfterLocationUpdate=true
            requestLocationUpdates()
        }
    }

    private fun showAppropiateUIForParking(parkingFeeForFleet: ParkingFeeForFleet) {
        if (parkingFeeForFleet.not_allowed==null) {
            view?.showServerErrorForParkingFee()
        } else if (parkingFeeForFleet.not_allowed!=null && parkingFeeForFleet.not_allowed!!) {
            view?.showRestrictedParking()
        } else if (parkingFeeForFleet.not_allowed!=null && parkingFeeForFleet.not_allowed!! == false) {
            if (parkingFeeForFleet.fee!=null && parkingFeeForFleet.fee!!>0 && parkingFeeForFleet.currency!=null) {
                FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.outOfParkingEventName,String.format(FirebaseUtil.outOfParkingEventMessage,ride?.rideId))
                view?.showOutOfBound(
                    parkingFeeForFleet.fee!!,
                    parkingFeeForFleet.currency!!
                )
            } else if (parkingFeeForFleet.outside!=null && parkingFeeForFleet.outside!!) {
                FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.outOfParkingEventName,String.format(FirebaseUtil.outOfParkingEventMessage,ride?.rideId))
                view?.showOutOfZone()
            } else {
                view?.startEndRide()
            }
        }else{
            view?.enableLockUnlockAndHideProgressAfterParkingFeeCheck()
        }
    }

    //// Parking fee :end


    fun checkIfAnyThingBlockedDueLocation(){
        if(isParkingFeeCheckRequiredAfterLocationUpdate){
            getParkingFeeForFleet()
        }else if(isRideStartingRequiredAfterLocationUpdate) {
            startRide()
        }else if(isDockHubCheckRequiredAfterLocationUpdate){
            getDockHubParking()
        }
    }

    fun requireToShowConnectToLockPopup():Boolean{
        if(connectToLockCounter==null || connectToLockCounter.value==null || connectToLockCounter.value!! < connectToLockThreshold){
            connectToLockCounter?.value = connectToLockCounter?.value!! + 1
            return true
        }
        return false
    }

    fun requireToShowTutorial():Boolean{
        if(imageSliderApplies() && (tutorialCounter==null || tutorialCounter.value==null || tutorialCounter.value!! < tutorialThreshold)){
            tutorialCounter?.value = tutorialCounter?.value!! + 1
            return true
        }
        return false
    }


    //// parking spot and zone :start

    fun findParkingsFromFleetId() {
        parkings=null
        subscriptions.add(
            getParkingsForFleetUseCase
                .withFleetId(ride?.bike_fleet_id!!)
                .execute(object : RxObserver<List<Parking>>() {
                   override fun onNext(newParkings: List<Parking>) {
                        if (newParkings != null && newParkings.size>0) {
                            parkings = newParkings
                            doesParkingSpotZoneExists=true
                            setParkingMarkerData(parkings!!)
                            view?.onFindingParkingSuccess()
                        }else{
                            createParkingFeatureCollection()
                            view?.onFindParkingFailure()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        createParkingFeatureCollection()
                        view?.onFindParkingFailure()

                    }
                })
        )
    }

    fun getParkingZones() {
        subscriptions.add(
            getParkingZoneUseCase
                .withFleetID(ride?.bike_fleet_id!!)
                .execute(object : RxObserver<List<ParkingZone>>() {
                    override fun onNext(parkingZone: List<ParkingZone>) {
                        if (parkingZone != null && parkingZone.size>0) {
                            doesParkingSpotZoneExists = true
                            view.onFindingZoneSuccess(parkingZone,false)
                        }
                        findParkingsFromFleetId()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        findParkingsFromFleetId()
                    }
                })
        )
    }


    fun setParkingMarkerData(parkingList: List<Parking>){

        for(parking in parkingList){
            val feature =
                Feature.fromGeometry(
                    Point.fromLngLat(
                        parking?.longitude!!,
                        parking?.latitude!!
                    )
                )

            feature.addStringProperty("poi",getResourcesByParkingType(parking.type))
            feature.addNumberProperty(MARKER_ID,parking.parking_spot_id)
            feature.addBooleanProperty(MARKER_SELECTED,false)
            parkingMarkerCoordinates.add(feature)

            val parkingLatLng =
                LatLng(parking.latitude!!, parking.longitude!!)
            points.add(Point.fromLngLat(parking.longitude!!,parking.latitude!!))
            latLngBounds.include(parkingLatLng)
        }

//        if(currentUserLocation!=null){
//            latLngBounds.include(LatLng(currentUserLocation?.latitude!!,currentUserLocation?.longitude!!))
//        }

        createParkingFeatureCollection()

    }

    fun getDockHubParking(){
        latLngBounds = LatLngBounds.Builder()
        HOLE_COORDINATES.clear()
        points.clear()
        doesParkingSpotZoneExists = false
        dockHubs=null
        isDockHubCheckRequiredAfterLocationUpdate=false
        parkingMarkerCoordinates= ArrayList()
        featureCollection = null

        if(currentUserLocation!=null) {
            subscriptions.add(
                getDockHubUseCase
                    .withBikeId(ride?.bikeId!!)
                    .withLocation(currentUserLocation!!)
                    .execute(object : RxObserver<List<DockHub>>() {
                        override fun onNext(newDockHubs: List<DockHub>) {
                            if (newDockHubs != null && newDockHubs.size > 0) {
                                dockHubs = newDockHubs
                                view?.onDockHubsSuccess()
                            } else {
                                getParkingZones()
                            }
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e)
                            getParkingZones()
                        }
                    })
            )
        }else {
            isDockHubCheckRequiredAfterLocationUpdate=true
            requestLocationUpdates()
        }

    }


    fun setDockHubsMarkerData(){
        for(dockHub in dockHubs!!){
            val feature =
                Feature.fromGeometry(
                    Point.fromLngLat(
                        dockHub.longitude!!,
                        dockHub.latitude!!
                    )
                )

            feature.addStringProperty("poi", hub_dock_parking+"_"+dockHub.ports?.size!!)
            feature.addNumberProperty(MARKER_ID,dockHub.hub_id)
            feature.addStringProperty(MARKER_TYPE, hub_dock_parking)
            feature.addBooleanProperty(MARKER_SELECTED,false)
            parkingMarkerCoordinates.add(feature)

            val parkingLatLng =
                LatLng(dockHub.latitude!!, dockHub.longitude!!)
            points.add(Point.fromLngLat(dockHub.longitude!!,dockHub.latitude!!))
            latLngBounds.include(parkingLatLng)
        }
//        if(currentUserLocation!=null){
//            latLngBounds.include(LatLng(currentUserLocation?.latitude!!,currentUserLocation?.longitude!!))
//        }
        getParkingZones()
    }

    fun createParkingFeatureCollection(){
        featureCollection = FeatureCollection.fromFeatures(parkingMarkerCoordinates);
    }

    fun addUserCurrentLocationToLatLng(){
        if(currentUserLocation!=null){
            latLngBounds.include(LatLng(currentUserLocation?.latitude!!,currentUserLocation?.longitude!!))
        }
    }


    fun setSelectedParking(feature: Feature?){

        if(feature?.getStringProperty(MARKER_TYPE)!=null && feature.getStringProperty(MARKER_TYPE).equals(
                hub_dock_parking) && dockHubs!=null){
            for(dockHub in dockHubs!!){
                if (feature?.getNumberProperty(MARKER_ID)!!.toInt()
                        .equals(dockHub?.hub_id)
                ) {
                    previouslySelectedParking = null
                    previouslySelectedHubDock = dockHub
                    break
                }
            }
        }else {
            if (parkings != null) {
                for (parking in parkings!!) {
                    if (feature?.getNumberProperty(MARKER_ID)!!.toInt()
                            .equals(parking?.parking_spot_id)
                    ) {
                        previouslySelectedParking = parking
                        previouslySelectedHubDock = null
                        break
                    }
                }
            }
        }
    }

    fun getSelectedFeature(): Feature? {
        if (featureCollection != null) {
            for (feature in featureCollection?.features()!!) {
                if (feature.getBooleanProperty(MARKER_SELECTED)) {
                    return feature
                }
            }
        }
        return null
    }

    fun getGeoFencesIfApplicable() {
        subscriptions.add(
            getGeoFenceUseCase
                .withFleetID(ride?.bike_fleet_id!!)
                .execute(object : RxObserver<List<GeoFence>>() {
                    override fun onNext(geoFencesNew: List<GeoFence>) {
                        if (geoFencesNew != null && geoFencesNew.size>0) {
                            geoFences = geoFencesNew
                            showGeoFenceIfApplicable()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                    }
                })
        )
    }

    fun showGeoFenceIfApplicable(){
        compatibleParkingZones = ArrayList()
        if(geoFences!=null && geoFences?.size!!>0){
            for(geoFence in geoFences!!){
                if(geoFence!=null && geoFence.geometry!=null && geoFence?.geometry?.shape!=null) {
                    var compatibleParkingZone= ParkingZone()
                    compatibleParkingZone.type = geoFence?.geometry?.shape!!
                    val parkingZoneGeometies:ArrayList<ParkingZoneGeometry>  = ArrayList()
                    if(geoFence.geometry?.shape.equals("circle", true) &&
                        geoFence?.geometry?.center!=null && geoFence?.geometry?.center?.latitude!=null && geoFence?.geometry?.center?.longitude!=null &&
                        geoFence?.geometry?.radius!=null && geoFence?.geometry?.radius?.value!=null){   // circle is the shape
                        val parkingZoneGeometry = ParkingZoneGeometry()
                        parkingZoneGeometry.latitude = geoFence?.geometry?.center?.latitude!!
                        parkingZoneGeometry.longitude = geoFence?.geometry?.center?.longitude!!
                        parkingZoneGeometry.radius = geoFence?.geometry?.radius?.value!!
                        if(geoFence?.geometry?.radius?.units!=null && geoFence?.geometry?.radius?.units.equals("kilometers",true)){
                            parkingZoneGeometry.radius = parkingZoneGeometry.radius * 1000
                        }

                        parkingZoneGeometry.radius = geoFence?.geometry?.radius?.value!! * 1000
                        parkingZoneGeometies.add(parkingZoneGeometry)
                    }else if(geoFence.geometry?.shape.equals("polygon",true) &&
                            geoFence.geometry?.points!=null && geoFence.geometry?.points?.size!!>0) {
                        for(point in geoFence.geometry?.points!!){
                            val parkingZoneGeometry = ParkingZoneGeometry()
                            parkingZoneGeometry.latitude = point.latitude!!
                            parkingZoneGeometry.longitude = point.longitude!!
                            parkingZoneGeometies.add(parkingZoneGeometry)
                        }
                    }else if(geoFence.geometry?.shape.equals("rectangle",true) &&
                        geoFence?.geometry?.bbox!=null && geoFence?.geometry?.bbox!!.size==4){

//                        72.5547553729584 -- 0
//                        22.98086404665355 -- 1
//                        72.63463964551002 -- 2
//                        23.038376527286204 -- 3
                        val parkingZoneGeometryNW = ParkingZoneGeometry()
                        parkingZoneGeometryNW.latitude = geoFence?.geometry?.bbox?.get(1)!!
                        parkingZoneGeometryNW.longitude = geoFence?.geometry?.bbox?.get(2)!!
                        parkingZoneGeometies.add(parkingZoneGeometryNW)

                        val parkingZoneGeometryNE = ParkingZoneGeometry()
                        parkingZoneGeometryNE.latitude = geoFence?.geometry?.bbox?.get(3)!!
                        parkingZoneGeometryNE.longitude = geoFence?.geometry?.bbox?.get(2)!!
                        parkingZoneGeometies.add(parkingZoneGeometryNE)

                        val parkingZoneGeometrySE = ParkingZoneGeometry()
                        parkingZoneGeometrySE.latitude = geoFence?.geometry?.bbox?.get(3)!!
                        parkingZoneGeometrySE.longitude = geoFence?.geometry?.bbox?.get(0)!!
                        parkingZoneGeometies.add(parkingZoneGeometrySE)

                        val parkingZoneGeometrySW = ParkingZoneGeometry()
                        parkingZoneGeometrySW.latitude = geoFence?.geometry?.bbox?.get(1)!!
                        parkingZoneGeometrySW.longitude = geoFence?.geometry?.bbox?.get(0)!!
                        parkingZoneGeometies.add(parkingZoneGeometrySW)

                    }
                    if(parkingZoneGeometies.size>0) {
                        compatibleParkingZone.parkingZoneGeometry = parkingZoneGeometies
                        compatibleParkingZones.add(compatibleParkingZone)
                    }
                }
            }
            if(compatibleParkingZones.size>0) view?.onFindingZoneSuccess(compatibleParkingZones,true)
        }
    }

    fun geoFenceRestrictionApplicable():Boolean{
        return geoFences!=null && geoFences?.size!!>0 && GEOFENCE_HOLE_COORDINATES!=null && GEOFENCE_HOLE_COORDINATES.size!!>0
    }

    //// parking spot and zone :end


    fun reportTheft() {
        subscriptions.add(
            reportTheftUseCase
                .withBikeId(ride?.bikeId!!)
                .withTripId(if(ride?.rideId==null) 0 else ride?.rideId!!)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(t: Boolean) {
                        super.onNext(t)
                        view?.onReportTheftSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onReportTheftFailure()
                    }
                })
        )
    }



    @Synchronized
    fun subscribeToGeoFenceIntervalIfApplicable(active: Boolean) {
        if(!geoFenceRestrictionApplicable()){
            return
        }

        if (active) {
            if (geoFenceTimerSubscription == null) {
                    geoFenceTimerSubscription = subscribeToGeoFenceInterval()
                    .subscribeOn(Schedulers.io())
                    .subscribeWith(object : DisposableObserver<Boolean>() {
                        override fun onComplete() {}
                        override fun onError(e: Throwable) {}
                        override fun onNext(success: Boolean) {}
                    })
                subscriptions.add(geoFenceTimerSubscription!!)
            }
        }else{
            cancelGeoFenceTimerSubscription()
        }
    }

    private fun subscribeToGeoFenceInterval(): Observable<Boolean> {
        return Observable.interval(
            GET_GEO_FENCE_INTERVAL_MILLIS.toLong(),
            TimeUnit.MILLISECONDS
        ).flatMap(requestGeoFenceTimer)
    }

    private val requestGeoFenceTimer: Function<Long, Observable<Boolean>> = Function<Long, Observable<Boolean>> {
            view?.applyGeoFenceRestrictionIfApplicable()
            Observable.just(true)
        }

    fun cancelGeoFenceTimerSubscription() {
        if (geoFenceTimerSubscription != null) {
            geoFenceTimerSubscription!!.dispose()
            geoFenceTimerSubscription = null
        }
    }


    //// IoT pop up timer :start
    fun iotPopUpAction(active:Boolean) {
        iotPopUpTimer?.cancel()
        if(active) {
            iotPopUpTimer = object : CountDownTimer(IOT_POP_UP_DURATION.toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {
                        view?.hideIotHintPopupDueToTimer()
                }
            }.start()
        }else{
            iotPopUpTimer = null
        }
    }

    fun setHighIotPopUpDelay(){
        iotPopUpDelay = 6000L
    }

    fun setIotPopUpDelayToNormal(){
        iotPopUpDelay = 50L
    }

    fun getIotPopUpDelay():Long{
        return iotPopUpDelay
    }

    //// IoT pop up timer :end







}