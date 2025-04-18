package com.lattis.lattis.presentation.bikelist

import com.lattis.lattis.presentation.reservation.AvailableVehiclesActionListener

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lattis.domain.models.Bike
import com.lattis.lattis.presentation.customview.CustomButton
import com.lattis.lattis.presentation.customview.CustomTextView
import com.lattis.lattis.utils.ResourceHelper.getBikeType
import io.lattis.lattis.R

class DockHubBikesListAdapater(
    var mContext: Context,
    var bikeList: List<Bike>?,
    var availableVehiclesActionListener: AvailableVehiclesActionListener
) : RecyclerView.Adapter<DockHubBikesListAdapater.ViewHolder>() {
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
        if (bikeList != null) {
            holder.fleetName.setText(bikeList!![position].fleet_name)
            holder.bikeName.setText(bikeList!![position].bike_name)
            holder.bikeType.setText(getBikeType(bikeList!![position].type,mContext))

            val requestOptions = RequestOptions()
            requestOptions.placeholder(R.drawable.bike_default)
            requestOptions.error(R.drawable.bike_default)
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
            requestOptions.dontAnimate()

            Glide.with(mContext)
                .load(bikeList!![position].pic)
                .apply(requestOptions)
                .into(holder.bikeImage)


            holder.info.setOnClickListener {
                availableVehiclesActionListener.onBikeInfoSelected(position)
            }

            holder.select.setOnClickListener {
                availableVehiclesActionListener.onBikeSelected(position)
            }

        }
    }

    override fun getItemCount(): Int {
        return bikeList!!.size
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){

        var bikeType: CustomTextView = itemView.findViewById(R.id.ct_bike_type_in_reservation_bike_card )
        var bikeName:CustomTextView = itemView.findViewById(R.id.ct_bike_name_in_reservation_reservation_bike_card)
        var fleetName:CustomTextView = itemView.findViewById(R.id.ct_fleet_name_in_reservation_reservation_reservation_bike_card)
        var bikeImage:ImageView = itemView.findViewById(R.id.iv_bike_image_in_reservation_bike_card)

        var info:ImageView = itemView.findViewById(R.id.iv_dock_hub_info_vehicles_item)
        var select:CustomButton = itemView.findViewById(R.id.btn_select_in_dock_hub_vehicles)
    }

}