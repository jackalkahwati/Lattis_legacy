package com.lattis.domain.repository

import com.lattis.domain.models.axa.AxaKey
import io.reactivex.rxjava3.core.Observable

interface AxaLockRepository {
    fun getAxaKey(lockId: String): Observable<AxaKey>
}