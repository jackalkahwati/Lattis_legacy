package com.lattis.lattis.presentation.qrscan

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.lattis.domain.models.SignedMessageAndPublicKey
import com.lattis.domain.usecase.card.GetCardUseCase
import com.lattis.domain.usecase.location.GetLocationUpdatesUseCase
import com.lattis.domain.usecase.lock.SignedMessagePublicKey.SignedMessagePublicKeyUseCase
import com.lattis.domain.usecase.lock.connect.ConnectToLockUseCase
import com.lattis.domain.usecase.lock.disconnect.DisconnectAllLockUseCase
import com.lattis.domain.usecase.lock.realm.DeleteLockUseCase
import com.lattis.domain.usecase.lock.realm.SaveLockUseCase
import com.lattis.domain.usecase.lock.scanner.ScanForLockUseCase
import com.lattis.domain.usecase.lock.setter.BlinkLedUseCase
import com.lattis.domain.usecase.lock.setter.SetLockPositionUseCase
import com.lattis.domain.usecase.ride.StartRideUseCase
import com.lattis.domain.usecase.user.GetUserUseCase
import com.lattis.domain.usecase.user.SendCodeToPhoneNumberUseCase
import com.lattis.domain.usecase.user.ValidateCodeForChangePhoneNumberUseCase
import com.lattis.domain.models.*
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.usecase.bike.*
import com.lattis.domain.usecase.membership.GetLocalSubscriptionsUseCase
import com.lattis.domain.usecase.v2.*
import com.lattis.domain.utils.Constants
import com.lattis.lattis.presentation.base.activity.bluetooth.BaseBluetoothActivityPresenter
import com.lattis.lattis.presentation.base.fragment.bluetooth.BaseBluetoothFragmentPresenter
import com.lattis.lattis.presentation.qrscan.ScanBikeQRCodeActivity.Companion.IS_SCAN_IOT_MODE
import com.lattis.lattis.presentation.qrscan.ScanBikeQRCodeActivity.Companion.SCAN_IOT_BIKE_ID
import com.lattis.lattis.presentation.qrscan.ScanBikeQRCodeActivity.Companion.SCAN_IOT_QR_REQUIRED_QR_CODE
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.FirebaseMessagingHelper
import com.lattis.lattis.presentation.utils.FirebaseUtil
import com.lattis.lattis.presentation.utils.RideUtil
import com.lattis.lattis.presentation.utils.RideUtil.provideControllerKeys
import com.lattis.lattis.uimodel.mapper.BikeHubMapper
import com.lattis.lattis.uimodel.mapper.BikePortMapper
import com.lattis.lattis.uimodel.mapper.LockModelMapper
import com.lattis.lattis.uimodel.mapper.ScannedLockModelMapper
import com.lattis.lattis.uimodel.model.LockModel
import com.lattis.lattis.uimodel.model.RentalFareSelected
import com.lattis.lattis.utils.SentinelHelper.isSentinel
import io.lattis.ellipse.sdk.exception.BluetoothException
import io.reactivex.rxjava3.disposables.Disposable
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named

class ScanBikeQRCodeActivityPresenter @Inject internal constructor(
    private val bikeDetailUseCase: BikeDetailUseCase,
    private val findByQRCodeUseCase: FindByQRCodeUseCase,
    private val scanForLockUseCase: ScanForLockUseCase,
    private val connectToLockUseCase: ConnectToLockUseCase,
    private val signedMessagePublicKeyUseCase: SignedMessagePublicKeyUseCase,
    private val saveLockUseCase: SaveLockUseCase,
    private val disconnectAllLockUseCase: DisconnectAllLockUseCase,
    private val blinkLedUseCase: BlinkLedUseCase,
    private val startRideUseCase: StartRideUseCase,
    private val startTripUseCase: StartTripUseCase,
    private val deleteLockUseCase: DeleteLockUseCase,
    private val getLocationUpdatesUseCase: GetLocationUpdatesUseCase,
    private val reserveBikeUseCase: ReserveBikeUseCase,
    private val bookingsUseCase: BookingsUseCase,
    private val setLockPositionUseCase: SetLockPositionUseCase,
    private val cancelReserveBikeUseCase: CancelBikeReservationUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val getCardUseCase: GetCardUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val scannedLockModelMapper: ScannedLockModelMapper,
    private val lockModelMapper: LockModelMapper,
    @param:Named("ISDCode") val countryCode: Int,
    @param:Named("ISO31662Code") val countryCodeString: String,
    val sendCodeToPhoneNumberUseCase: SendCodeToPhoneNumberUseCase,
    val validateCodeForChangePhoneNumberUseCase: ValidateCodeForChangePhoneNumberUseCase,
    val getLocalSubscriptionsUseCase: GetLocalSubscriptionsUseCase,
    val firebaseMessagingHelper: FirebaseMessagingHelper,
    private val lockUnlockV2IotBikeUseCase: LockUnlockV2IotBikeUseCase,
    private val getV2IoTBikeStatusUseCase: GetV2IoTBikeStatusUseCase,
    private val bikePortMapper: BikePortMapper,
    private val unlockSentinelBikeUseCase: UnlockSentinelBikeUseCase
) : BaseBluetoothActivityPresenter<ScanBikeQRCodeActivityView>() {



    var current_qr_code_id = -1
    var current_qr_code:String? = null
    var current_iot_qr_code:String? = null
    private val TAG = ScanBikeQRCodeActivityPresenter::class.java.name
    private var connectionSubscription: Disposable? = null
    private var startScanSubscription: Disposable? = null
    private var setPositionSubscription: Disposable? = null
    private var locationSubscription: Disposable? = null
    private var cancelReserveBikeSubscription: Disposable? = null
    private var isAccessDeniedForEllipse = false
    private val isStartRideInProgress = false
    private var currentUserLocation: Location? = null
    private var isDisconnectRequiredForApp = false
    private var isRideNeedsToBeStartedAfterLocation = false
    private var isCreateBookingNeedsToBeDoneAfterLocation = false
    var qrScanProgress = QRScanProgress.NOTHING
    var lockModel: LockModel? = null
    private var isReconnection = false
    private val isPhoneNumberCheckRequired = true
    private var isPhoneNumberOK = false
    private var getUserSubscription: Disposable? = null

    var cards: List<Card>? = null
    var bike: Bike? = null
    var hub:DockHub?=null
    var port:DockHub.Port?=null
    var ride:Ride?=null
    var phoneNumber:String?=null
    var code:String?=null
    var isIotScanMode =false
    var scanIotRequiredQRCode:String?=null
    var scanIotBikeId:Int?=null
    var scanIotRentalName:String?=null
    var userSubscriptions:List<Subscription>?=null
    var rentalFareSelected = RentalFareSelected()


    override fun setup(arguments: Bundle?) {
        super.setup(arguments)

        if (arguments != null && arguments.containsKey(IS_SCAN_IOT_MODE)) {
            this.isIotScanMode = arguments.getBoolean(IS_SCAN_IOT_MODE)
        }

        if (arguments != null && arguments.containsKey(SCAN_IOT_QR_REQUIRED_QR_CODE)) {
            this.scanIotRequiredQRCode = arguments.getString(SCAN_IOT_QR_REQUIRED_QR_CODE)
        }

        if (arguments != null && arguments.containsKey(SCAN_IOT_BIKE_ID)) {
            this.scanIotBikeId = arguments.getInt(SCAN_IOT_BIKE_ID)
        }

        if (arguments != null && arguments.containsKey(ScanBikeQRCodeActivity.SCAN_IOT_RENTAL_NAME)) {
            this.scanIotRentalName = arguments.getString(ScanBikeQRCodeActivity.SCAN_IOT_RENTAL_NAME)
            if(!TextUtils.isEmpty(scanIotRentalName)) view?.setIotScanSubTitle()
        }
    }


    fun startLockConnectionDependingUponMode(){
        if(isIotModule()==BaseBluetoothFragmentPresenter.HardwareType.IOT_ONLY){
//            unlockIoTBike(true)
            getIoTBikeStatus()
        }else if(isIotModule()==BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_ONLY){
            getSignedMessagePublicKey(bike!!)
        }else if(isIotModule()==BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_IOT_COMBINE){
            unlockIoTBike(false)
            getSignedMessagePublicKey(bike!!)
        }else if(isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.NONE){
            view?.startRide()
        }else if(isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.MANUAL_LOCK){
            view?.startRide()
        }
    }

    fun processQRCodeDependingUponMode(qrCodeDatatString: String){
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.QR_CODE_SCANNING,FirebaseUtil.QR_CODE_SCANNING)
        if(isIotScanMode && scanIotRequiredQRCode!=null){
            compareQRCodeForScanIot(qrCodeDatatString)
        }else{
            RequestToAddBikeFromQRCode(qrCodeDatatString)
        }
    }

    fun compareQRCodeForScanIot(qrCodeDatatString: String?){
        val qr_code_id = getScannedIoTQRCode(qrCodeDatatString)
        val qr_code_id_lattis = getScannedLattisQRCode(qrCodeDatatString)
        if (qr_code_id != null && scanIotRequiredQRCode.equals( qr_code_id,true)) {
            view?.onIotScanSuccess()
        }else if(qr_code_id_lattis!=null && scanIotRequiredQRCode.equals(""+qr_code_id_lattis,true)){
            view?.onIotScanSuccess()
        }else if(scanIotBikeId!=null && isIotScanMode){
            RequestToAddBikeFromQRCode(qrCodeDatatString)
        }else{
            view?.onIotScanFailure()
        }
    }

    fun RequestToAddBikeFromQRCode(qrCodeDatatString: String?) {
        val qr_code_id = getScannedLattisQRCode(qrCodeDatatString)

        if(qr_code_id!=null){
            findByQRCode(qr_code_id.toString())
            return
        }

        val iot_qr_code = getScannedIoTQRCode(qrCodeDatatString)
        if (iot_qr_code!=null) {
            findByQRCode(iot_qr_code)
            return
        }
        current_qr_code_id = -1
        current_iot_qr_code=null
        resetCurrentQRCode()
        view?.onInvalidQRCode()
    }

    fun getScannedLattisQRCode(qrCodeDatatString: String?):Int?{

        var scannedQRCode : Int?=null
        if(TextUtils.isEmpty(qrCodeDatatString)) return scannedQRCode
        try{
            val qrCodeJSONObject = JSONObject(qrCodeDatatString)
            if (qrCodeJSONObject != null) {
                scannedQRCode = qrCodeJSONObject.getInt("qr_id")
            }
        }catch (e:Exception){
        }
        return scannedQRCode
    }


    fun getScannedIoTQRCode(qrCodeDatatString: String?):String?{
        var scannedQRCode : String?=null
        try {
            scannedQRCode = qrCodeDatatString?.substringAfterLast("/")
        }catch(e:Exception){

        }
        return scannedQRCode
    }

    fun findByQRCode(qr_code:String) {

        if(current_qr_code!=null && current_qr_code.equals(qr_code)){
            return
        }

        view?.showProgressbar()

        subscriptions.add(findByQRCodeUseCase
            .withQRCode(qr_code)
            .execute(object : RxObserver<Rental>(view) {
                override fun onNext(rental: Rental) {
                    super.onNext(rental)
                    view?.hideProgressbar()
                    bike = rental.bike
                    hub = rental.hub
                    port = rental.port

                    current_qr_code = qr_code

                    if (bike != null) {
                        /// This is if Lattis QR code is used for IoT bike
                        if(scanIotBikeId!=null && isIotScanMode){
                            if(scanIotBikeId==bike?.bike_id) {
                                view?.onIotScanSuccess()
                            }else{
                                view?.onIotScanFailure()
                            }
                            return
                        }


                        if (bike?.status != null && bike?.current_status != null) {
                            if (bike?.status.equals(
                                    "active",
                                    ignoreCase = true
                                ) && bike?.current_status.equals("parked", ignoreCase = true)
                            ) {
                                view?.onBikeDetailsSuccess(bike!!)
                                return
                            }else  if (bike?.status.equals(
                                    "active",
                                    ignoreCase = true
                                ) && bike?.current_status.equals("on_trip", ignoreCase = true)
                            ) {
                                view?.onBikeAlreadyRented()
                                return
                            }
                        }
                        resetCurrentQRCode()
                        view?.onBikeNotAvailable()
                    } else if (port!=null &&
                        port?.hub!=null &&
                        port?.fleet!=null){
                        bike = bikePortMapper.mapOut(port?.hub,port,port?.fleet)
                        if(bike!=null){

                            if(scanIotBikeId!=null && isIotScanMode){
                                if(scanIotBikeId==bike?.bike_id) {
                                    view?.onIotScanSuccess()
                                }else{
                                    view?.onIotScanFailure()
                                }
                                return
                            }


                            view?.onBikeDetailsSuccess(bike!!)
                        }else{
                            resetCurrentQRCode()
                            view?.onBikeDetailsFailure()
                        }
                    }else if (hub!=null){

                        if(scanIotBikeId!=null && isIotScanMode){
                            view?.restartScanner()
                            return
                        }

                        view?.showClosedHubPorts()
                    }else{
                        resetCurrentQRCode()
                        view?.onBikeDetailsFailure()
                    }
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.hideProgressbar()
                    resetCurrentQRCode()
                    if(scanIotBikeId!=null && isIotScanMode){
                        view?.onIotScanFailure()
                        return
                    }
                    if (e is HttpException) {
                        val exception =
                            e
                        if (exception.code() == 401) {
                            view?.onBikeUnAuthorised()
                        } else if (exception.code() == 404) {
                            view?.onBikeNotFound()
                        } else if (exception.code() == 409) {
                            view?.onBikeAlreadyRented()
                        } else if (exception.code() == 422) {
                            view?.onBikeNotLive()
                        } else {
                            view?.onBikeDetailsFailure()
                        }
                    } else {
                        view?.onBikeDetailsFailure()
                    }
                }
            })
        )
    }

    fun resetCurrentQRCode(){
        current_qr_code=null
    }

    fun getCards(){
        subscriptions.add(
            getCardUseCase.execute(object : RxObserver<List<Card>>(view) {
                override fun onNext(newCards: List<Card>) {
                    cards = newCards
                    if(getPrimaryCard()!=null) view?.handleCard(getPrimaryCard()!!) else view?.handleNoCard()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.handleNoCard();
                }

            })
        )
    }

    fun getPrimaryCard():Card?{
        if(cards==null || cards?.size==0)
            return null

        for(card in cards!!){
            if(card.is_primary){
                return card
            }
        }

        return null

    }


    ////////////////////////////////// RESERVE BIKE CODE : START //////////////////////////////////////////////////
    fun reserveBike(bike: Bike?) {
        this.bike = bike
        if (currentUserLocation == null) {
            requestLocationUpdates()
            isCreateBookingNeedsToBeDoneAfterLocation = true
            return
        }

        if(pricingOptionSelectionRemaining()){
            view?.onBikeReserveFailureDuePricingOptionSelectionRemaining()
            return
        }


        subscriptions.add(
            bookingsUseCase
                .withBike(bike!!)
                .withScanStatus(true)
                .withLatitude(currentUserLocation!!.latitude)
                .withLongitude(currentUserLocation!!.longitude)
                .withDeviceToken(firebaseMessagingHelper.getFirebaseToken())
                .withPricingOptionId(if(isPricingOption())rentalFareSelected.pricingOptionSelected?.pricing_option_id else null)
                .execute(object : RxObserver<Ride>(view) {
                    override fun onNext(newRide: Ride) {
                        super.onNext(newRide)
                        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.RESERVE,FirebaseUtil.QR_SCAN_CODE_RESERVE)
                        ride = newRide
                        qrScanProgress = QRScanProgress.BIKE_RESERVE
                        view?.OnReserveBikeSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (e is HttpException) {
                            if (e.code() == 404) {
                                view?.OnReserveBikeNotFound()
                            }else if(e.code() == 409){
                                view?.onMissingUserCard()
                            } else if(e.code() == 401){
                                view?.onBikeAlreadyRented()
                            } else {
                                view?.OnReserveBikeFail()
                            }
                        } else {
                            view?.OnReserveBikeFail()
                        }
                    }
                })
        )
    }

    fun cancelBikeReservation() {

        if(ride==null || ride?.bike_booking_id==null){
            return
        }
        cancelCancelReserveBikeSubscription()
        qrScanProgress = QRScanProgress.NOTHING
        cancelReserveBikeSubscription = cancelBookingUseCase
            .withBookingId(ride?.bike_booking_id!!)
            .withDamage(false)
            .withLockIssue(false)
            .execute(object : RxObserver<Boolean>(view) {
                override fun onNext(status: Boolean) {
                    super.onNext(status)
                    if (view != null) view?.onCancelBikeSuccess()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    if (view != null) view?.onCancelBikeFail()
                }
            })
    }

    ////////////////////////////////// START RIDE CODE : START //////////////////////////////////////////////////
    fun startRide(isRideStarted: Boolean) {
        Log.e(TAG, "startRide::")
        if (currentUserLocation == null) {
            Log.e(TAG, "startRide::location is null")
            requestLocationUpdates()
            isRideNeedsToBeStartedAfterLocation = true
            return
        }
        Log.e(TAG, "startRide::location is NOT null")
        qrScanProgress = QRScanProgress.RIDE_STARTED
        subscriptions.add(
            startTripUseCase
                .withRide(ride)
                .withLocation(currentUserLocation)
                .withFirstLockConnect(!isRideStarted)
                .withDeviceToken(firebaseMessagingHelper.getFirebaseToken())
                .execute(object : RxObserver<Ride>(view) {
                    override fun onNext(ride: Ride) {
                        super.onNext(ride)
                        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.startRideEventName,String.format(FirebaseUtil.startRideEventMessage,ride.rideId))
                        if(isIotModule()==BaseBluetoothFragmentPresenter.HardwareType.IOT_ONLY){
                            unlockIoTBike(true)
                        }else{
                            view?.onStartRideSuccess()
                        }

                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onStartRideFail()
                    }
                })
        )
    }

    ////////////////////////////////// LOCK CONNECTION CODE : START //////////////////////////////////////////////////
    fun requestLocationUpdates() {
        requestStopLocationUpdates()
        subscriptions.add(getLocationUpdatesUseCase.execute(object :
            RxObserver<Location>() {
            override fun onNext(location: Location) {
                requestStopLocationUpdates()
                currentUserLocation = location
                if (isRideNeedsToBeStartedAfterLocation) {
                    isRideNeedsToBeStartedAfterLocation = false
                    view?.startRide()
                } else if (isCreateBookingNeedsToBeDoneAfterLocation && bike != null) {
                    isCreateBookingNeedsToBeDoneAfterLocation = false
                    reserveBike(bike)
                }
            }
        }).also { locationSubscription = it })
    }

    fun requestStopLocationUpdates() {
        if (locationSubscription != null) {
            locationSubscription!!.dispose()
        }
    }

    fun saveLock(lockModel: LockModel?) {
        subscriptions.add(
            saveLockUseCase
                .withLock(lockModelMapper!!.mapOut(lockModel))
                .execute(object : RxObserver<Lock>(view) {
                    override fun onNext(lock: Lock) {
                        super.onNext(lock)
                        view?.onSaveLockSuccess(lock)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onSaveLockFailure()
                    }
                })
        )
    }

    fun setPosition(position: Boolean?) {
        cancelSetPositionSubscription()
        subscriptions.add(
            setLockPositionUseCase
                .withLockVendor(getLockVendor())
                .withState(position!!)
                .forLock(lockModelMapper!!.mapOut(lockModel))
                .withFleetId(bike?.fleet_id)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        if(status && getLockVendor()==UseCase.LockVendor.AXA && lockModel!=null && lockModel?.publicKey!=null){
                            lockModel?.publicKey = lockModel?.publicKey!!.substringAfter("-")
                            saveLock(lockModel)
                        }
                        view?.OnSetPositionStatus(status)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.OnSetPositionStatus(false)
                    }
                }).also { setPositionSubscription = it }
        )
    }

    fun cancelSetPositionSubscription() {
        if (setPositionSubscription != null) {
            setPositionSubscription!!.dispose()
            setPositionSubscription = null
        }
    }

    fun cancelCancelReserveBikeSubscription() {
        if (cancelReserveBikeSubscription != null) {
            cancelReserveBikeSubscription!!.dispose()
            cancelReserveBikeSubscription = null
        }
    }

    fun deleteLock() {
        subscriptions.add(
            deleteLockUseCase
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                    }
                })
        )
    }

    fun getSignedMessagePublicKey(bike: Bike) {
        subscriptions.add(
            signedMessagePublicKeyUseCase
                .withLockVendor(getLockVendor())
                .withBikeId(bike.bike_id)
                .withMacId(bike.mac_id!!)
                .withFleetId(bike.fleet_id)
                .execute(object : RxObserver<SignedMessageAndPublicKey>(view) {
                    override fun onNext(signedMessageAndPublicKey: SignedMessageAndPublicKey) {
                        super.onNext(signedMessageAndPublicKey)
                        view?.OnSignedMessagePublicKeySuccess(
                            signedMessageAndPublicKey.signed_message!!,
                            signedMessageAndPublicKey.public_key!!
                        )
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.OnSignedMessagePublicKeyFailure()
                    }
                })
        )
    }

    fun startScan() {
        if (startScanSubscription != null) {
            startScanSubscription!!.dispose()
            startScanSubscription = null
        }
        subscriptions.add(scanForLockUseCase.execute(object : RxObserver<ScannedLock>() {
            override fun onStart() {
                super.onStart()
                view?.onScanStart()
            }

            override fun onComplete() {
                super.onComplete()
                view?.onScanStop()
            }

            override fun onError(throwable: Throwable) {
                super.onError(throwable)
                if (throwable is BluetoothException) {
                    val exception = throwable
                    if (exception != null) {
                        if (exception.status != null) {
                            if (exception.status == BluetoothException.Status.BLUETOOTH_DISABLED) {
                                view?.requestEnableBluetooth()
                            }
                        }
                    }
                } else {
                    view?.onScanStop()
                }
            }

            override fun onNext(lock: ScannedLock) {
                super.onNext(lock)
                view?.onLockScanned(scannedLockModelMapper!!.mapIn(lock))
            }
        }).also { startScanSubscription = it })
    }

    fun connectTo() {
        cancelConnectionSubscription()
        isAccessDeniedForEllipse = false
        subscriptions.add(
            connectToLockUseCase
                .withLockVendor(getLockVendor())
                .execute(
                lockModelMapper!!.mapOut(lockModel),
                object : RxObserver<Lock.Connection.Status>(
                    view,
                    false
                ) {
                    override fun onStart() {
                        super.onStart()
                        view?.showConnecting(!isReconnection)
                    }

                    override fun onComplete() {
                        super.onComplete()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (e is BluetoothException) {
                            if (e.status != null) {
                                if (e.status == BluetoothException.Status.DEVICE_NOT_FOUND) {
                                    isReconnection = true
                                    connectTo()
                                } else if (e.status == BluetoothException.Status.BLUETOOTH_DISABLED) {
                                    view?.requestEnableBluetooth()
                                }
                            }
                        }
                    }

                    override fun onNext(state: Lock.Connection.Status) {
                        if (state === Lock.Connection.Status.OWNER_VERIFIED || state === Lock.Connection.Status.GUEST_VERIFIED) {
                            super.onNext(state)
                            qrScanProgress = QRScanProgress.LOCK_CONNECTED
                            view?.onLockConnected(lockModelMapper!!.mapIn(state.lock))
                        } else if (state === Lock.Connection.Status.ACCESS_DENIED) {
                            super.onNext(state)
                            isAccessDeniedForEllipse = true
                            view?.onLockConnectionAccessDenied()
                        } else if (state === Lock.Connection.Status.DISCONNECTED) {
                            super.onNext(state)
                            if (!isAccessDeniedForEllipse) {
                                view?.onLockConnectionFailed()
                            }
                        }
                    }
                }).also { connectionSubscription = it }
        )
    }

    private fun cancelConnectionSubscription() {
        if (connectionSubscription != null) {
            connectionSubscription!!.dispose()
            connectionSubscription = null
        }
        if (startScanSubscription != null) {
            startScanSubscription!!.dispose()
            startScanSubscription = null
        }
        isRideNeedsToBeStartedAfterLocation = false
        isCreateBookingNeedsToBeDoneAfterLocation = false
    }

    fun cancelAllSubscription() {
        requestStopLocationUpdates()
        isRideNeedsToBeStartedAfterLocation = false
        isCreateBookingNeedsToBeDoneAfterLocation = false
        cancelConnectionSubscription()
        cancelSetPositionSubscription()
        cancelCancelReserveBikeSubscription()
        cancelGetUserSubscription()
    }


    override fun onBluetoothEnabled() {
        disconnectAllLocks();
    }


    fun disconnectAllLocks() {
        subscriptions.add(
            disconnectAllLockUseCase
                .withLockVendor(getLockVendor())
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        if (!isDisconnectRequiredForApp) {
                            isReconnection = false
                            connectTo()
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        super.onError(throwable)
                        if (throwable is BluetoothException) {
                            val exception = throwable
                            if (exception != null) {
                                if (exception.status != null) {
                                    if (exception.status == BluetoothException.Status.BLUETOOTH_DISABLED) {
                                        if (view != null) view?.requestEnableBluetooth()
                                    }
                                }
                            }
                        } else {
                            if (!isDisconnectRequiredForApp) {
                                isReconnection = false
                                connectTo()
                            }
                        }
                    }
                })
        )
    }

    fun setDisconnectRequiredForApp(disconnectRequiredForApp: Boolean) {
        isDisconnectRequiredForApp = disconnectRequiredForApp
    }

    fun blinkLed(lockModel: LockModel) {
        subscriptions.add(
            blinkLedUseCase.withMacAddress(lockModel.macAddress!!)
                .execute(RxObserver(view, false))
        )
    }

    enum class QRScanProgress {
        NOTHING, BIKE_RESERVE, LOCK_CONNECTED, RIDE_STARTED
    }

    val userProfile: Unit
        get() {
            isPhoneNumberOK = false
            cancelGetUserSubscription()
            getUserSubscription = getUserUseCase.execute(object :
                RxObserver<User>(view, false) {
                override fun onNext(currUser: User) {
                    if (currUser != null) {
                        isPhoneNumberOK = if (TextUtils.isEmpty(currUser.phoneNumber)) false  else true
                    }
                    view?.onGetUserProfile()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onGetUserProfile()
                }
            })
        }

    fun phoneNumberCheckPassed(bike: Bike): Boolean {
        return if (bike.require_phone_number) isPhoneNumberOK else true
    }

    fun cancelGetUserSubscription() {
        if (getUserSubscription != null) {
            getUserSubscription!!.dispose()
            getUserSubscription = null
        }
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
                        userProfile
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onCodeValidateFailure()
                    }
                })
        )
    }

    //// Phone number :end


    fun isIotModule(): BaseBluetoothFragmentPresenter.HardwareType {
        if(bike?.controllers!=null && bike?.controllers?.size!!>0){
            for(controller in bike?.controllers!!){
                if(!TextUtils.isEmpty(controller.device_type) && controller.device_type.equals("iot",true)){
                    return if(TextUtils.isEmpty(bike?.mac_id)) BaseBluetoothFragmentPresenter.HardwareType.IOT_ONLY else BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_IOT_COMBINE
                }
            }
        }
        return if(bike?.mac_id==null) BaseBluetoothFragmentPresenter.HardwareType.NONE else BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_ONLY
    }

    fun getLockVendor(): UseCase.LockVendor {
        if(bike?.controllers!=null && bike?.controllers?.size!!>0){
            for(controller in bike?.controllers!!){
                if(!TextUtils.isEmpty(controller.vendor) && controller.vendor.equals("AXA",true)){
                    return UseCase.LockVendor.AXA
                }else if(!TextUtils.isEmpty(controller.vendor) && (controller.vendor.equals("TAPKEY",true) || controller.vendor.equals("TAP KEY",true))){
                    return UseCase.LockVendor.TAPKEY
                }else if(!TextUtils.isEmpty(controller.vendor) && (controller.vendor.equals("sas",true))){
                    return UseCase.LockVendor.SAS
                }
            }
        }
        return UseCase.LockVendor.ELLIPSE
    }

    fun getUserSubscription(){
        subscriptions.add(
            getLocalSubscriptionsUseCase
                .execute(object : RxObserver<List<Subscription>>(view, false) {
                    override fun onNext(subscriptionList: List<Subscription>) {
                        userSubscriptions = subscriptionList
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        userSubscriptions=null
                    }
                })
        )
    }

    //// membership popup :start
    fun getMembershipDiscount(fleet_id:Int):String?{
        if(userSubscriptions!=null && !userSubscriptions?.isEmpty()!!){
            for(subscription in userSubscriptions!!){
                if(subscription.fleet_membership?.fleet_id == fleet_id){
                    return subscription?.fleet_membership?.membership_incentive
                }
            }
        }

        return null
    }
    //// membership popup :end


    //// IoT :start


    fun getIoTBikeStatus(){

        if(ride?.bike_originalTypeOfObject!=null &&
            (ride?.bike_originalTypeOfObject.equals(Constants.port,true) || ride?.bike_originalTypeOfObject.equals(Constants.hub,true))
        ){
            view?.startRide()
            return
        }

        subscriptions.add(
            getV2IoTBikeStatusUseCase
                .withRide(ride)
                .withControllerId(RideUtil.provideIoTControllerId(ride))
                .execute(object : RxObserver<IoTBikeStatus>(view) {
                    override fun onNext(ioTBikeStatus: IoTBikeStatus) {
                        super.onNext(ioTBikeStatus)
                        view?.startRide()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onStartRideFail()
                    }
                })
        )
    }


    fun unlockSentinelBike(rideAlreadyStarted:Boolean){
        subscriptions.add(
            unlockSentinelBikeUseCase
                .withBikeId(ride?.bikeId!!)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        if(rideAlreadyStarted)view?.onStartRideSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        if(rideAlreadyStarted)view?.onStartRideSuccess()
                    }
                })
        )
    }


    fun unlockIoTBike(rideAlreadyStarted:Boolean) {

        if(linkaLock()){
            if(rideAlreadyStarted)view?.onStartRideSuccess()
            return
        }

        if(isSentinel(ride)){
//            unlockSentinelBike(rideAlreadyStarted)
            if(rideAlreadyStarted)view?.onStartRideSuccess()
            return
        }

        subscriptions.add(
            lockUnlockV2IotBikeUseCase
                .withRide(ride)
                .withLock(false)
                .withControllerId(RideUtil.provideIoTControllerId(ride))
                .execute(object : RxObserver<IoTBikeLockUnlockCommandStatus>(view) {
                    override fun onNext(t: IoTBikeLockUnlockCommandStatus) {
                        super.onNext(t)
                        if(rideAlreadyStarted)view?.onStartRideSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        if(rideAlreadyStarted)view?.onStartRideSuccess()
                    }
                })
        )
    }

    fun linkaLock():Boolean{
        if(ride?.controllers!=null && ride?.controllers?.size!!>0) {
            for (controller in ride?.controllers!!) {
                if (!TextUtils.isEmpty(controller.device_type) && controller.device_type.equals(
                        "iot",
                        true
                    ) &&
                    !TextUtils.isEmpty(controller.vendor) && controller.vendor.equals(
                        "Linka IoT",
                        true
                    )
                ) {
                    return true
                }
            }
        }
        return false
    }

    //// IoT :end



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
        }else if(rentalFareSelected.newRentalFareSelectedIndex!=-1 && bike?.pricing_options?.size!!>rentalFareSelected.newRentalFareSelectedIndex){
            rentalFareSelected.finalPayPerUseSelected =false
            rentalFareSelected.newPayPerUseSelected = false
            rentalFareSelected.rentalFareSelected = true
            rentalFareSelected.finalRentalFareSelectedIndex=rentalFareSelected.newRentalFareSelectedIndex
            rentalFareSelected.newRentalFareSelectedIndex =-1
            rentalFareSelected.pricingOptionSelected = bike?.pricing_options?.get(rentalFareSelected.finalRentalFareSelectedIndex)
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
        return bike!=null &&
                bike?.pricing_options!=null &&
                bike?.pricing_options?.size!!>0
    }
    ////rentalfare :end

    //// Tapkey :start
    fun isLockTapkey():Boolean{
        return (isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_IOT_COMBINE ||
                isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_ONLY) &&
                getLockVendor() == UseCase.LockVendor.TAPKEY
    }

    //// Tapkey :end


    //// SaS PSLock :start
    fun isLockSaSOrPSLock():Boolean{
        return (isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_IOT_COMBINE ||
                isIotModule() == BaseBluetoothFragmentPresenter.HardwareType.ELLIPSE_ONLY) &&
                (getLockVendor() == UseCase.LockVendor.SAS || getLockVendor() == UseCase.LockVendor.PSLOCK)
    }

    fun doNeedfulForSaSOrPSLockWhenStartingRide(){
        if(isLockSaSOrPSLock())cancelConnectionSubscription()
    }

    //// SaS PSLock :end


    //// closedhub :start
    fun processClosedHubPortSelection(position:Int){
        if(hub!=null && hub?.ports!=null && hub?.ports?.size!!>position){
            bike = bikePortMapper.mapOut(hub,position)
            if(bike!=null){
                view?.onClosedHubPortSelectionSuccess()
                return
            }
        }

        view?.onClosedHubPortSelectionFailure()
    }
    //// closedhub :end
}