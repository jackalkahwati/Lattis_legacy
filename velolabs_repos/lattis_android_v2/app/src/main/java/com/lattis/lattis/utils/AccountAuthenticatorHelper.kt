package com.lattis.lattis.utils

import android.os.Bundle

object AccountAuthenticatorHelper {

    val APP_START_BUNDLE = "APP_START_BUNDLE"
    val APP_LOGOUT_BUNDLE = "APP_LOGOUT_BUNDLE"
    val APP_STATUS_BUNDLE = "APP_STATUS_BUNDLE"

    fun getAppStartBundle():Bundle{
        var bundle = Bundle()
        bundle.putString(APP_STATUS_BUNDLE,APP_START_BUNDLE)
        return bundle
    }

    fun getAppLogOutBundle():Bundle{
        var bundle = Bundle()
        bundle.putString(APP_STATUS_BUNDLE,APP_LOGOUT_BUNDLE)
        return bundle
    }
}