package com.lattis.data.mapper.bluetooth

import com.lattis.data.mapper.AbstractDataMapper
import com.lattis.domain.models.Lock
import io.lattis.ellipse.sdk.Ellipse
import javax.inject.Inject

class LockPositionMapper @Inject constructor() :
    AbstractDataMapper<Lock.Hardware.Position?, Ellipse.Hardware.Position?>() {
    override fun mapIn(position: Lock.Hardware.Position?): Ellipse.Hardware.Position {
        return when (position) {
            Lock.Hardware.Position.LOCKED -> Ellipse.Hardware.Position.LOCKED
            Lock.Hardware.Position.UNLOCKED -> Ellipse.Hardware.Position.UNLOCKED
            Lock.Hardware.Position.BETWEEN_LOCKED_AND_UNLOCKED -> Ellipse.Hardware.Position.BETWEEN_LOCKED_UNLOCKED
            Lock.Hardware.Position.INVALID -> Ellipse.Hardware.Position.INVALID
            else -> Ellipse.Hardware.Position.INVALID
        }
    }

    override fun mapOut(position: Ellipse.Hardware.Position?): Lock.Hardware.Position {
        return when (position) {
            Ellipse.Hardware.Position.UNLOCKED -> Lock.Hardware.Position.UNLOCKED
            Ellipse.Hardware.Position.LOCKED -> Lock.Hardware.Position.LOCKED
            Ellipse.Hardware.Position.BETWEEN_LOCKED_UNLOCKED -> Lock.Hardware.Position.BETWEEN_LOCKED_AND_UNLOCKED
            Ellipse.Hardware.Position.INVALID -> Lock.Hardware.Position.INVALID
            else -> Lock.Hardware.Position.INVALID
        }
    }
}