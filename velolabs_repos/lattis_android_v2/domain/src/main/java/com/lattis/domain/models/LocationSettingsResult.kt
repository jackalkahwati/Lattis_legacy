package com.lattis.domain.models

import com.google.android.gms.common.api.ApiException

open class LocationSettingsResult {

    var apiException: ApiException? = null
    var status: Int = 0
}
