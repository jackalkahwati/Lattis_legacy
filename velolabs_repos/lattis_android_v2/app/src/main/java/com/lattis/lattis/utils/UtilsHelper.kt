package com.lattis.lattis.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.lattis.lattis.presentation.utils.LocaleTranslatorUtils
import io.lattis.lattis.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object UtilsHelper {


    fun getDateTimeCurrentTimeZone(
        context: Context,
        timestamp: Long
    ): String {
        try {
            val calendar = Calendar.getInstance()
            val tz = TimeZone.getTimeZone("UTC")
            calendar.timeInMillis = timestamp * 1000
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.timeInMillis))
            val currenTimeZone = calendar.time as Date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy")
            val timeFormat = SimpleDateFormat("hh:mm a")
            return dateFormat.format(currenTimeZone) + " | " + timeFormat.format(
                currenTimeZone
            )
        } catch (e: java.lang.Exception) {
        }
        return ""
    }

    fun getDateTimeWithAtLabel(
        context: Context,
        date: Date?
    ): String? {
        if(date!=null) {
            try {
                val dateFormat = SimpleDateFormat("MMM dd")
                val timeFormat = SimpleDateFormat("hh:mm a")
                return dateFormat.format(date) + " " + context.getString(R.string.at_label) + " " + timeFormat.format(
                    date
                )
            } catch (e: java.lang.Exception) {
            }
        }
        return ""
    }

    fun isDateToday(inputDateString:String?):Boolean{
        if(inputDateString==null) return false
        var inputDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(inputDateString)
        val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd")
        val inputDateString: String =dateOnlyFormat .format(inputDate)
        var inputDateOnly = dateOnlyFormat.parse(inputDateString)
        val currentDate: Date = dateOnlyFormat.parse(dateOnlyFormat.format(Date()))
        return (inputDateOnly.compareTo(currentDate)==0)
    }

    fun getDateOnly(
        date: Date
    ): String? {
        try {
            val cal = Calendar.getInstance()
            cal.time = date
            val day = cal[Calendar.DATE]
            return if (!(day > 10 && day < 19)) when (day % 10) {
                1 -> SimpleDateFormat("EEE MMM d'st'").format(date)
                2 -> SimpleDateFormat("EEE MMM d'nd'").format(date)
                3 -> SimpleDateFormat("EEE MMM d'rd'").format(date)
                else -> SimpleDateFormat("EEE MMM d'th'").format(date)
            } else SimpleDateFormat("EEE MMM d'th'").format(date)
        } catch (e: java.lang.Exception) {
        }
        return ""
    }


    fun getDateOnlyWithYear(
        date: Date
    ): String? {
        try {
            val cal = Calendar.getInstance()
            cal.time = date
            val day = cal[Calendar.DATE]
            return if (!(day > 10 && day < 19)) when (day % 10) {
                1 -> SimpleDateFormat("EEE MMM d'st', yyyy").format(date)
                2 -> SimpleDateFormat("EEE MMM d'nd', yyyy").format(date)
                3 -> SimpleDateFormat("EEE MMM d'rd', yyyy").format(date)
                else -> SimpleDateFormat("EEE MMM d'th', yyyy").format(date)
            } else SimpleDateFormat("EEE MMM d'th', yyyy").format(date)
        } catch (e: java.lang.Exception) {
        }
        return ""
    }


    fun getTimeOnly(
        date: Date
    ): String? {
        try {
            val timeFormat = SimpleDateFormat("hh:mma")
            return timeFormat.format(date)
        } catch (e: java.lang.Exception) {
        }
        return ""
    }


    fun getDurationFromNowInHourMin(context: Context,inputDate: Date):String{
        try {
            return getDurationBreakdownWithHourMin(context,(inputDate.time - Date().time)/1000)
        }catch (exception:Exception){

        }
        return ""
    }

    fun getDurationFromNow(inputDate: Date):String{
        try {
            return getDurationBreakdown((inputDate.time - Date().time)/1000)
        }catch (exception:Exception){

        }
        return ""
    }


    fun getDateCurrentTimeZone(
        context: Context,
        timestamp: Long
    ): String {
        try {
            val calendar = Calendar.getInstance()
            val tz = TimeZone.getTimeZone("UTC")
            calendar.timeInMillis = timestamp * 1000
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.timeInMillis))
            val currenTimeZone = calendar.time as Date
            return getFormattedDate(currenTimeZone)
//            val dateFormat = SimpleDateFormat("dd-MMM-yyyy")
//            val timeFormat = SimpleDateFormat("hh:mm a")
//            return dateFormat.format(currenTimeZone)    + " " + context.getString(R.string.at_label) + " " + timeFormat.format(currenTimeZone)
        } catch (e: Exception) {
        }
        return ""
    }

    fun getFormattedDate(date: Date): String {
        val cal = Calendar.getInstance()
        cal.time = date
        val day = cal[Calendar.DATE]
        return if (!(day > 10 && day < 19)) when (day % 10) {
            1 -> SimpleDateFormat("d'st' MMMM yyyy").format(date)
            2 -> SimpleDateFormat("d'nd' MMMM yyyy").format(date)
            3 -> SimpleDateFormat("d'rd' MMMM yyyy").format(date)
            else -> SimpleDateFormat("d'th' MMMM yyyy").format(date)
        } else SimpleDateFormat("d'th' MMMM yyyy").format(date)
    }

    fun getTimeFromDuration(duration: String?): String {
        try {
            if (duration != null && !duration.isEmpty()) {
                return convertSecondsToHMmSs(duration.toLong())
            }
        } catch (e: Exception) {
            Log.e("getTimeFromDuration", e.localizedMessage)
        }
        return ""
    }

    fun convertSecondsToHMmSs(seconds: Long): String {
        val s = seconds % 60
        val m = seconds / 60 % 60
        val h = seconds / (60 * 60)
        return if (h == 0L) {
            String.format("%02d:%02d", m, s)
        } else {
            String.format("%d:%02d:%02d", h, m, s)
        }
    }


    fun getDurationBreakdownWithHourMin(
        context: Context,
        millis: Long
    ): String {
        var millis = millis
        millis = millis * 1000
        if (millis < 0) { //throw new IllegalArgumentException("Duration must be greater than zero!");
            return ""
        }
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        val sb = StringBuilder(64)
        if (hours != 0L) {
            sb.append(LocaleTranslatorUtils.getLocaleString(context,"hours",hours.toString()))
        }
        sb.append(" ")
        sb.append(LocaleTranslatorUtils.getLocaleString(context,"minutes",minutes.toString()))

        return sb.toString()
    }

    fun getDurationBreakdown(
        millis: Long
    ): String {
        var millis = millis
        millis = millis * 1000
        if (millis < 0) { //throw new IllegalArgumentException("Duration must be greater than zero!");
            return ""
        }
        //        long days = TimeUnit.MILLISECONDS.toDays(millis);
//        millis -= TimeUnit.DAYS.toMillis(days);
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        val sb = StringBuilder(64)
        //        if (days != 0) {
//            sb.append(days);
//            sb.append(" " + context.getString(R.string.days) + " ");
//        }
        if (hours != 0L) {
            if (hours < 10) {
                sb.append("0$hours")
            } else {
                sb.append(hours)
            }
            sb.append(":")
        }
        if (minutes < 10) {
            sb.append("0$minutes")
        } else {
            sb.append(minutes)
        }

        sb.append(":")
        if (seconds < 10) {
            sb.append("0$seconds")
        } else {
            sb.append(seconds)
        }

        return sb.toString()
    }

    fun getTime(): Long
    {
            val date = Date()
            return date.time / 1000
    }

    fun getDotAfterNumber(number: String?): String {
        return if (number != null) {
            if (number.indexOf(".") != -1) {
                number
            } else {
                "$number.00"
            }
        } else {
            ""
        }
    }

    fun getBitmapFromVectorDrawable(
        context: Context?,
        drawableId: Int
    ): Bitmap {
        var drawable =
            ContextCompat.getDrawable(context!!, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun getAppAccentColor(context: Context?): String {
        return "#" + Integer.toHexString(
            ContextCompat.getColor(
                context!!,
                R.color.lattis_accent_color
            ) and 0x00ffffff
        )
    }

    fun addMinutesToJavaUtilDate(date: Date?, minutes: Int?): Date? {
        if(minutes==null)
            return date

        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MINUTE, minutes)
        return calendar.time
    }

    fun isPeriod(ISO8601Duration:String):Boolean{
        return ISO8601Duration.contains("M",true) || ISO8601Duration.contains("D",true)
    }

    fun addYearsMonthsDaysToJavaUtilDate(date: Date?, years: Int?, months: Int?, days: Int?): Date? {
        if((days==null && months== null && years == null) || date == null)
            return date

        val calendar = Calendar.getInstance()
        calendar.time = date
        days?.let {
            calendar.add(Calendar.DAY_OF_MONTH, it)
        }

         months?.let {
             calendar.add(Calendar.MONTH, it)
         }

        years?.let {
            calendar.add(Calendar.YEAR, it)
        }


        return calendar.time
    }

    fun dateFromUTC(date: Date): Date? {
        return Date(date.time + Calendar.getInstance().timeZone.getOffset(date.time))
    }

    fun dateToUTC(date: Date): Date? {
        return Date(date.time - Calendar.getInstance().timeZone.getOffset(date.time))
    }

    fun getCurrentDateFromString(dateStr:String):String?{
        val formatter =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        return SimpleDateFormat("yyyy-MM-dd").format(dateFromUTC(formatter.parse(dateStr)))


    }


    val integerChars = '0'..'9'

    fun isNumber(input: String): Boolean {
        var dotOccurred = 0
        return input.all { it in integerChars || it == '.' && dotOccurred++ < 1 }
    }

    fun isInteger(input: String) = input.all { it in integerChars }


    fun getAlarmTimeMinusMinutes(alarmDateTime: Date?,minusMinutes:Int): Long?
    {

        if(alarmDateTime==null)
            return null

        val c = Calendar.getInstance()
        val currentTime = c.time

        c.time = alarmDateTime
        c.add(Calendar.MINUTE, -minusMinutes);
        val alarmDateTimeMinus30Min = c.time

        return if(alarmDateTimeMinus30Min.compareTo(currentTime)<0 ){
            c.time = alarmDateTime
            c.timeInMillis
        }else if(alarmDateTimeMinus30Min.compareTo(currentTime)==0){
            c.time = alarmDateTime
            c.add(Calendar.SECOND,5 );
            c.timeInMillis
        }else{
            c.timeInMillis
        }
    }


    fun isDate2GreaterThanDate1(date1String:String?,date2String:String?):Boolean{
        if(!TextUtils.isEmpty(date2String) && !TextUtils.isEmpty(date1String)){
            try {
                var date1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date1String)
                var date2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date2String)
                return date2.after(date1)
            }catch (e:Exception){

            }
        }
        return false
    }

}