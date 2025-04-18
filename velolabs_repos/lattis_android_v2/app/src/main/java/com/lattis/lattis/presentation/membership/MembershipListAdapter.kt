package com.lattis.lattis.presentation.membership

import android.content.Context
import android.text.TextUtils
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
import com.lattis.domain.models.Membership
import com.lattis.domain.models.PrivateNetwork
import com.lattis.domain.models.Ride
import com.lattis.lattis.presentation.customview.CustomTextView
import com.lattis.lattis.presentation.utils.CurrencyUtil
import com.lattis.lattis.presentation.utils.IsRidePaid
import com.lattis.lattis.presentation.utils.LocaleTranslatorUtils
import io.lattis.lattis.R
import java.util.*
import kotlin.collections.ArrayList

class MembershipListAdapter(
    var mContext: Context,
    var memberships: List<Membership>,
    var alreadyMember:Boolean,
    var showOnlyOne:Boolean,
    var membershipActionListener: MembershipActionListener
) : RecyclerView.Adapter<MembershipListAdapter.ViewHolder>(), Filterable {

    var membershipsFilterList : List<Membership>

    init {
        membershipsFilterList = memberships
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_membership_list_item, parent, false)
        return ViewHolder(
            view

        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        if (membershipsFilterList != null) {
            holder.ct_fleet_name_in_membership_list_item.text = membershipsFilterList!![position]?.fleet?.fleet_name
            holder.ct_fleet_address_in_membership_list_item .text = membershipsFilterList!![position].fleet?.address?.city + ", " + membershipsFilterList!![position].fleet?.address?.country

            if(alreadyMember){
                holder.ct_fleet_price_in_membership_list_item.visibility = View.GONE
                holder.iv_next_arrow.visibility = View.VISIBLE
                holder.iv_next_arrow.setOnClickListener {
                    membershipActionListener?.onMembershipClicked(position,alreadyMember)
                }
            }else{
                holder.ct_fleet_price_in_membership_list_item.visibility = View.VISIBLE
                holder.ct_fleet_price_in_membership_list_item.text = getMembershipPrice(membershipsFilterList!![position])
                holder.iv_next_arrow.visibility = View.GONE
                holder.ct_fleet_price_in_membership_list_item.setOnClickListener {
                    membershipActionListener?.onMembershipClicked(position,alreadyMember)
                }
            }


            if(membershipsFilterList!![position]?.fleet?.logo!=null) {
                downloadImage(
                    holder,
                    membershipsFilterList!![position]?.fleet?.logo
                )
            }


        }
    }

    override fun getFilter(): Filter {
        return doMembershipsFilter
    }

    private val doMembershipsFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val charSearch = constraint.toString()
            if (charSearch.isEmpty()) {
                membershipsFilterList = memberships
            } else {
                val resultList = ArrayList<Membership>()
                for (membership in memberships) {
                    if (membership?.fleet?.fleet_name?.toLowerCase(Locale.ROOT)!!.contains(charSearch.toLowerCase(
                            Locale.ROOT))) {
                        resultList.add(membership)
                    }
                }
                membershipsFilterList = resultList
            }
            val filterResults = FilterResults()
            filterResults.values = membershipsFilterList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            membershipsFilterList = results?.values as ArrayList<Membership>
            notifyDataSetChanged()
        }
    }



    fun getMembershipPrice(membership: Membership):String{
        if (!TextUtils.isEmpty(membership.membership_price) &&
            !TextUtils.isEmpty(membership.membership_price_currency) &&
            !TextUtils.isEmpty(membership.payment_frequency)  ) {
                return CurrencyUtil.getCurrencySymbolByCode(membership.membership_price_currency,membership.membership_price
                        .toString()) + " / " + LocaleTranslatorUtils.getLocaleString(
                                mContext,
                        membership.payment_frequency
                    ).toString()
        }
        return "";
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
            .into(holder.iv_fleet_image_in_membership_list_item)
    }

    override fun getItemCount(): Int {
        return if(showOnlyOne) 1 else membershipsFilterList!!.size
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        var iv_fleet_image_in_membership_list_item: ImageView =
            itemView.findViewById(R.id.iv_fleet_image_in_membership_list_item)
        var ct_fleet_name_in_membership_list_item: CustomTextView =
            itemView.findViewById(R.id.ct_fleet_name_in_membership_list_item)
        var ct_fleet_address_in_membership_list_item: CustomTextView =
            itemView.findViewById(R.id.ct_fleet_address_in_membership_list_item)

        var ct_fleet_price_in_membership_list_item: CustomTextView =
            itemView.findViewById(R.id.ct_price_in_membership_list_item)

        var iv_next_arrow : ImageView =
            itemView.findViewById(R.id.iv_next_in_membership_list_item)

    }


}