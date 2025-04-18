package com.lattis.data.repository.implementation.api

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.lattis.data.net.activetrip.ActiveTripService
import com.lattis.domain.models.UpdateTripData
import com.lattis.domain.repository.ActiveTripRepository
import com.lattis.domain.models.Lock
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.functions.Function
import javax.inject.Inject

class ActiveTripRepositoryImp @Inject constructor(
    val context: Context
):ActiveTripRepository{
    private var activeTripService: ActiveTripService? = null
    private var serviceConnection: ServiceConnection? = null
    private var serviceNotificationTitle:String?=null


    init {

    }

    override fun stopActiveTripService(): Observable<Boolean> {
        if(activeTripService!=null){
                activeTripService?.stopService()
                activeTripService = null
        }
        return Observable.just(true)
    }

    override fun startActiveTripService(trip_id: Int,title:String): Observable<UpdateTripData> {
        serviceNotificationTitle = title
        return getActiveTripService().flatMap{ activeTripService ->
            activeTripService?.startActiveTrip(
                trip_id
            )
        }
    }

    override fun startLocationTracking(lock: Lock): Observable<Boolean> {
        return getActiveTripService().flatMap{ activeTripService ->
            activeTripService?.startLocationTracking(
                lock
            )
        }
    }

    override fun stopLocationTracking(): Observable<Boolean> {
        if (activeTripService != null) {
            activeTripService?.stopLocationTracking(true)
        }
        return Observable.just(true)

    }

    override fun stopGetTripDetailsThreadIfApplicable(): Observable<Boolean> {
        if (activeTripService != null) {
            activeTripService!!.cancelGetTripDetailsSubscription()
        }
        return Observable.just(true)
    }

    private fun getActiveTripService(): Observable<ActiveTripService?> {
        return Observable.create { emitter: ObservableEmitter<ActiveTripService?> ->
            if (activeTripService == null) {
                serviceConnection = object : ServiceConnection {
                    override fun onServiceConnected(
                        name: ComponentName,
                        binder: IBinder
                    ) {
                        activeTripService = (binder as ActiveTripService.UpdateTripServiceBinder).service
                        activeTripService?.startInForeground(serviceConnection,serviceNotificationTitle)
                        emitter.onNext(activeTripService!!)
                    }

                    override fun onServiceDisconnected(name: ComponentName) {
                        emitter.onComplete()
                        activeTripService = null
                    }
                }
                context.bindService(
                    Intent(context, ActiveTripService::class.java),
                    serviceConnection!!, Context.BIND_AUTO_CREATE
                )
            } else {
                emitter.onNext(activeTripService!!)
            }
        }
    }


    override fun pauseUpdateTrip(active:Boolean): Observable<Boolean> {
        return getActiveTripService().flatMap{ activeTripService ->
            activeTripService?.pauseUpdateTrip(active)
            Observable.just(true)
        }
    }

}