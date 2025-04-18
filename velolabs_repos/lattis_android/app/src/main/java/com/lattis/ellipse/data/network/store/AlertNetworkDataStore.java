package com.lattis.ellipse.data.network.store;

import com.lattis.ellipse.data.network.api.AlertApi;

import javax.inject.Inject;

public class AlertNetworkDataStore {

    private AlertApi alertApi;

    @Inject
    public AlertNetworkDataStore(AlertApi alertApi) {
        this.alertApi = alertApi;
    }
}
