package com.lattis.domain.models

data class FirebasePushNotification(
    val title: String?,
    val body : String?,
    val titleLocKey : String?,
    val bodyLocKey : String?,
    val clickAction : String?,
){
    companion object{
        val docking = "docking"
        val docked = "docked"
        val locked = "locked"
        val sentinel_lock_online = "sentinel_lock_online"
        val sentinel_lock_closed = "sentinel_lock_closed"
        val sentinel_lock_opened = "sentinel_lock_opened"
    }
}