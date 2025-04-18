package com.lattis.data.repository.implementation.api

import com.lattis.data.database.store.SavedAddressDataStore
import com.lattis.domain.repository.SavedAddressRepository
import com.lattis.domain.models.SavedAddress
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SavedAddressRepositoryImp @Inject constructor(private val savedAddressDataStore: SavedAddressDataStore) :
    SavedAddressRepository {
    override fun saveAddresses(savedAddress: List<SavedAddress>): Observable<Boolean> {
        return savedAddressDataStore.saveSavedAddresses(savedAddress)
    }

    override val savedAddresses: Observable<List<SavedAddress>>
        get() = savedAddressDataStore.savedAddress

    override fun deleteSavedAddress(): Observable<Boolean> {
        return savedAddressDataStore.deleteSavedAddress()
    }

}