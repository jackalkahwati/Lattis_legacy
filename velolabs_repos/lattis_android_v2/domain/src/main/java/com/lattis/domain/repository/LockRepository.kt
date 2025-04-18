package com.lattis.domain.repository

import com.lattis.domain.models.SignedMessageAndPublicKey
import com.lattis.domain.models.Lock
import io.reactivex.rxjava3.core.Observable

interface LockRepository {
    fun getSignedMessagePublicKey(
        bike_id: Int,
        macId: String
    ): Observable<SignedMessageAndPublicKey>

    fun createOrUpdateLock(lock: Lock): Observable<Lock>
    fun getLock(): Observable<Lock>
    fun deleteLock(): Observable<Boolean>

    fun getTapkeyAccess(
        fleet_id: Int,
        mac_id: String
    ): Observable<SignedMessageAndPublicKey>
}