package com.lattis.ellipse.domain.model;

import com.google.android.gms.common.api.ApiException;

public class LocationSettingsResult {

    public ApiException getApiException() {
        return apiException;
    }

    public void setApiException(ApiException apiException) {
        this.apiException = apiException;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private ApiException apiException;
    private int status;
}
