package com.lattis.lattis.presentation.payment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.lattis.domain.models.Card
import com.lattis.lattis.presentation.customview.CustomTextView
import io.lattis.lattis.R

class UserCardListAdapter(
    var mContext: Context,
    var cardList: List<Card>?,
    var isSetClickEvent: Boolean,
    var userCardListListener: PaymentCardClickListener
) : RecyclerView.Adapter<UserCardListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_payment_item_card_list, parent, false)
        return ViewHolder(
            view,
            isSetClickEvent
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        if (cardList != null) {
            var number = cardList!![position].cc_no
            holder.ct_card_number_in_payment_card_list!!.text = cardList!!.get(position).cc_type+" "+number!!.substring(number.length - 4, number.length)
            val cardName = cardList!![position].cc_type!!.toUpperCase().replace(" ", "_")
            holder.iv_card_list_item!!.setImageResource(getResource(cardName))
            holder.iv_check_mark_in_payment_card_list!!.isChecked = cardList!![position].getIs_primary()
            holder.iv_check_mark_in_payment_card_list!!.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    userCardListListener.onCardCheckBoxClicked(position)
                } else {
                    holder.iv_check_mark_in_payment_card_list!!.isChecked = true
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return cardList!!.size
    }

    inner class ViewHolder(
        itemView: View,
        isSetClickEvent: Boolean
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var iv_card_list_item:ImageView = itemView.findViewById(R.id.iv_card_list_item)
        var ct_card_number_in_payment_card_list:CustomTextView = itemView.findViewById(R.id.ct_card_number_in_payment_card_list)
        var iv_check_mark_in_payment_card_list : CheckBox = itemView.findViewById(R.id.iv_check_mark_in_payment_card_list)


        override fun onClick(v: View) {
            userCardListListener.onCardClicked(adapterPosition)
        }

        init {
            if (isSetClickEvent) itemView.setOnClickListener(this)
        }
    }

    private fun getResource(card_type: String): Int {
        when (card_type) {
            "VISA" -> return R.drawable.bt_ic_visa
            "MASTERCARD" -> return R.drawable.bt_ic_mastercard
            "DISCOVER" -> return R.drawable.bt_ic_discover
            "AMERICAN_EXPRESS" -> return R.drawable.bt_ic_amex
            "MAESTRO" -> return R.drawable.bt_ic_maestro
            "DINERS_CLUB" -> return R.drawable.bt_ic_diners_club
            "UNIONPAY" -> return R.drawable.bt_ic_unionpay
            "JCB" -> return R.drawable.bt_ic_jcb
        }
        return R.drawable.bt_ic_unknown
    }

}