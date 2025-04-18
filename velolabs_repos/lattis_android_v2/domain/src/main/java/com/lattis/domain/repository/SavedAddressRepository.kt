package com.lattis.domain.repository

import com.lattis.domain.models.SavedAddress
import io.reactivex.rxjava3.core.Observable

interface SavedAddressRepository {
    fun saveAddresses(savedAddress: List<SavedAddress>): Observable<Boolean>
    fun deleteSavedAddress(): Observable<Boolean>
    val savedAddresses: Observable<List<SavedAddress>>?
}