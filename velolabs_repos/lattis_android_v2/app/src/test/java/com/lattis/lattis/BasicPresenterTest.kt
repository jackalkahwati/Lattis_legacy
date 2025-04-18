package com.lattis.lattis

import android.util.Log
import com.lattis.lattis.utils.UtilsHelper
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.Period
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class BasicPresenterTest {

    @Test
    fun testISO1806Date(){
        var minReservationDuration = "PT8H"
        var minimumDate:Date?=null
        if(minReservationDuration!!.contains("T",true)){
            val min_duration_minute = Duration.parse("PT".plus(minReservationDuration!!.substringAfter("T")))
            minimumDate = UtilsHelper.addMinutesToJavaUtilDate(
                Date(),
                min_duration_minute.toMinutes().toInt()
            )
            minReservationDuration = minReservationDuration.substringBefore("T")
        }

        if(!minReservationDuration.equals("P",true)){   // There are months days
            val min_period = Period.parse(minReservationDuration)
            minimumDate = UtilsHelper.addYearsMonthsDaysToJavaUtilDate(
                if (minimumDate == null) Date() else minimumDate,
                min_period.years,min_period.months,min_period.days
            )
        }

        Log.e("",minimumDate.toString())
    }


    @Test
    fun isSameDate(){
        var inputDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("2020-09-24T11:00:00Z")
        val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd")
        val inputDateString: String =dateOnlyFormat .format(inputDate)
        var inputDateOnly = dateOnlyFormat.parse(inputDateString)

        val currentDate: Date = dateOnlyFormat.parse(dateOnlyFormat.format(Date()))
        if(inputDateOnly.compareTo(currentDate)>0){
            print("Date1 is after Date2");
        }else if(inputDateOnly.compareTo(currentDate)<0){
            print("Date1 is before Date2");
        }else if(inputDateOnly.compareTo(currentDate)==0){
            print("Date1 is equal to Date2");
        }
    }


    @Test
    fun testCurrencyFormat(){
        val format: NumberFormat = NumberFormat.getInstance()
        val currency = Currency.getInstance("USD")
        format.setMaximumFractionDigits(currency.getDefaultFractionDigits())
        format.setCurrency(currency)

        System.out.println(format.format(1))
    }

}