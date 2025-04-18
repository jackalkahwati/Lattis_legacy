package com.lattis.data.repository.implementation.api

import com.lattis.data.net.sasorpslock.SaSOrPSLockApiClient
import com.lattis.domain.models.sasorpslock.SaSOrPSLockUnlockTokenResponse
import com.lattis.domain.repository.SaSOrPSLockRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SaSOrPSLockRepositoryImp @Inject constructor(
    val saSOrPSLockApiClient: SaSOrPSLockApiClient
) : SaSOrPSLockRepository{
//    override fun getUnlockToken(device_id:String,nonce: String): Observable<SaSOrPSLockUnlockToken> {
//        return saSOrPSLockApiClient.api.getUnlockToken(device_id,nonce)
//    }

    override fun getUnlockToken(
        fleetId: Int,
        device_id: String,
        nonce: String
    ): Observable<SaSOrPSLockUnlockTokenResponse> {
        return saSOrPSLockApiClient.api.getUnlockToken(device_id,nonce,fleetId)
    }
}