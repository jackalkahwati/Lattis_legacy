package io.lattis.operator.presentation.vehicle.fragments.equipment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.customview.CustomTextView

class VehicleDetailOtherEquipmentsAdapter(
    var mContext: Context,
    val vehicleDetailOtherEquipmentsListener: VehicleDetailOtherEquipmentsListener,
    var things: List<Vehicle.Thing>
) : RecyclerView.Adapter<VehicleDetailOtherEquipmentsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle_detail_other_equipment, parent, false)
        return ViewHolder(
            view

        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.ct_vendor_other_equipment.text = things.get(position+1)?.vendor
        holder.ct_device_type_other_equipment.text = things.get(position+1)?.deviceType
        holder.itemView.setOnClickListener {
            vehicleDetailOtherEquipmentsListener.onOtherEquipmentClicked(position+1)
        }
    }

    override fun getItemCount(): Int {
        return if(things == null || things!!.size<=1) 0 else things!!.size-1
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        var ct_vendor_other_equipment: CustomTextView =
            itemView.findViewById(R.id.ct_vendor_other_equipment)
        var ct_device_type_other_equipment: CustomTextView =
            itemView.findViewById(R.id.ct_device_type_other_equipment)
    }




}