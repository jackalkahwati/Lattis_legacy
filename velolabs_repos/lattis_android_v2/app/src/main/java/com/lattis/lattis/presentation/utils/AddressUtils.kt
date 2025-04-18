package com.lattis.lattis.presentation.utils

import android.content.Context
import android.location.Geocoder
import com.lattis.domain.models.Location
import io.lattis.lattis.R
import java.util.*

object AddressUtils {
    fun getAddress(
        context: Context,
        location: Location
    ): String {
        var strAdd = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses =
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null) {
                val returnedAddress = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                return if (strAdd == "") {
                    context.getString(R.string.pickup_location)
                } else {
                    strAdd
                }
            } else {
            }
        } catch (e: Exception) {

        }
        return context.getString(R.string.pickup_location)
    }
}