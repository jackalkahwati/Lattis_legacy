package io.lattis.operator.presentation.vehicle.fragments.vehicle

import android.os.Bundle
import android.text.TextUtils
import com.google.gson.Gson
import io.lattis.domain.models.Vehicle
import io.lattis.domain.usecase.vehicle.ChangeStatusUseCase
import io.lattis.operator.model.ChangeStatus
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.fragment.FragmentPresenter
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity.Companion.TAB_TITLE
import okhttp3.ResponseBody
import javax.inject.Inject

class VehicleDetailVehicleFragmentPresenter @Inject constructor(
        val changeStatusUseCase: ChangeStatusUseCase
):FragmentPresenter<VehicleDetailVehicleFragmentView>() {

    lateinit var vehicle: Vehicle
    lateinit var tabTitle:String
    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(VehicleDetailActivity.VEHICLE)) {
            val referencedFleetString = arguments.getString(VehicleDetailActivity.VEHICLE)
            vehicle = Gson().fromJson(referencedFleetString, Vehicle::class.java)
            view?.startShowingInformation()
        }
    }

    fun changeStatus(changeStatus:ChangeStatus) {
        subscriptions.add(
            changeStatusUseCase
                .withVehicleId(vehicle.id!!)
                .withMaintenance(changeStatus.maintenance)
                .withStatus(changeStatus.status!!)
                .withUsage(changeStatus.usage!!)
                .execute(object : RxObserver<ResponseBody>(view, false) {
                    override fun onNext(status:ResponseBody) {
                        super.onNext(status)
                        vehicle?.status=changeStatus.status
                        view?.onChangeStatusSuccess(changeStatus)
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onChangeStatusFailure()
                    }
                })
        )
    }

    fun getQRCode():String?{
        if(vehicle.things!=null){
            for(thing in vehicle.things!!){
                if(!TextUtils.isEmpty(thing.qrCode)){
                    return thing.qrCode
                }
            }
        }
        return vehicle.qrCode
    }


}