package com.lattis.ellipse.data;

import com.lattis.ellipse.data.network.store.AlertNetworkDataStore;
import com.lattis.ellipse.domain.repository.AlertRepository;

import javax.inject.Inject;

public class AlertDataRepository implements AlertRepository{

    AlertNetworkDataStore alertNetworkDataStore;

    @Inject
    public AlertDataRepository(AlertNetworkDataStore alertNetworkDataStore) {
        this.alertNetworkDataStore = alertNetworkDataStore;
    }
}
