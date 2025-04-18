package com.lattis.lattis.presentation.reservation

import android.os.Bundle
import com.google.gson.Gson
import com.lattis.domain.models.CostEstimate
import com.lattis.domain.models.Reserve
import com.lattis.domain.usecase.bike.SearchBikeUseCase
import com.lattis.domain.usecase.card.GetCardUseCase
import com.lattis.domain.usecase.reservation.GetAvailableVehiclesUseCase
import com.lattis.domain.usecase.reservation.GetCostEstimateUseCase
import com.lattis.domain.usecase.reservation.ReserveUseCase
import com.lattis.domain.usecase.user.GetLocalUserUseCase
import com.lattis.domain.usecase.user.GetUserUseCase
import com.lattis.domain.usecase.user.SendCodeToPhoneNumberUseCase
import com.lattis.domain.usecase.user.ValidateCodeForChangePhoneNumberUseCase
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Card
import com.lattis.domain.models.Location
import com.lattis.domain.models.User
import com.lattis.lattis.presentation.base.activity.location.BaseLocationActivityPresenter
import com.lattis.lattis.presentation.reservation.ReservationActivity.Companion.REFERENCED_BIKE
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.uimodel.model.RentalFareSelected
import com.lattis.lattis.utils.UtilsHelper.dateToUTC
import io.reactivex.rxjava3.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class ReservationActivityPresenter @Inject constructor(
    private val getAvailableVehiclesUseCase: GetAvailableVehiclesUseCase,
    private val getCostEstimateUseCase: GetCostEstimateUseCase,
    private val getCardUseCase: GetCardUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getLocalUserUseCase: GetLocalUserUseCase,
    @param:Named("ISDCode") val countryCode: Int,
    @param:Named("ISO31662Code") val countryCodeString: String,
    val sendCodeToPhoneNumberUseCase: SendCodeToPhoneNumberUseCase,
    val validateCodeForChangePhoneNumberUseCase: ValidateCodeForChangePhoneNumberUseCase,
    val reserveUseCase: ReserveUseCase
) : BaseLocationActivityPresenter<ReservationActivityView>(){



    private var pickUpDate:Date?=null
    private var returnDate:Date?=null
    var selectedVehicle : Bike?=null
    var isReturnClickEnabled = false
    var isVehicleClickEnabled = false
    var isPricingOptionsClickEnabled = false
    private var getAvailableVehiclesDisposable: Disposable? = null
    private var searchBikeDisposable: Disposable? = null
    private var costEstimateDisposable: Disposable? = null
    private var reserveDisposable: Disposable? = null
    var availableVehicleList:List<Bike>?=null
    var cards: List<Card>?=null
    var user:User?=null
    var referencedBike:Bike?=null
    var costEstimate:CostEstimate?=null
    var bikeInfoViewFromConfirm:Boolean=false
    private var isPhoneNumberOK = false
    private var getUserSubscription: Disposable? = null
    var phoneNumber:String?=null
    var code:String?=null
    var reserve:Reserve?=null
    var rentalFareSelected = RentalFareSelected()

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(REFERENCED_BIKE)) {
            val referencedBikeString = arguments.getString(REFERENCED_BIKE)
            referencedBike = Gson().fromJson(referencedBikeString,Bike::class.java)
        }

        view?.setPricingOptionsUI(bikeHasPricingOptionSelection())
    }

    fun setPickUpDate(date:Date){
        if(pickUpDate==null || pickUpDate!=date){
            view?.resetReturnState(true)
            view?.resetPricingOptionsState(false)
            view?.resetVehicleState(false)
            isReturnClickEnabled=true
            isVehicleClickEnabled=false
            isPricingOptionsClickEnabled=false
            returnDate=null;
            selectedVehicle=null
        }
        pickUpDate = date
    }

    fun getPickUpDate():Date?{
        return pickUpDate
    }

    fun setReturnDate(date:Date){
        if(returnDate==null || returnDate!=date){
            if(bikeHasPricingOptionSelection()){
                view?.resetPricingOptionsState(true)
                isPricingOptionsClickEnabled=true
                view?.resetVehicleState(false)
                isVehicleClickEnabled=false
                selectedVehicle=null
            }else{
                view?.resetVehicleState(true)
                isVehicleClickEnabled=true
                selectedVehicle=null
            }
        }
        returnDate = date
    }

    fun getReturnDate():Date?{
        return returnDate
    }


    fun startFetchAvailableVehicles(){
        getAvailableVehiclesDisposable?.dispose()


        getAvailableVehiclesDisposable=getAvailableVehiclesUseCase
            .withFleetId(referencedBike?.reservation_settings?.fleet_id!!)
            .withPickUpDate(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dateToUTC(pickUpDate!!)))
            .withReturnDate(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dateToUTC(returnDate!!)))
            .execute(object : RxObserver<List<Bike>>(view, false) {
                override fun onNext(bikeList: List<Bike>) {
                    super.onNext(bikeList)
                    availableVehicleList = bikeList
                    view?.onAvailableVehiclesSuccess()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onAvailableVehiclesFailure()
                }
            })

        subscriptions.add(getAvailableVehiclesDisposable!!)
    }

    fun calculateCostEstimate(){

        costEstimateDisposable?.dispose()

        subscriptions.add(getCostEstimateUseCase
            .withBikeId(selectedVehicle?.bike_id!!)
            .withPickUpDate(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dateToUTC(pickUpDate!!)))
            .withReturnDate(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dateToUTC(returnDate!!)))
            .withPricingOptionId(if(isPricingOption())rentalFareSelected.pricingOptionSelected?.pricing_option_id else null)
            .execute(object : RxObserver<CostEstimate>(view, false) {
                override fun onNext(newCostEstimate: CostEstimate) {
                    super.onNext(newCostEstimate)
                    costEstimate = newCostEstimate
                    view?.onCostEstimationSuccess()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onCostEstimationFailure()
                }
            }).also { costEstimateDisposable = it }
        )

    }

    fun reserve(){

        reserveDisposable?.dispose()

        subscriptions.add(reserveUseCase
            .withBikeId(selectedVehicle?.bike_id!!)
            .withPickUpDate(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dateToUTC(pickUpDate!!)))
            .withReturnDate(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dateToUTC(returnDate!!)))
            .withPricingOptionId(if(isPricingOption())rentalFareSelected.pricingOptionSelected?.pricing_option_id else null)
            .execute(object : RxObserver<Reserve>(view, false) {
                override fun onNext(newReserve: Reserve) {
                    super.onNext(newReserve)
                    reserve = newReserve
                    view?.onReservenSuccess()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onReserveFailure()
                }
            }).also { reserveDisposable = it }
        )

    }

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
                    cards=null
                    view?.handleNoCard()
                }
            }))
    }

    val getUser: Unit
        get() {
            isPhoneNumberOK = false
            getUserSubscription = getLocalUserUseCase.execute(object : RxObserver<User>(view, false) {
                override fun onNext(currUser: User) {

                    if (currUser != null) {
                        user = currUser
                        isPhoneNumberOK = if (currUser.phoneNumber != null && currUser.phoneNumber != "") true else false
                    }

                }
                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            })
        }

    fun phoneNumberCheckPassed(): Boolean {
        return if (selectedVehicle?.require_phone_number!!) isPhoneNumberOK else true
    }


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

    fun onRentalFareConfirmationClicked(pricingOptionsSelectionReason: PricingOptionsSelectionReason){
        if(rentalFareSelected.newPayPerUseSelected){    //pay per use is selected
            rentalFareSelected.finalPayPerUseSelected =true
            rentalFareSelected.newPayPerUseSelected = false
            rentalFareSelected.rentalFareSelected = false
            rentalFareSelected.newRentalFareSelectedIndex =-1
            rentalFareSelected.finalRentalFareSelectedIndex=-1
            rentalFareSelected.pricingOptionSelected = null
            view?.showPayPerUse()
            when(pricingOptionsSelectionReason){
                PricingOptionsSelectionReason.MAIN -> enableVehicleList()
                PricingOptionsSelectionReason.COST -> enableCalculateCostEstimate()
            }
        }else if(rentalFareSelected.newRentalFareSelectedIndex!=-1 && referencedBike?.fleet?.pricing_options?.size!!>rentalFareSelected.newRentalFareSelectedIndex){
            rentalFareSelected.finalPayPerUseSelected =false
            rentalFareSelected.newPayPerUseSelected = false
            rentalFareSelected.rentalFareSelected = true
            rentalFareSelected.finalRentalFareSelectedIndex=rentalFareSelected.newRentalFareSelectedIndex
            rentalFareSelected.newRentalFareSelectedIndex =-1
            rentalFareSelected.pricingOptionSelected = referencedBike?.fleet?.pricing_options?.get(rentalFareSelected.finalRentalFareSelectedIndex)
            view?.showRentalFare()
            when(pricingOptionsSelectionReason){
                PricingOptionsSelectionReason.MAIN -> enableVehicleList()
                PricingOptionsSelectionReason.COST -> enableCalculateCostEstimate()
            }
        }else{
            disableVehicleList()
        }
    }

    fun enableCalculateCostEstimate(){
        view?.enableCalculateCostEstimate()
    }

    fun disableVehicleList(){
        view?.resetVehicleState(false)
        isVehicleClickEnabled=false
        selectedVehicle=null
    }

    fun enableVehicleList(){
        view?.resetVehicleState(true)
        isVehicleClickEnabled=true
        selectedVehicle=null
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
        return referencedBike!=null &&
                referencedBike?.fleet?.pricing_options!=null &&
                referencedBike?.fleet?.pricing_options?.size!!>0
    }

    enum class PricingOptionsSelectionReason{
        MAIN,
        COST
    }
    ////rentalfare :end

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

}