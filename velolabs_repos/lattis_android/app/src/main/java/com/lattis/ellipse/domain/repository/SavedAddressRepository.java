package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.domain.model.SavedAddress;

import java.util.List;

import io.reactivex.Observable;

public interface SavedAddressRepository {

    Observable<Boolean> saveAddresses(List<SavedAddress> savedAddress);
    Observable<Boolean> deleteSavedAddress();
    Observable<List<SavedAddress>> getSavedAddresses();
}
