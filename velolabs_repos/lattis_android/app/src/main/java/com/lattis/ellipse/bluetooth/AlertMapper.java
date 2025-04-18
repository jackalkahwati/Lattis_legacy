package com.lattis.ellipse.bluetooth;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.domain.model.Alert;

import javax.inject.Inject;

public class AlertMapper extends AbstractDataMapper<Alert, io.lattis.ellipse.sdk.model.Alert> {

    @Inject
    public AlertMapper() {
    }

    @NonNull
    @Override
    public io.lattis.ellipse.sdk.model.Alert mapIn(@NonNull Alert alert) {

         if(alert == null)
             return io.lattis.ellipse.sdk.model.Alert.OFF;

        switch (alert){
            case OFF:
                return io.lattis.ellipse.sdk.model.Alert.OFF;
            case THEFT:
                return io.lattis.ellipse.sdk.model.Alert.THEFT;
            case CRASH:
                return io.lattis.ellipse.sdk.model.Alert.CRASH;
            default:return null;
        }
    }

    @NonNull
    @Override
    public Alert mapOut(@NonNull io.lattis.ellipse.sdk.model.Alert alert) {

        if(alert == null)
            return Alert.OFF;

        switch (alert){
            case OFF:
                return Alert.OFF;
            case THEFT:
                return Alert.THEFT;
            case CRASH:
                return Alert.CRASH;
            default:return null;
        }
    }
}
