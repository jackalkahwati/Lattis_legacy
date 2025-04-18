package com.lattis.ellipse.bluetooth;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.domain.model.Lock;

import javax.inject.Inject;

import io.lattis.ellipse.sdk.Ellipse;

public class LockPositionMapper extends AbstractDataMapper<Lock.Hardware.Position,Ellipse.Hardware.Position> {

    @Inject
    public LockPositionMapper() {
    }

    @NonNull
    @Override
    public Ellipse.Hardware.Position mapIn(@NonNull Lock.Hardware.Position position) {
        switch (position){
            case LOCKED:
                return Ellipse.Hardware.Position.LOCKED;
            case UNLOCKED:
                return Ellipse.Hardware.Position.UNLOCKED;
            case BETWEEN_LOCKED_AND_UNLOCKED:
                return Ellipse.Hardware.Position.BETWEEN_LOCKED_UNLOCKED;
            case INVALID:
                return Ellipse.Hardware.Position.INVALID;
            default:return Ellipse.Hardware.Position.INVALID;
        }
    }

    @NonNull
    @Override
    public Lock.Hardware.Position mapOut(@NonNull Ellipse.Hardware.Position position) {
        switch (position){
            case UNLOCKED:
                return Lock.Hardware.Position.UNLOCKED;
            case LOCKED:
                return Lock.Hardware.Position.LOCKED;
            case BETWEEN_LOCKED_UNLOCKED:
                return Lock.Hardware.Position.BETWEEN_LOCKED_AND_UNLOCKED;
            case INVALID:
                return Lock.Hardware.Position.INVALID;
            default:return Lock.Hardware.Position.INVALID;
        }
    }
}
