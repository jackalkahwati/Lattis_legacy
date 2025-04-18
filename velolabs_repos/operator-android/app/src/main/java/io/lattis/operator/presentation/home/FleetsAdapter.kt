package io.lattis.operator.presentation.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.contentValuesOf
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.lattis.domain.models.Fleet
import io.lattis.operator.R
import io.lattis.operator.presentation.customview.CustomTextView

class FleetsAdapter(
    var mContext: Context,
    var fleets: List<Fleet>?,
    var fleetClickListener: FleetClickListener
) : RecyclerView.Adapter<FleetsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fleet_home_activity, parent, false)
        return ViewHolder(
            view

        )
    }

    fun setFilteredList(fleets: List<Fleet>){
        this.fleets = fleets
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        if (fleets != null) {
            holder.ct_fleet_name_in_home_fleet_item.text = fleets!![position].name


            if(fleets!![position].vehiclesCount!=null) {
                holder.ct_fleet_vehicles_in_home_fleet_item.text =
                    (if(fleets!![position].vehiclesCount==1) mContext.getString(R.string.vehicle) else mContext.getString(R.string.vehicles) ) +
                            " " +fleets!![position].vehiclesCount
            }
            if(fleets!![position].logo!=null) {
                downloadImage(
                    holder,
                    fleets!![position].logo
                )
            }

            holder.itemView.setOnClickListener {
                fleetClickListener.onFleetClicked(fleets?.get(position)!!)
            }
        }
    }

    fun downloadImage(
        holder: ViewHolder,
        url: String?
    ) {

        val requestOptions = RequestOptions()
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(mContext)
            .load(url)
            .apply(requestOptions)
            .into(holder.iv_fleet_image_in_home_fleet_item)
    }

    override fun getItemCount(): Int {
        return if(fleets==null) 0 else fleets!!.size
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        var iv_fleet_image_in_home_fleet_item: ImageView =
            itemView.findViewById(R.id.iv_fleet_image_in_home_fleet_item)
        var ct_fleet_vehicles_in_home_fleet_item: CustomTextView =
            itemView.findViewById(R.id.ct_fleet_vehicles_in_home_fleet_item)
        var ct_fleet_name_in_home_fleet_item: CustomTextView =
            itemView.findViewById(R.id.ct_fleet_name_in_home_fleet_item)

    }


}