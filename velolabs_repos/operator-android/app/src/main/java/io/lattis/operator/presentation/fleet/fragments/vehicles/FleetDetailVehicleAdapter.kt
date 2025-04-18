package io.lattis.operator.presentation.fleet.fragments.vehicles

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.customview.CustomTextView
import io.lattis.operator.utils.ResourceUtils.convertStatus
import io.lattis.operator.utils.ResourceUtils.convertUsage

class FleetDetailVehicleAdapter(
    var mContext: Context,
    var vehicles: List<Vehicle>,
    var fleetDetailVehicleListener: FleetDetailVehicleListener
) : RecyclerView.Adapter<FleetDetailVehicleAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fleet_details_vehicle, parent, false)
        return ViewHolder(
            view

        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.ct_name_in_fleet_details_vehicle.text = vehicles.get(position).name
        holder.ct_status_in_fleet_details_vehicle.text = convertStatus(mContext,vehicles.get(position).status)
        holder.ct_bike_type_in_fleet_details_vehicle.text = vehicles.get(position).group?.type
        holder.ct_condition_in_fleet_details_vehicle.text = convertUsage(mContext,vehicles.get(position).usage)

        holder.itemView.setOnClickListener {
            fleetDetailVehicleListener.onVehicleClicked(position)
        }

    }

    override fun getItemCount(): Int {
        return vehicles!!.size
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        var ct_name_in_fleet_details_vehicle: CustomTextView =
            itemView.findViewById(R.id.ct_name_in_fleet_details_vehicle)
        var ct_status_in_fleet_details_vehicle: CustomTextView =
            itemView.findViewById(R.id.ct_status_in_fleet_details_vehicle)
        var ct_bike_type_in_fleet_details_vehicle: CustomTextView =
            itemView.findViewById(R.id.ct_bike_type_in_fleet_details_vehicle)
        var ct_condition_in_fleet_details_vehicle: CustomTextView =
            itemView.findViewById(R.id.ct_condition_in_fleet_details_vehicle)

    }







}