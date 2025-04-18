package com.lattis.lattis.presentation.qrscan

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Card
import com.lattis.domain.models.Lock
import com.lattis.domain.usecase.base.UseCase
import com.lattis.lattis.presentation.base.activity.bluetooth.BaseBluetoothActivity
import com.lattis.lattis.presentation.bikelist.BikeListFragment
import com.lattis.lattis.presentation.bikelist.ParkingHubPortsListAdapater
import com.lattis.lattis.presentation.payment.PaymentActivity
import com.lattis.lattis.presentation.payment.add.AddPaymentCardActivity
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.popup.edit.PopUpEditActivity
import com.lattis.lattis.presentation.rentalfare.RentalFareAdapter
import com.lattis.lattis.presentation.rentalfare.RentalFareClickListener
import com.lattis.lattis.presentation.reservation.AvailableVehiclesActionListener
import com.lattis.lattis.presentation.utils.*
import com.lattis.lattis.presentation.utils.StrictTCUtil.hasStrictTC
import com.lattis.lattis.presentation.webview.WebviewActivity
import com.lattis.lattis.uimodel.model.LockModel
import com.lattis.lattis.utils.ResourceHelper
import com.lattis.lattis.utils.ResourceHelper.getBikeType
import com.lattis.lattis.utils.communication.AndroidBus
import io.lattis.lattis.R
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_loading.view.*
import kotlinx.android.synthetic.main.activity_scan_bike_qr_code.*
import kotlinx.android.synthetic.main.fragment_bikelist.*
import kotlinx.android.synthetic.main.fragment_bikelist_slider.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_bike_card.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_confirm_reserve.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_confirm_reserve.view.*
import kotlinx.android.synthetic.main.fragment_bikelist_with_hub_bikes.view.*
import kotlinx.android.synthetic.main.layout_rental_fare_main.view.*
import kotlinx.android.synthetic.main.layout_strict_tc.view.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RuntimePermissions
class ScanBikeQRCodeActivity :
    BaseBluetoothActivity<ScanBikeQRCodeActivityPresenter,ScanBikeQRCodeActivityView>(), ScanBikeQRCodeActivityView,
    BarcodeCallback,RentalFareClickListener, AvailableVehiclesActionListener {
    private val TAG = ScanBikeQRCodeActivity::class.java.name
    private val bikeList: MutableList<Bike?> = ArrayList()
    private var isErrorPopUpBeingShown = false
    private var signedMessage: String? = null
    private var publicKey: String? = null
    private var startScanTimerSubscription: Disposable? = null
    private var connectingTimerSubscription: Disposable? = null
    private var unlockingTimerSubscription: Disposable? = null
    private var startRideTimerSubscription: Disposable? = null
    private var isBikeConnectedThruBluetooth = false
    private var isBackPressed = false
    private var flashLightStatus = false

    @Inject
    lateinit var context: Context

    var animFadeIn: Animation?=null

    var animFadeOut: Animation?=null


    @Inject
    override lateinit var presenter: ScanBikeQRCodeActivityPresenter
    override val activityLayoutId = R.layout.activity_scan_bike_qr_code
    override var view: ScanBikeQRCodeActivityView = this

    fun configureClick(){


        btn_confirm_in_confirm_reserve.setOnClickListener {
            checkConditionForReserve(false)
        }



        confirm_reserve_in_qr_code.btn_close_in_confirm_reserve.setOnClickListener {
            presenter.current_qr_code_id=-1
            presenter.current_iot_qr_code=null
            presenter.resetCurrentQRCode()
            animateConfirmBackground(R.color.confirm_reserve_semi_transparent_background,android.R.color.transparent,false)
//            confirm_reserve_in_qr_code.visibility = View.GONE
            startFadeOutAnimation()
            restartScanner()
        }


        iv_close_in_qr_code.setOnClickListener {
            finish()
        }


        cl_flash_light.setOnClickListener {
            toggleFlashLight()
        }

        confirm_reserve_in_qr_code.ct_add_credit_card_in_confirm_reserve.setOnClickListener {
            launchAddPaymentCardActivity()
        }

        confirm_reserve_in_qr_code.iv_add_credit_card_in_confirm_reserve.setOnClickListener {
            launchAddPaymentCardActivity()
        }


        confirm_reserve_in_qr_code.ct_bike_book_terms_policy.setOnClickListener {
            openBikeTermsAndCondition()
        }


        ////rentalfare :start
        layout_rental_fare_in_qr_code.iv_close_in_rental_fare.setOnClickListener {
            hideRentalFarePop()
        }

        layout_rental_fare_in_qr_code.cl_pay_per_use_in_rental_fare.setOnClickListener {
            payPerUseClicked()
        }
        layout_rental_fare_in_qr_code.btn_confirm_in_rental_fare.setOnClickListener {
            rentalFareConfirmationClicked()
        }

        layout_rental_fare_in_qr_code.iv_close_in_rental_fare.setOnClickListener {
            rentalFareSelectionCancelled()
        }
        ////rentalfare :end

        confirm_reserve_in_qr_code.cl_bike_price_in_confirm_reserve.setOnClickListener {
            showRentalFarePop(presenter.bike!!)
        }


        ////Strict TC :start

        layout_strict_tc_in_qr_code.btn_cancel_in_strict_tc.setOnClickListener {
            layout_strict_tc_in_qr_code.visibility= GONE
            layout_strict_tc_in_qr_code.sm_1_in_strict_tc.isChecked = false
            layout_strict_tc_in_qr_code.sm_2_in_strict_tc.isChecked = false
            layout_strict_tc_in_qr_code.btn_accept_unselected_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_qr_code.btn_accept_selected_in_strict_tc.visibility = GONE
        }

        layout_strict_tc_in_qr_code.btn_accept_selected_in_strict_tc.setOnClickListener {
            layout_strict_tc_in_qr_code.visibility= GONE
            checkConditionForReserve(true)
        }

        layout_strict_tc_in_qr_code.sm_1_in_strict_tc.setOnCheckedChangeListener { buttonView, isChecked ->
            handleAcceptCancelInStrictTC()
        }

        layout_strict_tc_in_qr_code.sm_2_in_strict_tc.setOnCheckedChangeListener { buttonView, isChecked ->
            handleAcceptCancelInStrictTC()
        }
        ////Strict TC :end


        bikelist_with_hub_bikes_in_qr_code.iv_close_in_dock_hub_bike_list_vehicles.setOnClickListener {
            bikelist_with_hub_bikes_in_qr_code.visibility = GONE
            presenter.resetCurrentQRCode()
            restartScanner()
        }

    }

    fun handleAcceptCancelInStrictTC(){
        val isChecked = layout_strict_tc_in_qr_code.sm_1_in_strict_tc.isChecked &&
                layout_strict_tc_in_qr_code.sm_2_in_strict_tc.isChecked
        if(isChecked) {
            layout_strict_tc_in_qr_code.btn_accept_selected_in_strict_tc.visibility =
                VISIBLE
            layout_strict_tc_in_qr_code.btn_accept_unselected_in_strict_tc.visibility =
                GONE
        }else{
            layout_strict_tc_in_qr_code.btn_accept_selected_in_strict_tc.visibility =
                GONE
            layout_strict_tc_in_qr_code.btn_accept_unselected_in_strict_tc.visibility =
                VISIBLE
        }
    }


    fun handleStrictTCPopOpUI(){
        val strings = StrictTCUtil.getStrictTCString()
        if(strings==null) {
            layout_strict_tc_in_qr_code.cl_consent_1_in_strict_tc.visibility = GONE
            layout_strict_tc_in_qr_code.cl_consent_2_in_strict_tc.visibility = GONE
        }else if(strings?.size!! == 1){
            layout_strict_tc_in_qr_code.ct_consent_1_in_strict_tc.text = strings?.get(0)
            layout_strict_tc_in_qr_code.sm_1_in_strict_tc.isChecked = false
            layout_strict_tc_in_qr_code.cl_consent_1_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_qr_code.sm_2_in_strict_tc.isChecked = true  //if only one string, it will make the second true
            layout_strict_tc_in_qr_code.cl_consent_2_in_strict_tc.visibility = GONE
        }else if(strings?.size!! == 2){
            layout_strict_tc_in_qr_code.ct_consent_1_in_strict_tc.text = strings?.get(0)
            layout_strict_tc_in_qr_code.ct_consent_2_in_strict_tc.text = strings?.get(1)
            layout_strict_tc_in_qr_code.sm_1_in_strict_tc.isChecked = false
            layout_strict_tc_in_qr_code.sm_2_in_strict_tc.isChecked = false
            layout_strict_tc_in_qr_code.cl_consent_1_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_qr_code.cl_consent_2_in_strict_tc.visibility = VISIBLE
        }

        val strictTCLink = StrictTCUtil.getStrictTCLink()
        if (TextUtils.isEmpty(strictTCLink)) {
            layout_strict_tc_in_qr_code.ct_privacy_and_terms_in_strict_tc.visibility = GONE
        } else{
            layout_strict_tc_in_qr_code.ct_privacy_and_terms_in_strict_tc.visibility = VISIBLE
            layout_strict_tc_in_qr_code.ct_privacy_and_terms_in_strict_tc.text =
                HtmlCompat.fromHtml(
                    String.format(
                        strictTCLink!!,
                        BuildConfigUtil.privacyPolicy(),
                        BuildConfigUtil.termsOfService()
                    ), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
        layout_strict_tc_in_qr_code.ct_privacy_and_terms_in_strict_tc.movementMethod = LinkMovementMethod.getInstance()

        layout_strict_tc_in_qr_code.btn_accept_unselected_in_strict_tc.visibility = VISIBLE
        layout_strict_tc_in_qr_code.btn_accept_selected_in_strict_tc.visibility = GONE
        layout_strict_tc_in_qr_code.visibility= VISIBLE
    }

    override fun setIotScanSubTitle() {
        ct_subtitle_in_qr_code.text = getString(R.string.find_qr_code_on,presenter.scanIotRentalName)
    }

    fun checkConditionForReserve(bypassStrictTC:Boolean){
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.CONFIRM,FirebaseUtil.QR_SCAN_CODE_CONFIRM)
        if (bikeList.size > 0) {
            if (bikeList[0] != null) {
                presenter.bike = bikeList[0]
                if(presenter.bike!=null) {
                    if (!presenter.phoneNumberCheckPassed(presenter.bike!!)) {
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
                    }else if(hasStrictTC() && !bypassStrictTC){
                        handleStrictTCPopOpUI()
                    } else{

                        if (!IsRidePaid.isRidePaidForFleet(presenter.bike?.fleet_type)) {
                            showLoadingForQRCodeScan(getString(R.string.starting_ride_loader))
                            presenter.reserveBike(presenter.bike)
                        } else {
                            if (presenter?.cards == null || presenter.cards?.size == 0) {
                                launchAddCardPopUp()
                            } else if (presenter.getPrimaryCard()==null){
                                launchMakeCardPrimaryPopUp()
                            } else {
                                showLoadingForQRCodeScan(getString(R.string.starting_ride_loader))
                                presenter.reserveBike(presenter.bike)
                            }
                        }
                    }
                }
            }
        }else {
            showQRCodeFailure(
                getString(R.string.general_error_title),
                getString(R.string.general_error_message)
            )
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
            REQUEST_CODE_ADD_CARD_ACTIVITY,
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

    fun openBikeTermsAndCondition(){
        if (bikeList.size > 0 && bikeList.get(0) is Bike) {
            if(!TextUtils.isEmpty(bikeList.get(0)?.fleet_t_and_c)) {
//                val url = bikeList.get(0)?.fleet_t_and_c!!
//                val i = Intent(Intent.ACTION_VIEW)
//                i.data = Uri.parse(url)
//                startActivity(i)

                WebviewActivity.launchForResult(this, REQUEST_CODE_TERMS_AND_CONDITION,bikeList.get(0)?.fleet_t_and_c)
            }
        }
    }

    fun launchAddPaymentCardActivity(){
        AddPaymentCardActivity.launchForResult(this,
            REQUEST_ADD_PAYMENT_CARD,
            null,
            presenter.bike?.fleet_id,
            presenter.bike?.payment_gateway
        )
    }





    override fun configureViews() {
        super.configureViews()
        setToolBarBackGround(Color.WHITE)
        decoratedBarcodeView.setStatusText("")
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        animFadeOut= AnimationUtils.loadAnimation(this, R.anim.fade_out)

        configureClick()
        showFlashLightIfAvailable()
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCameraPermissionWithPermissionCheck()
        presenter.requestLocationUpdates()
        presenter.getUserSubscription()
        presenter.userProfile
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun requestCameraPermission() {
        decoratedBarcodeView.decodeSingle(this)
    }

    override fun restartScanner() {
        Observable.timer(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ decoratedBarcodeView.decodeSingle(this@ScanBikeQRCodeActivity) }) { }
    }

    override fun onBikeDetailsSuccess(bike: Bike) {
//        restartScanner()
        bikeList.clear()
        bikeList.add(bike)
//        confirm_reserve_in_qr_code.visibility= View.VISIBLE
        startFadeInAnimation()

        setBikeCardInConfirmReserve(bike)
        // TODO show the bike detail screen here
    }



    override fun onBikeDetailsFailure() {
        showQRCodeFailure(
            getString(R.string.general_error_title),
            getString(R.string.general_error_message)
        )
    }

    override fun onBikeUnAuthorised() {
        showQRCodeFailure(
            getString(R.string.general_error_title),
            getString(R.string.fleet_access_denied_label)
        )
    }

    override fun onBikeAlreadyRented() {
        showQRCodeFailure(
            getString(R.string.general_error_title),
            getString(R.string.bike_already_rented_label)
        )
    }

    override fun onBikeNotAvailable() {
        showQRCodeFailure(
            getString(R.string.general_error_title),
            getString(R.string.bike_unavailable_label)
        )
    }

    override fun onBikeNotFound() {
        showQRCodeFailure(
            getString(R.string.general_error_title),
            getString(R.string.qr_code_no_fleet_label)
        )
    }

    override fun onInvalidQRCode() {
        showQRCodeFailure(
            getString(R.string.general_error_title),
            getString(R.string.qr_code_no_fleet_label)
        )
    }

    override fun onBikeNotLive() {
        showQRCodeFailure(
            getString(R.string.general_error_title),
            getString(R.string.asset_not_available_qr_code_scanned_label)
        )
    }

    override fun possibleResultPoints(list: List<ResultPoint>) {

    }

    override fun barcodeResult(barcodeResult: BarcodeResult) {
        resetRentalFare()
        presenter.processQRCodeDependingUponMode(barcodeResult.text)
    }

    protected override fun onResume() {
        super.onResume()
        resumeScanner()
        if (presenter.cards == null) presenter.getCards()
    }

    protected fun resumeScanner() {
        if (!decoratedBarcodeView.isActivated()) decoratedBarcodeView.resume()
    }

    protected fun pauseScanner() {
        decoratedBarcodeView.pause()
    }

    protected override fun onPause() {
        super.onPause()
        pauseScanner()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        cancelAllSubscription()
    }

    private fun showQRCodeFailure(title: String, subTitle: String) {
        bikeList.clear()
        if (!isErrorPopUpBeingShown) {
            isErrorPopUpBeingShown = true
            PopUpActivity.launchForResult(
                this,
                REQUEST_QR_CODE_BIKE_DETAILS_FAIL,
                title,
                subTitle,
                null,
                getString(R.string.general_btn_ok),
                null,
                null,
                null
            )
        }
    }

    private fun initializeView() {

    }

    private fun setCardDetailsInAdapter() {

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_QR_CODE_BIKE_DETAILS_FAIL) {
            isErrorPopUpBeingShown = false
            restartScanner()
        } else if (requestCode == REQUEST_CODE_ADD_CARD_ACTIVITY && resultCode == Activity.RESULT_OK) {
            launchAddPaymentCardActivity()
        } else if (requestCode == PHONE_NUMBER_ERROR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            launchPhoneNumberFillingActivity()
        } else if (requestCode == REQUEST_ADD_PAYMENT_CARD && resultCode == Activity.RESULT_OK){
            presenter.getCards()
        }else if(requestCode== REQUEST_CODE_ADD_PHONE_NUMBER &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            showLoadingForQRCodeScan(getString(R.string.loading))
            presenter.phoneNumber = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.sendCodeToUpdatePhoneNumber()
        }else if(requestCode== REQUEST_CODE_VALIDATE_PHONE_NUMBER &&
            resultCode== Activity.RESULT_OK &&
            data!=null &&
            data.hasExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
        ){
            showLoadingForQRCodeScan(getString(R.string.loading))
            presenter.code = data.getStringExtra(PopUpEditActivity.EDIT_TEXT_VALUE_POP_UP_EDIT)
            presenter.validateCodeForUpdatePhoneNumber()
        }else if (requestCode == MAKE_CARD_PRIMARY_REQUEST_CODE){
            presenter.getCards()
        }else if(requestCode == PRIMARY_CARD_ERROR_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            launchMakeCardPrimaryActivity()
        }

    }

    ////////////////////////////////// RESERVE BIKE CODE : START //////////////////////////////////////////////////
    override fun OnReserveBikeSuccess() {
        isBikeConnectedThruBluetooth = false
        presenter.deleteLock()
//        presenter.getSignedMessagePublicKey(presenter.bike!!)
        presenter.startLockConnectionDependingUponMode()
    }

    override fun onMissingUserCard() {
        hideLoadingForQRCodeScan()
        launchAddPaymentCardActivity()
    }

    override fun OnReserveBikeFail() {
        cancelBookingDisconnectLockShowErrorPopupWith(
            getString(R.string.general_error_title),
            getString(R.string.general_error_message)
        )
    }

    override fun OnReserveBikeNotFound() {
        cancelBookingDisconnectLockShowErrorPopupWith(
            getString(R.string.general_error_title),
            getString(R.string.bike_unavailable_label)
        )
    }

    ////////////////////////////////// LOCK CONNECTION CODE : START //////////////////////////////////////////////////
    override fun OnSignedMessagePublicKeySuccess(
        signedMessage: String,
        publicKey: String
    ) {
        this.signedMessage = signedMessage
        this.publicKey = publicKey
        presenter.setDisconnectRequiredForApp(false)
        val lockModel = LockModel()
        lockModel.signedMessage = signedMessage
        lockModel.publicKey = publicKey
        lockModel.userId = presenter.bike!!.fleet_key
        lockModel.macId = presenter.bike!!.mac_id
        presenter.lockModel = lockModel
        presenter.disconnectAllLocks() //this will disconnect all previous connections and start scanning for required lock
    }

    override fun onLockScanned(lockModel: LockModel?) {
        if (lockModel != null) {
            if (lockModel.macId == presenter.bike!!.mac_id) {
                subscribeToStartScanTimer(false)
                lockModel.signedMessage = signedMessage
                lockModel.publicKey = publicKey
                lockModel.userId = presenter.bike!!.fleet_key
                presenter.lockModel = lockModel
                presenter.connectTo()
            }
        }
    }

    override fun onLockConnected(lockModel: LockModel) {
//        showLoadingForQRCodeScan(getString(R.string.scan_qr_code_unlocking_loading))
        subscribeToConnectingTimer(false)
        subscribeToStartScanTimer(false)
        isBikeConnectedThruBluetooth = true
        presenter.saveLock(lockModel)
        subscribeToStartRideTimer(false)
        subscribeToUnlockTimer(false)
        subscribeToStartRideTimer(true)
        subscribeToUnlockTimer(true)
    }

    override fun onSaveLockSuccess(lock: Lock?) {}

    override fun OnSetPositionStatus(status: Boolean) {
        Log.e(TAG, "OnSetPositionStatus::$status")
        if (status || presenter.isLockTapkey() || presenter.isLockSaSOrPSLock()) {
            startRide()
        } else {
            subscribeToUnlockTimer(true)
        }
    }

    override fun startRide() {
        Log.e(TAG, "startRide::")
        presenter.doNeedfulForSaSOrPSLockWhenStartingRide()
        subscribeToUnlockTimer(false)
        subscribeToStartRideTimer(false)
        presenter.cancelSetPositionSubscription()
        presenter.startRide(false)
    }

    override fun onStartRideSuccess() {
        Log.e(TAG, "onStartRideSuccess::")


        AndroidBus.stringPublishSubject.onNext(QR_CODE_SCANNING_RIDE_STARTED)


        Observable.timer(6000,TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                finish()
            },{
                finish()
            })

//        val data = Intent()
//        data.putExtra(QR_CODE_SCANNING_RIDE_STARTED, true)
//        setResult(Activity.RESULT_OK, data)
//        finish()



    }

    override fun onCancelBikeSuccess() {
        if (isBackPressed) {
            finish()
        }
    }

    override fun onCancelBikeFail() {
        if (isBackPressed) {
            finish()
        }
    }

    override fun onBluetoothEnabled() {}
    override fun requestEnableBluetooth() {
        super.requestEnableBluetooth()
    }

    override fun onStartRideFail() {
        cancelBookingDisconnectLockShowErrorPopupWith(
            getString(R.string.general_error_title),
            getString(R.string.general_error_message)
        )
    }

    override fun onSaveLockFailure() {
        cancelBookingDisconnectLockShowErrorPopupWith(
            getString(R.string.general_error_title),
            getString(R.string.general_error_message)
        )
    }

    override fun showConnecting(requiresReconnection: Boolean) {
        if (requiresReconnection) {
            subscribeToConnectingTimer(false)
        }
        subscribeToConnectingTimer(true)
    }

    override fun onLockConnectionFailed() {
//        presenter.getSignedMessagePublicKey(presenter.bike!!)
        presenter.startLockConnectionDependingUponMode()
    }

    override fun onLockConnectionAccessDenied() {
        cancelBookingDisconnectLockShowErrorPopupWith(
            getString(R.string.general_error_title),
            getString(R.string.ellipse_access_denided_text)
        )
    }

    override fun onScanStart() {
        subscribeToStartScanTimer(true)
    }

    override fun OnSignedMessagePublicKeyFailure() {
        cancelBookingDisconnectLockShowErrorPopupWith(
            getString(R.string.general_error_title),
            getString(R.string.general_error_message)
        )
    }

    override fun onScanStop() {
        if (!isBikeConnectedThruBluetooth) presenter.getSignedMessagePublicKey(presenter.bike!!)
    }

    fun cancelBookingDisconnectLockShowErrorPopupWith(title: String?, subTitle: String?) {
        this.runOnUiThread(Runnable { hideLoadingForQRCodeScan() })
        cancelAllSubscription()
        cancelBikeBookingAndDisconnectLock()
        PopUpActivity.launchForResult(
            this,
            REQUEST_QR_CODE_BIKE_DETAILS_FAIL,
            title,
            subTitle,
            null,
            getString(R.string.general_btn_ok),
            null,
            null,
            null
        )
    }

    ////////////////////////////////// BIKE CARD WITH CONFIRM  : START //////////////////////////////////////////////////


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


        confirm_reserve_in_qr_code.ct_bike_type_in_confirm_reserve.setText(getBikeType(bike.type,this))
        confirm_reserve_in_qr_code.ct_bike_name_in_confirm_reserve.setText(bike.bike_name)
        confirm_reserve_in_qr_code.ct_fleet_name_in_confirm_reserve.setText(bike.fleet_name)

        ResourceHelper.setBatteryImageAndText(
            bike.bike_battery_level,
            confirm_reserve_in_qr_code.iv_bike_battery_confirm_reserve,
            confirm_reserve_in_qr_code.ct_bike_battery_confirm_reserve
        )



        setPayPerUseOrPricingOption(bike)
        if(TextUtils.isEmpty(bike.fleet_t_and_c)){
            confirm_reserve_in_qr_code.ct_bike_book_terms_policy.visibility = View.INVISIBLE
        }else{
            confirm_reserve_in_qr_code.ct_bike_book_terms_policy.visibility = View.VISIBLE
            confirm_reserve_in_qr_code.ct_bike_book_terms_policy.text = HtmlCompat.fromHtml(getString(R.string.bike_details_terms_policy, bike.fleet_t_and_c), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }



//        var card = presenter.getPrimaryCard()
//        if(card!=null){
//            handleCard(card)
//        }else{
//            handleNoCard()
//        }

        handleCardDetailsAfterBikeSelection(IsRidePaid.isRidePaidForFleet(bike.fleet_type))

//        Observable.timer(1000,TimeUnit.MILLISECONDS)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
                // set unlock fee in bike card and confirm screen
                confirm_reserve_in_qr_code.ct_unlock_price_label_in_confirm_reserve.visibility = View.GONE
                confirm_reserve_in_qr_code.ct_unlock_price_value_in_confirm_reserve.visibility = View.GONE
                setUnlockFee(bike)



                // set surcharge fee in bike card and confirm screen
                confirm_reserve_in_qr_code.ct_surcharge_description_in_confirm_reserve.visibility = View.GONE
                confirm_reserve_in_qr_code.ct_surcharge_price_value_in_confirm_reserve.visibility = View.GONE
                confirm_reserve_in_qr_code.ct_surcharge_price_label_in_confirm_reserve.visibility = View.GONE
                setExcessUsageFee(bike)


                confirm_reserve_in_qr_code.ct_bike_parking_in_confirm_reserve.visibility = View.GONE
                confirm_reserve_in_qr_code.ct_bike_parking_value_in_confirm_reserve.visibility = View.GONE
                confirm_reserve_in_qr_code.ct_parking_description_in_confirm_reserve.visibility = View.GONE
                setParkingFee(bike)
                setPreAuth(bike)
                setPromotionDiscount(bike)
                setRentalTimeLimit(bike)

                setMembershipDiscount(bike)
//            },{
//
//            })

        animateConfirmBackground(android.R.color.transparent,R.color.qr_code_semi_transparent_background,false)

    }

    fun animateConfirmBackground(colorFrom:Int,colorTo:Int,withDelay: Boolean=true){

        if(withDelay) {

            Observable.timer(550, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    confirm_reserve_in_qr_code.background =
                        ContextCompat.getDrawable(this, colorTo)
                }, {

                })
        }else{
            confirm_reserve_in_qr_code.background =
                ContextCompat.getDrawable(this, colorTo)
        }
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
        confirm_reserve_in_qr_code.iv_select_pricing_option_in_confirm_reserve.visibility = VISIBLE
        confirm_reserve_in_qr_code.ct_bike_price_value_in_confirm_reserve.text = getString(R.string.select_pricing)
    }

    fun setPricingOption(){
        confirm_reserve_in_qr_code.ct_bike_price_value_in_confirm_reserve.text =
            BikeFareUtil.getRentalFare(this, presenter.getSelectedRentalFare())
    }

    fun setPayPerUse(bike:Bike){
        confirm_reserve_in_qr_code.ct_bike_price_value_in_confirm_reserve.text = getBikeCost(bike)
    }


    fun setExcessUsageFee(bike: Bike){
        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) &&
            !TextUtils.isEmpty(bike.excess_usage_fees) &&
            bike.excess_usage_fees?.toFloatOrNull()!=null &&
            bike.excess_usage_fees!!.toFloat()!=0F) {

            if (!TextUtils.isEmpty(bike.excess_usage_fees) && !TextUtils.isEmpty(bike.excess_usage_type_value) && !TextUtils.isEmpty(bike.excess_usage_type) ) {
                val cost =  CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.excess_usage_fees
                    .toString()) + " " + getString(R.string.label_per) + " " +
                        bike.excess_usage_type_value
                            .toString() + " "+LocaleTranslatorUtils.getLocaleString(
                    this,
                    bike.excess_usage_type
                ).toString()


                confirm_reserve_in_qr_code.ct_surcharge_price_value_in_confirm_reserve.visibility = View.VISIBLE
                confirm_reserve_in_qr_code.ct_surcharge_price_label_in_confirm_reserve.visibility = View.VISIBLE
                confirm_reserve_in_qr_code.ct_surcharge_price_value_in_confirm_reserve.text = cost
                setExcessUsageFreq(bike)

                return

            }else if (!TextUtils.isEmpty(bike.excess_usage_fees) ){
                val cost =  CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.excess_usage_fees
                    .toString())

                confirm_reserve_in_qr_code.ct_surcharge_price_value_in_confirm_reserve.visibility = View.VISIBLE
                confirm_reserve_in_qr_code.ct_surcharge_price_label_in_confirm_reserve.visibility = View.VISIBLE
                confirm_reserve_in_qr_code.ct_surcharge_price_value_in_confirm_reserve.text = cost
                setExcessUsageFreq(bike)
                return
            }

        }

    }

    fun setExcessUsageFreq(bike:Bike){
        if(!TextUtils.isEmpty(bike.excess_usage_type_after_value) && !TextUtils.isEmpty(bike.excess_usage_type_after_type)){
            val surchage_freq = getString(R.string.surcharge_description,LocaleTranslatorUtils.getLocaleString(
                this,
                bike.excess_usage_type_after_type,
                bike.excess_usage_type_after_value.toString()
            ).toString())


            confirm_reserve_in_qr_code.ct_surcharge_description_in_confirm_reserve.visibility = View.VISIBLE
            confirm_reserve_in_qr_code.ct_surcharge_description_in_confirm_reserve.text = surchage_freq
            return
        }

    }


    fun setUnlockFee(bike: Bike){

        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) &&
            !TextUtils.isEmpty(bike.price_for_bike_unlock) && bike.price_for_bike_unlock!!.toFloat()!=0F) {
            val cost = CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.price_for_bike_unlock
                .toString())

            confirm_reserve_in_qr_code.ct_unlock_price_label_in_confirm_reserve.visibility = View.VISIBLE
            confirm_reserve_in_qr_code.ct_unlock_price_value_in_confirm_reserve.visibility = View.VISIBLE
            confirm_reserve_in_qr_code.ct_unlock_price_value_in_confirm_reserve.text = cost

        }



    }


    fun setParkingFee(bike:Bike){
        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) &&
            !TextUtils.isEmpty(bike.price_for_penalty_outside_parking) && bike.price_for_penalty_outside_parking!!.toFloat()!=0F) {
            val cost =
                CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.price_for_penalty_outside_parking
                    .toString())


            confirm_reserve_in_qr_code.ct_bike_parking_in_confirm_reserve.visibility = View.VISIBLE
            confirm_reserve_in_qr_code.ct_bike_parking_value_in_confirm_reserve.visibility = View.VISIBLE
            confirm_reserve_in_qr_code.ct_parking_description_in_confirm_reserve.visibility = View.VISIBLE
            confirm_reserve_in_qr_code.ct_bike_parking_value_in_confirm_reserve.text = cost
        }
    }


    fun setPreAuth(bike:Bike){
        if(!TextUtils.isEmpty(bike.enable_preauth) && (bike.enable_preauth.equals("1") || bike.enable_preauth.equals("true",true))){
            confirm_reserve_in_qr_code.ct_bike_preauth_value_in_confirm_reserve.text = CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.preauth_amount)
            confirm_reserve_in_qr_code.ct_bike_preauth_label_in_confirm_reserve.visibility = View.VISIBLE
            confirm_reserve_in_qr_code.ct_bike_preauth_value_in_confirm_reserve.visibility = View.VISIBLE
            confirm_reserve_in_qr_code.ct_preauth_description_in_confirm_reserve.visibility = View.VISIBLE
        }else{
            confirm_reserve_in_qr_code.ct_bike_preauth_label_in_confirm_reserve.visibility = View.GONE
            confirm_reserve_in_qr_code.ct_bike_preauth_value_in_confirm_reserve.visibility = View.GONE
            confirm_reserve_in_qr_code.ct_preauth_description_in_confirm_reserve.visibility = View.GONE
        }
    }

    fun setPromotionDiscount(bike:Bike){
        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) && bike.promotions!=null && bike.promotions?.size!!>0){
            confirm_reserve_in_qr_code.ct_promotion_label_in_confirm_reserve.visibility = VISIBLE
            confirm_reserve_in_qr_code.ct_promotion_value_in_confirm_reserve.visibility = VISIBLE
            confirm_reserve_in_qr_code.ct_promotion_value_in_confirm_reserve.text = getString(R.string.membership_discount_template,bike.promotions?.get(0)?.amount)
        }else{
            confirm_reserve_in_qr_code.ct_promotion_label_in_confirm_reserve.visibility = GONE
            confirm_reserve_in_qr_code.ct_promotion_value_in_confirm_reserve.visibility = GONE
        }
    }

    fun setRentalTimeLimit(bike:Bike){
        val rentalTimeLimit = BikeFareUtil.getRentalTimeLimit(this, bike.reservation)
        if(TextUtils.isEmpty(rentalTimeLimit)){
            confirm_reserve_in_qr_code.ct_rental_time_limit_label_in_confirm_reserve.visibility = GONE
            confirm_reserve_in_qr_code.ct_rental_time_limit_value_in_confirm_reserve.visibility = GONE
            confirm_reserve_in_qr_code.ct_rental_time_limit_description_in_confirm_reserve.visibility = GONE
        }else{
            confirm_reserve_in_qr_code.ct_rental_time_limit_value_in_confirm_reserve.text = rentalTimeLimit
            confirm_reserve_in_qr_code.ct_rental_time_limit_description_in_confirm_reserve.text = getString(R.string.rental_time_limit_description,rentalTimeLimit)
            confirm_reserve_in_qr_code.ct_rental_time_limit_label_in_confirm_reserve.visibility = VISIBLE
            confirm_reserve_in_qr_code.ct_rental_time_limit_value_in_confirm_reserve.visibility = VISIBLE
            confirm_reserve_in_qr_code.ct_rental_time_limit_description_in_confirm_reserve.visibility = VISIBLE
        }
    }


    fun handleCardDetailsAfterBikeSelection(isRidePaid:Boolean){
        if(presenter.getPrimaryCard()==null ){
            if(!isRidePaid) {
                handleNoCardForFreeRide()
            }else{
                handleNoCard()
            }
        }else{
            handleCard(presenter.getPrimaryCard()!!)
        }
    }
    fun handleNoCardForFreeRide(){
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.iv_bike_payment_img_in_confirm_reserve.visibility =
            View.GONE
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.ct_bike_payment_value_in_confirm_reserve.visibility =
            View.GONE

        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.iv_add_credit_card_in_confirm_reserve.visibility =
            View.GONE
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.ct_add_credit_card_in_confirm_reserve.visibility =
            View.GONE

        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.ct_bike_payment_in_confirm_reserve.visibility =
            View.GONE
    }

    override fun handleCard(card: Card) {
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.ct_bike_payment_in_confirm_reserve.visibility =
            View.VISIBLE
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.iv_bike_payment_img_in_confirm_reserve.visibility= View.VISIBLE
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.ct_bike_payment_value_in_confirm_reserve.visibility= View.VISIBLE
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.iv_add_credit_card_in_confirm_reserve.visibility= View.GONE
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.ct_add_credit_card_in_confirm_reserve.visibility= View.GONE
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.ct_bike_payment_value_in_confirm_reserve.setText("XXXX-" + card.cc_no!!.substring(card.cc_no!!.length - 4))
    }

    override fun handleNoCard() {
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.ct_bike_payment_in_confirm_reserve.visibility =
            View.VISIBLE
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.iv_bike_payment_img_in_confirm_reserve.visibility= View.GONE
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.ct_bike_payment_value_in_confirm_reserve.visibility= View.GONE

        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.iv_add_credit_card_in_confirm_reserve.visibility= View.VISIBLE
        confirm_reserve_in_qr_code.cl_payment_confirm_reserve.ct_add_credit_card_in_confirm_reserve.visibility= View.VISIBLE
    }

    fun getBikeCost(bike: Bike):String{
        if (IsRidePaid.isRidePaidForFleet(bike.fleet_type) &&
            !TextUtils.isEmpty(bike.price_for_membership)  && bike.price_for_membership!!.toFloat()!=0F) {

            if (bike.price_type != null && !bike.price_type.equals("")) {
                var rideCost =
                    CurrencyUtil.getCurrencySymbolByCode(bike.currency,bike.price_for_membership
                        .toString()) + " " + getString(R.string.label_per) + " " +
                            bike.price_type_value
                                .toString() + LocaleTranslatorUtils.getLocaleString(
                       this,
                        bike.price_type
                    ).toString()

                return rideCost
            }

        }else{
            return getString(R.string.bike_detail_bike_cost_free)
        }
        return "";
    }

    fun setMembershipDiscount(bike:Bike){

        val membershipDiscount = presenter.getMembershipDiscount(bike.fleet_id!!)
        if(membershipDiscount!=null){
            confirm_reserve_in_qr_code.ct_membership_discount_label_in_confirm_reserve.visibility = View.VISIBLE
            confirm_reserve_in_qr_code.ct_membership_discount_value_in_confirm_reserve.visibility = View.VISIBLE
            ct_membership_discount_value_in_confirm_reserve.text = getString(R.string.membership_discount_template,membershipDiscount)
        }else{
            confirm_reserve_in_qr_code.ct_membership_discount_label_in_confirm_reserve.visibility = View.GONE
            confirm_reserve_in_qr_code.ct_membership_discount_value_in_confirm_reserve.visibility = View.GONE
        }

    }

    override fun hideProgressbar() {
        pb_scanning_in_qr_code.visibility = View.GONE
        showFlashLightIfAvailable()
    }

    override fun showProgressbar() {
        pb_scanning_in_qr_code.visibility = View.VISIBLE
        flash_unselected.visibility = View.VISIBLE
        flash_selected.visibility = View.GONE
        cl_flash_light.isEnabled = false
    }

    fun showFlashLightIfAvailable(){
        val isFlashAvailable = applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        if(isFlashAvailable){
            flash_unselected.visibility = View.GONE
            flash_selected.visibility = View.VISIBLE
            cl_flash_light.isEnabled = true
        }else{
            flash_unselected.visibility = View.VISIBLE
            flash_selected.visibility = View.GONE
            cl_flash_light.isEnabled = false
        }
    }

    fun toggleFlashLight(){

        if (flashLightStatus) {
            flashLightStatus = false;
            decoratedBarcodeView.setTorchOff();
        } else {
            flashLightStatus = true;
            decoratedBarcodeView.setTorchOn();
        }

    }

    ////////////////////////////////// BIKE CARD WITH CONFIRM  : END //////////////////////////////////////////////////


    ////////////////////////////////// SCAN CONNECTING TIMER CODE : START //////////////////////////////////////////////////
    @Synchronized
    fun subscribeToStartScanTimer(active: Boolean) {
        if (active) {
            if (startScanTimerSubscription == null) {
                startScanTimerSubscription = Observable.timer(
                    MILLISECONDS_FOR_START_SCAN.toLong(),
                    TimeUnit.MILLISECONDS
                )
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        subscribeToStartScanTimer(false)
                        cancelBookingDisconnectLockShowErrorPopupWith(
                            getString(R.string.general_error_title),
                            getString(R.string.qr_error_ellipse_not_around)
                        )
                    }) { }
            }
        } else {
            if (startScanTimerSubscription != null) {
                startScanTimerSubscription!!.dispose()
                startScanTimerSubscription = null
            }
        }
    }

    @Synchronized
    fun subscribeToConnectingTimer(active: Boolean) {
        if (active) {
            if (connectingTimerSubscription == null) {
                connectingTimerSubscription = Observable.timer(
                    MILLISECONDS_FOR_CONNECTING_TIMER.toLong(),
                    TimeUnit.MILLISECONDS
                )
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        subscribeToConnectingTimer(false)
                        cancelBookingDisconnectLockShowErrorPopupWith(
                            getString(R.string.general_error_title),
                            getString(R.string.bike_out_of_range_connection_error)
                        )
                    }) { }
            }
        } else {
            if (connectingTimerSubscription != null) {
                connectingTimerSubscription!!.dispose()
                connectingTimerSubscription = null
            }
        }
    }

    @Synchronized
    fun subscribeToStartRideTimer(active: Boolean) {
        Log.e(TAG, "subscribeToStartRideTimer::$active")
        if (active) {
            if (startRideTimerSubscription == null) {
                startRideTimerSubscription = Observable.timer(
                    if(presenter.isLockSaSOrPSLock()) MILLISECONDS_FOR_START_RIDE_TIMER_FOR_SASPSLOCK.toLong() else if( presenter.isLockTapkey())  MILLISECONDS_FOR_START_RIDE_TIMER_FOR_TAPKEY.toLong() else MILLISECONDS_FOR_START_RIDE_TIMER.toLong(),
                    TimeUnit.MILLISECONDS
                )
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        Log.e(
                            TAG,
                            "subscribeToStartRideTimer::$active call"
                        )
                        startRide()
                    }) { }
            }
        } else {
            if (startRideTimerSubscription != null) {
                startRideTimerSubscription!!.dispose()
                startRideTimerSubscription = null
            }
        }
    }

    @Synchronized
    fun subscribeToUnlockTimer(active: Boolean) {
        Log.e(TAG, "subscribeToUnlockTimer::$active")
        if (active) {
            if (unlockingTimerSubscription == null) {
                unlockingTimerSubscription = Observable.timer(
                    MILLISECONDS_FOR_UNLOCKING_TIMER.toLong(),
                    TimeUnit.MILLISECONDS
                )
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        Log.e(TAG, "subscribeToUnlockTimer::$active call")
                        presenter.setPosition(false)
                        subscribeToUnlockTimer(false)
                    }) {

                    }
            }
        } else {
            if (unlockingTimerSubscription != null) {
                unlockingTimerSubscription!!.dispose()
                unlockingTimerSubscription = null
            }
        }
    }

    fun cancelBikeBookingAndDisconnectLock() {
        presenter.setDisconnectRequiredForApp(true)
        presenter.deleteLock()
        presenter.disconnectAllLocks()
        presenter.cancelBikeReservation()
    }

    fun cancelAllSubscription() {
        subscribeToConnectingTimer(false)
        subscribeToStartScanTimer(false)
        subscribeToStartRideTimer(false)
        subscribeToUnlockTimer(false)
        presenter.cancelAllSubscription()
    }

    fun showLoadingForQRCodeScan(message: String?) {
        this.runOnUiThread(Runnable {
            scan_bike_qr_code_loading_operation_view.visibility = (View.VISIBLE)
            scan_bike_qr_code_loading_operation_view.ct_loading_title.text = (message)
        })
    }

    fun hideLoadingForQRCodeScan() {
        scan_bike_qr_code_loading_operation_view.visibility = (View.GONE)
    }

    protected override fun onInternetConnectionChanged(isConnected: Boolean) {}
    override fun onBackPressed() {
        Log.e(TAG, "On Back Pressed")
        isBackPressed = true
        if (presenter.qrScanProgress === ScanBikeQRCodeActivityPresenter.QRScanProgress.NOTHING) {
            super.onBackPressed()
            return
        } else if (presenter.qrScanProgress === ScanBikeQRCodeActivityPresenter.QRScanProgress.LOCK_CONNECTED
            || presenter.qrScanProgress === ScanBikeQRCodeActivityPresenter.QRScanProgress.BIKE_RESERVE
        ) {
            cancelAllSubscription()
            cancelBikeBookingAndDisconnectLock()
        } else if (presenter.qrScanProgress === ScanBikeQRCodeActivityPresenter.QRScanProgress.RIDE_STARTED) {

        }
    }

    fun launchPhoneNumberFillingActivity() {
        PopUpEditActivity.launchForResult(
            this,
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
        PopUpEditActivity.launchForResult(
            this,
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
        hideLoadingForQRCodeScan()
        openCodeValidationActivity()
    }

    override fun onCodeSentFailure() {
        hideLoadingForQRCodeScan()
        showServerGeneralError(REQUEST_CODE_ADD_PHONE_NUMBER_ERROR)
    }

    override fun onCodeValidateFailure() {
        hideLoadingForQRCodeScan()
        showServerGeneralError(REQUEST_CODE_ADD_PHONE_NUMBER_ERROR)
    }

    override fun onGetUserProfile() {
        hideLoadingForQRCodeScan()
//        rl_connect_book_start_ride.setEnabled(true)
    }

    fun startFadeOutAnimation(){

        confirm_reserve_in_qr_code.startAnimation(animFadeOut)
        animFadeOut?.setAnimationListener(object :Animation.AnimationListener{
            override fun onAnimationEnd(p0: Animation?) {
                confirm_reserve_in_qr_code.visibility=View.GONE
            }

            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationRepeat(p0: Animation?) {

            }
        })
    }


    fun startFadeInAnimation(){
        confirm_reserve_in_qr_code.startAnimation(animFadeIn)
        animFadeIn?.setAnimationListener(object :Animation.AnimationListener{
            override fun onAnimationEnd(p0: Animation?) {

            }

            override fun onAnimationStart(p0: Animation?) {
                confirm_reserve_in_qr_code.visibility=View.VISIBLE
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }
        })
    }


    override fun onIotScanSuccess() {
        var intent = Intent()
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

    override fun onIotScanFailure() {
        showQRCodeFailure(
            getString(R.string.general_error_title),
            getString(R.string.wrong_qr_code_scanned_label)
        )
    }

    companion object {
        private val PHONE_NUMBER_ERROR_REQUEST_CODE = 4024
        private val REQUEST_CODE_ADD_PHONE_NUMBER = 3248
        private val REQUEST_CODE_VALIDATE_PHONE_NUMBER = 3249
        private val REQUEST_CODE_ADD_PHONE_NUMBER_ERROR = 3250
        private val REQUEST_QR_CODE_BIKE_DETAILS_FAIL = 3101
        private val REQUEST_CODE_ADD_CARD_ACTIVITY = 7023
        private val REQUEST_ADD_PAYMENT_CARD = 7024
        private val REQUEST_CODE_TERMS_AND_CONDITION = 7025
        private val PRIMARY_CARD_ERROR_REQUEST_CODE = 7026
        private val MAKE_CARD_PRIMARY_REQUEST_CODE = 7027
        const val QR_CODE_SCANNING_RIDE_STARTED = "QR_CODE_SCANNING_RIDE_STARTED"
        private const val MILLISECONDS_FOR_START_SCAN = 60000
        private const val MILLISECONDS_FOR_CONNECTING_TIMER = 60000
        private const val MILLISECONDS_FOR_UNLOCKING_TIMER = 200
        private const val MILLISECONDS_FOR_START_RIDE_TIMER_FOR_TAPKEY = 40000
        private const val MILLISECONDS_FOR_START_RIDE_TIMER_FOR_SASPSLOCK = 20000
        private const val MILLISECONDS_FOR_START_RIDE_TIMER = 10000
        val IS_SCAN_IOT_MODE = "IS_SCAN_IOT_MODE"
        val SCAN_IOT_QR_REQUIRED_QR_CODE = "SCAN_IOT_QR_REQUIRED_QR_CODE"
        val SCAN_IOT_BIKE_ID = "SCAN_IOT_BIKE_ID"
        val SCAN_IOT_RENTAL_NAME = "SCAN_IOT_RENTAL_NAME"


        fun getIntent(context: Context, bikeId:Int?,iotQrCode:String?, isIotScanMode:Boolean,iotScanModeRentalName:String?): Intent {
            val intent = Intent(context, ScanBikeQRCodeActivity::class.java)
            if(bikeId!==null) {
                intent.putExtra(SCAN_IOT_BIKE_ID, bikeId)
            }
            if(iotQrCode!==null) {
                intent.putExtra(SCAN_IOT_QR_REQUIRED_QR_CODE, iotQrCode)
            }
            intent.putExtra(SCAN_IOT_RENTAL_NAME,iotScanModeRentalName)
            intent.putExtra(IS_SCAN_IOT_MODE,isIotScanMode)
            return intent
        }
    }



    ////rentalfare :start

    fun showRentalFarePop(bike:Bike){
        if(presenter.bikeHasPricingOptionSelection()){
            layout_rental_fare_in_qr_code.visibility= VISIBLE
            layout_rental_fare_in_qr_code.ct_pay_per_use_value_in_rental_fare.text = getBikeCost(bike)
            layout_rental_fare_in_qr_code.rv_rental_fare_in_rental_fare.setLayoutManager(
                LinearLayoutManager(this)
            )
            layout_rental_fare_in_qr_code.rv_rental_fare_in_rental_fare.adapter = RentalFareAdapter(this,this,bike.pricing_options,presenter.rentalFareSelected.finalRentalFareSelectedIndex)
        }
    }

    fun hideRentalFarePop(){
        layout_rental_fare_in_qr_code.visibility= GONE
    }

    fun payPerUseClicked(){
        presenter.onPayPerUseClicked()
        layout_rental_fare_in_qr_code.iv_pay_per_use_value_in_rental_fare.setImageResource(R.drawable.check_mark)
        (layout_rental_fare_in_qr_code.rv_rental_fare_in_rental_fare.adapter as RentalFareAdapter).reset()
    }

    override fun onRentalFareSelected(position:Int){
        layout_rental_fare_in_qr_code.iv_pay_per_use_value_in_rental_fare.setImageResource(0)
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
        if(layout_rental_fare_in_qr_code.rv_rental_fare_in_rental_fare.adapter is RentalFareAdapter){
            (layout_rental_fare_in_qr_code.rv_rental_fare_in_rental_fare.adapter as RentalFareAdapter).reset()
        }
        layout_rental_fare_in_qr_code.iv_pay_per_use_value_in_rental_fare.setImageResource(0)
    }

    override fun showPayPerUse() {
        setPayPerUseOrPricingOption(presenter.bike!!)
        hideRentalFarePop()
    }

    override fun showRentalFare() {
        setPayPerUseOrPricingOption(presenter.bike!!)
        hideRentalFarePop()
    }

    override fun onBikeReserveFailureDuePricingOptionSelectionRemaining() {
        hideLoadingForQRCodeScan()
        showRentalFarePop(presenter.bike!!)
    }

    ////rentalfare :end


    ////closed hub :start
    override fun showClosedHubPorts(){

        if(presenter.hub==null || presenter.hub?.ports==null)
            return

        bikelist_with_hub_bikes_in_qr_code.ct_title_in_dock_hub_bike_list_vehicles.text = presenter.hub?.hub_name + "(" + presenter.hub?.ports?.size!! + ")"

        bikelist_with_hub_bikes_in_qr_code.rv_in_dock_hub_bike_list_vehicles.setLayoutManager(LinearLayoutManager(this))
        bikelist_with_hub_bikes_in_qr_code.rv_in_dock_hub_bike_list_vehicles.setAdapter(ParkingHubPortsListAdapater(this, presenter.hub!!,  this))
        bikelist_with_hub_bikes_in_qr_code.visibility = VISIBLE
    }

    override fun onBikeSelected(position: Int) {
        bikelist_with_hub_bikes_in_qr_code.visibility = GONE
        presenter.processClosedHubPortSelection(position)
    }

    override fun onMapSelected(position: Int) {

    }

    override fun onBikeInfoSelected(position: Int) {

    }

    override fun onClosedHubPortSelectionFailure() {
        onBikeDetailsFailure()
        restartScannerWithQRCodeValue()
    }

    override fun onClosedHubPortSelectionSuccess() {
        if(presenter.bike!=null)onBikeDetailsSuccess(presenter.bike!!)else onClosedHubPortSelectionFailure()
    }

    fun restartScannerWithQRCodeValue(){
        presenter.resetCurrentQRCode()
        restartScanner()
    }

    ////closed hub :end


}