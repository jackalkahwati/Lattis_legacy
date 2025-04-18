package com.lattis.lattis.presentation.base.fragment.bluetooth

import android.text.TextUtils
import android.util.Log
import com.lattis.domain.models.*
import com.lattis.domain.models.FirebasePushNotification.Companion.docked
import com.lattis.domain.models.FirebasePushNotification.Companion.docking
import com.lattis.domain.models.FirebasePushNotification.Companion.locked
import com.lattis.domain.models.FirebasePushNotification.Companion.sentinel_lock_closed
import com.lattis.domain.models.FirebasePushNotification.Companion.sentinel_lock_online
import com.lattis.domain.models.FirebasePushNotification.Companion.sentinel_lock_opened
import com.lattis.domain.models.axa.AxaKey
import com.lattis.domain.usecase.activetripservice.PauseUpdateTripUseCase
import com.lattis.domain.usecase.activetripservice.StartActiveTripUseCase
import com.lattis.domain.usecase.activetripservice.StartLocationTrackInActiveTripUseCase
import com.lattis.domain.usecase.activetripservice.StopGetTripDetailsThreadIfApplicableUseCase
import com.lattis.domain.usecase.axa.GetAxaLockKeyUseCase
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.usecase.bike.*
import com.lattis.domain.usecase.dockhub.UnDockBikeUseCase
import com.lattis.domain.usecase.lock.SignedMessagePublicKey.SignedMessagePublicKeyUseCase
import com.lattis.domain.usecase.lock.connect.ConnectToLastLockedLockUseCase
import com.lattis.domain.usecase.lock.connect.ConnectToLockUseCase
import com.lattis.domain.usecase.lock.disconnect.DisconnectAllLockUseCase
import com.lattis.domain.usecase.lock.firmware.GetLockFirmwareVersionCase
import com.lattis.domain.usecase.lock.observe.ObserveConnectionStateUseCase
import com.lattis.domain.usecase.lock.observe.ObserveHardwareStateUseCase
import com.lattis.domain.usecase.lock.observe.ObserveLockPositionUseCase
import com.lattis.domain.usecase.lock.realm.GetLockUseCase
import com.lattis.domain.usecase.lock.realm.SaveLockUseCase
import com.lattis.domain.usecase.lock.setter.SetLockPositionUseCase
import com.lattis.domain.usecase.ride.GetRideUseCase
import com.lattis.domain.usecase.updatetrip.StopActiveTripUseCase
import com.lattis.domain.usecase.updatetrip.StopLocationTrackInActiveTripUseCase
import com.lattis.domain.usecase.v2.GetV2IoTBikeStatusUseCase
import com.lattis.domain.usecase.v2.LockUnlockV2IotBikeUseCase
import com.lattis.lattis.presentation.base.fragment.usercurrentlocation.BaseUserCurrentStatusPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.RideUtil
import com.lattis.lattis.presentation.utils.RideUtil.provideIoTControllerId
import com.lattis.lattis.uimodel.mapper.LockModelMapper
import com.lattis.lattis.uimodel.model.LockModel
import com.lattis.lattis.utils.SentinelHelper.isSentinel
import com.lattis.lattis.utils.communication.AndroidBus
import com.lattis.lattis.utils.localnotification.RESERVATION_TIMER_OVER_NOTIFICATION_TYPE
import io.lattis.ellipse.sdk.exception.BluetoothException
import io.lattis.ellipse.sdk.exception.ConnectionException
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open abstract class BaseBluetoothFragmentPresenter<View: BaseBluetoothFragmentView>: BaseUserCurrentStatusPresenter<View>() {

    private var connectToLastSubscription: Disposable? = null
    private var connectToSubscription: Disposable? = null
    private var positionSubscription: Disposable? = null
    private var connectionSubscription: Disposable? = null
    private var hardwareSubscription: Disposable? = null
    private var setPositionTimerSubscription: Disposable? = null
    private var setPositionErrorTimerSubscription: Disposable? = null
    private var setPositionSubscription: Disposable? = null
    private var getFirmwareVersionSubscription: Disposable? = null
    private var getFirmwareTimerSubscription: Disposable? = null
    private var startLocationTrackInActiveTripSubscription: Disposable? = null
    private var stopLocationTrackInActiveTripSubscription: Disposable? = null
    private var startActiveTripSubscription: Disposable? = null
    private val stopActiveTripSubscription: Disposable? = null
    private var sentinelUnlockTimerDisposable : Disposable?=null
    private var sentinelLockTimerDisposable : Disposable?=null

    var connectionState:ConnectionState = ConnectionState.DISCONNECTED
    var ride: Ride? = null
    private var lockModel: LockModel? = null
    private var isAccessDeniedForEllipse = false
    private var connectedLock: LockModel? = null
    private var lastPositionRequested: Lock.Hardware.Position? = null
    private var setPositionRequest = true
    private var setPositionRetry = 0
    private val MAX_SET_POSITION_RETRY = 4
    private var shackleJam = false
    private var isJammingUpdated = false
    private var isBikeMetaDataUpdated = false
    var lock_battery: Int? = null
    private var firmwareVersion: String? = null
    private val MILLISECONDS_FOR_SET_POSITION_TIMER = 2000
    private val MILLISECONDS_FOR_SET_POSITION_ERROR_TIMER = 2000
    private val MILLISECONDS_FOR_FIRMWARE_TIMER = 10000
    var lockPosition:Lock.Hardware.Position?=null
    var serviceNotificationTitle:String?=null
    var iotLockPosition:Boolean?=null     // this will be inverted in function for first time so unlock it
    var ioTLockUnlockInProgress=false
    var ioTChangedFromUnlockToLock=false
    var ioTChangedFromlockToUnLock=false
    var dockHubUUID:String?=null
    var dockHubType:String?=null

    //// AXA :start
    private var axaLockId:String? = "E22510006CC3253BD418"
    private var mOTPasskeyNr: Int = 0
    private var mOTPKeyparts: Array<String> = emptyArray()
    private var mEKeyAscii: String? = null
    //// AXA :end


    //// LINKA :start
    private val GET_LINKA_LOCK_STATUS_INTERVAL_MILLIS = 5000
    private var getLinkaLockStatusSubscription: Disposable? = null
    //// LINKA :end


    @Inject
    lateinit var connectToLockUseCase: ConnectToLockUseCase

    @Inject
    lateinit var connectToLastLockedLockUseCase: ConnectToLastLockedLockUseCase

    @Inject
    lateinit var signedMessagePublicKeyUseCase: SignedMessagePublicKeyUseCase

    @Inject
    lateinit var setLockPositionUseCase: SetLockPositionUseCase

    @Inject
    lateinit var observeLockPositionUseCase: ObserveLockPositionUseCase

    @Inject
    lateinit var observeConnectionStateUseCase: ObserveConnectionStateUseCase

    @Inject
    lateinit var observeHardwareStateUseCase: ObserveHardwareStateUseCase

    @Inject
    lateinit var getRideUseCase: GetRideUseCase

    @Inject
    lateinit var getLockUseCase: GetLockUseCase

    @Inject
    lateinit var saveLockUseCase : SaveLockUseCase

    @Inject
    lateinit var disconnectAllLockUseCase: DisconnectAllLockUseCase

    @Inject
    lateinit var getLockFirmwareVersionUseCase: GetLockFirmwareVersionCase

    @Inject
    lateinit var updateBikeMetaDatUseCase: UpdateBikeMetaDatUseCase

    @Inject
    lateinit var lockModelMapper : LockModelMapper


    @Inject
    lateinit var startActiveTripUseCase: StartActiveTripUseCase

    @Inject
    lateinit var startLocationTrackInActiveTripUseCase: StartLocationTrackInActiveTripUseCase

    @Inject
    lateinit var stopLocationTrackInActiveTripUseCase: StopLocationTrackInActiveTripUseCase

    @Inject
    lateinit var stopActiveTripUseCase: StopActiveTripUseCase

    @Inject
    lateinit var stopGetTripDetailsThreadIfApplicableUseCase: StopGetTripDetailsThreadIfApplicableUseCase

    @Inject
    lateinit var lockUnlockV2IotBikeUseCase: LockUnlockV2IotBikeUseCase

    @Inject
    lateinit var unlockSentinelBikeUseCase: UnlockSentinelBikeUseCase

    @Inject
    lateinit var getAxaLockKeyUseCase: GetAxaLockKeyUseCase

    @Inject
    lateinit var getV2IoTBikeStatusUseCase: GetV2IoTBikeStatusUseCase

    @Inject
    lateinit var getLinkaIoTBikeStatusUseCase: GetLinkaIoTBikeStatusUseCase

    @Inject
    lateinit var unDockBikeUseCase: UnDockBikeUseCase

    @Inject
    lateinit var pauseUpdateTripUseCase: PauseUpdateTripUseCase
    private var isEndRideActivityLaunched = false


    fun onBluetoothEnabled() {
        getSignedMessagePublicKey()
    }


    enum class HardwareType{
        ELLIPSE_ONLY,
        IOT_ONLY,
        ELLIPSE_IOT_COMBINE,
        MANUAL_LOCK,
        NONE
    }


    ////////////////////////// Bluetooth connection : start ///////////////////////////
    open fun getRide() {
        subscriptions.add(
            getRideUseCase
                .execute(object : RxObserver<Ride>(view) {
                    override fun onNext(newRide: Ride) {
                        super.onNext(newRide)
                        ride = newRide
                        view?.onRideSuccess(ride!!)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onRideFailure()
                    }
                })
        )
    }

    open fun connectToLastLockedLock() {
        if (connectToLastSubscription != null) connectToLastSubscription?.dispose()

        Log.e("BBFP:LastConnected::","connectToLastLockedLock")

        isAccessDeniedForEllipse = false
        if (lockModel == null && ride != null) {
            lockModel = LockModel()
            lockModel?.userId = (ride?.bike_bike_fleet_key)
            lockModel?.macId = (ride?.bike_mac_id)
        }
        subscriptions.add(connectToLastLockedLockUseCase
            .withLockVendor(getLockVendor())
            .execute(object :
                RxObserver<Lock.Connection.Status>() {
                override fun onStart() {
                    super.onStart()
                    //connectionState = BikeDirectionFragmentPresenter.ConnectionState.CONNECTING;
//                view?.showConnecting();
                }

                override fun onNext(status: Lock.Connection.Status) {
                    super.onNext(status)
                    Log.e("BBFP:LastConnected",status.toString())
                    if (status.isAuthenticated) {
                        val lastConnectedLockModel: LockModel = lockModelMapper.mapIn(status.lock)
                        connectionState = ConnectionState.CONNECTED
                        if (ride != null && lastConnectedLockModel != null && lastConnectedLockModel.macId != null && lastConnectedLockModel.macId.equals(
                                ride?.bike_mac_id
                            )
                        ) {
                            view?.onLockConnected(lastConnectedLockModel.also {
                                connectedLock = it
                                saveLock()
                            })
                            if(lockModel!=null && lastConnectedLockModel.signedMessage!=null && lastConnectedLockModel.publicKey!=null){
                                lockModel?.signedMessage = lastConnectedLockModel.signedMessage
                                lockModel?.publicKey = lastConnectedLockModel.publicKey
                            }
                        } else {
                            getSignedMessagePublicKey()
                        }
                    } else if (status.equals(Lock.Connection.Status.DISCONNECTED)) {
                        connectionState = ConnectionState.DISCONNECTED
                        if (!isAccessDeniedForEllipse) {
                            view?.onLockConnectionFailed()
                        }
                        stopLocationTrackInActiveTripService()
                    } else if (status.equals(Lock.Connection.Status.ACCESS_DENIED)) {
                        connectionState = ConnectionState.CONNECTION_FAIL
                        isAccessDeniedForEllipse = true
                        view?.onLockConnectionAccessDenied()
                        logCustomException(Throwable("Acess denied: " + lockModel?.macId))
                        stopLocationTrackInActiveTripService()
                    }
                }

                override fun onError(throwable: Throwable) {
                    Log.e("BBFP:LastConnected::","connectToLastLockedLock::onError")
                    super.onError(throwable)
                    if (throwable is BluetoothException) {
                        val exception = throwable as BluetoothException?
                        if (exception != null) {
                            if (exception.status != null) {
                                if (exception.status == BluetoothException.Status.BLUETOOTH_DISABLED) {
                                    view?.requestEnableBluetooth()
                                }
                            }
                        }
                    } else {
                        Log.e("BBFP:LastConnected::","connectToLastLockedLock::onError::::::")
                        connectionState = ConnectionState.DISCONNECTED
                        view?.onLockConnectionFailed()
                    }
                }
            }).also { connectToLastSubscription = it })
    }

    open fun getSignedMessagePublicKey() {

        Log.e("BaseBleFragPres","getSignedMessagePublicKey")

        if(ride == null || ride?.bikeId==null || ride?.bike_mac_id==null){
            view?.onSignedMessagePublicKeyFailure()
            return
        }

        subscriptions.add(
            signedMessagePublicKeyUseCase
                .withLockVendor(getLockVendor())
                .withBikeId(ride?.bikeId!!)
                .withMacId(ride?.bike_mac_id!!)
                .withFleetId(ride?.bike_fleet_id!!)
                .execute(object : RxObserver<SignedMessageAndPublicKey>(view) {
                    override fun onNext(signedMessageAndPublicKey: SignedMessageAndPublicKey) {
                        super.onNext(signedMessageAndPublicKey)

                        if(lockModel==null)lockModel = LockModel()
                        lockModel?.signedMessage =(signedMessageAndPublicKey.signed_message)
                        lockModel?.publicKey  = (signedMessageAndPublicKey.public_key)
                        lockModel?.userId = (ride?.bike_bike_fleet_key)
                        lockModel?.macId = (ride?.bike_mac_id)


                        view?.onSignedMessagePublicKeySuccess(
                            signedMessageAndPublicKey.signed_message,
                            signedMessageAndPublicKey.public_key
                        )
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (e is SocketTimeoutException || e is UnknownHostException) {
                            getLock()
                        } else {
                            view?.onSignedMessagePublicKeyFailure()
                        }
                    }
                })
        )
    }

    open fun saveLock() {
        subscriptions.add(
            saveLockUseCase
                .withLock(lockModelMapper.mapOut(connectedLock))
                .execute(object : RxObserver<Lock>(view) {
                    override fun onNext(lock: Lock) {
                        super.onNext(lock)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                    }
                })
        )
    }

    open fun getLock() {
        subscriptions.add(
            getLockUseCase
                .execute(object : RxObserver<Lock>(view) {
                    override fun onNext(lock: Lock) {
                        super.onNext(lock)
                        if (lock != null && lock.signedMessage != null && lock.publicKey != null) {

                            if(lockModel==null)lockModel = LockModel()
                            lockModel?.signedMessage =(lock.signedMessage)
                            lockModel?.publicKey  = (lock.publicKey)
                            lockModel?.userId = (ride?.bike_bike_fleet_key)
                            lockModel?.macId = (ride?.bike_mac_id)

                            view?.onSignedMessagePublicKeySuccess(
                                lock.signedMessage,
                                lock.publicKey
                            )
                        } else {
                            view?.onSignedMessagePublicKeyFailure()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onSignedMessagePublicKeyFailure()
                    }
                })
        )
    }


    open fun disconnectAllLocks() {
        subscriptions.add(
            disconnectAllLockUseCase
                .withLockVendor(getLockVendor())
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        connectTo()
                    }

                    override fun onError(throwable: Throwable) {
                        super.onError(throwable)
                        if (throwable is BluetoothException) {
                            val exception = throwable as BluetoothException?
                            if (exception != null) {
                                if (exception.status != null) {
                                    if (exception.status == BluetoothException.Status.BLUETOOTH_DISABLED) {
                                        view?.requestEnableBluetooth()
                                    }
                                }
                            }
                        } else {
                            connectTo()
                        }
                    }
                })
        )
    }

    open fun disconnectAllLocksAfterEndingRide() {
        onDestroy()
        subscriptions.add(
            disconnectAllLockUseCase
                .withLockVendor(getLockVendor())
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        view?.onLockDisconnectedAfterEndingRide()
                    }

                    override fun onError(throwable: Throwable) {
                        super.onError(throwable)
                        view?.onLockDisconnectedAfterEndingRide()
                    }
                })
        )
    }


    fun connectTo() {
        Log.e("BaseBleFragPres","connectTo")
        if (connectToLastSubscription != null) connectToLastSubscription?.dispose()
        if (connectToSubscription != null) connectToSubscription?.dispose()


        isAccessDeniedForEllipse = false
        subscriptions.add(connectToLockUseCase
            .withLockVendor(getLockVendor())
            .execute(
                lockModelMapper.mapOut(lockModel),
                object : RxObserver<Lock.Connection.Status>(view, false) {
                    override fun onStart() {
                        super.onStart()
                        //connectionState = BikeDirectionFragmentPresenter.ConnectionState.CONNECTING;
//                    view?.showConnecting()
                    }

                    override fun onComplete() {
                        super.onComplete()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        Log.e("BaseBleFragPres","connectTo:::onError"+e.message)
                        if (e is BluetoothException) {
                            if (e.status != null) {
                                if (e.status == BluetoothException.Status.DEVICE_NOT_FOUND) {
                                    view?.onLockNotFound()
                                } else if (e.status == BluetoothException.Status.BLUETOOTH_DISABLED) {
                                    view?.requestEnableBluetooth()
                                }
                            }
                        }
                    }

                    override fun onNext(state: Lock.Connection.Status) {
                        Log.e("BaseBleFragPres",state.toString())
                        if (state === Lock.Connection.Status.OWNER_VERIFIED || state === Lock.Connection.Status.GUEST_VERIFIED) {
                            super.onNext(state)
                            Log.e("BaseBleFragPres","OWNER_VERIFIEDDDDDDDDDDDDDDDD")
                            connectionState = ConnectionState.CONNECTED
                            view?.onLockConnected(lockModelMapper.mapIn(state.lock).also({
                                connectedLock = it
                                saveLock()
                            }))

                        } else if (state === Lock.Connection.Status.DEVICE_FOUND) {
                            view?.showConnectingAsDeviceFound()
                        } else if (state.equals(Lock.Connection.Status.DISCONNECTED)) {
                            connectionState = ConnectionState.DISCONNECTED
                            if (!isAccessDeniedForEllipse) {
                                view?.onLockConnectionFailed()
                            }
                            stopLocationTrackInActiveTripService()
                        } else if (state.equals(Lock.Connection.Status.ACCESS_DENIED)) {
                            isAccessDeniedForEllipse = true
                            connectionState = ConnectionState.CONNECTION_FAIL
                            logCustomException(Throwable("Acess denied: " + lockModel?.macId))
                            view?.onLockConnectionAccessDenied()
                            stopLocationTrackInActiveTripService()
                        }
                    }
                }).also { connectToSubscription = it }
        )
    }


    private fun handleConnectionException(e: Throwable) {
        if (e is ConnectionException) {
            val connectionException = e
            if (connectionException != null && connectionException.type == ConnectionException.Type.CONNECTION_NOT_FOUND && view != null) {
                resetSetPositionVariables()
                view?.onLockConnectionFailed()
            }
        }
    }

    private fun resetSetPositionVariables() {
        subscribeToSetPositionTimer(false)
        lastPositionRequested = null
        setPositionRequest = false
        setPositionRetry = 0
    }

    fun observeLockPosition(lockModel: LockModel) {
        if (positionSubscription != null) positionSubscription?.dispose()
        subscriptions.add(observeLockPositionUseCase
            .withLockVendor(getLockVendor())
            .forLock(lockModelMapper.mapOut(lockModel))
            .execute(object : RxObserver<Lock.Hardware.Position>() {
                override fun onNext(newPosition: Lock.Hardware.Position) {
                    lockPosition = newPosition
                    if (lockPosition === Lock.Hardware.Position.INVALID || lockPosition === Lock.Hardware.Position.BETWEEN_LOCKED_AND_UNLOCKED) {
                        shackleJam = true
                        if (!isJammingUpdated) {
                            isJammingUpdated = true
                            resetSetPositionVariables()
                            subscribeToSetPositionTimer(false)  // stop trying
                            startLockPositionError()
                            updateBikeMetaData()
                        }
                    } else {
                        setPosition(lockPosition!!)
                    }
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    handleConnectionException(e)
                }
            }).also { positionSubscription = it }
        )
    }


    open fun observeConnectionState(lockModel: LockModel) {

        if(getLockVendor()!=UseCase.LockVendor.ELLIPSE)
            return

        if (connectionSubscription != null) connectionSubscription?.dispose()
        subscriptions.add(observeConnectionStateUseCase
            .forLock(lockModelMapper.mapOut(lockModel))
            .execute(object : RxObserver<Lock.Connection.Status>() {
                override fun onNext(status: Lock.Connection.Status) {
                    super.onNext(status)
                    if (status === Lock.Connection.Status.DISCONNECTED) {
                        connectionState = ConnectionState.DISCONNECTED
                    } else if (status === Lock.Connection.Status.OWNER_VERIFIED || status === Lock.Connection.Status.GUEST_VERIFIED) {
                        isBikeMetaDataUpdated = false
                        subscribeToFirmwareTimer(lockModel, true)
                    }
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    handleConnectionException(e)
                }
            }).also { connectionSubscription = it }
        )
    }


    fun observeHardwareState(lockModel: LockModel) {

        if (hardwareSubscription != null) {
            hardwareSubscription?.dispose()
            hardwareSubscription = null
        }
        subscriptions.add(observeHardwareStateUseCase
            .withLockVendor(getLockVendor())
            .forLock(lockModelMapper.mapOut(lockModel))
            .execute(object : RxObserver<Lock.Hardware.State>() {
                override fun onNext(state: Lock.Hardware.State) {

                    super.onNext(state)
                    lock_battery = state.batteryLevel
                    lockPosition = state.position
                    if (lockPosition === Lock.Hardware.Position.INVALID || lockPosition === Lock.Hardware.Position.BETWEEN_LOCKED_AND_UNLOCKED) { // observeLockPosition is handling this as invalid is received in that only.
                    } else {
                        setPosition(lockPosition!!)
                    }
                    updateBikeMetaData()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    handleConnectionException(e)
                }
            }).also { hardwareSubscription = it }
        )
    }

    fun startLockPositionError(){
        subscribeToShowLockPositionError(false)
        subscribeToShowLockPositionError(true)
    }

    fun stopLockPositionError(){
        subscribeToShowLockPositionError(false)
    }

    @Synchronized
    fun subscribeToShowLockPositionError(active:Boolean){
        if (active) {
            if (setPositionErrorTimerSubscription == null) {
                setPositionErrorTimerSubscription = Observable.timer(
                    MILLISECONDS_FOR_SET_POSITION_ERROR_TIMER.toLong(),
                    TimeUnit.MILLISECONDS
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        stopLockPositionError()
                        if(lockPosition != Lock.Hardware.Position.LOCKED){
                            view?.showInvalidLockPositionError()
                        }
                    }) { throwable ->
                        stopLockPositionError()
                    }
            }
        } else {
            if (setPositionErrorTimerSubscription != null) {
                setPositionErrorTimerSubscription?.dispose()
                setPositionErrorTimerSubscription = null
            }
        }
    }

    @Synchronized
    fun subscribeToSetPositionTimer(active: Boolean) {
        if (active) {
            if (setPositionTimerSubscription == null) {
                setPositionTimerSubscription = Observable.timer(
                    MILLISECONDS_FOR_SET_POSITION_TIMER.toLong(),
                    TimeUnit.MILLISECONDS
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        subscribeToSetPositionTimer(false)
                        if (lastPositionRequested != null) {
                            setPosition(
                                if (lastPositionRequested === Lock.Hardware.Position.LOCKED) true else false,
                                true
                            )
                        } else {
                            resetSetPositionVariables()
                            if (view != null) {
                                view?.onSetPositionFailure()
                            }
                        }
                    }) { throwable ->
                        if (throwable != null && throwable.localizedMessage != null) resetSetPositionVariables()
                        if (view != null) {
                            view?.onSetPositionFailure()
                        }
                    }
            }
        } else {
            if (setPositionTimerSubscription != null) {
                setPositionTimerSubscription?.dispose()
                setPositionTimerSubscription = null
            }
        }
    }

    fun setPosition(){


        Log.e("BLEBASE","SET POSITION ----------------------- 1");

        if(isIotModule()==HardwareType.IOT_ONLY){
            lockUnlockIotBike()
            return
        }

        if(lockModel==null){
            getSignedMessagePublicKey()
            return
        }
        var newPosition = true
        if (lockPosition != null) {
            newPosition = if(lockPosition === Lock.Hardware.Position.LOCKED)  false else true
        }

        if(!newPosition) view?.isEndRidePossible(newPosition)
        if(newPosition && getLockVendor()==UseCase.LockVendor.AXA) view?.lockGuidancePopup(true)


        lockPosition = null
        setPosition(newPosition, false);
    }

    open fun setPosition(position: Boolean, isRetry: Boolean) {
        if (connectionState === ConnectionState.DISCONNECTED) {
            getSignedMessagePublicKey()
            return
        }

        if (getLockVendor()==UseCase.LockVendor.ELLIPSE && !isRetry) {
            subscribeToSetPositionTimer(false)
            subscribeToSetPositionTimer(true)
            setPositionRetry = 0
            setPositionRequest = true
            lastPositionRequested =
                if (position) Lock.Hardware.Position.LOCKED else Lock.Hardware.Position.UNLOCKED
        } else if(getLockVendor()==UseCase.LockVendor.ELLIPSE && isRetry) {
            setPositionRetry++
            if (setPositionRetry < MAX_SET_POSITION_RETRY ) {
                subscribeToSetPositionTimer(false)
                subscribeToSetPositionTimer(true)
            } else {
                resetSetPositionVariables()
                view?.onSetPositionFailure()
                return
            }
        }else{
            resetSetPositionVariables()
        }

        if (setPositionSubscription != null) {
            setPositionSubscription?.dispose()
        }
        setPositionSubscription = setLockPositionUseCase
            .withLockVendor(getLockVendor())
            .withState(position)
            .forLock(lockModelMapper.mapOut(lockModel))
            .withFleetId(ride?.bike_fleet_id)
            .execute(object : RxObserver<Boolean>(view) {
                override fun onNext(status: Boolean) {
                    super.onNext(status)
                    if(status && getLockVendor()==UseCase.LockVendor.AXA && connectedLock!=null && connectedLock?.publicKey!=null){
                        connectedLock?.publicKey = connectedLock?.publicKey!!.substringAfter("-")
                        saveLock()
                    }
                    view?.onSetPositionStatus(status)
                    handleTapkeySetPositionIfApplicable(status)
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onSetPositionStatus(false)
                    handleConnectionException(e)
                    handleTapkeySetPositionIfApplicable(false)
                }
            })
    }


    private fun setPosition(position: Lock.Hardware.Position) {
        isJammingUpdated = false
        shackleJam = false
        if(position == Lock.Hardware.Position.UNLOCKED)ioTChangedFromUnlockToLock=true
        if(position == Lock.Hardware.Position.LOCKED)ioTChangedFromlockToUnLock=true
        if (lastPositionRequested != null && setPositionRequest && lastPositionRequested == position ||
            lastPositionRequested == null
        ) {
            resetSetPositionVariables()
            view?.showLockPositionSuccess(position)
        }
    }


    @Synchronized
    open fun subscribeToFirmwareTimer(lockModel: LockModel?, active: Boolean) {
        if (active) {
            if (getFirmwareTimerSubscription == null) {
                getFirmwareTimerSubscription = Observable.timer(
                    MILLISECONDS_FOR_FIRMWARE_TIMER.toLong(),
                    TimeUnit.MILLISECONDS
                )
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        subscribeToFirmwareTimer(null, false)
                        getLockFirmwareVersion(lockModel!!)
                    }) { }
            }
        } else {
            if (getFirmwareTimerSubscription != null) {
                getFirmwareTimerSubscription?.dispose()
                getFirmwareTimerSubscription = null
            }
        }
    }

    open fun getLockFirmwareVersion(lockModel: LockModel) {
        if (getFirmwareVersionSubscription != null) getFirmwareVersionSubscription?.dispose()
        subscriptions.add(getLockFirmwareVersionUseCase
            .forLock(lockModelMapper.mapOut(lockModel))
            .execute(object : RxObserver<String>() {
                override fun onNext(version: String) {
                    if (version != null) {
                        firmwareVersion = version
                        updateBikeMetaData()
                    }
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    handleConnectionException(e)
                }
            }).also { getFirmwareVersionSubscription = it }
        )
    }

    private fun updateBikeMetaData() {
        if (getLockVendor()!=UseCase.LockVendor.ELLIPSE || lock_battery == null || firmwareVersion == null || isBikeMetaDataUpdated) {
            return
        }
        subscriptions.add(
            updateBikeMetaDatUseCase
                .withFirmWare(firmwareVersion)
                .withShackleJamStatus(shackleJam)
                .withLockBattery(lock_battery)
                .withBikeBattery(-1)
                .forBike(ride?.bikeId!!)
                .execute(object : RxObserver<Boolean>() {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        isBikeMetaDataUpdated = true
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                    }
                })
        )
    }


    open fun startActiveTripService() {
        if (startActiveTripSubscription != null) startActiveTripSubscription?.dispose()
        subscriptions.add(startActiveTripUseCase
            .withTripId(ride?.rideId!!)
            .withTitle(serviceNotificationTitle!!)
            .execute(object : RxObserver<UpdateTripData>(view) {
                override fun onNext(updateTripData: UpdateTripData) {
                    super.onNext(updateTripData)
                    view?.showActiveTripData(updateTripData)
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    if(e.message!=null && "TRIP_ENDED".equals(e.message) && !isEndRideActivityLaunched){
                        doNeedFulWhenTripEndedFromBackend()
                    }
                }
            }).also({ startActiveTripSubscription = it })
        )
    }


    open fun stopLocationTrackInActiveTripService() {
        if (stopLocationTrackInActiveTripSubscription != null) {
            stopLocationTrackInActiveTripSubscription?.dispose()
        }
        subscriptions.add(stopLocationTrackInActiveTripUseCase
            .execute(object : RxObserver<Boolean>(view) {
                override fun onNext(aVoid: Boolean) {
                    super.onNext(aVoid)
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            }).also { stopLocationTrackInActiveTripSubscription = it }
        )
    }

    open fun startLocationTrackInActiveTripService() {
        if (startLocationTrackInActiveTripSubscription != null) {
            startLocationTrackInActiveTripSubscription?.dispose()
        }
        subscriptions.add(startLocationTrackInActiveTripUseCase
            .withLock(lockModelMapper.mapOut(lockModel))
            .execute(object : RxObserver<Boolean>(view) {
                override fun onNext(aVoid: Boolean) {
                    super.onNext(aVoid)
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            }).also({ startLocationTrackInActiveTripSubscription = it })
        )
    }


    open fun stopActiveTripService() {
        subscriptions.add(
            stopActiveTripUseCase
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(aVoid: Boolean) {
                        super.onNext(aVoid)
                        disconnectAllLocksAfterEndingRide()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        disconnectAllLocksAfterEndingRide()
                    }
                })
        )
    }

    open fun stopGetTripDetailsThreadIfApplicable() {
        if (stopGetTripDetailsThreadIfApplicableUseCase == null) return
        stopGetTripDetailsThreadIfApplicableUseCase
            .execute(object : RxObserver<Boolean>(view) {
                override fun onNext(aVoid: Boolean) {
                    super.onNext(aVoid)
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            })
    }


    fun isIotModule():HardwareType{
        if(ride?.controllers!=null && ride?.controllers?.size!!>0){
            for(controller in ride?.controllers!!){
                if(!TextUtils.isEmpty(controller.device_type) && controller.device_type.equals("iot",true)){
                    return if(TextUtils.isEmpty(ride?.bike_mac_id)) HardwareType.IOT_ONLY else HardwareType.ELLIPSE_IOT_COMBINE
                }else if(!TextUtils.isEmpty(controller.vendor) && controller.vendor.equals("Manual Lock",true)){
                    return HardwareType.MANUAL_LOCK
                }
            }
        }
        return if(ride?.bike_mac_id==null) HardwareType.NONE else HardwareType.ELLIPSE_ONLY
    }

    fun getLockVendor(): UseCase.LockVendor {
        if(ride?.controllers!=null && ride?.controllers?.size!!>0){
            for(controller in ride?.controllers!!){
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

    fun provideApplicableQRCode():String?{
        if(ride?.qr_code_id!=null){
            return ride?.qr_code_id
        }else if(provideIoTQRCodeOrNull()!=null){
            return provideIoTQRCodeOrNull()
        }else if(provideAdapterQRCodeOrNull()!=null){
            return provideAdapterQRCodeOrNull()
        }else{
            return null
        }
    }

    fun provideIoTQRCodeOrNull():String?{
        if (ride?.controllers != null && ride?.controllers?.size!! > 0) {
            for (controller in ride?.controllers!!) {
                if (!TextUtils.isEmpty(controller.device_type) && controller.device_type.equals(
                        "iot",
                        true
                    )
                ) {
                    return controller.qr_code
                }
            }
        }
        return null
    }

    fun provideAdapterQRCodeOrNull():String?{
        if(ride?.controllers!=null && ride?.controllers?.size!!>0){
            for(controller in ride?.controllers!!){
                if(!TextUtils.isEmpty(controller.device_type) && controller.device_type.equals("adapter",true)){
                    return controller.qr_code
                }
            }
        }
        return null
    }

    //// IOT BIKE LOCK UNLOCK ////

    fun requiredToShowIoTEllipsePopup():Boolean{
        if(isIotModule()==HardwareType.ELLIPSE_IOT_COMBINE && lockPosition == Lock.Hardware.Position.LOCKED && ioTChangedFromUnlockToLock) {
            ioTChangedFromUnlockToLock = false
            return true
        }
        return false
    }

    fun requiredToShowLockUnlockPopup():Boolean{
        return (isIotModule() == HardwareType.IOT_ONLY)
    }

    fun turnONIoTIfApplicable(){
        if(isIotModule()==HardwareType.ELLIPSE_IOT_COMBINE && ioTChangedFromlockToUnLock) {
            ioTChangedFromlockToUnLock = false
            unLockTurnONIotBike()
        }
    }

    fun turnOFFIoTIfApplicable(){
        if(isIotModule()==HardwareType.ELLIPSE_IOT_COMBINE) {
            lockTurnOFFIotBike()
        }
    }

    fun unLockTurnONIotBike(){
        iotLockPosition = true // it will be inverted in calling function
        lockUnlockIotBike()
    }

    fun lockTurnOFFIotBike(){
        iotLockPosition = false  // it will be invert in calling function
        lockUnlockIotBike()
    }

    fun getIoTBikeStatus(){
        if(kisiLock() || lockem()){
            informLockStatusAfterDelay()
            return
        }
        subscriptions.add(
            getV2IoTBikeStatusUseCase
                .withRide(ride)
                .withControllerId(provideIoTControllerId(ride))
                .execute(object : RxObserver<IoTBikeStatus>(view) {
                    override fun onNext(ioTBikeStatus: IoTBikeStatus) {
                        super.onNext(ioTBikeStatus)
                        iotLockPosition = ioTBikeStatus?.locked
                        if (doesIoTPositionShown()) view?.handleIoTLockPosition()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
//                        lockUnlockIotBike()
                    }
                })
        )
    }

    //// sentinel :start
    fun doNeedfulForSentinelBikeForActiveTrip(){
        if(isSentinel(ride)){
            startListeningToFirebasePushNotification()
        }
    }

    fun doNeedfulWhenSentinelIsOnline(){
        if(ioTLockUnlockInProgress) {
            iotLockPosition = true  // this will be inverted while making the call
            unlockSentinelBike(false)
        }
    }

    fun doNeedfulWhenSentinelIsLocked(){
        sentinelLockTimerDisposable?.dispose()
        ioTLockUnlockInProgress=false
        iotLockPosition=true
        if(doesIoTPositionShown()) view?.handleIoTLockPosition()
        sentinelLockGuidance(false)
        sentinelTapGuidance(false)

    }

    fun doNeedfulWhenSentinelIsUnLocked(){
        sentinelUnlockTimerDisposable?.dispose()
        ioTLockUnlockInProgress=false
        iotLockPosition=false
        if(doesIoTPositionShown()) view?.handleIoTLockPosition()
        sentinelTapGuidance(false)

    }

    fun doNeedfulWhenSentinelUnlockingTimerOver(){
        // TODO should check for getIoTStatus()
        ioTLockUnlockInProgress=false
//        iotLockPosition=true
//        if(doesIoTPositionShown()) view?.handleIoTLockPosition()
        getIoTBikeStatus()
        Observable.timer(1000,TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                sentinelLockGuidance(false)
                sentinelTapGuidance(false)
            },{
                sentinelLockGuidance(false)
                sentinelTapGuidance(false)
            })

    }

    fun doNeedfulWhenSentinelLockedIsPressed(){
        sentinelLockGuidance(true)
    }

    fun sentinelTapGuidance(active: Boolean){
        if(active){
            startUnlockTimer()
        }
        view?.sentinelTapGuidance(active)
    }

    fun doNeedfulWhenSentinelLockingTimerOver(){
        sentinelLockGuidance(false)
        ioTLockUnlockInProgress=false
        iotLockPosition=false
        if(doesIoTPositionShown())view?.handleIoTLockPosition()
    }

    fun sentinelLockGuidance(active: Boolean){
        if(active){
            sentinelLockTimerDisposable?.dispose()
            sentinelLockTimerDisposable = Observable.timer(120000,TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    doNeedfulWhenSentinelLockingTimerOver()
                },{
                    doNeedfulWhenSentinelLockingTimerOver()
                })
        }
        view?.sentinelLockGuidance(active)
    }

    fun startUnlockTimer(){
        sentinelUnlockTimerDisposable?.dispose()
        sentinelUnlockTimerDisposable = Observable.timer(120000,TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                doNeedfulWhenSentinelUnlockingTimerOver()
            },{
                doNeedfulWhenSentinelUnlockingTimerOver()
            })
    }

    //    fun mockUnLockSentinelNotification(){
//        Observable.timer(10000,TimeUnit.MILLISECONDS)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                doNeedfulWhenSentinelIsUnLocked()
//            },{
//                doNeedfulWhenSentinelIsUnLocked()
//            })
//    }
//
//    fun mockLockSentinelNotification(){
//        Observable.timer(10000,TimeUnit.MILLISECONDS)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                doNeedfulWhenSentinelIsLocked()
//            },{
//
//            })
//    }
    fun getSentinelUnlockStatus(){
        subscriptions.add(
            getV2IoTBikeStatusUseCase
                .withRide(ride)
                .withControllerId(RideUtil.provideIoTControllerId(ride))
                .execute(object : RxObserver<IoTBikeStatus>(view) {
                    override fun onNext(ioTBikeStatus: IoTBikeStatus) {
                        super.onNext(ioTBikeStatus)
                        iotLockPosition = ioTBikeStatus?.locked
                        if(iotLockPosition!=null && !iotLockPosition!! && doesIoTPositionShown()) {
                            ioTLockUnlockInProgress =false
                            view?.handleIoTLockPosition()
                        }else{
                            unlockSentinelBike(false)
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        unlockSentinelBike(false)
                    }
                })
        )
    }

    fun unlockSentinelBike(checkBikeStatus:Boolean){
        iotLockPosition = if(iotLockPosition==null) false else !iotLockPosition!!

        if(iotLockPosition!!){
            doNeedfulWhenSentinelLockedIsPressed()
            return
        }else if(checkBikeStatus){
            getSentinelUnlockStatus()
            return
        }

        subscriptions.add(
            unlockSentinelBikeUseCase
                .withBikeId(ride?.bikeId!!)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        startUnlockTimer()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)

                        if(e is HttpException && e.code() == 404){
                            sentinelTapGuidance(true)
                        }else{
//                            iotLockPosition = true
//                            if(doesIoTPositionShown())  view?.handleIoTLockPosition()
                            getIoTBikeStatus()
                            ioTLockUnlockInProgress=false
                        }
                    }
                })
        )
    }
    //// sentinel :end

    fun lockUnlockIotBike() {

        if(ioTLockUnlockInProgress) return

        ioTLockUnlockInProgress = true

        if(linkaLock()) view?.lockUnlockPopUp(true,if(iotLockPosition==null) false else !iotLockPosition!!)

        if(omniLock() && if(iotLockPosition==null) false else !iotLockPosition!!) view?.lockGuidancePopup(true)

        if(isSentinel(ride)) {
            unlockSentinelBike(true)
            return
        }

        if(lockem() && iotLockPosition!=null && !iotLockPosition!!){
            subscriptions.add(Observable.timer(1000,TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    iotLockPosition = false
                    ioTLockUnlockInProgress = false
                    view?.handleIoTLockPosition()
                },{
                    iotLockPosition = false
                    ioTLockUnlockInProgress = false
                    view?.handleIoTLockPosition()
                })
            )
            return
        }

        subscriptions.add(
            lockUnlockV2IotBikeUseCase
                .withRide(ride)
                .withLock(if(iotLockPosition==null) false else !iotLockPosition!!)
                .withControllerId(provideIoTControllerId(ride))
                .execute(object : RxObserver<IoTBikeLockUnlockCommandStatus>(view) {
                    override fun onNext(ioTBikeLockUnlockCommandStatus: IoTBikeLockUnlockCommandStatus) {
                        super.onNext(ioTBikeLockUnlockCommandStatus)
                        ioTLockUnlockInProgress = false
                        if(kisiLock() || lockem()) {
                            if(iotLockPosition==null) iotLockPosition=false else iotLockPosition = !iotLockPosition!!
                            if(doesIoTPositionShown())view?.handleIoTLockPosition()
                            informLockStatusAfterDelay()
                        }else if(linkaLock()){
                            subscribeToGetLinkaLockStatusForCommand(true,ioTBikeLockUnlockCommandStatus.command_id!!)   //TODO put real commandId
                        }else if(omniLock()){
                            if(iotLockPosition==null) iotLockPosition=false else iotLockPosition = !iotLockPosition!!
                            if(!iotLockPosition!!) view?.handleIoTLockPosition()
                        } else if(doesIoTPositionShown()){
                            if(iotLockPosition==null) iotLockPosition=false else iotLockPosition = !iotLockPosition!!
                            view?.handleIoTLockPosition()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        ioTLockUnlockInProgress =false
                        if(linkaLock()) view?.lockUnlockPopUp(false,null)
                        if(omniLock())view?.lockGuidancePopup(false)
                        if(doesIoTPositionShown())view?.handleIoTLockPosition()
                    }
                })
        )
    }

    fun informLockStatusAfterDelay(){
        subscriptions.add(Observable.timer(4000,TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                iotLockPosition = true
                if(doesIoTPositionShown()) view?.handleIoTLockPosition()
            },{
                iotLockPosition = true
                if(doesIoTPositionShown()) view?.handleIoTLockPosition()
            })
        )
    }

    fun doesIoTPositionShown():Boolean{
        return isIotModule()==HardwareType.IOT_ONLY
    }

    fun lockem():Boolean{
        if(ride?.controllers!=null && ride?.controllers?.size!!>0) {
            for (controller in ride?.controllers!!) {
                if (!TextUtils.isEmpty(controller.device_type) && controller.device_type.equals(
                        "iot",
                        true
                    ) &&
                    !TextUtils.isEmpty(controller.vendor) && controller.vendor.equals(
                        "ParcelHive",
                        true
                    )
                ) {
                    return true
                }
            }
        }
        return false
    }

    fun kisiLock():Boolean{
        if(ride?.controllers!=null && ride?.controllers?.size!!>0) {
            for (controller in ride?.controllers!!) {
                if (!TextUtils.isEmpty(controller.device_type) && controller.device_type.equals(
                        "iot",
                        true
                    ) &&
                    !TextUtils.isEmpty(controller.vendor) && controller.vendor.equals(
                        "kisi",
                        true
                    )
                ) {
                    return true
                }
            }
        }
        return false
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

    fun omniLock():Boolean{
        if(ride?.controllers!=null && ride?.controllers?.size!!>0) {
            for (controller in ride?.controllers!!) {
                if (!TextUtils.isEmpty(controller.device_type) && controller.device_type.equals(
                        "iot",
                        true
                    ) &&
                    !TextUtils.isEmpty(controller.vendor) && controller.vendor.equals("omni", true)
                ) {
                    return true
                }
            }
        }
        return false
    }


    private fun subscribeToGetLinkaLockStatusForCommand(commandId:String): Observable<Boolean> {
        return Observable.interval(
            GET_LINKA_LOCK_STATUS_INTERVAL_MILLIS.toLong(),
            TimeUnit.MILLISECONDS
        ).map {
            getLinkaIoTBikeStatusForCommand(commandId)
            true
        }
    }


    fun getLinkaIoTBikeStatusForCommand(commandId:String){
        subscriptions.add(
            getLinkaIoTBikeStatusUseCase
                .withCommandId(commandId)
                .withBikeId(ride?.bikeId!!)
                .execute(object : RxObserver<IoTBikeLockUnlockCommandStatus>(view) {
                    override fun onNext(ioTBikeLockUnlockCommandStatus: IoTBikeLockUnlockCommandStatus) {
                        super.onNext(ioTBikeLockUnlockCommandStatus)
                        if(ioTBikeLockUnlockCommandStatus.status==2){
                            subscribeToGetLinkaLockStatusForCommand(false,"")
                            view?.lockUnlockPopUp(false,null)
                            if(ioTBikeLockUnlockCommandStatus.status_desc.equals("ERROR_STALL",true)){
                                iotLockPosition = if(ioTBikeLockUnlockCommandStatus.command?.equals("LOCK",true)!!)false else true
                            }else{
                                iotLockPosition = if(ioTBikeLockUnlockCommandStatus.command?.equals("LOCK",true)!!)true else false
                            }
                            if(doesIoTPositionShown())view?.handleIoTLockPosition()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)

                    }
                })
        )
    }

    @Synchronized
    fun subscribeToGetLinkaLockStatusForCommand(active: Boolean,commandId:String) {
        cancelGetLinkaLockStatusSubscription()
        if (active) {
            if (getLinkaLockStatusSubscription == null) {
                getLinkaLockStatusSubscription = subscribeToGetLinkaLockStatusForCommand(commandId)
                    .subscribeOn(Schedulers.io())
                    .subscribeWith(object : DisposableObserver<Boolean>() {
                        override fun onComplete() {}
                        override fun onError(e: Throwable) {}
                        override fun onNext(success: Boolean) {}
                    })
            }
        }else{
            cancelGetLinkaLockStatusSubscription()
        }
    }

    fun cancelGetLinkaLockStatusSubscription() {
        if (getLinkaLockStatusSubscription != null) {
            getLinkaLockStatusSubscription!!.dispose()
            getLinkaLockStatusSubscription = null
        }
    }



    enum class ConnectionState {
        NOT_CONNECTED, CONNECTING, CONNECTED, DISCONNECTED, SCANNING, CONNECTION_FAIL
    }


    ///////// AXA :start
    ////AXA LOCK API: start ////
    fun getAxaLockKey(){
        subscriptions.add(getAxaLockKeyUseCase
            .withLockId(axaLockId!!)
            .execute(object : RxObserver<AxaKey>() {
                override fun onNext(axaKey: AxaKey) {
                    Log.e("AxeKey is ",axaKey.ekey!!)
                    Log.e("AxePassKey is ",axaKey.passkey!!)
                    mEKeyAscii = axaKey.ekey
                    mOTPKeyparts = axaKey.passkey?.split("-".toRegex())?.toTypedArray()!!
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            }))
    }
    ///////// AXA :end


    ///////// HUB DOCK :start



    fun doNeedfulIfKuhmuteIsUndockedFromPopUp(){
        if(isItValidKuhmute()){
            when (isIotModule()) {
                HardwareType.IOT_ONLY -> {
                    unDockBike()
                    unLockTurnONIotBike()
                }
                HardwareType.ELLIPSE_ONLY -> {
                    unDockBike()
                    if(lockPosition!=Lock.Hardware.Position.LOCKED){
                        setPosition()
                    }
                }
                HardwareType.ELLIPSE_IOT_COMBINE -> {
                    unDockBike()
                    unLockTurnONIotBike()
                    if(lockPosition!=Lock.Hardware.Position.LOCKED){
                        setPosition()
                    }
                }
                HardwareType.NONE->{
                    unDockBike()
                }
            }
        }
    }

    fun doNeedfulIfKuhmuteAfterStartRide(){
        if(isItValidKuhmute()){
//            when (isIotModule()) {
//                HardwareType.NONE, HardwareType.ELLIPSE_ONLY->{
                    unDockBike()
//                }
//            }
        }
    }

    fun doNeedfulIfKuhmuteBeforeEndRide(){
        if(isItValidKuhmute()) {
            when (isIotModule()) {
                HardwareType.IOT_ONLY -> {
                    lockTurnOFFIotBike()
                }
                HardwareType.ELLIPSE_ONLY -> {
                    if(lockPosition!=Lock.Hardware.Position.LOCKED){
                        setPosition()
                    }
                }
                HardwareType.ELLIPSE_IOT_COMBINE -> {
                    lockTurnOFFIotBike()
                    if(lockPosition!=Lock.Hardware.Position.LOCKED){
                        setPosition()
                    }
                }
            }
        }
    }

    fun isItValidKuhmute():Boolean{
        if(ride?.controllers!=null && ride?.controllers?.size!!>0){
            for(controller in ride?.controllers!!){
                if(!TextUtils.isEmpty(controller.vendor) && controller.vendor.equals("Kuhmute",true) &&
                    !TextUtils.isEmpty(controller.device_type) && controller.device_type.equals("adapter",true) &&
                    !TextUtils.isEmpty(controller.key) ){
                    dockHubUUID = ride?.bike_uuid
                    dockHubType = controller.vendor?.lowercase()
                    return true
                }else if(!TextUtils.isEmpty(controller.vendor) && controller.vendor.equals("duckt",true) &&
                    !TextUtils.isEmpty(controller.device_type) && controller.device_type.equals("adapter",true) &&
                    !TextUtils.isEmpty(controller.key) ){
                    dockHubUUID = ride?.bike_uuid
                    dockHubType = controller.vendor?.lowercase()
                    return true
                }else if(!TextUtils.isEmpty(controller.vendor) && controller.vendor.equals("dck-mob-e",true) &&
                    !TextUtils.isEmpty(controller.device_type) && controller.device_type.equals("adapter",true) &&
                    !TextUtils.isEmpty(controller.key) ){
                    dockHubUUID = ride?.bike_uuid
                    dockHubType = controller.vendor?.lowercase()
                    return true
                }
            }
        }
        return false
    }

    fun unDockBike(){
        if(TextUtils.isEmpty(dockHubUUID))return
        subscriptions.add(unDockBikeUseCase
            .withUUID(dockHubUUID!!)
            .withHubType(dockHubType!!)
            .execute(object : RxObserver<Boolean>() {
                override fun onNext(status:Boolean) {

                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            }))
    }

    fun startListeningToFirebasePushNotification(){
        subscriptions.add(AndroidBus.firebasePushNotificationPublishSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                {
                    if(it.clickAction.equals(docking,true) || it.clickAction.equals(docked,true)) {
                        view?.showDockHubDockingNotification(it)
                    }else if(it.clickAction.equals(locked,true) && omniLock() ){
                        iotLockPosition = true
                        view?.handleIoTLockPosition()
                    }else if(it.clickAction.equals(sentinel_lock_online)){
                        doNeedfulWhenSentinelIsOnline()
                    }else if(it.clickAction.equals(sentinel_lock_closed)){
                        doNeedfulWhenSentinelIsLocked()
                    }else if(it.clickAction.equals(sentinel_lock_opened)){
                        doNeedfulWhenSentinelIsUnLocked()
                    }
                },{
                    Log.e("","")

                }
            ))
    }

    fun isDockHubBikeDocked():Boolean{
        return isItValidKuhmute() && ride!=null && ride?.dock_hub_bike_docked!=null && ride?.dock_hub_bike_docked!!
    }

    ///////// HUB DOCK :end


    ///////// NO LOCK Condition handled :start

    fun handleNoLockForBikeCondition(){
        if(isItValidKuhmute()){
            view?.showAdapterScanAndCancelBtns()
        }
    }

    ///////// NO LOCK Condition handled :end


    //// trip ended from backend :start
    fun doNeedFulWhenTripEndedFromBackend(){
        getRideSummaryForEndingRide(ride?.rideId!!)
    }

    //// manual lock :start
    fun isManualLock():Boolean{
        return isIotModule() == HardwareType.MANUAL_LOCK
    }

    fun provideManualLockCode():String?{
        if(ride?.controllers!=null && ride?.controllers?.size!!>0){
            for(controller in ride?.controllers!!){
                if(!TextUtils.isEmpty(controller.vendor) && controller.vendor.equals("Manual Lock",true)){
                    return controller.key
                }
            }
        }
        return null
    }

    //// manual lock :end


    //// EndRideLaunch :start

    fun doNeedfulWhenEndRideActivityLaunched(){
        isEndRideActivityLaunched = true
        pauseUpdateTrip(true)
    }

    fun doNeedfulWhenEndRideActivityFinished(){
        isEndRideActivityLaunched = false
        pauseUpdateTrip(false)
    }

    fun endRideActivityLaunched():Boolean = isEndRideActivityLaunched

    fun pauseUpdateTrip(active: Boolean){
        pauseUpdateTripUseCase
            .withActive(active)
            .execute(object : RxObserver<Boolean>(view) {
                override fun onNext(status: Boolean) {
                    super.onNext(status)

                }

                override fun onError(e: Throwable) {
                    super.onError(e)

                }
            })
    }

    //// EndRideLaunch :end


    //// Handle Tapkey :start
    fun handleTapkeySetPositionIfApplicable(status:Boolean){
        if((isIotModule()==HardwareType.ELLIPSE_IOT_COMBINE || isIotModule() == HardwareType.ELLIPSE_ONLY) &&
            (getLockVendor() == UseCase.LockVendor.TAPKEY || getLockVendor() == UseCase.LockVendor.SAS || getLockVendor() == UseCase.LockVendor.PSLOCK)
        ) {
            lockPosition = if (status) Lock.Hardware.Position.UNLOCKED else Lock.Hardware.Position.LOCKED
            view?.showTapkeyUnlockSuccessFailurePopup(status)
            setPosition(lockPosition!!)
        }
    }

    fun handleTapkeyAfterStartRideIfApplicable(){
        if((isIotModule()==HardwareType.ELLIPSE_IOT_COMBINE || isIotModule() == HardwareType.ELLIPSE_ONLY) &&
            getLockVendor() == UseCase.LockVendor.TAPKEY
        ) {
            connectToLastLockedLock()
        }
    }
    //// Handle Tapkey :end



    //// localnotification :start
    fun startListeningToLocalNotification(){
        subscriptions.add(AndroidBus.stringPublishSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                {
                    if (it.equals(RESERVATION_TIMER_OVER_NOTIFICATION_TYPE)) {
                        view?.showReservationTimerOverPopUp()
                    }
                },{

                }
            )
        )
    }
    //// localnotification :end



    ///// BLE Permissions :start
    fun handleLockConnectionForBLE(){
        view?.checkBLEPermissions()
    }
    open fun startLockConnectionForBLE(){

    }
    //// BLE Permissions :end
}