package io.lattis.operator.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.lattis.operator.R
import io.lattis.operator.model.ChangeStatus
import io.lattis.operator.presentation.fleet.fragments.tickets.FleetDetailTicketAdapter

object ResourceUtils {

    fun convertStatus(context: Context,status:String?):String{
        return when(status) {
            "active"-> context.getString(R.string.status_live)
            "suspended" -> context.getString(R.string.status_out_of_service)
            "deleted" -> context.getString(R.string.status_archive)
            "inactive" -> context.getString(R.string.status_staging)
            else ->{
                if(TextUtils.isEmpty(status))
                    ""
                else
                    status!!
            }
        }
    }

    fun convertUsage(context: Context,usage:String?):String{
        return when(usage){
            "parked" -> context.getString(R.string.parked)
            "on_trip" -> context.getString(R.string.on_trip)
            "stolen" -> context.getString(R.string.stolen)
            "lock_assigned" -> context.getString(R.string.equipment_assigned)
            "reported_stolen" -> context.getString(R.string.reported_stolen)
            "lock_not_assigned" -> context.getString(R.string.lock_not_assigned)
            "controller_assigned" -> context.getString(R.string.equipment_assigned)
            "damaged" -> context.getString(R.string.damaged)
            "under_maintenance" -> context.getString(R.string.under_maintenance)
            "collect" -> context.getString(R.string.collect)
            "balancing" -> context.getString(R.string.balancing)
            "defleet" -> context.getString(R.string.defleet)
            "total_loss" -> context.getString(R.string.total_loss)
            "stollen" -> context.getString(R.string.stolen)
            "transport" -> context.getString(R.string.transport)
            else ->{
                if(TextUtils.isEmpty(usage))
                    ""
                else
                    usage!!
            }
        }
    }

    fun convertCategory(context: Context,category:String?):String{
        return when(category){
            "parking_outside_geofence" -> context.getString(R.string.parking_outside_geofence)
            "issue_detected" -> context.getString(R.string.issue_detected)
            "damage_reported" -> context.getString(R.string.damage_reported)
            "service_due" -> context.getString(R.string.service_due)
            "reported_theft" -> context.getString(R.string.reported_theft)
            "low_battery" -> context.getString(R.string.low_battery)
            else ->{
                if(TextUtils.isEmpty(category))
                    ""
                else
                    category!!
            }
        }
    }


    fun returnCategoryMap(context: Context):Map<String,String>{
        return mapOf(
                "damage_reported" to context.getString(R.string.damage_reported),
                "reported_theft" to context.getString(R.string.reported_theft)
        )
    }

    fun downloadImage(
        context: Context,
        imageView: ImageView,
        url: String?
    ) {

        if(TextUtils.isEmpty(url)){
            return
        }

        val requestOptions = RequestOptions()
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(context)
            .load(url)
            .apply(requestOptions)
            .into(imageView)
    }


    fun changeStatusList(removeCurrentStatus:Boolean,context: Context,currentStatus:String?):Map<String,String>{
        val mapOfStatus = mutableMapOf<String,String>(
            "active" to context.getString(R.string.status_live),
            "suspended" to context.getString(R.string.status_out_of_service),
            "deleted" to context.getString(R.string.status_archive),
            "inactive" to context.getString(R.string.status_staging)
        )
        if(removeCurrentStatus && !TextUtils.isEmpty(currentStatus)){
            mapOfStatus.remove(currentStatus)
        }
        return mapOfStatus
    }

    fun returnSubStatusMapDependingUponStatus(context: Context,status:String):Map<String,String>? {
        return when(status){
            "active" ->{ subStatusForActive(context) }
            "suspended" -> { subStatusForSuspended(context)}
            "deleted" -> { subStatusForDeleted(context)}
            "inactive" -> { subStatusForInActive(context)}
            else -> null
        }
    }

    fun subStatusForDeleted(context: Context):Map<String,String> {
        val mapOfStatus = mutableMapOf<String, String>(
            "defleet" to context.getString(R.string.defleet),
            "total_loss" to context.getString(R.string.total_loss),
            "stolen" to context.getString(R.string.stolen)
        )
        return mapOfStatus
    }

    fun subStatusForActive(context: Context):Map<String,String> {
        val mapOfStatus = mutableMapOf<String, String>(
            "parked" to context.getString(R.string.parked),
            "collect" to context.getString(R.string.collect)
        )
        return mapOfStatus
    }

    fun subStatusForInActive(context: Context):Map<String,String> {
        val mapOfStatus = mutableMapOf<String, String>(
            "lock_assigned" to context.getString(R.string.equipment_assigned),
            "balancing" to context.getString(R.string.balancing)
        )
        return mapOfStatus
    }

    fun subStatusForSuspended(context: Context):Map<String,String> {
        val mapOfStatus = mutableMapOf<String, String>(
            "damaged" to context.getString(R.string.damaged),
            "under_maintenance" to context.getString(R.string.under_maintenance),
            "reported_stolen" to context.getString(R.string.reported_stolen),
            "transport" to context.getString(R.string.transport)
        )
        return mapOfStatus
    }


    fun returnChangeStatus(status: String,subStatus:String?):ChangeStatus?{
        return when(status){
            "inactive" -> ChangeStatus(status,subStatus,null)
            "active" -> ChangeStatus(status,subStatus,null)
            "suspended" -> ChangeStatus(status,subStatus,null)
            "deleted" -> ChangeStatus(status,subStatus,null)
            else -> null
        }
    }


    fun headTailOptionsList(context: Context):Map<String,String>{
        val mapOfHeadTailOptions = mutableMapOf<String,String>(
                "0" to context.getString(R.string.off),
                "1" to context.getString(R.string.on),
                "2" to context.getString(R.string.flicker)
        )
        return mapOfHeadTailOptions
    }


    fun soundOptionsList(context: Context):Map<String,String>{
        val mapOfHeadTailOptions = mutableMapOf<String,String>(
            context.getString(R.string.horn) to context.getString(R.string.horn),
            context.getString(R.string.on) to context.getString(R.string.on),
            context.getString(R.string.off) to context.getString(R.string.off)
        )
        return mapOfHeadTailOptions
    }



}