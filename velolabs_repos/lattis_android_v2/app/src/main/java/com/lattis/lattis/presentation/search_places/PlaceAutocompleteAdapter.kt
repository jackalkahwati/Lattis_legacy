package com.lattis.lattis.presentation.search_places

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lattis.domain.models.map.PlaceAutocomplete
import com.lattis.lattis.presentation.customview.CustomTextView
import com.lattis.lattis.presentation.search_places.PlaceAutocompleteAdapter.PlaceViewHolder
import com.lattis.lattis.utils.ResourceHelper.getBikeResource
import io.lattis.lattis.R
import java.util.*

class PlaceAutocompleteAdapter(var mContext: Context) :
    RecyclerView.Adapter<PlaceViewHolder>() {
    interface PlaceAutoCompleteInterface {
        fun onPlaceClick(
            mResultList: ArrayList<PlaceAutocomplete>?,
            position: Int
        )
    }

    var mListener: PlaceAutoCompleteInterface
    var mResultList: ArrayList<PlaceAutocomplete>? = null

    /*
    Clear List items
     */
    fun clearList() {
        if (mResultList != null && mResultList!!.size > 0) {
            mResultList!!.clear()
        }
    }

    fun setSearchResult(results: ArrayList<PlaceAutocomplete>?) {
        mResultList = results
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PlaceViewHolder {
        val layoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val convertView =
            layoutInflater.inflate(R.layout.activity_search_places_item, viewGroup, false)
        return PlaceViewHolder(convertView)
    }

    override fun onBindViewHolder(mPredictionHolder: PlaceViewHolder, i: Int) {

        if(mResultList!![i].bike==null){
            mPredictionHolder.mAddress1.text = mResultList!![i].address1
            mPredictionHolder.mAddress2.text = mResultList!![i].address2
            mPredictionHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.search_location_icon))
        }else{
            mPredictionHolder.mIcon.setImageDrawable(getBikeResource(mContext,mResultList!![i].bike?.type))
            mPredictionHolder.mAddress1.text = mResultList!![i].bike?.bike_name
            mPredictionHolder.mAddress2.text = mResultList!![i].bike?.fleet_name
        }

        mPredictionHolder.mParentLayout.setOnClickListener {
            mListener.onPlaceClick(
                mResultList,
                i
            )
        }
    }

    override fun getItemCount(): Int {
        return if (mResultList != null) mResultList!!.size else 0
    }

    fun getItem(position: Int): PlaceAutocomplete {
        return mResultList!![position]
    }

    inner class PlaceViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var mAddress1: CustomTextView
        var mAddress2: CustomTextView
        var mParentLayout: ConstraintLayout
        var mIcon : ImageView

        init {
            mAddress1 =
                itemView.findViewById<View>(R.id.ct_search_address1_in_search_places_item) as CustomTextView
            mAddress2 =
                itemView.findViewById<View>(R.id.ct_search_address2_in_search_places_item) as CustomTextView
            mParentLayout =
                itemView.findViewById<View>(R.id.search_places_item_parent) as ConstraintLayout

            mIcon = itemView.findViewById<View>(R.id.iv_search_location_in_search_places_item) as ImageView

        }
    }

    companion object {
        private const val TAG = "PlaceAutocompleteAdapter"
    }

    init {
        mListener = mContext as PlaceAutoCompleteInterface
    }
}