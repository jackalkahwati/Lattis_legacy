package com.lattis.lattis.presentation.history

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.lattis.domain.models.RideHistory.RideHistoryData
import com.lattis.lattis.presentation.history.detail.RideHistoryDetailActivity
import com.lattis.lattis.presentation.utils.CurrencyUtil.getCurrencySymbolByCode
import com.lattis.lattis.utils.UtilsHelper
import com.lattis.lattis.utils.UtilsHelper.getDateCurrentTimeZone
import com.lattis.lattis.utils.UtilsHelper.getDateTimeCurrentTimeZone
import com.lattis.lattis.utils.UtilsHelper.getDotAfterNumber
import io.lattis.lattis.R

class RideHistoryAdapter(
    var mContext: Context,
    var rideHistoryDataResponses: List<RideHistoryData>
) : RecyclerView.Adapter<RideHistoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_ride_history_item, parent, false)
        return ViewHolder(
            view,
            rideHistoryDataResponses
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        if (rideHistoryDataResponses[position].date_created != null) {
            holder.tv_trip_time!!.text = getDateTimeCurrentTimeZone(
                mContext,
                rideHistoryDataResponses[position].date_created!!.toLong()
            )
        }
        if (rideHistoryDataResponses[position].total != null) {

            holder.tv_trip_cost!!.text = getCurrencySymbolByCode(
                rideHistoryDataResponses[position].currency,getDotAfterNumber(
                rideHistoryDataResponses[position].total)
            )
        } else holder.tv_trip_cost!!.text = mContext.getString(R.string.bike_detail_bike_cost_free)


        holder.tv_trip_duration.setText(UtilsHelper.getTimeFromDuration(rideHistoryDataResponses[position].duration))
        holder.tv_fleet_name!!.text = rideHistoryDataResponses[position].fleet_name
    }

    override fun getItemCount(): Int {
        return rideHistoryDataResponses.size
    }

    inner class ViewHolder(
        itemView: View,
        rideHistoryDataResponses: List<RideHistoryData>
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var tv_trip_time: TextView = itemView.findViewById(R.id.ct_date_in_ride_history_item)

        var tv_trip_duration: TextView = itemView.findViewById(R.id.ct_duration_in_ride_history_item)

        var tv_fleet_name: TextView = itemView.findViewById(R.id.ct_fleet_name_in_ride_history_item)

        var tv_trip_cost: TextView = itemView.findViewById(R.id.ct_cost_in_ride_history_item)

        var rideHistoryDataResponses: List<RideHistoryData>
        override fun onClick(v: View) {
            mContext.startActivity(
                RideHistoryDetailActivity.getIntent(mContext,rideHistoryDataResponses[adapterPosition])
            )
        }

        init {
            itemView.setOnClickListener(this)
            this.rideHistoryDataResponses = rideHistoryDataResponses
        }
    }

}