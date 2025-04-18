package com.lattis.lattis.presentation.reservation

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Reservation
import com.lattis.lattis.presentation.customview.CustomButton
import com.lattis.lattis.presentation.customview.CustomTextView
import com.lattis.lattis.utils.ResourceHelper.getBikeType
import com.lattis.lattis.utils.UtilsHelper
import com.lattis.lattis.utils.UtilsHelper.dateFromUTC
import com.lattis.lattis.utils.UtilsHelper.getDateOnly
import com.lattis.lattis.utils.UtilsHelper.getDurationFromNow
import com.lattis.lattis.utils.UtilsHelper.getDurationFromNowInHourMin
import com.lattis.lattis.utils.UtilsHelper.isDateToday
import io.lattis.lattis.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AvailableReservationsListAdapter(
    var mContext: Context,
    var reservationList: List<Reservation>?,
    var availableReservationsActionListener: AvailableReservationsActionListener
) : RecyclerView.Adapter<AvailableReservationsListAdapter.ViewHolder>() {

    var holdersAvailableTimers = ArrayList<CountDownTimer>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_reservation_list_item, parent, false)
        return ViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        if (reservationList != null) {
            holder.fleetName.setText(reservationList!![position].bike?.fleet?.fleet_name)
            holder.bikeName.setText(reservationList!![position].bike?.bike_name)
            holder.bikeType.setText(getBikeType(reservationList!![position].bike?.bike_group?.type,mContext))


            val reservationStartTime = dateFromUTC(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reservationList!![position]?.reservation_start))
            if(!reservationStartTime!!.after(Date())){
                holder.other.visibility = View.GONE
                holder.today.visibility = View.VISIBLE
                holder.todayDate.text = getDateOnly(
                    dateFromUTC(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reservationList!![position]?.reservation_start)
                    )!!
                )
                holder.todayTime.text = mContext.getString(R.string.start)
            }else if(isDateToday(reservationList!![position]?.reservation_start)){
                startReservationAvailableTimer(holder,SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reservationList!![position]?.reservation_start))
                holder.other.visibility = View.GONE
                holder.today.visibility = View.VISIBLE
                holder.todayDate.text = getDateOnly(
                    dateFromUTC(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reservationList!![position]?.reservation_start)
                    )!!
                )
            } else{
                holder.other.visibility = View.VISIBLE
                holder.today.visibility = View.GONE
                holder.otherDate.text = UtilsHelper.getDateOnly(
                    UtilsHelper.dateFromUTC(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reservationList!![position]?.reservation_start)
                    )!!
                )
                holder.otherTime.text = UtilsHelper.getTimeOnly(
                    UtilsHelper.dateFromUTC(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reservationList!![position]?.reservation_start)
                    )!!
                )+ " - " +
                        UtilsHelper.getTimeOnly(
                            UtilsHelper.dateFromUTC(
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reservationList!![position]?.reservation_end)
                            )!!
                        )
            }






            val requestOptions = RequestOptions()
            requestOptions.placeholder(R.drawable.bike_default)
            requestOptions.error(R.drawable.bike_default)
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
            requestOptions.dontAnimate()

            Glide.with(mContext)
                .load(reservationList!![position].bike?.bike_group?.pic)
                .apply(requestOptions)
                .into(holder.bikeImage)

            holder.other.setOnClickListener {
                availableReservationsActionListener.onReservationSelected(position)
            }

            holder.today.setOnClickListener {
                availableReservationsActionListener.onReservationSelected(position)
            }

            holder.nextScreen.setOnClickListener {
                availableReservationsActionListener.onReservationSelected(position)
            }

            holder.todayStartingReservation.setOnClickListener {
                availableReservationsActionListener.onReservationSelected(position)
            }

        }
    }

    override fun getItemCount(): Int {
        return reservationList!!.size
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){

        var availableTimer: CountDownTimer? = null

        var bikeType: CustomTextView = itemView.findViewById(R.id.ct_bike_type_in_reservation_bike_card )
        var bikeName:CustomTextView = itemView.findViewById(R.id.ct_bike_name_in_reservation_reservation_bike_card)
        var fleetName:CustomTextView = itemView.findViewById(R.id.ct_fleet_name_in_reservation_reservation_reservation_bike_card)
        var bikeImage:ImageView = itemView.findViewById(R.id.iv_bike_image_in_reservation_bike_card)

        var other:ConstraintLayout = itemView.findViewById(R.id.cl_reservation_list_other_day)
        var today:ConstraintLayout = itemView.findViewById(R.id.cl_reservation_list_today)
        var nextScreen:ImageView = itemView.findViewById(R.id.iv_next_in_reservation_list_item)

        var todayDate:CustomTextView = itemView.findViewById(R.id.ct_today_date_in_reservation_list)
        var todayTime:CustomTextView = itemView.findViewById(R.id.ct_today_timer_in_reservation_list)

        var otherDate:CustomTextView = itemView.findViewById(R.id.ct_other_date_in_reservation_list_item)
        var otherTime:CustomTextView = itemView.findViewById(R.id.ct_other_time_in_reservation_list_item)

        var todayStartingReservation:ConstraintLayout = itemView.findViewById(R.id.cl_today_in_reserve_list)

    }


    fun startReservationAvailableTimer(holder: ViewHolder, startDate:Date ){
        if(holder.availableTimer!=null)
            return

        stopeReservationAvailableTimer(holder)

        var timeRemaining =
            UtilsHelper.dateFromUTC(
                startDate
            )!!.time - Date().time

        holder.availableTimer = object : CountDownTimer((timeRemaining).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                holder.todayTime.text = UtilsHelper.getDurationBreakdown(millisUntilFinished / 1000)
            }
            override fun onFinish() {
                holdersAvailableTimers.remove(holder.availableTimer!!)
                holder.todayTime.text = mContext.getString(R.string.start)
            }
        }.start()

        holdersAvailableTimers?.add(holder.availableTimer!!)
    }

    fun stopeReservationAvailableTimer(holder: ViewHolder ){
        if (holder.availableTimer != null) {
            holder.availableTimer?.cancel()
            holder.availableTimer=null
        }
    }

    fun cancelAllAvailalbeTimer(){
        for(availableTimer in holdersAvailableTimers){
            availableTimer?.cancel()
        }
    }

}