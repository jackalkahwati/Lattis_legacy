package com.lattis.ellipse.bluetooth;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.domain.model.Lock;

import javax.inject.Inject;

import io.lattis.ellipse.sdk.Ellipse.Hardware.State;


public class HardwareStateMapper extends AbstractDataMapper<State, Lock.Hardware.State> {

    private LockPositionMapper lockPositionMapper;

    @Inject
    public HardwareStateMapper(LockPositionMapper lockPositionMapper) {
        this.lockPositionMapper = lockPositionMapper;
    }

    @NonNull
    @Override
    public Lock.Hardware.State mapIn(@NonNull State state) {
        return new Lock.Hardware.State(lockPositionMapper.mapOut(state.getPosition()),
                state.getBatteryLevel(),
                state.getRssiLevel(),
                state.getTemperature());
    }

    @NonNull
    @Override
    public State mapOut(@NonNull Lock.Hardware.State state) {
        return null;
    }
}
