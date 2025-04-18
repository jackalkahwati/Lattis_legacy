package com.lattis.ellipse.data.database.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.database.base.AbstractRealmDataMapper;
import com.lattis.ellipse.data.database.model.RealmPrivateNetwork;
import com.lattis.ellipse.data.database.model.RealmRide;
import com.lattis.ellipse.data.database.model.RealmSavedAddress;
import com.lattis.ellipse.domain.model.PrivateNetwork;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.model.SavedAddress;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.RealmResults;

public class RealmSavedAddressMapper extends AbstractRealmDataMapper<SavedAddress, RealmSavedAddress> {

    @Inject
    public RealmSavedAddressMapper() {}

    @NonNull
    @Override
    public RealmSavedAddress mapIn(@NonNull SavedAddress savedAddress) {
        RealmSavedAddress realmSavedAddress = new RealmSavedAddress();
        realmSavedAddress.setId(savedAddress.getId());
        realmSavedAddress.setLatitude(savedAddress.getLatitude());
        realmSavedAddress.setLongitude(savedAddress.getLongitude());
        realmSavedAddress.setName(savedAddress.getName());
        return realmSavedAddress;
    }


    @NonNull
    @Override
    public SavedAddress mapOut(@NonNull RealmSavedAddress realmSavedAddress) {
        SavedAddress savedAddress = new SavedAddress();
        savedAddress.setId(realmSavedAddress.getId());
        savedAddress.setLatitude(realmSavedAddress.getLatitude());
        savedAddress.setLongitude(realmSavedAddress.getLongitude());
        savedAddress.setName(realmSavedAddress.getName());
        return savedAddress;
    }

//    @NonNull
//    @Override
//    public List<SavedAddress> mapOut(@NonNull RealmResults<RealmSavedAddress> realmSavedAddresses) {
//        List<SavedAddress> SavedAddresses = new ArrayList<>();
//        for (RealmSavedAddress realmSavedAddress : realmSavedAddresses) {
//            SavedAddresses.add(mapOut(realmSavedAddress));
//        }
//        return SavedAddresses;
//    }
}
