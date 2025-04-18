package com.lattis.data.database.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class RealmLock : RealmObject() {
    @Required
    @PrimaryKey
    var id: String? = null
    var macId: String? = null
    var lockId: String? = null
    var macAddress: String? = null
    var publicKey: String? = null
    var signedMessage: String? = null
    var name: String? = null
    var version: String? = null
    var revision: String? = null
    var serialNumber: String? = null
    var userId: String? = null
    var isSharedWithMe:Boolean? = false
    var isSharedWithOther:Boolean? = false
    var shareWithUserId: String? = null
    var shareId: String? = null
    var usersId: String? = null
    var lastLocation: RealmLocation? = null
    var connectedDate: Date? = null
    var lockedDate: Date? = null
    var isLocked:Boolean? = false
    var isAutoProximityLock:Boolean? = false
    var isAutoProximityUnlock:Boolean?  = false
    var alertMode: String? = null
    var isDefaultPinCode: Boolean? = false

    override fun toString(): String {
        return "RealmLock{" +
                "id='" + id + '\'' +
                ", macId='" + macId + '\'' +
                ", userId='" + userId + '\'' +
                ", signedMessage='" + signedMessage + '\'' +
                ", publicKey=" + publicKey +
                '}'
    }

    companion object {
        const val COLUMN_NAME_LOCK_ID = "lockId"
        const val COLUMN_NAME_MAC_ID = "macId"
        const val COLUMN_NAME_USER_ID = "userId"
        const val COLUMN_NAME_LAST_LOCKED_DATE = "lockedDate"
        const val COLUMN_NAME_LAST_CONNECTED_DATE = "connectedDate"
    }
}