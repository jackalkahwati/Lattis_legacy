package com.lattis.lattis.presentation.utils

import android.content.Context
import android.text.TextUtils
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Reservation
import com.lattis.lattis.utils.UtilsHelper
import java.text.SimpleDateFormat

object BikeFareUtil {


    fun getRentalFare(context: Context,pricingOptions: Bike.Pricing_options?):String{
        if(pricingOptions==null)return ""
        return CurrencyUtil.getCurrencySymbolByCode(pricingOptions.price_currency,pricingOptions?.price
            .toString()) + " / " +
                 LocaleTranslatorUtils.getLocaleString(
            context,
            pricingOptions?.duration_unit,
                     pricingOptions?.duration
                         .toString()
        ).toString()
    }

    fun getRentalTimeLimit(context: Context,reservation: Reservation?):String?{
        if(reservation!=null &&
            !TextUtils.isEmpty(reservation.reservation_start) &&
            !TextUtils.isEmpty(reservation.reservation_end) ){

            val reservation_start = UtilsHelper.getDateTimeWithAtLabel(
                context, UtilsHelper.dateFromUTC(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reservation?.reservation_start)
                )
            )

            val reservation_end = UtilsHelper.getDateTimeWithAtLabel(
                context, UtilsHelper.dateFromUTC(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reservation?.reservation_end)
                )
            )

            if(!TextUtils.isEmpty(reservation_start)  &&  !TextUtils.isEmpty(reservation_end)){
                return UtilsHelper.getDurationFromNowInHourMin(
                    context, UtilsHelper.dateFromUTC(
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reservation?.reservation_start)
                    )!!
                )
            }
        }
        return null
    }


}