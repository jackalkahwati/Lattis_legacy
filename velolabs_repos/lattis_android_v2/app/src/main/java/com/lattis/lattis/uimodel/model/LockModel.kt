package com.lattis.lattis.uimodel.model

import com.lattis.domain.models.Alert
import com.lattis.domain.models.Location
import org.parceler.Parcel
import java.util.*

@Parcel
open class LockModel {
    var lockId: String? = null
    var macAddress: String? = null
    var macId: String? = null
    var userId: String? = null
    var name: String? = null
    var version: String? = null
    var serialNumber: String? = null
    var usersId: String? = null
    var sharedWithUserId: String? = null
    var shareId: String? = null
    var isSharedWithMe:Boolean? = false
    var isSharedWithOther:Boolean? = false
    var lastLocation: Location? = null
    var connectedDate: Date? = null
    var lockedDate: Date? = null
    var isLocked:Boolean? = false
    var isUseDefaultPinCode:Boolean? = false
    var alert: Alert? = null
    var isAutoProximityLock:Boolean? = false
    var isAutoProximityUnlock:Boolean? = false
    var publicKey: String? = null
    var signedMessage: String? = null


    override fun toString(): String {
        return "LockModel{" +
                "lockId='" + lockId + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", macId='" + macId + '\'' +
                ", userId='" + userId + '\'' +
                ", usersId='" + usersId + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", signedMessage=" + signedMessage +
                ", publicKey=" + publicKey +
                ", LockedDate=" + lockedDate +
                '}'
    }
}