package com.lattis.lattis.presentation.payment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lattis.domain.models.Promotion
import com.lattis.lattis.presentation.customview.CustomTextView
import io.lattis.lattis.R

class PromotionListAdapter(
    var mContext: Context,
    var promotions: List<Promotion>?
) : RecyclerView.Adapter<PromotionListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_payment_promotion_item_list, parent, false)
        return ViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        if (promotions != null) {
            holder.ct_fleet_name_in_payment_promotions_list.text = promotions!![position]?.fleet?.fleet_name
            holder.ct_discount_description_in_payment_promotions_list.text = mContext.getString(R.string.perk_template_bike,promotions!![position]?.amount!!)
        }
    }

    override fun getItemCount(): Int {
        return promotions!!.size
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){
        var ct_fleet_name_in_payment_promotions_list:CustomTextView = itemView.findViewById(R.id.ct_fleet_name_in_payment_promotions_list)
        var ct_discount_description_in_payment_promotions_list:CustomTextView = itemView.findViewById(R.id.ct_discount_description_in_payment_promotions_list)
    }

}