package com.lattis.data.database.mapper

import com.lattis.data.database.base.AbstractRealmDataMapper
import com.lattis.data.database.model.RealmSavedAddress
import com.lattis.domain.models.SavedAddress
import javax.inject.Inject

class RealmSavedAddressMapper @Inject constructor() :
    AbstractRealmDataMapper<SavedAddress, RealmSavedAddress>() {
    override fun mapIn(savedAddress: SavedAddress): RealmSavedAddress {
        val realmSavedAddress = RealmSavedAddress()
        realmSavedAddress.id = savedAddress.id
        realmSavedAddress.latitude = savedAddress.latitude
        realmSavedAddress.longitude = savedAddress.longitude
        realmSavedAddress.address1 = savedAddress.address1
        realmSavedAddress.address2 = savedAddress.address2
        return realmSavedAddress
    }

    override fun mapOut(realmSavedAddress: RealmSavedAddress): SavedAddress {
        val savedAddress = SavedAddress()
        savedAddress.id = realmSavedAddress.id
        savedAddress.latitude = realmSavedAddress.latitude
        savedAddress.longitude = realmSavedAddress.longitude
        savedAddress.address1 = realmSavedAddress.address1
        savedAddress.address2 = realmSavedAddress.address2
        return savedAddress
    }
}