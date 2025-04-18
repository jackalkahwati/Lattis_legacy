package com.lattis.ellipse.data;

import com.lattis.ellipse.data.database.SavedAddressDataStore;
import com.lattis.ellipse.data.database.mapper.RealmSavedAddressMapper;
import com.lattis.ellipse.domain.model.SavedAddress;
import com.lattis.ellipse.domain.repository.SavedAddressRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

public class SavedAddressDataRepository implements SavedAddressRepository {


    private SavedAddressDataStore savedAddressDataStore;

    @Inject
    public SavedAddressDataRepository(SavedAddressDataStore savedAddressDataStore){
            this.savedAddressDataStore = savedAddressDataStore;
    }

    @Override
    public Observable<Boolean> saveAddresses(List<SavedAddress> savedAddress) {
        return  savedAddressDataStore.saveSavedAddresses(savedAddress);
    }

    @Override
    public Observable<List<SavedAddress>> getSavedAddresses() {
        return  savedAddressDataStore.getSavedAddress();
    }

    @Override
    public Observable<Boolean> deleteSavedAddress() {
        return savedAddressDataStore.deleteSavedAddress();
    }
}
