package com.lattis.lattis.presentation.ride

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lattis.domain.models.RideSummary
import com.lattis.lattis.presentation.customview.CustomTextView
import com.lattis.lattis.presentation.utils.CurrencyUtil
import io.lattis.lattis.R

class RideSummaryTaxesAdapter(
    var mContext: Context,
    var taxes: List<RideSummary.Tax>,
    var currency:String?=null,
) : RecyclerView.Adapter<RideSummaryTaxesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_ride_summary_taxes_item, parent, false)
        return ViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        if (taxes != null) {
            val taxNumber = position+1
            holder.ct_tax_name_in_ride_summary.text = mContext.getString(R.string.tax)+ " " + taxNumber + "  " +taxes!![position]?.name
            holder.ct_tax_amount_in_ride_summary.text = CurrencyUtil.getCurrencySymbolByCode(currency,taxes!![position]?.amount)
        }
    }

    override fun getItemCount(): Int {
        return taxes.size
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){
        var ct_tax_name_in_ride_summary:CustomTextView = itemView.findViewById(R.id.ct_tax_name_in_ride_summary)
        var ct_tax_amount_in_ride_summary:CustomTextView = itemView.findViewById(R.id.ct_tax_amount_in_ride_summary)
    }

}