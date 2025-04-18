package io.lattis.operator.presentation.vehicle.fragments.equipment.other

import android.os.Bundle
import com.google.gson.Gson
import io.lattis.domain.models.Vehicle
import io.lattis.operator.presentation.base.activity.ActivityPresenter
import io.lattis.operator.presentation.vehicle.VehicleDetailActivity
import io.lattis.operator.presentation.vehicle.fragments.equipment.VehicleDetailEquipmentFragment
import javax.inject.Inject
import kotlin.properties.Delegates

class VehicleDetailOtherEquipmentPresenter @Inject constructor(

) : ActivityPresenter<VehicleDetailOtherEquipmentActivityView>(){

    lateinit var vehicle: Vehicle
    var currentPosition by Delegates.notNull<Int>()

    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null && arguments.containsKey(VehicleDetailActivity.VEHICLE)) {
            val referencedVehicleString = arguments.getString(VehicleDetailActivity.VEHICLE)
            vehicle = Gson().fromJson(referencedVehicleString, Vehicle::class.java)
        }

        if (arguments != null && arguments.containsKey(VehicleDetailEquipmentFragment.CURRENT_EQUIPMENT_POSITION)) {
            currentPosition = arguments.getInt(VehicleDetailEquipmentFragment.CURRENT_EQUIPMENT_POSITION)
            view?.startShowingInformation()
        }
    }
}