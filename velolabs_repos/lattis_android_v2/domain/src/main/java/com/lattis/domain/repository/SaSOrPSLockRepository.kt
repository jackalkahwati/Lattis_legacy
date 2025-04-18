package com.lattis.domain.repository

import com.lattis.domain.models.sasorpslock.SaSOrPSLockUnlockTokenResponse
import io.reactivex.rxjava3.core.Observable

interface SaSOrPSLockRepository {
//    fun getUnlockToken(device_id:String,nonce:String):Observable<SaSOrPSLockUnlockToken>
    fun getUnlockToken(fleetId:Int,device_id:String,nonce:String):Observable<SaSOrPSLockUnlockTokenResponse>
}