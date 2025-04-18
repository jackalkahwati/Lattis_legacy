package com.lattis.data.mapper.bluetooth

import com.lattis.data.mapper.AbstractDataMapper
import com.lattis.domain.models.Alert
import javax.inject.Inject

class AlertMapper @Inject constructor() :
    AbstractDataMapper<Alert, io.lattis.ellipse.sdk.model.Alert>() {
    override fun mapIn(alert: Alert?): io.lattis.ellipse.sdk.model.Alert {
        return if (alert == null) io.lattis.ellipse.sdk.model.Alert.OFF else when (alert) {
            Alert.OFF -> io.lattis.ellipse.sdk.model.Alert.OFF
            Alert.THEFT -> io.lattis.ellipse.sdk.model.Alert.THEFT
            Alert.CRASH -> io.lattis.ellipse.sdk.model.Alert.CRASH
            else -> io.lattis.ellipse.sdk.model.Alert.OFF
        }
    }

    override fun mapOut(alert: io.lattis.ellipse.sdk.model.Alert?): Alert {
        return if (alert == null) Alert.OFF else when (alert) {
            io.lattis.ellipse.sdk.model.Alert.OFF -> Alert.OFF
            io.lattis.ellipse.sdk.model.Alert.THEFT -> Alert.THEFT
            io.lattis.ellipse.sdk.model.Alert.CRASH -> Alert.CRASH
            else -> Alert.OFF
        }
    }
}