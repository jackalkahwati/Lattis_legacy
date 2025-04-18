package io.lattis.operator.presentation.qrcodescan

import android.os.Bundle
import android.text.TextUtils
import io.lattis.domain.models.Fleet
import io.lattis.domain.models.Vehicle
import io.lattis.domain.usecase.vehicle.ChangeBulkStatusUseCase
import io.lattis.domain.usecase.vehicle.GetVehicleFromQRCodeUseCase
import io.lattis.operator.model.ChangeStatus
import io.lattis.operator.presentation.base.RxObserver
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import io.lattis.operator.presentation.qrcodescan.ScanQRCodeActivity.Companion.FLEET_ID
import okhttp3.ResponseBody
import org.json.JSONObject
import javax.inject.Inject

class ScanQRCodeActivityPresenter @Inject constructor(
    private val getVehicleFromQRCodeUseCase: GetVehicleFromQRCodeUseCase,
    private val changeBulkStatusUseCase: ChangeBulkStatusUseCase
):ActivityPresenter<ScanQRCodeActivityView>() {


    var fleet_id:Int?=null
    var vehicles: ArrayList<Vehicle> = ArrayList()

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(FLEET_ID)) {
            this.fleet_id = arguments.getInt(FLEET_ID)
        }
    }



    fun processQRCode(qrCodeDatatString: String){
        val qr_code_id_thing = getScannedIoTQRCode(qrCodeDatatString)
        val qr_code_id_lattis = getScannedLattisQRCode(qrCodeDatatString)
        if(qr_code_id_lattis!=null){
            getVehicleFromQRCode(qr_code_id_lattis,null)
        }else if(qr_code_id_thing!=null){
            getVehicleFromQRCode(null,qr_code_id_thing)
        }else{
            view?.onQRCodeVehicleFailure()
        }
    }

    fun getVehicleFromQRCode(qrCode:String?,thingQRCode:String?) {
        view?.showProgressbar()
        subscriptions.add(
            getVehicleFromQRCodeUseCase
                .withFleedId(fleet_id!!)
                .withQRCode(qrCode)
                .withThingQRCode(thingQRCode)
                .execute(object : RxObserver<Vehicle>(view, false) {
                    override fun onNext(newVehicle: Vehicle) {
                        super.onNext(newVehicle)
                        view?.hideProgressbar()
                        if(vehicleAlreadyAdded(newVehicle)){
                            view?.restartScanner()
                        }else{
                            vehicles.add(newVehicle)
                            view?.onQRCodeVehicleSuccess()
                        }
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.hideProgressbar()
                        view?.onQRCodeVehicleFailure()
                    }
                })
        )
    }

    fun vehicleAlreadyAdded(newVehicle: Vehicle):Boolean{
        for(vehicle in vehicles){
            if(vehicle.id == newVehicle.id){
                return true
            }
        }
        return false
    }


    fun getScannedLattisQRCode(qrCodeDatatString: String?):String?{

        var scannedQRCode : String?=null
        if(TextUtils.isEmpty(qrCodeDatatString)) return scannedQRCode
        try{
            val qrCodeJSONObject = JSONObject(qrCodeDatatString)
            if (qrCodeJSONObject != null) {
                scannedQRCode = qrCodeJSONObject.getString("qr_id")
            }
        }catch (e:Exception){
        }
        return scannedQRCode
    }


    fun getScannedIoTQRCode(qrCodeDatatString: String?):String?{
        var scannedQRCode : String?=null
        try {
            val lastString = qrCodeDatatString?.substringAfterLast("/")
            scannedQRCode = lastString
        }catch(e:Exception){

        }
        return scannedQRCode
    }



    fun changeBulkStatus(changeStatus: ChangeStatus) {
        subscriptions.add(
            changeBulkStatusUseCase
                .withBatch(getBatchItems())
                .withMaintenance(changeStatus.maintenance)
                .withStatus(changeStatus.status!!)
                .withUsage(changeStatus.usage!!)
                .execute(object : RxObserver<ResponseBody>(view, false) {
                    override fun onNext(responseBody: ResponseBody) {
                        super.onNext(responseBody)
                        updateVehicleStatus(changeStatus)
                        view?.onChangeBulkStatusSuccess()
                    }
                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onChangeBuldStatusFailure()
                    }
                })
        )
    }

    fun getBatchItems():String{
        var batchItems = buildString {
            for (vehicle in vehicles){
                append(","+vehicle.id)
            }
        }
        return batchItems.drop(1)
    }

    fun updateVehicleStatus(changeStatus: ChangeStatus){
        for(vehicle in vehicles){
            vehicle.status = changeStatus.status
            vehicle.usage = changeStatus.usage
        }
    }
}