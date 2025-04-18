package com.lattis.lattis.presentation.ride

import android.os.Bundle
import com.lattis.domain.models.RideSummary
import com.lattis.domain.models.UploadImage
import com.lattis.domain.usecase.lock.disconnect.DisconnectAllLockUseCase
import com.lattis.domain.usecase.ride.EndRideUseCase
import com.lattis.domain.usecase.updatetrip.StopActiveTripUseCase
import com.lattis.domain.usecase.uploadimage.UploadImageUseCase
import com.lattis.domain.models.Location
import com.lattis.lattis.presentation.base.activity.location.BaseLocationActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.FirebaseUtil
import io.reactivex.rxjava3.disposables.Disposable
import retrofit2.HttpException
import javax.inject.Inject

class EndRidePresenter @Inject constructor(
    val uploadImageUseCase: UploadImageUseCase,
    val endRideUseCase: EndRideUseCase,
    val stopActiveTripUseCase: StopActiveTripUseCase
) : BaseLocationActivityPresenter<EndRideView>() {

    private var imageURL:String?=null
    private var uploadImageSubscription: Disposable? = null
    private var endRideSubscription: Disposable? = null

    private var trip_id = 0
    private var parkingId = -1
    private var isDamageBike = false
    private var loadingString: String? = null
    private var lock_battery: Int? = null
    var forceEndRide = false
    var endRideSummary:RideSummary?=null

    enum class END_RIDE_MODE{
        BEGIN,
        END_RIDE_STARTED
    }


    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null) {
            trip_id = arguments.getInt(TRIP_ID)
            isDamageBike = arguments.getBoolean(BIKE_DAMAGE)
            if (arguments.containsKey(LATITUDE_END_RIDE_ID) && arguments.containsKey(
                    LONGITUDE_END_RIDE_ID
                )
            ) {
                val latitude =
                    arguments.getDouble(LATITUDE_END_RIDE_ID)
                val longitude =
                    arguments.getDouble(LONGITUDE_END_RIDE_ID)
                currentUserLocation = Location(latitude, longitude)
            }
            if (arguments.containsKey(PARKING_END_RIDE_ID)) {
                parkingId = arguments.getInt(PARKING_END_RIDE_ID)
            } else {
                parkingId = -1
            }
            if (arguments.containsKey(LOCK_BATTERY)) {
                lock_battery = arguments.getInt(LOCK_BATTERY)
            }
            if (arguments.containsKey(FORCE_END_RIDE_ID)) {
                forceEndRide = arguments.getBoolean(FORCE_END_RIDE_ID)
            }
            if (arguments.containsKey(END_RIDE_ID_LOADING_STRING)) {
                loadingString =
                    arguments.getString(END_RIDE_ID_LOADING_STRING)
            }
        }
    }

    fun checkForEndRide(){
        if(forceEndRide){
            view?.handleUIForceEndRide()
            endRide()
        }else{
            view?.handleUINotForceEndRide()
        }
    }



    fun uploadImage(filePath: String) {
        subscriptions.add(uploadImageUseCase
            .withFilePath(filePath)
            .withUploadType("parking")
            .execute(object : RxObserver<UploadImage>() {
                override fun onNext(uploadImage: UploadImage) {
                    super.onNext(uploadImage)
                    forceEndRide=true
                    imageURL = uploadImage.path
                    deleteCurrentPhotoPath()
                    view?.onUploadImageSuccess()
                }

                override fun onError(e: Throwable) {
                    view?.onUploadImageFailure()
                }
            }).also {
                uploadImageSubscription=it
            })
    }

    fun endRide() {
        if (currentUserLocation != null) {
            subscriptions.add(endRideUseCase
                .withTripId(trip_id)
                .withLocation(currentUserLocation)
                .withImageURL(imageURL)
                .withParkingId(parkingId)
                .withReportDamage(isDamageBike)
                .withLockBattery(lock_battery)
                .execute(object : RxObserver<RideSummary>(view) {
                    override fun onNext(rideSummary: RideSummary) {
                        super.onNext(rideSummary)
                        FirebaseUtil.instance?.addCustomEvent(
                            FirebaseUtil.endRideEventName,String.format(
                                FirebaseUtil.endRideEventMessage,trip_id))
                        endRideSummary = rideSummary
                        view?.onEndTripSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (e is HttpException && view != null) {
                            val exception = e
                            if (exception.code() == 409) {
                                view?.onEndTripPaymentFailure()
                            } else if (exception.code() == 411) {
                                view?.onEndTripStripeConnectFailure()
                            } else if (exception.code() == 405) {
                                view?.onEndTripEnforeParkingFailure()
                            } else {
                                view?.onEndTripFailure()
                            }
                        } else {
                            view?.onEndTripFailure()
                        }
                    }
                }))
        } else {
            requestLocationUpdates()
        }
    }

    open fun stopActiveTripService() {
        subscriptions.add(
            stopActiveTripUseCase
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(aVoid: Boolean) {
                        super.onNext(aVoid)
                        view?.onEndTripSuccess()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onActiveTripStopped()
                    }
                })
        )
    }


    companion object {
        val TRIP_ID = "TRIP_ID"
        val LATITUDE_END_RIDE_ID = "LATITUDE_END_RIDE_ID"
        val LONGITUDE_END_RIDE_ID = "LONGITUDE_END_RIDE_ID"
        val PARKING_END_RIDE_ID = "PARKING_END_RIDE_ID"
        val FORCE_END_RIDE_ID = "FORCE_END_RIDE_ID"
        val END_RIDE_ID_LOADING_STRING = "END_RIDE_ID_LOADING_STRING"
        val LOCK_BATTERY = "LOCK_BATTERY"
        val BIKE_DAMAGE = "BIKE_DAMAGE"
    }

}