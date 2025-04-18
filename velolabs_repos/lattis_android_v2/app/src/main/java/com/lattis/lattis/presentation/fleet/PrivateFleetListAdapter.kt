package com.lattis.lattis.presentation.fleet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lattis.domain.models.PrivateNetwork
import com.lattis.lattis.presentation.customview.CustomTextView
import io.lattis.lattis.R

class PrivateFleetListAdapter(
    var mContext: Context,
    var privateFleetList: List<PrivateNetwork>?
) : RecyclerView.Adapter<PrivateFleetListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_private_fleets_item_fleet_list, parent, false)
        return ViewHolder(
            view

        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        if (privateFleetList != null) {
            holder.ct_fleet_name_in_private_fleets_item.text = privateFleetList!![position].fleet_name
            holder.ct_fleet_email_in_private_fleets_item .text = privateFleetList!![position].email
            if(privateFleetList!![position].logo!=null) {
                downloadImage(
                    holder,
                    privateFleetList!![position].logo
                )
            }
        }
    }

    fun downloadImage(
        holder: ViewHolder,
        url: String?
    ) {

        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.bike_default)
        requestOptions.error(R.drawable.bike_default)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(mContext)
            .load(url)
            .apply(requestOptions)
            .into(holder.iv_fleet_image_in_private_fleet_item)
    }

        override fun getItemCount(): Int {
            return privateFleetList!!.size
        }

        inner class ViewHolder(
            itemView: View
        ) : RecyclerView.ViewHolder(itemView) {

            var iv_fleet_image_in_private_fleet_item: ImageView =
                itemView.findViewById(R.id.iv_fleet_image_in_private_fleet_item)
            var ct_fleet_name_in_private_fleets_item: CustomTextView =
                itemView.findViewById(R.id.ct_fleet_name_in_private_fleets_item)
            var ct_fleet_email_in_private_fleets_item: CustomTextView =
                itemView.findViewById(R.id.ct_fleet_email_in_private_fleets_item)

        }


}