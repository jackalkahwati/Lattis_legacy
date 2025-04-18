package io.lattis.operator.presentation.qrcodescan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.lattis.domain.models.Vehicle
import io.lattis.operator.R
import io.lattis.operator.presentation.customview.CustomTextView
import io.lattis.operator.utils.ResourceUtils

class ScanQRCodeVehicleListAdapter(
    var mContext: Context,
    var scanQRCodeVehicleListAdapterListener: ScanQRCodeVehicleListAdapterListener
) : RecyclerView.Adapter<ScanQRCodeVehicleListAdapter.ViewHolder>() {

    var vehicles: ArrayList<Vehicle> = ArrayList()
    
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_fleet_detail_map_vehicle_card, parent, false)
        return ViewHolder(
            view

        )
    }
    
    fun updateVehicleList(vehicles: ArrayList<Vehicle>){
        this.vehicles = vehicles
        notifyDataSetChanged()
    }
    

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        

        holder.ct_vehicle_name_value_in_vehicle_card.text = vehicles?.get(position)?.name

        holder.ct_vehicle_type_value_in_vehicle_card.text = vehicles?.get(position)?.group?.type

        holder.ct_vehicle_usage_value_in_vehicle_card.text =
            ResourceUtils.convertUsage(mContext, vehicles?.get(position)?.usage)

        holder.ct_vehicle_status_value_in_vehicle_card.text =
            ResourceUtils.convertStatus(mContext, vehicles?.get(position)?.status)

        holder.iv_vehicle_navigation.visibility = View.GONE

        holder.itemView.setOnClickListener {
            scanQRCodeVehicleListAdapterListener.onVehicleClicked(position)
        }

    }

    override fun getItemCount(): Int {
        return vehicles!!.size
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        var ct_vehicle_name_value_in_vehicle_card: CustomTextView =
            itemView.findViewById(R.id.ct_vehicle_name_value_in_vehicle_card)
        var ct_vehicle_type_value_in_vehicle_card: CustomTextView =
            itemView.findViewById(R.id.ct_vehicle_type_value_in_vehicle_card)
        var ct_vehicle_usage_value_in_vehicle_card: CustomTextView =
            itemView.findViewById(R.id.ct_vehicle_usage_value_in_vehicle_card)
        var ct_vehicle_status_value_in_vehicle_card: CustomTextView =
            itemView.findViewById(R.id.ct_vehicle_status_value_in_vehicle_card)
        
        var iv_vehicle_navigation : ImageView = itemView.findViewById(R.id.iv_vehicle_navigation)

    }







}