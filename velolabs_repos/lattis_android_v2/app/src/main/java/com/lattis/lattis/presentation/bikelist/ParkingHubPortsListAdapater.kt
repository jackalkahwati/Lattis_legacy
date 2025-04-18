package com.lattis.lattis.presentation.bikelist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lattis.domain.models.DockHub
import com.lattis.lattis.presentation.customview.CustomButton
import com.lattis.lattis.presentation.customview.CustomTextView
import com.lattis.lattis.presentation.reservation.AvailableVehiclesActionListener
import com.lattis.lattis.utils.ParkingHubHelper.getParkingHubFleetName
import com.lattis.lattis.utils.ParkingHubHelper.getParkingHubPortName
import com.lattis.lattis.utils.ParkingHubHelper.getParkingHubPortType
import com.lattis.lattis.utils.ParkingHubHelper.getParkingHubPortUrl
import io.lattis.lattis.R

class ParkingHubPortsListAdapater(
    var mContext: Context,
    var dockHub: DockHub,
    var availableVehiclesActionListener: AvailableVehiclesActionListener
) : RecyclerView.Adapter<ParkingHubPortsListAdapater.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_bikelist_with_hub_bikes_item, parent, false)
        return ViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

            holder.fleetName.setText(getParkingHubFleetName(dockHub))
            holder.bikeName.setText(getParkingHubPortName(dockHub,position))
            holder.bikeType.setText(getParkingHubPortType(dockHub,position))

            val requestOptions = RequestOptions()
            requestOptions.placeholder(R.drawable.bike_default)
            requestOptions.error(R.drawable.bike_default)
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
            requestOptions.dontAnimate()

            Glide.with(mContext)
                .load(getParkingHubPortUrl(dockHub,position))
                .apply(requestOptions)
                .into(holder.bikeImage)


            holder.info.setOnClickListener {
                availableVehiclesActionListener.onBikeInfoSelected(position)
            }

            holder.select.setOnClickListener {
                availableVehiclesActionListener.onBikeSelected(position)
            }


    }

    override fun getItemCount(): Int {
        return if(dockHub==null || dockHub.ports==null) 0 else dockHub!!.ports?.size!!
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){

        var bikeType: CustomTextView = itemView.findViewById(R.id.ct_bike_type_in_reservation_bike_card )
        var bikeName: CustomTextView = itemView.findViewById(R.id.ct_bike_name_in_reservation_reservation_bike_card)
        var fleetName: CustomTextView = itemView.findViewById(R.id.ct_fleet_name_in_reservation_reservation_reservation_bike_card)
        var bikeImage: ImageView = itemView.findViewById(R.id.iv_bike_image_in_reservation_bike_card)

        var info: ImageView = itemView.findViewById(R.id.iv_dock_hub_info_vehicles_item)
        var select: CustomButton = itemView.findViewById(R.id.btn_select_in_dock_hub_vehicles)
    }

}