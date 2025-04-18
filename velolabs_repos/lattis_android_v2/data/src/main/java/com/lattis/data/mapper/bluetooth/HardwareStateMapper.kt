package com.lattis.data.mapper.bluetooth

import com.lattis.data.mapper.AbstractDataMapper
import com.lattis.domain.models.Lock
import io.lattis.ellipse.sdk.Ellipse
import javax.inject.Inject

class HardwareStateMapper @Inject constructor(private val lockPositionMapper: LockPositionMapper) :
    AbstractDataMapper<Ellipse.Hardware.State?, Lock.Hardware.State?>() {
    override fun mapIn(state: Ellipse.Hardware.State?): Lock.Hardware.State {
        return Lock.Hardware.State(
            lockPositionMapper.mapOut(state!!.position),
            state?.batteryLevel,
            state?.rssiLevel,
            state?.temperature
        )
    }

    override fun mapOut(state: Lock.Hardware.State?): Ellipse.Hardware.State {
        return Ellipse.Hardware.State(lockPositionMapper.mapIn(state?.position),state?.batteryLevel!!,state.rssiLevel!!,state.temperature!!)
    }

}