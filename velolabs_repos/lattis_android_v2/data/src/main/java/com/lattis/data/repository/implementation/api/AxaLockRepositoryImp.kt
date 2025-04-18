package com.lattis.data.repository.implementation.api

import com.lattis.data.entity.body.axa.GetKeyFromCloudIdBody
import com.lattis.data.net.axa.AxaApiClient
import com.lattis.domain.models.axa.AxaKey
import com.lattis.domain.repository.AxaLockRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class AxaLockRepositoryImp @Inject constructor(
    val axaApiClient: AxaApiClient
) : AxaLockRepository{
    override fun getAxaKey(lockId: String): Observable<AxaKey> {
        return axaApiClient.api.getCloudIdFromLockId(lockId)
            .flatMap {
                axaApiClient.api.getKeyFromCloudId(GetKeyFromCloudIdBody(24,"otp",255,true,"Order #2016-690"), it.result?.get(0)?.id!!)
                    .map {
                        it.result
                    }
            }
    }
}