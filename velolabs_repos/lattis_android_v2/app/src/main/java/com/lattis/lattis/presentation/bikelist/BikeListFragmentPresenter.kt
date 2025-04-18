package com.lattis.lattis.presentation.bikelist

import android.text.TextUtils
import android.util.Log
import com.lattis.domain.usecase.bike.ReserveBikeUseCase
import com.lattis.domain.usecase.bike.SearchBikeUseCase
import com.lattis.domain.usecase.card.GetCardUseCase
import com.lattis.domain.usecase.user.GetLocalUserUseCase
import com.lattis.domain.usecase.user.GetUserUseCase
import com.lattis.domain.usecase.user.SendCodeToPhoneNumberUseCase
import com.lattis.domain.usecase.user.ValidateCodeForChangePhoneNumberUseCase
import com.lattis.domain.models.*
import com.lattis.domain.usecase.v2.BookingsUseCase
import com.lattis.lattis.presentation.base.fragment.location.BaseLocationFragmentPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.FirebaseMessagingHelper
import com.lattis.lattis.presentation.utils.FirebaseUtil
import com.lattis.lattis.presentation.utils.MapboxUtil
import com.lattis.lattis.presentation.utils.MapboxUtil.MARKER_ID
import com.lattis.lattis.uimodel.mapper.BikePortMapper
import com.lattis.lattis.uimodel.model.RentalFareSelected
import com.lattis.domain.utils.Constants
import com.lattis.lattis.uimodel.mapper.BikeHubMapper
import com.lattis.lattis.utils.ResourceHelper.getBikeResource
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import io.reactivex.rxjava3.disposables.Disposable
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.ArrayList

class BikeListFragmentPresenter @Inject constructor(
    private val searchBikeUseCase: SearchBikeUseCase,
    private val getLocalUserUseCase: GetLocalUserUseCase,
    private val getCardUseCase: GetCardUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val reserveBikeUseCase: ReserveBikeUseCase,
    @param:Named("ISDCode") val countryCode: Int,
    @param:Named("ISO31662Code") val countryCodeString: String,
    val sendCodeToPhoneNumberUseCase: SendCodeToPhoneNumberUseCase,
    val validateCodeForChangePhoneNumberUseCase: ValidateCodeForChangePhoneNumberUseCase,
    val firebaseMessagingHelper: FirebaseMessagingHelper,
    val bikePortMapper: BikePortMapper,
    val bookingsUseCase: BookingsUseCase,
    val bikeHubMapper: BikeHubMapper
) : BaseLocationFragmentPresenter<BikeListFragmentView>() {


    lateinit var featureCollection: FeatureCollection
    private var searchBikeDisposable: Disposable? = null
    private var bikes:List<Bike>?=null
    var dockHubs:ArrayList<DockHub>?=null
    var parkingHubs:ArrayList<DockHub>?=null
    var cards: List<Card>?=null
    var previouslySelectedBike:Bike?=null
    var searchedBikeId:Int?=null
    var previouslySelectedDockHub:DockHub?=null
    var previouslySelectedParkingHub:DockHub?=null
    var markerCoordinates: ArrayList<Feature> = ArrayList()
    private var isPhoneNumberOK = false
    private var getUserSubscription: Disposable? = null
    private var user:User?=null
    var phoneNumber:String?=null
    var code:String?=null
    var dockHubBikes:ArrayList<Bike> = ArrayList<Bike>()
    var rentalFareSelected = RentalFareSelected()

    //  This is for showing the corner data and center point: start
//    lateinit var ne: LatLng
//    lateinit var sw:LatLng
//    lateinit var center:LatLng
    //  This is for showing the corner data and center point: end

    companion object {
        val BIKE_SELECTED = "selected"
    }

    fun searchBike(northEast: Location, southWest:Location){

        searchBikeDisposable?.dispose()

        searchBikeDisposable=searchBikeUseCase.withNorthEast(northEast)
            .withSouthWest(southWest)
            .execute(object : RxObserver<Rentals>(view, false) {
                override fun onNext(rentals: Rentals) {
                    super.onNext(rentals)
                    markerCoordinates = ArrayList()
                    var bikesWithoutHub: ArrayList<Bike> = ArrayList<Bike>()
                    bikes=null
                    if(rentals.bikeList!=null) {
                        bikes = rentals.bikeList
                        bikesWithoutHub.addAll(bikes!!)
                    }
                    dockHubs=null
                    parkingHubs=null
                    if(rentals.hubList!=null) {
                        for(dockHub in rentals.hubList!!){
                            if(dockHub.type.equals(Constants.DOCKING_STATION)){
                                if(dockHubs==null)dockHubs=ArrayList()
                                dockHubs?.add(dockHub)
                                if(dockHub.bikes!=null && dockHub.bikes!!.size>0) {
                                    for (bikeInHub in dockHub.bikes!!){
                                        for(bikeWithoutHub in bikesWithoutHub){
                                            if(bikeWithoutHub.bike_id == bikeInHub.bike_id){
                                                bikesWithoutHub.remove(bikeWithoutHub)
                                                break
                                            }
                                        }
                                    }
                                }
                            }else if(dockHub.type.equals(Constants.PARKING_STATION)){
                                if(parkingHubs==null)parkingHubs = ArrayList()
                                parkingHubs?.add(dockHub)
                            }
                        }
                    }
                    setMarkerData(bikesWithoutHub)
                    if(dockHubs!=null)view?.handleDockHubs()
                    if(parkingHubs!=null)view?.handleParkingHubs()
                    createBikeFeatureCollection()
                    view?.handleBikesAndDockHubs()

                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            })

        subscriptions.add(searchBikeDisposable!!)

    }

    fun setMarkerData(bikeList: List<Bike>){
        var isPreviouslySelectedBikePresentInNewList:Boolean = false
        for(bike in bikeList){
            val feature =
                Feature.fromGeometry(
                    Point.fromLngLat(
                        bike.longitude,
                        bike.latitude
                    )
                )

            feature.addStringProperty("poi",getBikeResource(bike.type,bike.bike_battery_level))
            feature.addNumberProperty("bike_id",bike.bike_id)
            if((previouslySelectedBike!=null && previouslySelectedBike?.bike_id!!.equals(bike.bike_id)) ||
                (searchedBikeId!=null && searchedBikeId?.equals(bike.bike_id)!!)){
                feature.addBooleanProperty(BIKE_SELECTED,true) // may be it can true but it will never show find bike in this case
                isPreviouslySelectedBikePresentInNewList=true
                previouslySelectedBike=bike
                searchedBikeId=null
            }else{
                feature.addBooleanProperty(BIKE_SELECTED,false)
            }
            markerCoordinates.add(feature)
        }

        if(!isPreviouslySelectedBikePresentInNewList)
        {
            previouslySelectedBike=null
        }

//  This is for showing the corner data and center point: start
//        val featureNE =
//            Feature.fromGeometry(
//                Point.fromLngLat(
//                    ne.longitude,
//                    ne.latitude
//                )
//            )
//
//        featureNE.addStringProperty("poi",nesw)
//        featureNE.addNumberProperty("bike_id",1)
//        featureNE.addBooleanProperty(BIKE_SELECTED,false)
//
//
//        val featureSW =
//            Feature.fromGeometry(
//                Point.fromLngLat(
//                    sw.longitude,
//                    sw.latitude
//                )
//            )
//
//        featureSW.addStringProperty("poi",nesw)
//        featureSW.addNumberProperty("bike_id",2)
//        featureSW.addBooleanProperty(BIKE_SELECTED,false)

//        val featureCenter =
//            Feature.fromGeometry(
//                Point.fromLngLat(
//                    center.longitude,
//                    center.latitude
//                )
//            )
//
//        featureCenter.addStringProperty("poi",nesw)
//        featureCenter.addNumberProperty("bike_id",2)
//        featureCenter.addBooleanProperty(BIKE_SELECTED,false)
//
//        markerCoordinates.add(featureNE)
//        markerCoordinates.add(featureSW)
//        markerCoordinates.add(featureCenter)
//  This is for showing the corner data and center point: end


//        featureCollection = FeatureCollection.fromFeatures(markerCoordinates);

    }

    fun setHubDockMarkerData(){
        for(hubDock in dockHubs!!){
            if(hubDock.bikes!=null && hubDock.bikes?.size!!>0) {
                val feature =
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            hubDock.longitude!!,
                            hubDock.latitude!!
                        )
                    )

                feature.addStringProperty(
                    "poi",
                    MapboxUtil.hub_dock_bike + "_" + hubDock.bikes?.size!!
                )
                feature.addNumberProperty(MapboxUtil.MARKER_ID, hubDock.hub_id)
                feature.addStringProperty(MapboxUtil.MARKER_TYPE, MapboxUtil.hub_dock_bike)
                feature.addBooleanProperty(MapboxUtil.MARKER_SELECTED, false)
                feature.addBooleanProperty(BIKE_SELECTED, false)
                markerCoordinates.add(feature)
            }
        }
    }

    fun setHubParkingMarkerData(){
        for(hubDock in parkingHubs!!){
            val feature =
                Feature.fromGeometry(
                    Point.fromLngLat(
                        hubDock.longitude!!,
                        hubDock.latitude!!
                    )
                )

            feature.addStringProperty("poi", MapboxUtil.hub_parking_bike +"_"+hubDock.ports?.size!!)
            feature.addNumberProperty(MapboxUtil.MARKER_ID,hubDock.hub_id)
            feature.addStringProperty(MapboxUtil.MARKER_TYPE, MapboxUtil.hub_parking_bike)
            feature.addBooleanProperty(MapboxUtil.MARKER_SELECTED,false)
            feature.addBooleanProperty(BIKE_SELECTED,false)
            markerCoordinates.add(feature)
        }
    }

    fun createBikeFeatureCollection(){
        featureCollection = FeatureCollection.fromFeatures(markerCoordinates);
    }

    fun getSelectedBike():Bike?{
        return previouslySelectedBike
    }

    fun getSelectedDockHub():DockHub?{
        return previouslySelectedDockHub
    }

    fun getSelectedParkingHub():DockHub?{
        return previouslySelectedParkingHub
    }

    fun setSelectedBike(feature: Feature?){
        previouslySelectedDockHub=null
        previouslySelectedParkingHub=null
        if(bikes!=null) {
            for (bike in bikes!!) {
                if(feature?.getNumberProperty("bike_id")!!.toInt().equals(bike.bike_id)){
                    previouslySelectedBike = bike
                    break
                }
            }
        }
    }

    fun setSelectedDockHub(feature: Feature?){
        previouslySelectedBike=null
        previouslySelectedParkingHub=null
        if(dockHubs!=null) {
            for (dockHub in dockHubs!!) {
                if(feature?.getNumberProperty(MARKER_ID)!!.toInt().equals(dockHub.hub_id)){
                    previouslySelectedDockHub = dockHub
                    break
                }
            }
        }
    }

    fun setSelectedParkingHub(feature: Feature?){
        previouslySelectedBike=null
        previouslySelectedDockHub=null
        if(parkingHubs!=null) {
            for (parkingHub in parkingHubs!!) {
                if(feature?.getNumberProperty(MARKER_ID)!!.toInt().equals(parkingHub.hub_id)){
                    previouslySelectedParkingHub = parkingHub
                    break
                }
            }
        }
    }

    fun setSelectedBikeFromHubCardBikeModel(){
        previouslySelectedBike = getHubCardBikeModel()
    }

    private fun getHubCardBikeModel():Bike?{
        return if(previouslySelectedParkingHub!=null)
                    bikeHubMapper.mapOut(previouslySelectedParkingHub)
                else null
    }

    fun getDockHubBikes(dockHub: DockHub):ArrayList<Bike>{
        dockHubBikes.clear()
        if(dockHub.bikes!=null && bikes!=null){
            for(bikeInDockHub in dockHub.bikes!!){
                for (bikeInMainList in bikes!!){
                    if(bikeInMainList.bike_id == bikeInDockHub.bike_id){
                        dockHubBikes.add(bikeInMainList)
                    }
                }
            }
        }
        return dockHubBikes
    }

    fun takeActionAfterBikeSelectedInDockHub(position:Int){
        if(previouslySelectedDockHub!=null && dockHubBikes.size>position){
            previouslySelectedBike = dockHubBikes.get(position)
        }else if(previouslySelectedParkingHub!=null && previouslySelectedParkingHub?.ports?.size!!>position){
            previouslySelectedBike = bikePortMapper.mapOut(previouslySelectedParkingHub,position)
        }
    }

    fun resetPreviouslySelectedDockHub(){
        previouslySelectedDockHub=null
    }

    fun resetPreviouslySelectedParkingHub(){
        previouslySelectedParkingHub=null
    }

    fun resetPreviouslySelectedBike(){
        previouslySelectedBike = null
    }


    fun getSelectedFeature(): Feature? {
        Log.e("BikeListFragment","getSelectedFeature")
        if (featureCollection != null) {
            for (feature in featureCollection.features()!!) {
                if (feature.getBooleanProperty(BIKE_SELECTED)) {
                    return feature
                }
            }
        }
        return null
    }



    //// Get Greeting message: start ////
    fun getGreetingMessage():String{
        val c = Calendar.getInstance()
        val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

        return when (timeOfDay) {
            in 0..11 -> "label_good_morning"
            in 12..15 -> "label_good_afternoon"
            else -> {
                "label_good_evening"
            }
        }
    }
    //// Get Greeting message: start ////

    ////// Get user: start //////////////
    val getUser: Unit
        get() {
            isPhoneNumberOK = false
            cancelGetUserSubscription()
            getUserSubscription = getLocalUserUseCase.execute(object : RxObserver<User>(view, false) {
                override fun onNext(currUser: User) {

                    if (currUser != null) {
                        user = currUser
                        isPhoneNumberOK = if (TextUtils.isEmpty(currUser.phoneNumber)) false  else true
                        view?.handleUser(user)
                    }

                }
                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            })
        }

    fun phoneNumberCheckPassed(): Boolean {
        return if (previouslySelectedBike?.require_phone_number!!) isPhoneNumberOK else true
    }

    fun cancelGetUserSubscription() {
        if (getUserSubscription != null) {
            getUserSubscription!!.dispose()
            getUserSubscription = null
        }
    }

    ////// Get user: end //////////////

    //// Get card: start ////
    fun fetchCardList(){
        subscriptions.add(getCardUseCase
            .execute(object : RxObserver<List<Card>>() {
                override fun onNext(newCards: List<Card>) {
                    cards = newCards
                    if(cards==null || cards?.size==0){
                        view?.handleNoCard()
                        return
                    }

                    for(card in cards!!){
                        if(card.is_primary){
                            view?.handleCard(card)
                            return
                        }
                    }
                    view?.handleNoCard()

                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.handleNoCard()
                }
            }))
    }


    fun getPrimaryCard():Card?{
        if(cards!=null && cards?.size!!>0){
            for(card in cards!!){
                if(card.is_primary){
                    return card
                }
            }
        }

        return null
    }
    //// Get card: end ////


    //// Reserve bike :start

    fun reserveBike() {
        if(previouslySelectedBike==null){
            view?.OnReserveBikeFail()
            return
        }

        if(pricingOptionSelectionRemaining()){
            view?.onBikeReserveFailureDuePricingOptionSelectionRemaining()
            return
        }


        subscriptions.add(
            bookingsUseCase
                .withBike(previouslySelectedBike!!)
                .withScanStatus(false)
                .withLatitude(currentUserLocation?.latitude)
                .withLongitude(currentUserLocation?.longitude)
                .withDeviceToken(firebaseMessagingHelper.getFirebaseToken())
                .withPricingOptionId(if(isPricingOption())rentalFareSelected.pricingOptionSelected?.pricing_option_id else null)
                .execute(object : RxObserver<Ride>(view) {
                    override fun onNext(ride: Ride) {
                        super.onNext(ride)
                        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.RESERVE, FirebaseUtil.NORMAL_RESERVE)
                        view.OnReserveBikeSuccess(
                            ride.bike_booked_on,
                            ride.bike_expires_in
                        )
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        if (e is HttpException) {
                            if (e.code() == 404) {
                                view.OnReserveBikeNotFound()
                            }else if(e.code() == 409 && previouslySelectedBike!=null &&
                                !TextUtils.isEmpty(previouslySelectedBike?.enable_preauth) &&
                                (previouslySelectedBike?.enable_preauth.equals("1") || previouslySelectedBike?.enable_preauth.equals("true",true))){
                                view?.onPreAuthFailure()
                            } else if(e.code() == 409){
                                view?.onMissingUserCard()
                            } else if(e.code()==401){
                                view?.onBikeAlreadyRented()
                            } else {
                                view.OnReserveBikeFail()
                            }
                        } else {
                            view.OnReserveBikeFail()
                        }
                    }
                })
        )
    }

    //// Reserve bike :end


    ///// Phone number :start
    fun sendCodeToUpdatePhoneNumber() {
        subscriptions.add(
            sendCodeToPhoneNumberUseCase
                .withPhoneNumber(phoneNumber)
                .withCountryCode(countryCodeString)
                .execute(object : RxObserver<Boolean>(view, false) {
                    override fun onNext(status: Boolean) {
                        view?.onCodeSentSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onCodeSentFailure()
                    }
                })
        )
    }

    fun validateCodeForUpdatePhoneNumber() {
        subscriptions.add(
            validateCodeForChangePhoneNumberUseCase
                .withCode(code)
                .withPhoneNumber(phoneNumber)
                .execute(object : RxObserver<Boolean>(view, false) {
                    override fun onNext(status: Boolean) {
                        getUserProfile()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onCodeValidateFailure()
                    }
                })
        )
    }


    fun getUserProfile() {
        subscriptions.add(getUserUseCase.execute(object : RxObserver<User>(view, false) {
            override fun onNext(currUser: User) {
                user = currUser
                isPhoneNumberOK = if (TextUtils.isEmpty(currUser.phoneNumber)) false  else true
                view?.onUserProfileSuccess()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                view?.onCodeValidateFailure()
            }
        }))
    }
    //// Phone number :end


    ////rentalfare :start
    fun resetRentalFare(){
        rentalFareSelected = RentalFareSelected()
    }

    fun onPayPerUseClicked(){
        rentalFareSelected.newPayPerUseSelected = true
        rentalFareSelected.newRentalFareSelectedIndex = -1
    }

    fun onRentalFareClicked(position:Int){
        rentalFareSelected.newPayPerUseSelected = false
        rentalFareSelected.newRentalFareSelectedIndex = position
    }

    fun onRentalFareConfirmationClicked(){
        if(rentalFareSelected.newPayPerUseSelected){    //pay per use is selected
            rentalFareSelected.finalPayPerUseSelected =true
            rentalFareSelected.newPayPerUseSelected = false
            rentalFareSelected.rentalFareSelected = false
            rentalFareSelected.newRentalFareSelectedIndex =-1
            rentalFareSelected.finalRentalFareSelectedIndex=-1
            rentalFareSelected.pricingOptionSelected = null
            view?.showPayPerUse()
        }else if(rentalFareSelected.newRentalFareSelectedIndex!=-1 && getSelectedBike()?.pricing_options?.size!!>rentalFareSelected.newRentalFareSelectedIndex){
            rentalFareSelected.finalPayPerUseSelected =false
            rentalFareSelected.newPayPerUseSelected = false
            rentalFareSelected.rentalFareSelected = true
            rentalFareSelected.finalRentalFareSelectedIndex=rentalFareSelected.newRentalFareSelectedIndex
            rentalFareSelected.newRentalFareSelectedIndex =-1
            rentalFareSelected.pricingOptionSelected = getSelectedBike()?.pricing_options?.get(rentalFareSelected.finalRentalFareSelectedIndex)
            view?.showRentalFare()
        }
    }

    fun getSelectedRentalFare():Bike.Pricing_options?{
        return rentalFareSelected.pricingOptionSelected
    }

    fun onRentalFareSelectionCancelled(){
        rentalFareSelected.newPayPerUseSelected = false
        rentalFareSelected.newRentalFareSelectedIndex = -1
    }


    fun isPayPerUse():Boolean{
        return rentalFareSelected.finalPayPerUseSelected
    }

    fun isPricingOption():Boolean{
        return rentalFareSelected.rentalFareSelected && rentalFareSelected.finalRentalFareSelectedIndex!=-1 &&
                rentalFareSelected.pricingOptionSelected!=null
    }

    fun pricingOptionSelectionRemaining():Boolean{
        if( bikeHasPricingOptionSelection() ){
            if(!isPayPerUse() && !isPricingOption()){
                return true
            }
        }
        return false
    }

    fun bikeHasPricingOptionSelection():Boolean{
        return getSelectedBike()!=null &&
                getSelectedBike()?.pricing_options!=null &&
                getSelectedBike()?.pricing_options?.size!!>0
    }
    ////rentalfare :end


    ////
    fun convertBikePricingOptionsFormatAsPerReservation(){
        var fleet = Bike.Fleet()
        fleet.pricing_options = previouslySelectedBike?.pricing_options
        previouslySelectedBike?.fleet = fleet
    }

}