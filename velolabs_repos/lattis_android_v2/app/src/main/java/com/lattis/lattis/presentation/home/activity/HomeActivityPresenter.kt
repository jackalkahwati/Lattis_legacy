package com.lattis.lattis.presentation.home.activity


import android.os.Bundle
import com.lattis.domain.models.Reservation
import com.lattis.domain.usecase.logout.LogOutUseCase
import com.lattis.domain.usecase.ride.GetRideUseCase
import com.lattis.domain.usecase.user.GetLocalUserUseCase
import com.lattis.domain.usecase.user.GetUserCurrentStatusUseCase
import com.lattis.domain.models.Ride
import com.lattis.domain.models.Subscription
import com.lattis.domain.models.User
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.usecase.lock.disconnect.DisconnectAllLockUseCase
import com.lattis.domain.usecase.membership.GetSubscriptionsUseCase
import com.lattis.domain.usecase.reservation.GetReservationsUseCase
import com.lattis.domain.usecase.updatetrip.StopActiveTripUseCase
import com.lattis.lattis.presentation.base.activity.location.BaseLocationActivityPresenter
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.presentation.utils.FirebaseUtil
import javax.inject.Inject

class HomeActivityPresenter @Inject constructor(
    val getUserCurrentStatusUseCase: GetUserCurrentStatusUseCase,
    val getRideUseCase: GetRideUseCase,
    val getLocalUserUseCase: GetLocalUserUseCase,
    val logOutUseCase: LogOutUseCase,
    val getSubscriptionsUseCase: GetSubscriptionsUseCase,
    val getReservationsUseCase: GetReservationsUseCase,
    val stopActiveTripUseCase: StopActiveTripUseCase,
    val disconnectAllLockUseCase: DisconnectAllLockUseCase
) : BaseLocationActivityPresenter<HomeActivityView>() {

    var user:User?=null
    var subscriptionList:List<Subscription>?=null
    var reservations: List<Reservation>?=null

    fun getScreen(currentStatus: BaseUserCurrentStatusPresenter.Companion.CurrentStatus){
        if(currentStatus== BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ERROR_FETCHING_STATUS){
            view?.showServerError()
        }else if(currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.NO_BOOKING_NO_TRIP){
            stopActiveTripService()
            disconnectAlLocks()
            view?.startShowingBikeListFragment()
        }else if(currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING){
            view?.startShowingBikeBookedFragment()
        }else if(currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_BOOKING_WITH_TRIP_STARTED){
            view?.startShowingBikeBookedWithActiveTrip()
        }else if(currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.ACTIVE_TRIP){
            view?.startShowingActiveTripFragment(false)
        }else if(currentStatus == BaseUserCurrentStatusPresenter.Companion.CurrentStatus.NO_INTERNET){
            getRide()
        }
    }

    fun getRide() {
        subscriptions.add(
            getRideUseCase
                .execute(object : RxObserver<Ride>(view) {
                    override fun onNext(ride: Ride) {
                        super.onNext(ride)
                        if (ride != null && ride.rideId !== 0 && ride.isFirst_lock_connect) { // this will ensure that active trip
                            view?.onRideSuccess(ride)
                        } else {
                            view?.onRideFailure()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                        view?.onRideFailure()
                    }
                })
        )
    }


    ////// Get user: start //////////////
    val getUser: Unit
        get() {
            subscriptions.add(getLocalUserUseCase.execute(object : RxObserver<User>(view, false) {
                override fun onNext(currUser: User) {

                    if (currUser != null) {
                        user = currUser
                        view?.handleUser(user!!)
                    }

                }
                override fun onError(e: Throwable) {
                    super.onError(e)
                }
            }))
        }



    override fun updateViewState() {
    }



    ////logout
    open fun logOut(): Unit {
        subscriptions.add(logOutUseCase.execute(object : RxObserver<Boolean>(view, false) {
            override fun onNext(success: Boolean) {
                view?.onLogOutSuccessfull()
                FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.LOGOUT, FirebaseUtil.LOGOUT)

            }

            override fun onError(e: Throwable) {
                super.onError(e)
                view?.onLogOutFailure()
            }
        }))
    }


    fun getSubscriptions(takeAction:Boolean=false){
        subscriptions.add(
            getSubscriptionsUseCase.execute(
                object : RxObserver<List<Subscription>>(view) {
                    override fun onError(e: Throwable) {
                        super.onError(e)
                    }

                    override fun onNext(t: List<Subscription>) {
                        super.onNext(t)
                        subscriptionList=t
                        if(takeAction){
                            view?.onSubscriptionSuccess()
                        }
                    }
                }
            )
        )
    }

    //// membership popup :start
    fun getMembershipDiscount(fleet_id:Int):String?{
        if(subscriptionList!=null && !subscriptionList?.isEmpty()!!){
            for(subscription in subscriptionList!!){
                if(subscription.fleet_membership?.fleet_id == fleet_id){
                    return subscription?.fleet_membership?.membership_incentive
                }
            }
        }

        return null
    }
    //// membership popup :end

    //// reservations :start
    fun getReservations(){
        subscriptions.add(getReservationsUseCase
            .execute(object : RxObserver<List<Reservation>>(view, false) {
                override fun onNext(newReservations: List<Reservation>) {
                    super.onNext(newReservations)
                    reservations = newReservations
                    view?.onReservationsAvailable()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    view?.onReservationNotAvailable()
                }
            })
        )
    }
    //// reservations :end



    //// stop service and disconnect as no active trip :start
    fun stopActiveTripService() {
        subscriptions.add(
            stopActiveTripUseCase
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(aVoid: Boolean) {
                        super.onNext(aVoid)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                    }
                })
        )
    }

    fun disconnectAlLocks(){
        disconnectAxaAllLocks()
        disconnectEllipseAllLocks()
    }

    fun disconnectAxaAllLocks() {
        subscriptions.add(
            disconnectAllLockUseCase
                .withLockVendor(UseCase.LockVendor.AXA)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                    }

                    override fun onError(throwable: Throwable) {
                        super.onError(throwable)
                    }
                })
        )
    }

    fun disconnectEllipseAllLocks() {
        subscriptions.add(
            disconnectAllLockUseCase
                .withLockVendor(UseCase.LockVendor.ELLIPSE)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                    }

                    override fun onError(throwable: Throwable) {
                        super.onError(throwable)
                    }
                })
        )
    }
    //// stop service and disconnect as no active trip :end
}