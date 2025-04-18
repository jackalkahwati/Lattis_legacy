package com.lattis.lattis.presentation.reservation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lattis.domain.models.Bike
import com.lattis.lattis.presentation.customview.CustomTextView
import io.lattis.lattis.R
import java.util.*
import kotlin.collections.ArrayList

class ReservationFleetListAdapter(
    var mContext: Context,
    var reservationFleetList: List<Bike.Fleet>,
    var reservationFleetActionListener: ReservationFleetActionListener
) : RecyclerView.Adapter<ReservationFleetListAdapter.ViewHolder>(), Filterable {

    var reservationFleetFilterList : List<Bike.Fleet>

    init {
        reservationFleetFilterList = reservationFleetList
    }

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
        if (reservationFleetFilterList != null) {
            holder.ct_fleet_name_in_private_fleets_item.text = reservationFleetFilterList!![position].fleet_name
            holder.ct_fleet_email_in_private_fleets_item .text = reservationFleetFilterList!![position].address?.city + ", " + reservationFleetFilterList!![position].address?.country


            if(reservationFleetFilterList!![position].logo!=null) {
                downloadImage(
                    holder,
                    reservationFleetFilterList!![position].logo
                )
            }

            holder.iv_next_arrow.setOnClickListener {
                reservationFleetActionListener?.onReservationFleetSelected(reservationFleetFilterList!![position])
            }
        }
    }

    override fun getFilter(): Filter {
        return reservationFleetFilter
    }

    private val reservationFleetFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val charSearch = constraint.toString()
            if (charSearch.isEmpty()) {
                reservationFleetFilterList = reservationFleetList
            } else {
                val resultList = ArrayList<Bike.Fleet>()
                for (fleet in reservationFleetList) {
                    if (fleet.fleet_name?.toLowerCase(Locale.ROOT)!!.contains(charSearch.toLowerCase(Locale.ROOT))) {
                        resultList.add(fleet)
                    }
                }
                reservationFleetFilterList = resultList
            }
            val filterResults = FilterResults()
            filterResults.values = reservationFleetFilterList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            reservationFleetFilterList = results?.values as ArrayList<Bike.Fleet>
            notifyDataSetChanged()
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
        return reservationFleetFilterList!!.size
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
        var iv_next_arrow : ImageView =
            itemView.findViewById(R.id.iv_next_in_private_fleet_item)

        init {
            iv_next_arrow.visibility=View.VISIBLE
        }

    }


}

