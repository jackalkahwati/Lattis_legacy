package com.lattis.domain.models

import android.os.Parcel
import android.os.Parcelable


class Card : Parcelable {
    var id: Int = 0
    var user_id: Int = 0
    var stripe_net_profile_id: String? = null
    var stripe_net_payment_id: String? = null
    var is_primary: Boolean = false
    var type_card: String? = null
    var cc_no: String? = null
    var exp_month: Int = 0
    var exp_year: Int = 0
    var fingerprint: String? = null
    var cc_type: String? = null
    var created_date: Int = 0
    var last_updated: String? = null
    var card_id : String?=null

    fun getIs_primary():Boolean{
        return is_primary;
    }


    protected constructor(`in`: Parcel) {
        id = `in`.readInt()
        user_id = `in`.readInt()
        stripe_net_profile_id = `in`.readString()
        stripe_net_payment_id = `in`.readString()
        is_primary = `in`.readByte().toInt() != 0
        type_card = `in`.readString()
        cc_no = `in`.readString()
        exp_month = `in`.readInt()
        exp_year = `in`.readInt()
        fingerprint = `in`.readString()
        cc_type = `in`.readString()
        created_date = `in`.readInt()
        last_updated = `in`.readString()
        card_id = `in`.readString()
    }

    constructor() {

    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(user_id)
        dest.writeString(stripe_net_profile_id)
        dest.writeString(stripe_net_payment_id)
        dest.writeByte((if (is_primary) 1 else 0).toByte())
        dest.writeString(type_card)
        dest.writeString(cc_no)
        dest.writeInt(exp_month)
        dest.writeInt(exp_year)
        dest.writeString(fingerprint)
        dest.writeString(cc_type)
        dest.writeInt(created_date)
        dest.writeString(last_updated)
        dest.writeString(card_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }


}
