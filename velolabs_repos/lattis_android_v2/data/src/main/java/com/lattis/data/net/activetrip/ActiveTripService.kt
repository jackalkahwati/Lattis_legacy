package com.lattis.data.net.activetrip

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.lattis.data.R
import com.lattis.data.utils.GeneralHelper.getPendingIntentFlags
import com.lattis.domain.models.Location
import com.lattis.domain.models.Lock
import com.lattis.domain.models.RideSummary
import com.lattis.domain.models.UpdateTripData
import com.lattis.domain.repository.BluetoothRepository
import com.lattis.domain.repository.LocationRepository
import com.lattis.domain.repository.RideRepository
import dagger.android.AndroidInjection
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ActiveTripService : Service() {



    @Inject
    lateinit var context:Context

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var rideRepository: RideRepository

    @Inject
    lateinit var bluetoothRepository: BluetoothRepository

    private val TAG = ActiveTripService::class.java.name
    private var trip_id = 0
    private var lock: Lock? = null
    private val updateTripDataBehaviorSubject = PublishSubject.create<UpdateTripData>()
    private var getTripDetailsSubscription: Disposable? = null
    private val GET_TRIP_DETAILS_REFRESH_INTERVAL_MILLIS = 10000


    private val mBinder: IBinder = UpdateTripServiceBinder()
    private var currentUserLocation: Location? = null
    private var lastLocation: Location? = null
    private var locationSubscription: Disposable? = null
    private var positionSubscription: Disposable? = null
    private var connectionSubscription: Disposable? = null
    private val lastPosition: Lock.Hardware.Position? = null
    private var sendPosition = false
    private val lockPositionIntValue = 0
    private val connectionState: Lock.Connection.Status? = null
    protected var subscriptions = CompositeDisposable()
    private var notificationManager: NotificationManager? = null
    private val updateTripServiceNotificationId = "update.trip.service"
    private var serviceConnection: ServiceConnection? = null
    private var unbinded = false
    private var TRIP_ENDED = "TRIP_ENDED"
    private var pauseUpdateTrip = false



    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onUnbind(intent: Intent): Boolean {
        return true
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    inner class UpdateTripServiceBinder : Binder() {
        val service: ActiveTripService
            get() = this@ActiveTripService
    }

    fun startActiveTrip(trip_id: Int): PublishSubject<UpdateTripData> {
        this.trip_id = trip_id
        startReleventUpdateDataThread()
        return updateTripDataBehaviorSubject
    }

    private fun startReleventUpdateDataThread() {
        if (locationSubscription == null) {
            startGetTripDetails()
        }
    }

    fun startLocationTracking(lock: Lock?): Observable<Boolean> {
        this.lock = lock
        if (trip_id != 0) {
            stopGetTripDetails()
            requestLocationUpdates()
        }
        observeConnectionState()
        return Observable.just(true)
    }

    fun stopLocationTracking(needToStartGetTripDetails:Boolean): Observable<Boolean> {
        requestStopLocationUpdates()
        requestStopLockPositionAndConnectionUpdates()
        if(needToStartGetTripDetails) startGetTripDetails()
        return Observable.just(true)
    }

    fun startInForeground(
        serviceConnection: ServiceConnection?,
        serviceNotificationTitle:String?
    ) {
         val defaultServiceNotificationTitle="Lattis"

        this.serviceConnection = serviceConnection
        val `in` = Intent("updatetrip.service.notification.clicked")
        `in`.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        var pendingIntent:PendingIntent = PendingIntent.getActivity(this, 0, `in`, getPendingIntentFlags(false))


//        val pendingIntent =
//            PendingIntent.getActivity(this, 0, `in`, PendingIntent.FLAG_CANCEL_CURRENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                updateTripServiceNotificationId,
                "notify_update_trip",
                importance
            )
            notificationManager!!.createNotificationChannel(notificationChannel)
            val notification =
                NotificationCompat.Builder(this, updateTripServiceNotificationId)
                    .setContentTitle(if(serviceNotificationTitle!=null) serviceNotificationTitle else defaultServiceNotificationTitle )
                    .setContentText(if(serviceNotificationTitle!=null) serviceNotificationTitle + " app" else defaultServiceNotificationTitle + " app")
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build()
            startForeground(
                NOTIFICATION_ID_SERVICE_FOREGROUND,
                notification
            )
        } else {
            val notification =
                NotificationCompat.Builder(this)
                    .setContentTitle(if(serviceNotificationTitle!=null) serviceNotificationTitle else defaultServiceNotificationTitle)
                    .setContentText(if(serviceNotificationTitle!=null) serviceNotificationTitle + " app" else defaultServiceNotificationTitle + " app")
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build()
            startForeground(
                NOTIFICATION_ID_SERVICE_FOREGROUND,
                notification
            )
        }
        return
    }

    override fun onDestroy() {
        unbinded = true
        Log.e(TAG, "ActiveTripService is destroyed")
        stopEverything()
        super.onDestroy()
    }

    private fun stopEverything() {
        stopForeground(true)
        requestStopLocationUpdates()
        requestStopLockPositionAndConnectionUpdates()
        stopGetTripDetails()
        trip_id=0
        subscriptions.clear()
    }


    fun requestLocationUpdates() {
        requestStopLocationUpdates()
        locationSubscription = locationRepository.getLocationUpdates(false)
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe({location->
                currentUserLocation = location
                updateTrip()
            },{

            })
    }

    //        return 300;
    private val distanceinMeters: // results in meters
            Float
        private get() { //        return 300;
            val results = FloatArray(3)
            android.location.Location.distanceBetween(
                currentUserLocation!!.latitude, currentUserLocation!!.longitude,
                lastLocation!!.latitude, currentUserLocation!!.longitude, results
            )
            return results[0] // results in meters
        }

    fun requestStopLocationUpdates() {
        if (locationSubscription != null) {
            locationSubscription!!.dispose()
            locationSubscription = null
        }
    }

    fun requestStopLockPositionAndConnectionUpdates() {
        if (positionSubscription != null) {
            positionSubscription!!.dispose()
            positionSubscription = null
        }
        if (connectionSubscription != null) {
            connectionSubscription!!.dispose()
            connectionSubscription = null
        }
    }


    fun updateTrip() {
        if (trip_id == 0) {
            return
        }
        if(pauseUpdateTrip)return

        if (currentUserLocation == null) return

        subscriptions.add(
            rideRepository.updateRide(trip_id,steps)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe({updateTripData->
                    lastLocation = currentUserLocation
                    if (TextUtils.isEmpty(updateTripData.endDate)) {
                        updateTripDataBehaviorSubject.onNext(updateTripData)
                    } else {
                        updateTripDataBehaviorSubject.onError(Throwable(TRIP_ENDED))
                        stopService()
                    }
                },{
                    if(it is HttpException && it.code() == 404){
                        updateTripDataBehaviorSubject.onError(Throwable(TRIP_ENDED))
                        stopService()
                    }
                }))

    }

    open fun observeConnectionState() {
        if(lock==null){
            return
        }
        bluetoothRepository.observeLockConnectionState(lock!!)
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it === Lock.Connection.Status.DISCONNECTED) {
                    stopLocationTracking(false)
                }
            },{

            })
    }

    private fun getUpdateTripDataObject(rideSummary: RideSummary): UpdateTripData {

        return UpdateTripData(
                rideSummary.duration?.toDouble(),
                rideSummary.total?.toFloat(),
                rideSummary.currency,
            null,
            null
            )

    }

    val steps: Array<DoubleArray>
        get() = if (sendPosition) {
            sendPosition = false
            arrayOf(
                doubleArrayOf(
                    currentUserLocation!!.latitude,
                    currentUserLocation!!.longitude,
                    doubleTime,
                    lockPositionIntValue.toDouble()
                )
            )
        } else {
            arrayOf(
                doubleArrayOf(
                    currentUserLocation!!.latitude,
                    currentUserLocation!!.longitude,
                    doubleTime
                )
            )
        }

    val doubleTime: Double
        get() {
            val dte = Date()
            return dte.time.toDouble() / 1000
        }

    ////// Get Trip details for displaying trip cost when GPS tracking is OFF ////////////
    private fun startGetTripDetails() {
        subscribeToGetTripDetails(false)
        subscribeToGetTripDetails(true)
    }

    private fun stopGetTripDetails() {
        subscribeToGetTripDetails(false)
    }

    private fun subscribeToGetTripDetails(): Observable<Boolean> {
        return Observable.interval(
            GET_TRIP_DETAILS_REFRESH_INTERVAL_MILLIS.toLong(),
            TimeUnit.MILLISECONDS
        ).flatMap(requestGetTripDetails)
    }

    private val requestGetTripDetails: Function<Long, Observable<Boolean>> =
            Function<Long, Observable<Boolean>>() {
                blankUpdateTrip
                Observable.just(true)
        }

    @Synchronized
    fun subscribeToGetTripDetails(active: Boolean) {
        cancelGetTripDetailsSubscription()
        if (active) {
            if (getTripDetailsSubscription == null) {
                getTripDetailsSubscription = subscribeToGetTripDetails()
                    .subscribeOn(Schedulers.io())
                    .subscribeWith(object : DisposableObserver<Boolean>() {
                        override fun onComplete() {}
                        override fun onError(e: Throwable) {}
                        override fun onNext(success: Boolean) {}
                    })
            }
        }else{
            cancelGetTripDetailsSubscription()
        }
    }

    fun cancelGetTripDetailsSubscription() {
        if (getTripDetailsSubscription != null) {
            getTripDetailsSubscription!!.dispose()
            getTripDetailsSubscription = null
        }
    }

    val blankUpdateTrip: Unit
        get() {
            if(pauseUpdateTrip)return
            subscriptions.add(
                rideRepository.updateRide(trip_id, emptyArray())
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .subscribe({updateTripData->
                        lastLocation = currentUserLocation
                        if (TextUtils.isEmpty(updateTripData.endDate)) {
                            updateTripDataBehaviorSubject.onNext(updateTripData)
                        } else {
                            updateTripDataBehaviorSubject.onError(Throwable(TRIP_ENDED))
                            stopService()
                        }
                    },{
                        if(it is HttpException && it.code() == 404){
                            updateTripDataBehaviorSubject.onError(Throwable(TRIP_ENDED))
                            stopService()
                        }
                    }))
        }

    fun stopService() {
        stopEverything()
        if (context != null && serviceConnection != null && !unbinded) {
            context!!.unbindService(serviceConnection!!)
            serviceConnection=null
        }
    }

    companion object {
        private const val NOTIFICATION_ID_SERVICE_FOREGROUND = 10
    }


    fun pauseUpdateTrip(active:Boolean){
        pauseUpdateTrip = active
    }
}