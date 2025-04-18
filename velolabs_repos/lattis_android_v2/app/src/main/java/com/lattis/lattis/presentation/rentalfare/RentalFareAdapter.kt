package com.lattis.lattis.presentation.rentalfare

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.lattis.domain.models.Bike
import com.lattis.lattis.presentation.customview.CustomTextView
import com.lattis.lattis.presentation.utils.BikeFareUtil.getRentalFare
import com.lattis.lattis.presentation.utils.CurrencyUtil
import com.lattis.lattis.presentation.utils.LocaleTranslatorUtils
import io.lattis.lattis.R

class RentalFareAdapter(
    var mContext: Context,
    var rentalFareClickListener: RentalFareClickListener,
    var rentalFareList: ArrayList<Bike.Pricing_options?>?,
    var selectedPricingOptionIndex: Int
) : RecyclerView.Adapter<RentalFareAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rental_fare, parent, false)
        return ViewHolder(
            view

        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        if (rentalFareList != null) {
            holder.ct_item_rental_fare_value_in_rental_fare.text = getRentalFare(mContext,rentalFareList!![position]!!)
        }

        holder.itemView.setOnClickListener {
            selectedPricingOptionIndex = position
            rentalFareClickListener.onRentalFareSelected(position)
            notifyDataSetChanged()
        }

        if(selectedPricingOptionIndex != position){
            holder.iv_item_select_in_rental_fare.setImageResource(0)
        }else{
            holder.iv_item_select_in_rental_fare.setImageResource(R.drawable.check_mark)
        }
    }

    override fun getItemCount(): Int {
        return rentalFareList!!.size
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        var iv_item_select_in_rental_fare: ImageView =
            itemView.findViewById(R.id.iv_item_select_in_rental_fare)
        var ct_item_rental_fare_value_in_rental_fare: CustomTextView =
            itemView.findViewById(R.id.ct_item_rental_fare_value_in_rental_fare)

    }

    fun reset(){
        selectedPricingOptionIndex =-1
        notifyDataSetChanged()
    }


}