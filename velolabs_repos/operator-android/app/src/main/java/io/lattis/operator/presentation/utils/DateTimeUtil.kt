package io.lattis.operator.presentation.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtil {

    val ticketCreatedDateFormat = "dd MMM yyyy, hh:mm a"

    fun getDateFromUnixTimeStamp(dateEpoc: Long?,dateFormat:String): String? {
        if(dateEpoc!=null) {
            try {
                val sdf = SimpleDateFormat(dateFormat)
                val netDate = Date(dateEpoc * 1000)
                return sdf.format(netDate)
            } catch (e: Exception) {

            }
        }
        return ""
    }
}