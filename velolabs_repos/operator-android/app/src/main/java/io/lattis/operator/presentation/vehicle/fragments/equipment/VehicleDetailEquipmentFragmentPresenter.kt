package io.lattis.operator.presentation.vehicle.fragments.equipment

import android.os.Bundle
import com.google.gson.Gson
import io.lattis.domain.models.ThingStatus
import io.lattis.domain.models.Vehicle
import io.lattis.domain.usecase.thing.*
import io.lattis.operator.model.ChangeStatus
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.fragment.FragmentPresenter
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import okhttp3.ResponseBody
import javax.inject.Inject

class VehicleDetailEquipmentFragmentPresenter @Inject constructor(
    val getThingStatusUseCase: GetThingStatusUseCase,
    val lockItUseCase: LockItUseCase,
    val unlockItUseCase: UnlockItUseCase,
    val uncoverItUseCase : UncoverItUseCase,
    val headTailLightUseCase: HeadTailLightUseCase,
    val soundUseCase: SoundUseCase
) :FragmentPresenter<VehicleDetailEquipmentFragmentView>() {

    lateinit var vehicle: Vehicle
    var thingStatus:ThingStatus?=null
    var currentThingPosition = 0
    override fun setup(arguments: Bundle?) {
        super.setup(arguments)

        if (arguments != null && arguments.containsKey(VehicleDetailEquipmentFragment.CURRENT_EQUIPMENT_POSITION)) {
            currentThingPosition = arguments.getInt(VehicleDetailEquipmentFragment.CURRENT_EQUIPMENT_POSITION)
        }
        if (arguments != null && arguments.containsKey(VehicleDetailActivity.VEHICLE)) {
            val referencedFleetString = arguments.getString(VehicleDetailActivity.VEHICLE)
            vehicle = Gson().fromJson(referencedFleetString, Vehicle::class.java)
            view?.startShowingInformation()
        }
    }

    fun getThingStatus() {
        subscriptions.add(
            getThingStatusUseCase
                .withThingId(vehicle.things?.get(currentThingPosition)?.id!!)
                .execute(object : RxObserver<ThingStatus>(view, false) {
                    override fun onNext(status: ThingStatus) {
                        super.onNext(status)
                        view?.onThingStatusSuccess(status)
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onThingStatusFailure()
                    }
                })
        )
    }
    
    fun shouldShowEquipmentControl():Boolean{
        if(vehicle.things!=null && vehicle.things?.size!!>0) {
            val thing = vehicle?.things?.get(currentThingPosition)
            return (thing!=null && thing?.deviceType != null &&
                    ("iot".equals(thing?.deviceType, true) ||
                            ("adapter".equals(thing?.deviceType, true)))
            )
        }
        return false
    }

    fun shouldShowOtherEquipmentControl():Boolean{
        return vehicle.things!=null && vehicle?.things?.size!!>1 && currentThingPosition==0
    }

    fun lockIt(thingId:Int) {
        view?.showProgressLoading()
        subscriptions.add(
            lockItUseCase
                .withThingId(thingId)
                .execute(object : RxObserver<ResponseBody>(view, false) {
                    override fun onNext(status: ResponseBody) {
                        super.onNext(status)
                        view?.hideProgressLoading()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.hideProgressLoading()
                        view?.onLockItFailure()
                    }
                })
        )
    }

    fun unlockIt(thingId:Int) {
        view?.showProgressLoading()
        subscriptions.add(
            unlockItUseCase
                .withThingId(thingId)
                .execute(object : RxObserver<ResponseBody>(view, false) {
                    override fun onNext(status: ResponseBody) {
                        super.onNext(status)
                        view?.hideProgressLoading()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.hideProgressLoading()
                        view?.onUnLockItFailure()
                    }
                })
        )
    }

    fun unCoverIt(thingId:Int) {
        view?.showProgressLoading()
        subscriptions.add(
                uncoverItUseCase
                        .withThingId(thingId)
                        .execute(object : RxObserver<ResponseBody>(view, false) {
                            override fun onNext(status: ResponseBody) {
                                super.onNext(status)
                                view?.hideProgressLoading()
                            }
                            override fun onError(e: Throwable) {
                                super.onError(e)
                                view?.hideProgressLoading()
                            }
                        })
        )
    }

    fun headTailLightIt(thingId:Int,headLight:Int?,tailight:Int?) {
        view?.showProgressLoading()
        subscriptions.add(
                headTailLightUseCase
                        .withThingId(thingId)
                        .withHeadLight(headLight)
                        .withTailLight(tailight)
                        .execute(object : RxObserver<ResponseBody>(view, false) {
                            override fun onNext(status: ResponseBody) {
                                super.onNext(status)
                                view?.hideProgressLoading()
                            }
                            override fun onError(e: Throwable) {
                                super.onError(e)
                                view?.hideProgressLoading()
                            }
                        })
        )
    }


    fun sound(thingId:Int,controlType:Int?,workMode:Int?) {
        view?.showProgressLoading()
        subscriptions.add(
            soundUseCase
                .withThingId(thingId)
                .withControlType(controlType)
                .withWorkMode(workMode)
                .execute(object : RxObserver<Boolean>(view, false) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        view?.hideProgressLoading()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.hideProgressLoading()
                    }
                })
        )
    }


}